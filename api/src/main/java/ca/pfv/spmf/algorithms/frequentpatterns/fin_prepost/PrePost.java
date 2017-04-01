package ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost;

import com.sikayetvar.textmining.api.model.FrequentItemsetData;

import java.util.*;
import java.util.Map.Entry;

/*
 * Copyright (c) 2008-2015 ZHIHONG DENG
 *
 * This file is part of the SPMF DATA MINING SOFTWARE
 * (http://www.philippe-fournier-viger.com/spmf).
 *
 * SPMF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * SPMF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * SPMF. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Java implementation of the PrePost/PrePost+ algorithm.
 * <p>
 * This implementation was obtained by converting the original C++ code of
 * Prepost by ZHIHONG DENG  to Java.
 *
 * @author Philippe Fournier-Viger
 */
public class PrePost {

    // the start time and end time of the last algorithm execution
    private long startTimestamp;
    private long endTimestamp;

    // number of itemsets found
    private int outputCount;

    private int[][] bf;
    private int bf_cursor;
    private int bf_size;
    private int bf_col;
    private int bf_currentSize;

    private int numOfFItem; // Number of items
    private int minSupport; // minimum support
    private Item[] item; // list of items sorted by support

    private int[] result; // the current itemset
    private int resultLen; // the size of the current itemset

    // Tree stuff
    private PPCTreeNode ppcRoot;
    private NodeListTreeNode nlRoot;
    private PPCTreeNode[] headTable;
    private int[] headTableLen;
    private int[] itemsetCount;
    private int[] sameItems;
    private Map<Set<String>, Integer> supportCountMap;

    /**
     * Comparator to sort items by decreasing order of frequency
     */
    private static Comparator<Item> comparator = (a, b) -> b.frequency - a.frequency;
    private int numberOfTransactions;
    private int maximumItemCount;

    /**
     * Run the algorithm
     *
     * @param transactionList the input
     * @param minimumSupport  the minimumSupport threshold
     */
    public FrequentItemsetData<String> runAlgorithm(List<Set<String>> transactionList, double minimumSupport, int maximumItemCount) {
        this.maximumItemCount = maximumItemCount;
        supportCountMap = new HashMap<>();

        outputCount = 0;
        ppcRoot = new PPCTreeNode();
        nlRoot = new NodeListTreeNode();
        resultLen = 0;

        MemoryLogger.getInstance().reset();

        // record the start time
        startTimestamp = System.currentTimeMillis();

        bf_size = 1000000;
        bf = new int[100000][];
        bf_currentSize = bf_size * 10;
        bf[0] = new int[bf_currentSize];

        bf_cursor = 0;
        bf_col = 0;

        // ==========================
        // Read Dataset
        getData(transactionList, minimumSupport);

        resultLen = 0;
        result = new int[numOfFItem];

        // Build tree
        buildTree(transactionList);

        nlRoot.label = numOfFItem;
        nlRoot.firstChild = null;
        nlRoot.next = null;

        // Initialize tree
        initializeTree();
        sameItems = new int[numOfFItem];

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;

        // Recursively traverse the tree
        NodeListTreeNode curNode = nlRoot.firstChild;
        NodeListTreeNode next;
        while (curNode != null) {
            next = curNode.next;
            // call the recursive "traverse" method
            traverse(curNode, 1, 0);
            for (int c = bf_col; c > from_col; c--) {
                bf[c] = null;
            }
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;
            curNode = next;
        }

        MemoryLogger.getInstance().checkMemory();

        // record the end time
        endTimestamp = System.currentTimeMillis();

        return new FrequentItemsetData<>(supportCountMap, minimumSupport, outputCount);
    }

