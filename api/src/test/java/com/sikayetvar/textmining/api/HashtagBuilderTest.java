package com.sikayetvar.textmining.api;

import com.sikayetvar.textmining.api.entity.NgramTerm;
import com.sikayetvar.textmining.api.nlp.NgramBuilder;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.util.Utils;
import org.junit.Test;
import zemberek.core.turkish.PrimaryPos;

import java.util.*;
import java.util.stream.Collectors;

public class HashtagBuilderTest {
    @Test
    public void stemNgrams() throws Exception {
        Map<String, Set<Long>> categoryTermPositions = new HashMap<>();
        Map<String, Set<Long>> expectedCategoryTermPositions = new HashMap<>();

        String text = "kredi kartı ekstre geldi. \n" +
                "        bugün kredi kartı ekstre gelmedi. \n" +
                "        yarın kredi kartı ekstre gelecek. \n" +
                "        kredi kartı ödeme oldu. \n" +
                "        kredi kartı borcunu yatırdım. \n" +
                "        kredi çekmek için geldim. \n" +
                "        bana kredi verirler mi. \n" +
                "        banka kart geldi.\n" +
                "        kart evde unuttum. \n" +
                "        hesap ekstre çıktı. \n" +
                "        ekstre kağıt nerede. \n" +
                "        bugün ödeme günü. \n"   +
                "        hediye çeki vermediler. \n";


                ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

        List<NgramTerm> ngramTerms = Arrays.asList(new NgramTerm("hediye çek", "hediye çeki"), new NgramTerm("kredi kart ekstre", "kredi kartı ekstresi"), new NgramTerm("kredi kart", "kredi kartı"));
        Set<String> ngramTermSet = ngramTerms.stream().map(NgramTerm::getRawTerm).collect(Collectors.toSet());

        List<WordParse> wordParses = stemmer.stemSentenceThenFilter(text);
        List<WordParse> ngramWordParses = new ArrayList<>();
        for (int n = 2; n <= 3; n++) {
            List<List<WordParse>> ngrams = NgramBuilder.build(wordParses, n);
            for (List<WordParse> ngram : ngrams) {
                String ngramTermRoot = ngram
                        .stream()
                        .map(WordParse::getRoot)
                        .collect(Collectors.joining(" "))
                        .intern();
                // add ngram only if it is in the ngram list
                if (ngramTermSet.contains(ngramTermRoot)) {
                    WordParse ngramTerm = new WordParse(ngramTermRoot, ngramTermRoot, PrimaryPos.Noun, ngram.get(0).getIndex());
                    ngramWordParses.add(ngramTerm);
                }
            }
        }
        wordParses.addAll(ngramWordParses);

        for (WordParse wordParse : wordParses) {
            Utils.putIfAbsent(categoryTermPositions, wordParse.getRoot(), new HashSet<>()).add(wordParse.getIndex());
        }

        expectedCategoryTermPositions.put("kredi kart ekstre", new HashSet<>(Arrays.asList(0L, 6L, 12L)));
        expectedCategoryTermPositions.put("kâğıt", new HashSet<>(Arrays.asList(50L)));
        expectedCategoryTermPositions.put("kredi kart", new HashSet<>(Arrays.asList(17L, 22L)));
        expectedCategoryTermPositions.put("ekstre", new HashSet<>(Arrays.asList(46L, 49L)));
        expectedCategoryTermPositions.put("kart", new HashSet<>(Arrays.asList(38L, 41L)));
        expectedCategoryTermPositions.put("borç", new HashSet<>(Arrays.asList(24L)));
        expectedCategoryTermPositions.put("hesap", new HashSet<>(Arrays.asList(45L)));
        expectedCategoryTermPositions.put("ev", new HashSet<>(Arrays.asList(42L)));
        expectedCategoryTermPositions.put("banka", new HashSet<>(Arrays.asList(37L)));
        expectedCategoryTermPositions.put("kredi", new HashSet<>(Arrays.asList(27L, 33L)));
        expectedCategoryTermPositions.put("hediye çek", new HashSet<>(Arrays.asList(57L)));
        expectedCategoryTermPositions.put("hediye", new HashSet<>(Arrays.asList()));
        expectedCategoryTermPositions.put("çek", new HashSet<>(Arrays.asList()));



        HashtagBuilder hashtagBuilder = new HashtagBuilder(null, new ArrayList<>(), ngramTerms, null, null);
        hashtagBuilder.stemNgrams(categoryTermPositions);

        assert categoryTermPositions.equals(expectedCategoryTermPositions);
    }

}