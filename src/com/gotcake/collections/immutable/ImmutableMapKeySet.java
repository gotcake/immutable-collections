package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author Aaron Cake (gotcake)
 */
class ImmutableMapKeySet<T> extends BaseImmutableSet<T, ImmutableMapKeySet<T>> {

    private final BaseImmutableMap<T, ?, ?> map;

    ImmutableMapKeySet(BaseImmutableMap<T, ?, ?> map) {
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
}