    /**
     * Build the tree
     *
     * @param transactionList the input
     */
    private void buildTree(List<Set<String>> transactionList) {

        ppcRoot.label = -1;

        // we will use a buffer to store each transaction that is read.
        Item[] transaction = new Item[1000];

        // for each line (transaction) until the end of the file
        for (Set<String> itemSet : transactionList) {

            // for each item in the transaction
            int tLen = 0; // tLen
            for (String itemString : itemSet) {

                // add each item from the transaction except infrequent item
                for (int j = 0; j < numOfFItem; j++) {
                    // if the item appears in the list of frequent items, we add
                    // it
                    if (itemString.equals(item[j].data)) {
                        transaction[tLen] = new Item();
                        transaction[tLen].data = itemString; // the item
                        transaction[tLen].frequency = 0 - j;
                        tLen++;
                        break;
                    }
                }
            }

            // sort the transaction
            Arrays.sort(transaction, 0, tLen, comparator);

            // Print the transaction
            // for(int j=0; j < tLen; j++){
            // System.out.print(" " + transaction[j].index + " ");
            // }
            // System.out.println();

            int curPos = 0;
            PPCTreeNode curRoot = (ppcRoot);
            PPCTreeNode rightSibling = null;
            while (curPos != tLen) {
                PPCTreeNode child = curRoot.firstChild;
                while (child != null) {
                    if (child.label == 0 - transaction[curPos].frequency) {
                        curPos++;
                        child.count++;
                        curRoot = child;
                        break;
                    }
                    if (child.rightSibling == null) {
                        rightSibling = child;
                        child = null;
                        break;
                    }
                    child = child.rightSibling;
                }
                if (child == null)
                    break;
            }
            for (int j = curPos; j < tLen; j++) {
                PPCTreeNode ppcNode = new PPCTreeNode();
                ppcNode.label = 0 - transaction[j].frequency;
                if (rightSibling != null) {
                    rightSibling.rightSibling = ppcNode;
                    rightSibling = null;
                } else {
                    curRoot.firstChild = ppcNode;
                }
                ppcNode.rightSibling = null;
                ppcNode.firstChild = null;
                ppcNode.father = curRoot;
                ppcNode.labelSibling = null;
                ppcNode.count = 1;
                curRoot = ppcNode;
            }
        }

        // System.out.println( "====");

        // Create a header table
        headTable = new PPCTreeNode[numOfFItem];

        headTableLen = new int[numOfFItem];

        PPCTreeNode[] tempHead = new PPCTreeNode[numOfFItem];

        itemsetCount = new int[(numOfFItem - 1) * numOfFItem / 2];

        PPCTreeNode root = ppcRoot.firstChild;
        int pre = 0;
        int last = 0;
        while (root != null) {
            root.foreIndex = pre;
            pre++;

            if (headTable[root.label] == null) {
                headTable[root.label] = root;
                tempHead[root.label] = root;
            } else {
                tempHead[root.label].labelSibling = root;
                tempHead[root.label] = root;
            }
            headTableLen[root.label]++;

            PPCTreeNode temp = root.father;
            while (temp.label != -1) {
                itemsetCount[root.label * (root.label - 1) / 2 + temp.label] += root.count;
                temp = temp.father;
            }
            if (root.firstChild != null) {
                root = root.firstChild;
            } else {
                // back visit
                root.backIndex = last;
                last++;
                if (root.rightSibling != null) {
                    root = root.rightSibling;
                } else {
                    root = root.father;
                    while (root != null) {
                        // back visit
                        root.backIndex = last;
                        last++;
                        if (root.rightSibling != null) {
                            root = root.rightSibling;
                            break;
                        }
                        root = root.father;
                    }
                }
            }
        }
    }

    /**
     * Initialize the tree
     */
    private void initializeTree() {

        NodeListTreeNode lastChild = null;
        for (int t = numOfFItem - 1; t >= 0; t--) {
            if (bf_cursor > bf_currentSize - headTableLen[t] * 3) {
                bf_col++;
                bf_cursor = 0;
                bf_currentSize = 10 * bf_size;
                bf[bf_col] = new int[bf_currentSize];
            }

            NodeListTreeNode nlNode = new NodeListTreeNode();
            nlNode.label = t;
            nlNode.support = 0;
            nlNode.NLStartinBf = bf_cursor;
            nlNode.NLLength = 0;
            nlNode.NLCol = bf_col;
            nlNode.firstChild = null;
            nlNode.next = null;
            PPCTreeNode ni = headTable[t];
            while (ni != null) {
                nlNode.support += ni.count;
                bf[bf_col][bf_cursor++] = ni.foreIndex;
                bf[bf_col][bf_cursor++] = ni.backIndex;
                bf[bf_col][bf_cursor++] = ni.count;
                nlNode.NLLength++;
                ni = ni.labelSibling;
            }
            if (nlRoot.firstChild == null) {
                nlRoot.firstChild = nlNode;
                lastChild = nlNode;
            } else {
                lastChild.next = nlNode;
                lastChild = nlNode;
            }
        }
    }

