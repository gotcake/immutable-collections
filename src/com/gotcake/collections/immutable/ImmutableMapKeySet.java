package com.gotcake.collections.immutable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A special ImmutableSet that wraps the keys of an ImmutableMap
 */
public class ImmutableMapKeySet<K, V> extends MapBackedImmutableTrieSet<K> {

    ImmutableMapKeySet(final ImmutableMap<K, V> map) {
        super(map);
    }

    @Override
    public ImmutableMapKeySet<K, V> delete(final K element) {
        final ImmutableMap<K, V> newMap = getMap().delete(element);
        if (newMap != map) {
            if (newMap.size() == 0) {
                return new ImmutableMapKeySet<>(EmptyImmutableMap.getInstance());
            }
            return new ImmutableMapKeySet<>(newMap);
        }
        return this;
    }

    @Override
    public ImmutableMapKeySet<K, V> filter(final Predicate<K> predicate) {
        final ImmutableMap<K, V> newMap = getMap().filterKeys(predicate);
        if (newMap != map) {
            return new ImmutableMapKeySet<>(newMap);
        }
        return this;
    }

    @Override
    public ImmutableMapKeySet<K, V> keepAll(final Collection<? extends K> elements) {
        final Set set;
        // if input is not a set, make a copy so contains check if O(1)
        if (elements instanceof Set) {
            set = (Set)elements;
        } else {
            set = new HashSet<>(elements);
        }
        return this.filter(set::contains);
    }

    @Override
    public ImmutableMapKeySet<K, V> deleteAll(final Collection<? extends K> elements) {
        ImmutableMapKeySet<K, V> set = this;
        for (final K element: elements) {
            set = set.delete(element);
        }
        return set;
    }

    @SuppressWarnings("unchecked")
    public ImmutableMap<K, V> getMap() {
        return (ImmutableMap<K, V>)map;
    }
}
