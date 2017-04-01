package com.sikayetvar.textmining.api.tests;

import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.PrePost;
import com.ecyrd.speed4j.StopWatch;
import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.model.FrequentItemsetData;

import java.util.*;
import java.util.stream.Collectors;

public class FisTest {
    public static void main(String[] args) {
        List<Set<String>> transactionList = new ArrayList<>();
        transactionList.add(new HashSet<>(Arrays.asList("1a 3a 4a".split(" "))));
        transactionList.add(new HashSet<>(Arrays.asList("2a 3a 5a".split(" "))));
        transactionList.add(new HashSet<>(Arrays.asList("1a 2a 3a 5a".split(" "))));
        transactionList.add(new HashSet<>(Arrays.asList("2a 5a".split(" "))));
        transactionList.add(new HashSet<>(Arrays.asList("1a 2a 3a 5a".split(" "))));

       /* List<?> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaints();
        System.out.println("Complaints reading completed.");
        List<Set<String>> transactionList = generateItemsetList((List<Document>) complaints, ZemberekStemmerBuilder.getInstance().getStemmer());
        System.out.println("Complaints parsing completed.");*/

        PrePost prePost = new PrePost();
        StopWatch sw = new StopWatch();
        FrequentItemsetData<String> fis1 = prePost.runAlgorithm(transactionList, 0.01, 2);
        sw.stop("PrePost");
        System.out.println(sw);
        prePost.printStats();
        Set<Set<String>> list1 = fis1.getSupportCountMap().keySet();
        for (Set<String> strings : list1) {
            System.out.println(strings);
        }

        for (Map.Entry<Set<String>, Integer> strings : fis1.getSupportCountMap().entrySet()) {
            System.out.println(strings);
        }

      /*  AprioriFrequentItemsetGenerator<String> itemsetGenerator = new AprioriFrequentItemsetGenerator<>();
        sw.start();
        FrequentItemsetData<String> fis2 = itemsetGenerator.generate(transactionList, 0.01);
        sw.stop("Apriori");
        System.out.println(sw);
        List<Set<String>> list2 = fis2.getFrequentItemsetList();*/

        /*for (Set<String> strings : list1) {
            System.out.println(strings);
        }

        System.out.println("=================================<>====================================");

        for (Set<String> strings : list2) {
            System.out.println(strings);
        }*/

       /* // compare itemset lists by summing their hash codes
        sw.start();
        List<Integer> sum1 = list1.stream().map(strings -> strings.stream().map(String::hashCode).mapToInt(value -> value).sum()).sorted().collect(Collectors.toList());
        List<Integer> sum2 = list2.stream().map(strings -> strings.stream().map(String::hashCode).mapToInt(value -> value).sum()).sorted().collect(Collectors.toList());
        sw.stop("Sorting");
        System.out.println(sw);
        System.out.println(String.format("sum1:%d,sum2:%d,sum1==sum2:%b", sum1.size(), sum2.size(), sum1.equals(sum2)));

        String str1 =  list1.stream().map(strings -> strings.stream().collect(Collectors.joining(","))).sorted().collect(Collectors.joining("\r\n"));
        String str2 =  list2.stream().map(strings -> strings.stream().collect(Collectors.joining(","))).sorted().collect(Collectors.joining("\r\n"));

        System.out.println(str1);
        System.out.println("=================================<>====================================");
        System.out.println(str2);*/

        System.exit(0);
    }

    private static List<Set<String>> generateItemsetList(List<Document> documents, ZemberekStemmer stemmer) {
        List<Set<String>> itemsetList = new ArrayList<>(documents.size());
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            List<WordParse> wordParses = stemmer.stemSentenceThenFilter(document.getBody());
            Set<String> terms = wordParses.stream().map(WordParse::getRoot).limit(3).collect(Collectors.toSet());
            itemsetList.add(terms);
        }

        return itemsetList;
    }
}
