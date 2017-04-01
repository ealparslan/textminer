package com.sikayetvar.textmining.api.nlp;

import org.junit.Test;
import zemberek.morphology.parser.MorphParse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ZemberekStemmerTest {
    private final ZemberekStemmer zemberekStemmer;
    private static final Locale TR = new Locale("tr");

    public ZemberekStemmerTest() throws IOException {
        // initialize stemmer
        zemberekStemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
    }

//
//    @Test
//    public void parse() throws Exception {
//        String[] testSet = new String[]{ "davası", "konu", "takım", "düzgün", "kotası", "sıra", "sürekli", "anadolu", "olmadığı", "fırın", "aradım", "boya", "üyeliğimin", "bağcığı", "güneşliklerden", "kazaların",};
//        String[] expectedSet = new String[]{ "dava", "konu", "takım", "düzgün", "kota", "sıra", "sürekli", "anadolu", null, "fırın", "aramak", "boya", "üye", "bağcık", "güneş", "kaza",};
//
//        for (int i = 0; i < testSet.length; i++) {
//            MorphParse parse = zemberekStemmer.parse(testSet[i]);
//            assert (parse == null && expectedSet[i] == null) || (parse.dictionaryItem.lemma.toLowerCase(TR).equals(expectedSet[i].toLowerCase(TR)));
//        }
//    }

    @Test
    public void stem() throws Exception {
//        String[] testSet = new String[]{"davası", "konu", "takım", "düzgün", "kotası", "sıra", "sürekli", "anadolu", "olmadığı", "fırın", "aradım", "boya", "üyeliğimin", "bağcığı", "güneşliklerden", "kazaların",};
//        String[] expectedSet = new String[]{ "dava", "konu", "takım", "düzgün", "kota", "sıra", "sürekli", "anadolu", null, "fırın", "aramak", "boya", "üye", "bağcık", "güneş", "kaza",};
        String[] testSet = new String[]{"bağcığı"};
        String[] expectedSet = new String[]{ "bağcık"};

        for (int i = 0; i < testSet.length; i++) {
            WordParse parse = zemberekStemmer.stem(testSet[i]);
            assert (parse == null && expectedSet[i] == null) || (parse.getRoot().equals(expectedSet[i]));
        }
    }

    @Test
    public void stemSentenceThenFilter() throws Exception {
        String input = "almak için gittim. aldım ya da almadım. Aman ha sakın geleyim deme. Ama gelmedi. ancak herhangi halen hepsiburada.com trt.net.tr hafta 12.44’de iphone7 olmadığı a320gtx 6 10/05/2013 ay olmasına rağmen iyi çalışmalar. iletişim : 534486**** 22.07.2013 tarihinde 14.299.00 TL";
        String expected = "hepsiburada trt tr iphone7 a320gtx iletişim";

        List<WordParse> wordParses = zemberekStemmer.stemSentenceThenFilter(input);

        assert (wordParses.stream().map(WordParse::getRoot).collect(Collectors.joining(" ")).equals(expected));
    }

    @Test
    public void generateUndefinedCompoundNounPrefixes() throws Exception {
        List<String> roots = Arrays.asList("kutu", "simit", "kol", "pınar", "şişe", "panda");
        List<String> expectedList = Arrays.asList("kutusu", "simidi", "kolu", "pınarı", "şişesi", "pandası");
        for (int i = 0; i < roots.size(); i++) {
            String word = roots.get(i);
            String expected = expectedList.get(i);
            final List<String> suffixes = zemberekStemmer.makeUndefinedCompoundNoun(word);
            assert suffixes.get(0).equals(expected);
        }
    }
}