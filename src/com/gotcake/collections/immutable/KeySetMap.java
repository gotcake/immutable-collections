package com.gotcake.collections.immutable;

import java.util.*;

/**
 * A simple map to help constructing ImmutableSets from sets
 * @author Aaron Cake
 */
class KeySetMap<T> implements Map<T, Boolean> {

    Set<T> keys;

    @SuppressWarnings("unchecked")
    public KeySetMap(final Collection<? extends T> keys) {
        if (keys instanceof Set) {
            this.keys = (Set<T>) keys;
        } else {
            this.keys = new HashSet<>(keys);
        }
    }
    @SuppressWarnings("unchecked")
    public KeySetMap(final T... keys) {
        this.keys = new HashSet<>((int)Math.ceil(keys.length / 0.75), 0.75f);
        Collections.addAll(this.keys, keys);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean get(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public Boolean put(T key, Boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Boolean remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends T, ? extends Boolean> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> keySet() {
        return (Set<T>)keys;
    }

    @Override
    public Collection<Boolean> values() {
        return Collections.singleton(Boolean.TRUE);
    }

    @Override
    public Set<Entry<T, Boolean>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
