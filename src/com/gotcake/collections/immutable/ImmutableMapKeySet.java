package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Aaron Cake (gotcake)
 */
class ImmutableMapKeySet<T> extends ImmutableSet<T, ImmutableMapKeySet<T>> {

    private final ImmutableMap<T, ?, ?> map;

    ImmutableMapKeySet(ImmutableMap<T, ?, ?> map) {
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
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return map.keyIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        map.forEachKey(action);
    }

    @Override
    public ImmutableMapKeySet<T> insert(T element) {
        throw new UnsupportedOperationException("insert is not supported");
    }

    @Override
    public ImmutableMapKeySet<T> delete(T element) {
        throw new UnsupportedOperationException("delete is not supported");
    }
}
