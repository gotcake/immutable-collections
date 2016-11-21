package com.gotcake.collections.immutable;

import java.util.*;

/**
 * @author Aaron Cake (gotcake)
 */
abstract class ImmutableSet<T, SelfType extends ImmutableSet<T, SelfType>> extends ImmutableCollection<T, SelfType> implements Set<T> {

    public abstract SelfType insert(T element);
    public abstract SelfType delete(T element);

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.CONCURRENT);
    }
}
