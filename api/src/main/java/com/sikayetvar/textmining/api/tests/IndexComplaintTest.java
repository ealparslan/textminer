package com.sikayetvar.textmining.api.tests;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * Created by deniz on 2/19/17.
 */
public class IndexComplaintTest {

    public static void main(String[] args) {

        TransportClient client = null;
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("luna.sikayetvar.com"), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


        XContentBuilder builder = null;
        try {
            builder = jsonBuilder()
                    .startObject()
                    .field("complaint_id", "999999999")
                    .field("stems", "superonline ödemek caymak bedel ödemek superonline")
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String json = null;
        try {
            json = builder.string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        IndexResponse ixresponse = client.prepareIndex("complaint_stems", "logs")
                .setSource(json)
                .get();

        RestStatus status = ixresponse.status();


    }
}
