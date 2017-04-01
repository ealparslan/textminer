package com.sikayetvar.textmining.api.tests;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import java.util.Scanner;

/**
 * Created by deniz on 2/17/17.
 */
public class ComplaintStemTest {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in, "UTF8");
        ServiceOperator serviceOperator = new ServiceOperator();

        String complaint;

        System.out.print("Enter complaint: ");
        complaint = scanner.nextLine();

        String stems = serviceOperator.getStems(complaint);

        System.out.print(stems);

    }
}