    private void getData(List<Set<String>> transactionList, double minimumSupport) {
        numberOfTransactions = 0;

        // (1) Scan the database and count the support of each item.
        // The support of items is stored in map where
        // key = item value = support count
        Map<String, Integer> mapItemCount = new HashMap<>();
        // scan the database
        // for each line (transaction) until the end of the file
        numberOfTransactions = transactionList.size();

        for (Set<String> transaction : transactionList) {

            for (String itemString : transaction) {
                // increase the support count of the item by 1
                Integer count = mapItemCount.get(itemString);
                if (count == null) {
                    mapItemCount.put(itemString, 1);
                } else {
                    mapItemCount.put(itemString, ++count);
                }
            }

        }

        minSupport = (int) Math.ceil(minimumSupport * numberOfTransactions);

        numOfFItem = mapItemCount.size();

        Item[] tempItems = new Item[numOfFItem];
        int i = 0;
        for (Entry<String, Integer> entry : mapItemCount.entrySet()) {
            if (entry.getValue() >= minSupport) {
                tempItems[i] = new Item();
                tempItems[i].data = entry.getKey();
                tempItems[i].frequency = entry.getValue();
                i++;
            }
        }

        item = new Item[i];
        System.arraycopy(tempItems, 0, item, 0, i);

        numOfFItem = item.length;

        Arrays.sort(item, comparator);
    }

    private NodeListTreeNode iskItemSetFreq(NodeListTreeNode ni, NodeListTreeNode nj, NodeListTreeNode lastChild, IntegerByRef sameCountRef) {

        // System.out.println("====\n" + "isk_itemSetFreq() samecount = " +
        // sameCountRef.count);

        if (bf_cursor + ni.NLLength * 3 > bf_currentSize) {
            bf_col++;
            bf_cursor = 0;
            bf_currentSize = bf_size > ni.NLLength * 1000 ? bf_size
                    : ni.NLLength * 1000;
            bf[bf_col] = new int[bf_currentSize];
        }

        NodeListTreeNode nlNode = new NodeListTreeNode();
        nlNode.support = 0;
        nlNode.NLStartinBf = bf_cursor;
        nlNode.NLCol = bf_col;
        nlNode.NLLength = 0;

        int cursor_i = ni.NLStartinBf;
        int cursor_j = nj.NLStartinBf;
        int col_i = ni.NLCol;
        int col_j = nj.NLCol;
        int last_cur = -1;
        while (cursor_i < ni.NLStartinBf + ni.NLLength * 3
                && cursor_j < nj.NLStartinBf + nj.NLLength * 3) {
            if (bf[col_i][cursor_i] > bf[col_j][cursor_j]
                    && bf[col_i][cursor_i + 1] < bf[col_j][cursor_j + 1]) {
                if (last_cur == cursor_j) {
                    bf[bf_col][bf_cursor - 1] += bf[col_i][cursor_i + 2];
                } else {
                    bf[bf_col][bf_cursor++] = bf[col_j][cursor_j];
                    bf[bf_col][bf_cursor++] = bf[col_j][cursor_j + 1];
                    bf[bf_col][bf_cursor++] = bf[col_i][cursor_i + 2];
                    nlNode.NLLength++;
                }
                nlNode.support += bf[col_i][cursor_i + 2];
                last_cur = cursor_j;
                cursor_i += 3;
            } else if (bf[col_i][cursor_i] < bf[col_j][cursor_j]) {
                cursor_i += 3;
            } else if (bf[col_i][cursor_i + 1] > bf[col_j][cursor_j + 1]) {
                cursor_j += 3;
            }
        }
        if (nlNode.support >= minSupport) {
            if (ni.support == nlNode.support) {
                sameItems[sameCountRef.count++] = nj.label;
                bf_cursor = nlNode.NLStartinBf;
                if (nlNode != null) {
                    nlNode = null;
                }
            } else {
                nlNode.label = nj.label;
                nlNode.firstChild = null;
                nlNode.next = null;
                if (ni.firstChild == null) {
                    ni.firstChild = nlNode;
                    lastChild = nlNode;
                } else {
                    lastChild.next = nlNode;
                    lastChild = nlNode;
                }
            }
            return lastChild;
        } else {
            bf_cursor = nlNode.NLStartinBf;
            if (nlNode != null)
                nlNode = null;
        }
        return lastChild;
    }

