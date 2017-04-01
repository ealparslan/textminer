package com.sikayetvar.textmining.api.tests;

import com.sikayetvar.textmining.api.datalayer.MysqlDataOperator;
import com.sikayetvar.textmining.api.entity.EndHashtag;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.middle.CorpusCache;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by deniz on 1/25/17.
 */
public class GetHashtagsTest {

    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in, "UTF8");
        ServiceOperator serviceOperator = new ServiceOperator();

        String companyId, complaint;

        for (int i = 0; i < 1000; i++) {
            System.out.print("Enter company: ");
            companyId = scanner.nextLine();
            System.out.print("Enter complaint: ");
            complaint = scanner.nextLine();

            List<EndHashtag> hashtags = serviceOperator.getEndHashtags(companyId, complaint, 30);

            for (int j = 0; j < hashtags.size(); j++) {
                EndHashtag hashtag = hashtags.get(j);
                System.out.printf("%3d %15s:%10.2f\n", j, hashtag.getTermId(), hashtag.getScore());
            }
        }

    }

}
