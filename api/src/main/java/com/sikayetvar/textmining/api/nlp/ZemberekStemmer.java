package com.sikayetvar.textmining.api.nlp;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.sikayetvar.textmining.api.datalayer.MysqlDataOperator;
import com.sikayetvar.textmining.api.entity.Dictionary;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.entity.Preference;
import com.sikayetvar.textmining.api.entity.Stopword;
import com.sikayetvar.textmining.api.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zemberek.core.turkish.PrimaryPos;
import zemberek.core.turkish.SecondaryPos;
import zemberek.morphology.lexicon.DictionaryItem;
import zemberek.morphology.lexicon.Suffix;
import zemberek.morphology.lexicon.tr.TurkishSuffixes;
import zemberek.morphology.parser.MorphParse;
import zemberek.morphology.parser.tr.TurkishWordParserGenerator;
import zemberek.morphology.structure.Turkish;
import zemberek.tokenizer.ZemberekLexer;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import static com.sikayetvar.textmining.api.util.Configuration.DEBUG;


public class ZemberekStemmer implements Stemmer {
    private final Set<String> preferences;
    private final Set<String> stopwords;
    private final TurkishWordParserGenerator parser;
    private final ZemberekLexer lexer;

    private static final Locale TR = new Locale("tr");
    private static final Logger logger = LoggerFactory.getLogger(ZemberekStemmer.class);
    private final List<Suffix> undefinedCompoundNounSuffixes;


    public ZemberekStemmer(List<Stopword> stopwords, List<Dictionary> dictionary, List<Preference> preferences) throws IOException {

        this.preferences = preferences.stream().map(Preference::getRoot).collect(Collectors.toSet());
        this.stopwords = new HashSet<>();
        for (Stopword stopword : stopwords) {
            this.stopwords.add(stopword.getName());
        }
        this.parser = TurkishWordParserGenerator.createWithDefaults();
        this.parser.invalidateAllCache();
        for (Dictionary dictionaryItem : dictionary) {
            this.parser.getGraph().addDictionaryItem(
                    new DictionaryItem(dictionaryItem.getLemma(), dictionaryItem.getRoot(), dictionaryItem.getPronunciation(), PrimaryPos.valueOf(dictionaryItem.getPos()), SecondaryPos.None));
            // treat dictionary words as preference
            this.preferences.add(dictionaryItem.getRoot());
        }
        this.parser.invalidateAllCache();
        this.lexer = new ZemberekLexer();
        TurkishSuffixes turkishSuffixes = (TurkishSuffixes) this.parser.getSuffixProvider();
        this.undefinedCompoundNounSuffixes = Arrays.asList(turkishSuffixes.A3sg, turkishSuffixes.P3sg, turkishSuffixes.Nom);
    }

    public MorphParse parse(String word) {
        Map<String, MorphParse> rootCandidates = new HashMap<>();
        // find root candidates
        List<MorphParse> parses;

        try {
            parses = parser.parse(word);
        } catch (Exception e) {
            return null;
        }

        for (MorphParse parse : parses) {

            //if (Configuration.DEBUG) logger.info(parse.toString());

            if (parse.dictionaryItem == null)
                return null;
            PrimaryPos primaryPos = parse.dictionaryItem.primaryPos;
            String lemmaString = parse.dictionaryItem.lemma.toLowerCase(TR);// lemma: boyamak, we call it as "root", do not be confused
            String rootString = parse.dictionaryItem.root.toLowerCase(TR);// root: boya

            // check for Unk
            if ("unk".equals(lemmaString)) {
                // this elimination is done before morphological parsing at this.WordParse stem(String word)
                //lemmaString = parse.root.toLowerCase(TR).replaceAll("[^\\p{L}\\p{Nd}']+", "");
                lemmaString = parse.root.toLowerCase(TR);
                // parse with Apostrophe
                if (lemmaString.contains("'")) {
                    String[] splits = lemmaString.split("'");
                    lemmaString = lemmaString.split("'")[0];
                    // special case for "s" (McDonald's, domino's)
                    if (splits.length > 1 && "s".equals(splits[1]))
                        lemmaString += "'s";
                }

                // check if any word char left
                if (!lemmaString.matches(".*\\p{L}.*"))
                    continue; // if so ignore

                if (stopwords.contains(lemmaString))
                    continue;

                return new MorphParse(new DictionaryItem(lemmaString, rootString, parse.getPronunciation(), PrimaryPos.Unknown, SecondaryPos.Unknown), rootString, null);
            }


            // check if same lemma is already a candidate
            if (rootCandidates.containsKey(rootString)) {
                // if this root is in verb form then ignore this one
                if (primaryPos == PrimaryPos.Verb)
                    continue;
            }

            rootCandidates.put(lemmaString, parse);
        } // for roots

        // check if any root is found
        if (rootCandidates.isEmpty()) {
            return null;
        }



        String rootString = null;

        // find most proper candidate
        for (String rootCandidateString : rootCandidates.keySet()) {
            // check for preferences
            if (preferences.contains(rootCandidateString)) {
                rootString = rootCandidateString;
                break;
            }
        }


        if (rootString == null)
        {
            // if we don't have any preference then we look for stopwords, if exists return null
            for (String rootCandidateString : rootCandidates.keySet()) {
                // check for stopwords
                if (stopwords.contains(rootCandidateString)) {
                    return null;
                }
            }
            // no any hits to stopwords so get the 1st candidate
            rootString = rootCandidates.entrySet().iterator().next().getKey();
            /* once we tried to eliminate 1 char morphparses but these are also eliminated in stopwords. no need anymore
            Map.Entry<String,MorphParse> rootStringEntry = rootCandidates.entrySet().stream().filter(candidate -> candidate.getKey().length() > 1).findFirst().orElse(null);
            if (null != rootStringEntry){
                rootString = rootStringEntry.getKey();
            }
            else {
                return null;
            }
            */
        }
        else // in some cases we want a root is preferred and also a stopword. For example : iyi , com , ay
        {
            if (stopwords.contains(rootString)) {
                return null;
            }
        }

        return rootCandidates.get(rootString);// unk için rootstring de lazım
    }

