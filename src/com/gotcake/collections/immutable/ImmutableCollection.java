package com.gotcake.collections.immutable;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * @author Aaron Cake (acake)
 */
public interface ImmutableCollection<T> extends Collection<T> {

    @Override
    default Object[] toArray() {
        return Iterators.toObjectArray(size(), iterator());
    }

    @Override
    default <A> A[] toArray(final A[] a) {
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
                Spliterator.IMMUTABLE | Spliterator.SIZED);
    }
}
