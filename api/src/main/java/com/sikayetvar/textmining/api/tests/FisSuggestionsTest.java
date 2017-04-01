package com.sikayetvar.textmining.api.tests;

import com.ecyrd.speed4j.StopWatch;
import com.sikayetvar.textmining.api.datalayer.CassandraDataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.entity.Suggestion;

import java.util.List;

public class FisSuggestionsTest {
    public static void main(String[] args) {
        // DataOperator dataOperator = RedisDataOperator.getInstance();
        DataOperator dataOperator = CassandraDataOperator.getInstance();

        StopWatch sw = new StopWatch();
        List<Suggestion> fisSuggestions = dataOperator.getFisSuggestions("102", "banka kredi", 100);
        System.out.println(sw.stop(dataOperator.getClass().getSimpleName()));
        System.out.println(fisSuggestions);

        dataOperator.destroy();
    }
}
