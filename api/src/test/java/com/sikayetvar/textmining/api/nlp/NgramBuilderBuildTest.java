package com.sikayetvar.textmining.api.nlp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class NgramBuilderBuildTest {
    private List<WordParse> wordParses;
    private int n;
    private List<String> expected;

    public NgramBuilderBuildTest(List<WordParse> wordParses, int n, List<String> expected) {
        this.wordParses = wordParses;
        this.n = n;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection getParameters() {
        List<WordParse> inputList = new ArrayList<>();
        inputList.add(new WordParse("t0", "t0", null, 0));
        inputList.add(new WordParse("t1", "t1", null, 1));
        inputList.add(new WordParse("t3", "t3", null, 3));
        inputList.add(new WordParse("t4", "t4", null, 4));
        inputList.add(new WordParse("t5", "t5", null, 5));

        return Arrays.asList(new Object[][]{
                {inputList, 1, Arrays.asList(
                        Arrays.asList(new WordParse("t0", "t0", null, 0)),
                        Arrays.asList(new WordParse("t1", "t1", null, 1)),
                        Arrays.asList(new WordParse("t3", "t3", null, 3)),
                        Arrays.asList(new WordParse("t4", "t4", null, 4)),
                        Arrays.asList(new WordParse("t5", "t5", null, 5)))},
                {inputList, 2, Arrays.asList(
                        Arrays.asList(new WordParse("t0", "t0", null, 0), new WordParse("t1", "t1", null, 1)),
                        Arrays.asList(new WordParse("t3", "t3", null, 3), new WordParse("t4", "t4", null, 4)),
                        Arrays.asList(new WordParse("t4", "t4", null, 4), new WordParse("t5", "t5", null, 5)))},
                {inputList, 3, Arrays.asList(
                        Arrays.asList(new WordParse("t3", "t3", null, 3), new WordParse("t4", "t4", null, 4), new WordParse("t5", "t5", null, 5)))},
                {inputList, 4, new ArrayList<String>()},
                {inputList, 5, new ArrayList<String>()},
                {inputList, 6, new ArrayList<String>()},
        });
    }

    @Test
    public void build() throws Exception {
        System.out.println("ngram : " + n);
        assert expected.equals(NgramBuilder.build(wordParses, n));
    }
}
