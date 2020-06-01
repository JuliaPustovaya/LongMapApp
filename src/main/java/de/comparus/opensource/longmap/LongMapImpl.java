package de.comparus.opensource.longmap;

import java.util.*;
import java.util.stream.Stream;

public class LongMapImpl<V> implements LongMap<V> {
    
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    
    private Entry<V>[] entryTable;
    
    private int size;
    
    private int threshold;
    
    private final float loadFactor;
    
    public LongMapImpl() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }
    
    public LongMapImpl(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
    
    public LongMapImpl(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Only Initial power of 2 must be initialCapacity  : " + initialCapacity);
        }
        
        if (initialCapacity > MAXIMUM_CAPACITY) {
            initialCapacity = MAXIMUM_CAPACITY;
        }
        
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Wrong loadFactor: " + loadFactor);
        }
        
        int capacity = 1;
        while (capacity < initialCapacity) {
            capacity <<= 1;
        }
        
        entryTable = new Entry[capacity];
        this.loadFactor = loadFactor;
        threshold = (int) (capacity * loadFactor);
    }
    
    private Entry<V>[] getEntryTable() {
        return entryTable;
    }
    
    final int hash(Object key) {
        return Math.abs(key.hashCode()) % entryTable.length;
    }
    
    public V put(long key, V value) {
        int h = hash(key);
        Entry<V>[] tab = getEntryTable();
        
        for (Entry<V> e = tab[h]; e != null; e = e.next) {
            if (h == e.hash && (Objects.equals(key, e.k))) {
                V oldValue = e.v;
                if (value != oldValue) {
                    e.v = value;
                }
                return oldValue;
            }
        }
        
        Entry<V> e = tab[h];
        tab[h] = new Entry<>(h, key, value, e);
        
        if (++size >= threshold) {
            sizeUp(tab.length * 2);
        }
        
        return null;
    }
    
    private void sizeUp(int newCapacity) {
        Entry<V>[] oldTable = getEntryTable();
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }
        
        Entry<V>[] newTable = new Entry[newCapacity];
        transfer(oldTable, newTable);
        entryTable = newTable;
        
        if (size >= threshold / 2) {
            threshold = (int) (newCapacity * loadFactor);
        } else {
            transfer(newTable, oldTable);
            entryTable = oldTable;
        }
    }
    
    private void transfer(Entry<V>[] entrySrc, Entry<V>[] entryDest) {
        for (int j = 0; j < entrySrc.length; ++j) {
            Entry<V> e = entrySrc[j];
            entrySrc[j] = null;
            while (e != null) {
                Entry<V> next = e.next;
                e.next = entryDest[e.hash];
                entryDest[e.hash] = e;
                e = next;
            }
        }
    }
    
    private Entry<V> getEntry(Object key) {
        int h = hash(key);
        Entry<V>[] tab = getEntryTable();
        Entry<V> e = tab[h];
        while (e != null && !(e.hash == h && (Objects.equals(key, e.k)))) {
            e = e.next;
        }
        return e;
    }
    
    private boolean containsNullValue() {
        Entry<V>[] tab = getEntryTable();
        
        for (int i = tab.length; i-- > 0; ) {
            for (Entry<V> e = tab[i]; e != null; e = e.next) {
                if (e.v == null) {
                    return true;
                }
            }
        }
        return false;
        
    }
    
    public V get(long key) {
        int h = hash(key);
        Entry<V>[] tab = getEntryTable();
        Entry<V> e = tab[h];
        while (e != null) {
            if (e.hash == h && (Objects.equals(key, e.k))) {
                return e.v;
            }
            e = e.next;
        }
        return null;
    }
    
    public V remove(long key) {
        int h = hash(key);
        Entry<V>[] tab = getEntryTable();
        Entry<V> prev = tab[h];
        Entry<V> e = prev;
        
        while (e != null) {
            Entry<V> next = e.next;
            if (h == e.hash && (Objects.equals(key, e.k))) {
                size--;
                if (prev == e) {
                    tab[h] = next;
                } else {
                    prev.next = next;
                }
                return e.v;
            }
            prev = e;
            e = next;
        }
        
        return null;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean containsKey(long key) {
        return getEntry(key) != null;
    }
    
    public boolean containsValue(V value) {
        if (value == null) {
            return containsNullValue();
        }
        
        Entry<V>[] tab = getEntryTable();
        
        for (int i = tab.length; i-- > 0; ) {
            for (Entry<V> e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.v)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public long[] keys() {
        return Stream.of(getEntryTable()).filter(Objects::nonNull).mapToLong(Entry::getKey).toArray();
    }
    
    @SuppressWarnings("unchecked")
    public V[] values() {
        return (size > 0) ? (V[]) Stream.of(getEntryTable()).filter(Objects::nonNull).map(Entry::getValue).toArray()
                : (V[]) Stream.empty().toArray();
    }
    
    public long size() {
        return size;
    }
    
    public void clear() {
        if (entryTable != null && size > 0) {
            size = 0;
            Arrays.fill(entryTable, null);
        }
        
    }
    
    static class Entry<V> {
        private final long k;
        private V v;
        final int hash;
        private Entry<V> next;
        
        public Entry(int hash, long k, V v, Entry<V> next) {
            this.k = k;
            this.v = v;
            this.next = next;
            this.hash = hash;
        }
        
        public long getKey() {
            return k;
        }
        
        public V getValue() {
            return v;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !this.getClass().getName().equals(o.getClass().getName())) {
                return false;
            }
            Entry<V> e = (Entry<V>) o;
            long k1 = getKey();
            Object k2 = e.getKey();
            if (Objects.equals(k1, k2)) {
                V v1 = getValue();
                Object v2 = e.getValue();
                return Objects.equals(v1, v2);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            
            long key = getKey();
            V value = getValue();
            
            int result = 1;
            final int prime = 31;
            result = (int) (prime * result + key);
            result = prime * result + ((value == null) ? 0 : value.hashCode());
            
            return result;
        }
        
        @Override
        public String toString() {
            return "key:  " + getKey() + " val: " + getValue();
        }
    }
}