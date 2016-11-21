package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Aaron Cake (gotcake)
 */
public class ImmutableMapEntrySet<K, V> extends ImmutableSet<Map.Entry<K, V>, ImmutableMapEntrySet<K, V>> {

    private final ImmutableMap<K, V, ?> map;

    public ImmutableMapEntrySet(ImmutableMap<K, V, ?> map) {
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
}
