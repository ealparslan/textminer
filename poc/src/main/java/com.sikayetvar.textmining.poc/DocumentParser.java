package com.sikayetvar.textmining.poc;

import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentParser {
    private final List<Document> documents;
    private final ZemberekStemmer stemmer;
    private List<List<String>> sentences;

    public DocumentParser(Document document, ZemberekStemmer stemmer) {
        this(Arrays.asList(document), stemmer);
    }

    public DocumentParser(List<Document> documents, ZemberekStemmer stemmer) {
        this.documents = documents;
        this.stemmer = stemmer;
    }

    public void parse() {
        sentences = new ArrayList<>();
        for (Document document : documents) {
            List<String> sentence;
            if (stemmer != null)
                sentence = stemmer.stemSentenceThenFilter(document.getBody()).stream().map(WordParse::getRoot).collect(Collectors.toList());
            else
                sentence = Arrays.asList(document.getBody().split("\\s+"));
            sentences.add(sentence);
        }
    }

    public List<List<String>> getSentences() {
        return sentences;
    }
}
