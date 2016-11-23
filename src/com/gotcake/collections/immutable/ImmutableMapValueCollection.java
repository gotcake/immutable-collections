package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Aaron Cake (acake)
 */
class ImmutableMapValueCollection<T> implements ImmutableCollection<T> {

    private final ImmutableMap<?, T> map;

    ImmutableMapValueCollection(ImmutableMap<?, T> map) {
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
        return map.containsValue(o);
    }

    @Override
    public Iterator<T> iterator() {
        return map.valueIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        map.forEachValue(action);
    }
}