    /**
     * Recursively traverse the tree to find frequent itemsets
     */
    private void traverse(NodeListTreeNode curNode, int level, int sameCount) {

        MemoryLogger.getInstance().checkMemory();

        // System.out.println("==== traverse(): " + curNode.label + " "+ level +
        // " " + sameCount);
        NodeListTreeNode sibling = curNode.next;
        NodeListTreeNode lastChild = null;
        while (sibling != null) {
            if (level > 1
                    || (level == 1 && itemsetCount[(curNode.label - 1)
                    * curNode.label / 2 + sibling.label] >= minSupport)) {
                // tangible.RefObject<Integer> tempRef_sameCount = new
                // tangible.RefObject<Integer>(
                // sameCount);
                // int sameCountTemp = sameCount;
                IntegerByRef sameCountTemp = new IntegerByRef();
                sameCountTemp.count = sameCount;
                lastChild = iskItemSetFreq(curNode, sibling, lastChild,
                        sameCountTemp);
                sameCount = sameCountTemp.count;

            }
            sibling = sibling.next;
        }

        result[resultLen++] = curNode.label;

        // ============= Write itemset(s) to file ===========
        getItemset(curNode, sameCount);
        // ======== end of write to file

        int from_cursor = bf_cursor;
        int from_col = bf_col;
        int from_size = bf_currentSize;
        NodeListTreeNode child = curNode.firstChild;
        NodeListTreeNode next;
        while (child != null) {
            next = child.next;
            traverse(child, level + 1, sameCount);
            for (int c = bf_col; c > from_col; c--) {
                bf[c] = null;
            }
            bf_col = from_col;
            bf_cursor = from_cursor;
            bf_currentSize = from_size;
            child = next;
        }
        resultLen--;
    }

    /**
     * This method write an itemset to file + all itemsets that can be made
     * using its node list.
     *
     * @param curNode   the current node
     * @param sameCount the same count
     */
    private void getItemset(NodeListTreeNode curNode, int sameCount) {
        if (curNode.support >= minSupport) {
            outputCount++;
            Set<String> itemset = new HashSet<>();
            // append items from the itemset to the StringBuilder
            for (int i = 0; i < resultLen && i < maximumItemCount; i++) {
                itemset.add(item[result[i]].data);
            }
            // append the support of the itemset
            supportCountMap.put(itemset, curNode.support);
        }
        // === Write all combination that can be made using the node list of
        // this itemset
        if (sameCount > 0) {
            // generate all subsets of the node list except the empty set
            for (long i = 1, max = 1 << sameCount; i < max; i++) {
                Set<String> itemset = new HashSet<>();
                for (int k = 0; k < resultLen && k < maximumItemCount; k++) {
                    itemset.add(item[result[k]].data);
                }

                // we create a new subset
                for (int j = 0; j < sameCount && itemset.size() < maximumItemCount; j++) {
                    // check if the j bit is set to 1
                    int isSet = (int) i & (1 << j);
                    if (isSet > 0) {
                        // if yes, add it to the set
                        itemset.add(item[sameItems[j]].data);
                        // newSet.add(item[sameItems[j]].index);
                    }
                }
                if (!supportCountMap.containsKey(itemset)) {
                    supportCountMap.put(itemset, curNode.support);
                    outputCount++;
                }
            }
        }
    }

    /**
     * Print statistics about the latest execution of the algorithm to
     * System.out.
     */
    public void printStats() {
        String prePost = "PrePost+";
        System.out.println("========== " + prePost + " - STATS ============");
        System.out.println(" Minsup = " + minSupport
                + "\n Number of transactions: " + numberOfTransactions);
        System.out.println(" Number of frequent  itemsets: " + outputCount);
        System.out.println(" Total time ~: " + (endTimestamp - startTimestamp)
                + " ms");
        System.out.println(" Max memory:"
                + MemoryLogger.getInstance().getMaxMemory() + " MB");
        System.out.println("=====================================");
    }

    /**
     * Class to pass an integer by reference as in C++
     */
    class IntegerByRef {
        int count;
    }

    class Item {
        public String data;
        private int frequency;
    }

    class NodeListTreeNode {
        public int label;
        private NodeListTreeNode firstChild;
        public NodeListTreeNode next;
        public int support;
        private int NLStartinBf;
        private int NLLength;
        private int NLCol;
    }

    class PPCTreeNode {
        private int label;
        private PPCTreeNode firstChild;
        private PPCTreeNode rightSibling;
        private PPCTreeNode labelSibling;
        private PPCTreeNode father;
        public int count;
        private int foreIndex;
        private int backIndex;
    }
}

class MemoryLogger {

    // the only instance  of this class (this is the "singleton" design pattern)
    private static MemoryLogger instance = new MemoryLogger();

    // variable to store the maximum memory usage
    private double maxMemory = 0;

    /**
     * Method to obtain the only instance of this class
     *
     * @return instance of MemoryLogger
     */
    public static MemoryLogger getInstance() {
        return instance;
    }

    /**
     * To get the maximum amount of memory used until now
     *
     * @return a double value indicating memory as megabytes
     */
    public double getMaxMemory() {
        return maxMemory;
    }

    /**
     * Reset the maximum amount of memory recorded.
     */
    public void reset() {
        maxMemory = 0;
    }

    /**
     * Check the current memory usage and record it if it is higher
     * than the amount of memory previously recorded.
     */
    public void checkMemory() {
        double currentMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                / 1024d / 1024d;
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }
}