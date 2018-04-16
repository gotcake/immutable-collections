package com.gotcake.collections.immutable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Aaron Cake
 */
class EmptyImmutableSet<T> implements ImmutableSet<T> {

    private static EmptyImmutableSet INSTANCE = new EmptyImmutableSet();

    @SuppressWarnings("unchecked")
    static <T> EmptyImmutableSet<T> getInstance() {
        return INSTANCE;
    }

    protected EmptyImmutableSet() {}

    private static Object[] EMPTY_ARRAY = new Object[0];

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(final Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.empty();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        // do nothing
    }

    @Override
    public Stream<T> stream() {
        return Stream.empty();
    }

    @Override
    public Stream<T> parallelStream() {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof Set && ((Set)obj).isEmpty());
    }

    @Override
    public ImmutableSet<T> insert(final T element) {
        return ImmutableSet.of(element);
    }

    @Override
    public ImmutableSet<T> delete(final T element) {
        return this;
    }

    @Override
    public ImmutableSet<T> filter(final Predicate<T> predicate) {
        return this;
    }

    @Override
    public ImmutableSet<T> insertAll(final Collection<? extends T> elements) {
        return ImmutableSet.of(elements);
    }


    @Override
    public ImmutableSet<T> keepAll(final  Collection<? extends T> elements) {
        return this;
    }

    @Override
    public ImmutableSet<T> deleteAll(final Collection<? extends T> elements) {
        return this;
    }

    @Override
    public Object[] toArray() {
        return EMPTY_ARRAY;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(final T1[] a) {
        return a.length == 0 ? a : (T1[]) Array.newInstance(a.getClass().getComponentType(), 0);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return c.isEmpty();
    }

}
