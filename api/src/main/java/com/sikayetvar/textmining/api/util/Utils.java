package com.sikayetvar.textmining.api.util;

import java.util.*;
import java.util.stream.Collectors;

public class Utils {
    public static long mergeInts(int a, int b) {
        return (long) a << 32 | b & 0xFFFFFFFFL;
    }

    public static Pair<Integer, Integer> splitLong(long a) {
        return new Pair<Integer, Integer>((int) (a >> 32), (int) a);
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, long topN) {
        return map.entrySet().stream().sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).limit(topN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Generates permutation of given list with order k, that is P(n,k)
     *
     * @param original The list of items to be permuted
     * @param k        The order of permutations, e.g. k=2 means binary tuples
     * @param <E>      Type parameter
     * @return Permutation set of given list with order k
     */
    public static <E> List<List<E>> generatePermutations(List<E> original, int k) {
        List<List<E>> permutations = new ArrayList<List<E>>();
        if (k == 0) {
            permutations.add(new ArrayList<>());
        } else {
            for (int i = 0; i < original.size(); i++) {
                List<E> remnant = new ArrayList<E>(original);
                E prefix = original.get(i);
                remnant.remove(i);
                List<List<E>> subPermutations = generatePermutations(remnant, k - 1);
                for (List<E> subPermutation : subPermutations) {
                    ArrayList<E> permutation = new ArrayList<>();
                    permutation.add(prefix);
                    permutation.addAll(subPermutation);
                    permutations.add(permutation);
                }
            }
        }

        return permutations;
    }

    /**
     * If the specified key is not already associated with a value associates it with the given value
     * and returns the value, else returns the current value.
     *
     * @param map   The map to be handled
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param <K>   the type of keys maintained by this map
     * @param <V>   the type of mapped values
     * @return the previous value associated with the specified key, or
     * the value given if there was no mapping for the key.
     */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        V existingValue = map.putIfAbsent(key, value);
        if (existingValue == null)
            return value;
        else
            return existingValue;
    }
}
