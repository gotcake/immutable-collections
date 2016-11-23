package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

import static com.gotcake.collections.immutable.Util.assertValidType;

/**
 * A simple ImmutableSet implementation backed by an ImmutableMap
 * @author Aaron Cake
 */
final class MapBackedImmutableTrieSet<T> implements ImmutableSet<T>, Validatable {

    private final ImmutableMap<T, Boolean> map;

    MapBackedImmutableTrieSet(ImmutableMap<T, Boolean> map) {
        this.map = map;
    }

    MapBackedImmutableTrieSet(T element1, T element2) {
        this.map = new RegularImmutableTrieMap<>(element1, Boolean.TRUE, element2, Boolean.TRUE);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
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
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Set)) return false;
        Set<?> other = (Set)obj;
        if (map.size() != other.size()) {
            return false;
        }
        if (obj instanceof MapBackedImmutableTrieSet) {
            return map.equals(((MapBackedImmutableTrieSet)obj).map);
        }
        Iterator<T> it = map.keyIterator();
        while (it.hasNext()) {
            if (!other.contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ImmutableSet<T> insert(T element) {
        ImmutableMap<T, Boolean> newMap = map.set(element, Boolean.TRUE);
        return newMap != map ? new MapBackedImmutableTrieSet<>(newMap) : this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableSet<T> delete(T element) {
        ImmutableMap<T, Boolean> newMap = map.delete(element);
        if (newMap != map) {
            if (newMap.size() == 1) {
                return new SingletonImmutableSet<>(newMap.keyIterator().next());
            }
            return new MapBackedImmutableTrieSet<>(newMap);
        }
        return this;
    }

    @Override
    public void assertValid(Writer debugWriter) throws IOException {
        assertValidType("map", map, false, RegularImmutableTrieMap.class);
        ((Validatable)map).assertValid(debugWriter);
    }
}
