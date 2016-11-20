package com.gotcake.collections.immutable;

import java.util.*;

/**
 * @author Aaron Cake (gotcake)
 */
abstract class BaseImmutableSet<T, SelfType extends BaseImmutableSet<T, SelfType>> extends BaseImmutableCollection<T, SelfType> implements Set<T> {
    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED | Spliterator.CONCURRENT);
    }
}
