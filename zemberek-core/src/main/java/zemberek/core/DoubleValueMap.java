package zemberek.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A compact set structure for double valued objects.
 *
 * @param <T>
 */
public class DoubleValueMap<T> extends HashBase<T> implements Iterable<T> {

    // Carries count values.
    private double[] values;

    public DoubleValueMap() {
        this(INITIAL_SIZE);
    }

    public DoubleValueMap(int size) {
        int k = INITIAL_SIZE;
        while (k < size)
            k <<= 1;
        keys = (T[]) new Object[k];
        values = new double[k];
        threshold = (int) (k * DEFAULT_LOAD_FACTOR);
        modulo = k - 1;
    }

    /**
     * If key does not exist, it adds it with count value 1. Otherwise, it increments the count value by 1.
     *
     * @param key key
     * @return the new count value after increment
     */
    public double increment(T key) {
        return incrementByAmount(key, 1);
    }

    /**
     * Adds all keys in Iterable.
     * If key does not exist, it adds it with count value 1. Otherwise, it increments the count value by 1.
     *
     * @param keys key
     */
    public void incrementAll(Iterable<T> keys) {
        for (T t : keys) {
            incrementByAmount(t, 1);
        }
    }

    /**
     * Returns the count of the key. If key does not exist, returns 0.
     *
     * @param key key
     * @return count of the key
     */
    public double get(T key) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        int probeCount = 0;
        int slot = firstProbe(hash(key));
        while (true) {
            final T t = keys[slot];
            if (t == null) {
                return 0;
            }
            if (t == TOMB_STONE) {
                slot = nextProbe(slot, ++probeCount);
                continue;
            }
            if (t.equals(key))
                return values[slot];
            slot = nextProbe(slot, ++probeCount);
        }
    }

    public double decrement(T key) {
        return incrementByAmount(key, -1);
    }

    public boolean contains(T key) {
        return locate(key) >= 0;
    }

    /**
     * increment the value by "amount". If value does not exist, it a applies set() operation.
     *
     * @param key    key
     * @param amount amount to increment
     * @return incremented value
     */
    public double incrementByAmount(T key, double amount) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        if (keyCount + removeCount == threshold) {
            expand();
        }
        int l = locate(key);
        if (l < 0) {
            l = -l - 1;
            values[l] = amount;
            keys[l] = key;
            keyCount++;
            return values[l];
        } else {
            values[l] += amount;
            return values[l];
        }
    }

    private void expand() {
        DoubleValueMap<T> h = new DoubleValueMap<>(values.length * 2);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null && keys[i] != TOMB_STONE)
                h.set(keys[i], values[i]);
        }
        assert (h.keyCount == keyCount);
        this.values = h.values;
        this.keys = h.keys;
        this.keyCount = h.keyCount;
        this.modulo = h.modulo;
        this.threshold = h.threshold;
        this.removeCount = 0;
    }

    /**
     * Sets the key with the value. If there is a matching key, it overwrites it (key and the value).
     *
     * @param key   key
     * @param value value
     */
    public void set(T key, double value) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        if (keyCount + removeCount == threshold) {
            expand();
        }
        int loc = locate(key);
        if (loc >= 0) {
            keys[loc] = key;
            values[loc] = value;
        } else {
            loc = -loc - 1;
            keys[loc] = key;
            values[loc] = value;
            keyCount++;
        }
    }

    public double[] values() {
        double[] result = new double[size()];
        int j = 0;
        for (int i = 0; i < keys.length; i++) {
            T key = keys[i];
            if(key!=null && key!=TOMB_STONE) {
                result[j++] = values[i];
            }
        }
        return result;
    }

    public List<Entry<T>> getAsEntryList() {
        List<Entry<T>> res = new ArrayList<>(keyCount);
        for (int i = 0; i < keys.length; i++) {
            if (keys[i] != null && keys[i] != TOMB_STONE)
                res.add(new Entry<>(keys[i], values[i]));
        }
        return res;
    }

    public Iterator<Entry<T>> entryIterator() {
        return new EntryIterator();
    }

    public Iterable<Entry<T>> iterableEntries() {
        return EntryIterator::new;
    }

    private class EntryIterator implements Iterator<Entry<T>> {

        int i;
        int k;

        @Override
        public boolean hasNext() {
            return k < keyCount;
        }

        @Override
        public Entry<T> next() {
            while (keys[i] == null || keys[i] == TOMB_STONE) {
                i++;
            }
            Entry<T> te = new Entry<>(keys[i], values[i]);
            i++;
            k++;
            return te;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Entry<T> implements Comparable<Entry<T>> {
        public final T key;
        public final double value;

        public Entry(T key, double value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(Entry<T> o) {
            return Double.compare(value, o.value);
        }
    }
}