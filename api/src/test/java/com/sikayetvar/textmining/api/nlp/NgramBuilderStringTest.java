package com.sikayetvar.textmining.api.nlp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class NgramBuilderStringTest {
    private List<String> terms;
    private int n;
    private List<String> expected;

    public NgramBuilderStringTest(List<String> terms, int n, List<String> expected) {
        this.terms = terms;
        this.n = n;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection getParameters() {
        List<String> inputList = Arrays.asList("t0", "t1", "t2", "t3", "t4");

        return Arrays.asList(new Object[][]{
                {inputList, 1, Arrays.asList("t0", "t1", "t2", "t3", "t4")},
                {inputList, 2, Arrays.asList("t0 t1", "t1 t2", "t2 t3", "t3 t4")},
                {inputList, 3, Arrays.asList("t0 t1 t2", "t1 t2 t3", "t2 t3 t4")},
                {inputList, 4, Arrays.asList("t0 t1 t2 t3", "t1 t2 t3 t4")},
                {inputList, 5, Arrays.asList("t0 t1 t2 t3 t4")},
                {inputList, 6, new ArrayList<String>()},
        });
    }

    @Test
    public void buildNgrams() throws Exception {
        System.out.println("ngram : " + n);
        assert expected.equals(NgramBuilder.buildNgrams(terms, n));
    }

}