    @Override
    public WordParse stem(String word) {
        // we have a filter for punctioation after stemming but this is not enough. because in a case we see "a." word on documents.
        // this point is eliminated by that regex below but only apostrophe is not eliminated
        MorphParse parse = parse(word.replaceAll("[^\\p{L}\\p{Nd}']+", "").replace('â','a'));
        return parse == null ? null : new WordParse(word, parse.dictionaryItem.lemma.toLowerCase(TR).intern(), parse.dictionaryItem.primaryPos);
    }

    @Override
    public List<WordParse> stemSentence(String sentence) {
        ArrayList<WordParse> wordParses = new ArrayList<>();

        String preprocessed = normalize(sentence);
        int index = 0;
        for (String s : Splitter.on(" ").omitEmptyStrings().trimResults().split(preprocessed)) {
            WordParse stem = stem(s);
            if (stem != null) {
                stem.setIndex(index);
                wordParses.add(stem);
            }
            index++;
        }

        return wordParses;
    }


    @Override
    public WordParse filter(WordParse wordParseS) {
        WordParse wordParse = filterPonctuation(filterEmpty(wordParseS));
        return (wordParse != null &&
                wordParse.getPos() != PrimaryPos.Conjunction &&
                wordParse.getPos() != PrimaryPos.Interjection &&
                wordParse.getPos() != PrimaryPos.Pronoun &&
                wordParse.getPos() != PrimaryPos.PostPositive &&
                wordParse.getPos() != PrimaryPos.Question &&
                wordParse.getPos() != PrimaryPos.Determiner &&
                wordParse.getPos() != PrimaryPos.Adverb &&
                wordParse.getPos() != PrimaryPos.Verb &&
                wordParse.getPos() != PrimaryPos.Numeral
        ) ? wordParse : null;
    }

    @Override
    public WordParse filterPonctuation(WordParse wordParse) {
        return ( wordParse != null &&
                wordParse.getPos() != PrimaryPos.Punctuation
        ) ? wordParse : null;
    }

    @Override
    public WordParse filterEmpty(WordParse wordParse) {
        return (wordParse != null && !wordParse.getRoot().isEmpty()
        ) ? wordParse : null;
    }

    @Override
    public List<WordParse> filter(Collection<WordParse> wordParses) {
        return wordParses.stream().filter(wordParse -> filter(wordParse) != null).collect(Collectors.toList());
    }

    @Override
    public List<WordParse> filterPonctuation(Collection<WordParse> wordParses) {
        return wordParses.stream().filter(wordParse -> filterPonctuation(wordParse) != null).collect(Collectors.toList());
    }

    @Override
    public WordParse stemThenFilter(String word) {
        return filter(stem(word));
    }
    @Override
    public WordParse stemThenFilterPonctuation(String word) {
        return filterPonctuation(stem(word));
    }

    @Override
    public List<WordParse> stemSentenceThenFilter(String sentence) {
        return filter(stemSentence(sentence));
    }

    @Override
    public List<WordParse> stemSentenceThenFilterPonctuation(String sentence) {
        return filterPonctuation(stemSentence(sentence));
    }


    @Override
    public String normalize(String str) {
        String quotesHyphensNormalized = null;
        try {
            quotesHyphensNormalized = Turkish.normalizeQuotesHyphens(str);
        } catch (Exception e) {
            logger.error("Exception in normalization process for " + str +" \n" + e.getMessage(), e);
        }
        return Joiner.on(" ").join(lexer.tokenStrings(quotesHyphensNormalized));
    }

    public List<String> regenerate(DictionaryItem newStem, List<Suffix> suffixes) {
        return Arrays.asList(parser.getGenerator().generate(newStem, suffixes));
    }

    @Override
    public List<String> makeUndefinedCompoundNoun(String root) {
        List<DictionaryItem> matchingItems = parser.getLexicon().getMatchingItems(root);
        if (matchingItems.isEmpty())
            return new ArrayList<>();
        return regenerate(matchingItems.get(0), undefinedCompoundNounSuffixes);
    }
}
