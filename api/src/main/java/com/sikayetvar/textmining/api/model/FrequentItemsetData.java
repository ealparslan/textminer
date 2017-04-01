package com.sikayetvar.textmining.api.model;

import java.util.Map;
import java.util.Set;

/**
 * This class holds the result information of a data-mining task.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Sep 14, 2015)
 */
public class FrequentItemsetData<I> {

    private final Map<Set<I>, Integer> supportCountMap;
    private final double minimumSupport;
    private final int numberOfTransactions;

    public FrequentItemsetData(Map<Set<I>, Integer> supportCountMap, double minimumSupport, int transactionNumber) {
        this.supportCountMap = supportCountMap;
        this.minimumSupport = minimumSupport;
        this.numberOfTransactions = transactionNumber;
    }

    public Map<Set<I>, Integer> getSupportCountMap() {
        return supportCountMap;
    }

    public double getMinimumSupport() {
        return minimumSupport;
    }

    public int getTransactionNumber() {
        return numberOfTransactions;
    }

    public double getSupport(Set<I> itemset) {
        return 1.0 * supportCountMap.get(itemset) / numberOfTransactions;
    }
}