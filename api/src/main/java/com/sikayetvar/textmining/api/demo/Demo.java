package com.sikayetvar.textmining.api.demo;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Category;
import com.sikayetvar.textmining.api.entity.Suggestion;
import com.sikayetvar.textmining.api.util.Configuration;

import java.util.List;
import java.util.Scanner;

/**
 * Created by John on 11.11.2016.
 */
public class Demo {
    public static final String DEFAULT_CATEGORY = "4";
    public static final int DEFAULT_RESULT_LIMIT = 10;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "Cp857");
        String option = "";
        String term = "";
        String category = DEFAULT_CATEGORY;
        int resultLimit = DEFAULT_RESULT_LIMIT;

        while (!option.equals("0")) {
            System.out.println("Current term = " + term);
            System.out.println("1 - Add new term");
            System.out.println("2 - Get suggestions");
            System.out.println("3 - Clear term");
            System.out.println("4 - Change category (current:" + category + ")");
            System.out.println("5 - Change result limit (current:" + resultLimit + ")");
            System.out.println("0 - Quit");
            System.out.println("Enter option");

            option = scanner.nextLine();
            switch (option) {
                case "1":
                    System.out.println("Enter term (current = " + term + ")");
                    term += (term.isEmpty() ? "" : " ") + scanner.nextLine();
                    break;
                case "2":
                    List<Suggestion> suggestionsTfidf = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getSuggestions(category, term, resultLimit);
                    List<Suggestion> suggestionsFrequentItemSet = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getFisSuggestions(category, term, resultLimit);

                    // Tfidf
                    System.out.printf("%19s %8s  %15s %8s%n", "TFIDF Term", "Score", "FIS Term", "Score");
                    for (int i = 0; i < resultLimit; i++) {
                        System.out.printf("%3d %15s %8.2f  %15s %8.7f  %3d%n", i + 1,
                                i < suggestionsTfidf.size() ? suggestionsTfidf.get(i).getTerm() : "N/A", i < suggestionsTfidf.size() ? suggestionsTfidf.get(i).getScore() : 0f,
                                i < suggestionsFrequentItemSet.size() ? suggestionsFrequentItemSet.get(i).getTerm() : "N/A", i < suggestionsFrequentItemSet.size() ? suggestionsFrequentItemSet.get(i).getScore() : 0f,
                                i + 1
                        );
                    }
                    break;
                case "3":
                    term = "";
                    break;
                case "4":
                    List<Category> categories = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getCategories();
                    for (Category cat : categories) {
                        System.out.printf("%3d - %s%n", cat.getId(), cat.getName());
                    }
                    System.out.println("Select category");
                    String catId = scanner.nextLine();
                    Category selectedCategory = categories.stream().filter(category1 -> catId.equals(Integer.toString(category1.getId()))).findAny().orElse(null);
                    if (selectedCategory != null)
                        category = selectedCategory.getName();
                    break;

                case "5":
                    System.out.println("Enter result limit");
                    try {
                        resultLimit = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number");
                    }
                    break;

                case "0":
                    System.out.println("Quitting");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid input");
                    break;
            }
        }
    }
}