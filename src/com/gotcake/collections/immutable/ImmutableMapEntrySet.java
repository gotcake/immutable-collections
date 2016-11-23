package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Aaron Cake (gotcake)
 */
class ImmutableMapEntrySet<K, V> implements ImmutableSet<Map.Entry<K, V>> {

    private final ImmutableMap<K, V> map;

    ImmutableMapEntrySet(ImmutableMap<K, V> map) {
        this.map = map;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        final Map.Entry entry = (Map.Entry)o;
        return map.containsEntry((K)entry.getKey(), (V)entry.getValue());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<Map.Entry<K, V>> iterator() {
        return (Iterator)map.entryIterator();
    }

    @Override
    public ImmutableMapEntrySet<K, V> insert(Map.Entry<K, V> element) {
        throw new UnsupportedOperationException("insert is not supported");
    }

    @Override
    public ImmutableMapEntrySet<K, V> delete(Map.Entry<K, V> element) {
        throw new UnsupportedOperationException("delete is not supported");
    }
}
