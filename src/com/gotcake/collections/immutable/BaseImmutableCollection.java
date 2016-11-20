package com.gotcake.collections.immutable;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;

/**
 * @author Aaron Cake (acake)
 */
abstract class BaseImmutableCollection<T, SelfType extends BaseImmutableCollection<T, SelfType>> implements Collection<T> {

    @Override
    public Object[] toArray() {
        return Iterators.toObjectArray(size(), iterator());
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO: throw ArrayStoreException:
        // if the runtime type of the specified array is not a supertype of the runtime type of every element in this set
        return Iterators.toArray(size(), iterator(), a);
    }

    @Override
    public boolean add(T item) {
        throw new UnsupportedOperationException("add is not supported");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll is not supported");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("retainAll is not supported");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("removeAll is not supported");
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException("removeIf is not supported");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("clear is not supported");
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.SIZED | Spliterator.CONCURRENT);
    }
}
