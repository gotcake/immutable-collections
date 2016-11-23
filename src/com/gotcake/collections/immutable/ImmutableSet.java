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

    static <T> ImmutableSet<T> of(T element) {
        if (element == null) throw new NullPointerException();
        return new SingletonImmutableSet<>(element);
    }

    static <T> ImmutableSet<T> of(T element1, T element2) {
        if (element2 == null) throw new NullPointerException();
        if (element1.equals(element2)) {
            return new SingletonImmutableSet<>(element1);
        }
        return new MapBackedImmutableTrieSet<>(element1, element2);
    }

    @SafeVarargs
    static <T> ImmutableSet<T> of(T... elements) {
        ImmutableMap<T, Boolean> map = EmptyImmutableMap.getInstance();
        for (T element: elements) {
            map = map.set(element, Boolean.TRUE);
        }
        return new MapBackedImmutableTrieSet<>(map);
    }

    ImmutableSet<T> insert(T element);
    ImmutableSet<T> delete(T element);

    @Override
    default Object[] toArray() {
        return Iterators.toObjectArray(size(), iterator());
    }

    @Override
    default <A> A[] toArray(A[] a) {
        return Iterators.toArrayTypeChecked(size(), iterator(), a);
    }

    @Override
    default boolean add(T item) {
        throw new UnsupportedOperationException("add is not supported");
    }

    @Override
    default boolean remove(Object o) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll is not supported");
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll is not supported");
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll is not supported");
    }

    @Override
    default boolean removeIf(Predicate<? super T> filter) {
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
