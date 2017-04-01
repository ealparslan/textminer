package com.sikayetvar.bigquery;

// Imports the Google Cloud client library

import com.google.cloud.bigquery.*;
import java.util.*;

/**
 * Created by deniz on 1/18/17.
 */
public class BigClient {

    public static void main(String... args) throws Exception {


        TableId tableId = TableId.of("deneme", "denemetable");
        // Values of the row to insert
        Map<String, Object> rowContent = new HashMap<>();
        rowContent.put("name", "denizf");
        //rowContent.put("birth", "05/13/2016 10:13:15");
        //rowContent.put("enrolled", "05/13/2016 10:13:15");
        rowContent.put("age", 33);

        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

        InsertAllResponse response = bigquery.insertAll(InsertAllRequest.newBuilder(tableId).addRow("rowId", rowContent).build());
        if (response.hasErrors()) {
            // If any of the insertions failed, this lets you inspect the errors
            for (Map.Entry<Long, List<BigQueryError>> entry : response.getInsertErrors().entrySet()) {
                System.out.print(entry.getValue());
            }


        }

    }
}



