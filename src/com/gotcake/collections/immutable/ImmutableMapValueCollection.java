package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Aaron Cake (acake)
 */
class ImmutableMapValueCollection<K, V> implements ImmutableCollection<V> {

    private final ImmutableMap<K, V> map;

    ImmutableMapValueCollection(final ImmutableMap<K, V> map) {
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
    public Iterator<V> iterator() {
        return map.valueIterator();
    }

    @Override
    public void forEach(final Consumer<? super V> action) {
        map.forEachValue(action);
    }


}
