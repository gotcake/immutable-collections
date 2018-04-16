package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Aaron Cake (gotcake)
 */
public class ImmutableMapEntrySet<K, V> extends AbstractImmutableSet<Map.Entry<K, V>> {

    private final ImmutableMap<K, V> map;

    ImmutableMapEntrySet(final ImmutableMap<K, V> map) {
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
    public boolean contains(final Object o) {
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
    public ImmutableMapEntrySet<K, V> insert(final Map.Entry<K, V> element) {
        final ImmutableMap<K, V> newMap = map.setIfAbsent(element.getKey(), element.getValue());
        if (newMap != map) {
            return new ImmutableMapEntrySet<>(newMap);
        }
        return this;
    }

    @Override
    public ImmutableMapEntrySet<K, V> delete(final Map.Entry<K, V> element) {
        final ImmutableMap<K, V> newMap = map.setIfAbsent(element.getKey(), element.getValue());
        if (newMap != map) {
            return new ImmutableMapEntrySet<>(newMap);
        }
        return this;
    }

    @Override
    public ImmutableMapEntrySet<K, V> filter(final Predicate<Map.Entry<K, V>> predicate) {
        final ImmutableMap<K, V> newMap = map.filter((k, v) -> predicate.test(new ImmutableMap.Entry<>(k, v)));
        if (newMap != map) {
            return new ImmutableMapEntrySet<>(newMap);
        }
        return this;
    }

    @Override
    public ImmutableMapEntrySet<K, V> insertAll(final Collection<? extends Map.Entry<K, V>> elements) {
        ImmutableMapEntrySet<K, V> set = this;
        for (final Map.Entry<K, V> element: elements) {
            set = set.insert(element);
        }
        return set;
    }

    @Override
    public ImmutableMapEntrySet<K, V> keepAll(Collection<? extends Map.Entry<K, V>> elements) {
        final Set set;
        // if input is not a set, make a copy so contains is a fast check
        if (elements instanceof Set) {
            set = (Set)elements;
        } else {
            set = new HashSet<>(elements);
        }
        return this.filter(set::contains);
    }

    @Override
    public ImmutableMapEntrySet<K, V> deleteAll(Collection<? extends Map.Entry<K, V>> elements) {
        ImmutableMapEntrySet<K, V> set = this;
        for (final Map.Entry<K, V> element: elements) {
            set = set.delete(element);
        }
        return set;
    }

    public ImmutableMap<K, V> getMap() {
        return map;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

}
