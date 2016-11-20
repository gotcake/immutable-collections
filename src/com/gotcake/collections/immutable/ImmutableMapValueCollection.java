package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author Aaron Cake (acake)
 */
public class ImmutableMapValueCollection<T> extends BaseImmutableCollection<T, ImmutableMapValueCollection<T>> {

    private final BaseImmutableMap<?, T, ?> map;

    public ImmutableMapValueCollection(BaseImmutableMap<?, T, ?> map) {
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
