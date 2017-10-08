package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Aaron Cake (gotcake)
 */
public interface ImmutableSet<T> extends ImmutableCollection<T>, Set<T> {

    @SuppressWarnings("unchecked")
    static <T> ImmutableSet<T> of() {
        return EmptyImmutableSet.getInstance();
    }

    static <T> ImmutableSet<T> of(final T element) {
        if (element == null) throw new NullPointerException();
        return new MapBackedImmutableTrieSet<>(element);
    }

    static <T> ImmutableSet<T> of(final T element1, final T element2) {
        return new MapBackedImmutableTrieSet<>(element1, element2);
    }

    @SafeVarargs
    static <T> ImmutableSet<T> of(final T... elements) {
        switch (elements.length) {
            case 0:
                return EmptyImmutableSet.getInstance();
            case 1:
                return of(elements[0]);
        }
        ImmutableMap<T, Boolean> map = ImmutableMap.of(elements[0], Boolean.TRUE, elements[1], Boolean.TRUE);
        for (int i = 2; i < elements.length; i++) {
            map = map.set(elements[i], Boolean.TRUE);
        }
        return new MapBackedImmutableTrieSet<>(map);
    }

    ImmutableSet<T> insert(final T element);
    ImmutableSet<T> delete(final T element);

    @Override
    default Object[] toArray() {
        return Iterators.toObjectArray(size(), iterator());
    }

    @Override
    default <A> A[] toArray(final A[] a) {
        return Iterators.toArrayTypeChecked(size(), iterator(), a);
    }

    @Override
    default boolean add(final T item) {
        throw new UnsupportedOperationException("add is not supported");
    }

    @Override
    default boolean remove(final Object o) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    default boolean containsAll(final Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    default boolean addAll(final Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll is not supported");
    }

    @Override
    default boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("retainAll is not supported");
    }

    @Override
    default boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("removeAll is not supported");
    }

    @Override
    default boolean removeIf(final Predicate<? super T> filter) {
        throw new UnsupportedOperationException("removeIf is not supported");
    }

    @Override
    default void clear() {
        throw new UnsupportedOperationException("clear is not supported");
    }


    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.CONCURRENT);
    }
}
