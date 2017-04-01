package com.sikayetvar.textmining.api.tests;

import com.sikayetvar.textmining.api.middle.ServiceOperator;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;

import java.util.Scanner;

/**
 * Created by deniz on 2/24/17.
 */
public class StemWordTest {

    public static void main(String[] args) {

        ServiceOperator serviceOperator = new ServiceOperator();

        Scanner scanner = new Scanner(System.in, "UTF8");
        ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
        String word;
        for (int i = 0; i <10000000 ; i++) {
            System.out.print("Enter word: \n");
            word = scanner.nextLine();
            //WordParse parses = stemmer.stem(word);
            if (word.equals("reload")) serviceOperator.reload();
            System.out.println(serviceOperator.getStem(word) + "\n");
        }


    }
}


