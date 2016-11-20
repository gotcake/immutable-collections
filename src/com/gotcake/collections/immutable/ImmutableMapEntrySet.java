package com.gotcake.collections.immutable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Aaron Cake (gotcake)
 */
public class ImmutableMapEntrySet<K, V> extends BaseImmutableSet<Map.Entry<K, V>, ImmutableMapEntrySet<K, V>>  {

    private final BaseImmutableMap<K, V, ?> map;
    private List<BaseImmutableMap.Entry<K, V>> entryCache;

    public ImmutableMapEntrySet(BaseImmutableMap<K, V, ?> map) {
        this.map = map;
        entryCache = null;
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
        if (map.hasFastEntryIteration()) {
            return (Iterator)map.entryIterator();
        }
        if (entryCache == null) {
            entryCache = new ArrayList<>(size());
            final Iterator<BaseImmutableMap.Entry<K, V>> entryIterator = map.entryIterator();
            while (entryIterator.hasNext()) {
                entryCache.add(entryIterator.next());
            }
        }
        return (Iterator)entryCache.iterator();
    }
}
