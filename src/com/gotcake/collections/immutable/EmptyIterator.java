package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * An empty Iterator singleton
 * @author Aaron Cake (gotcake)
 */
public final class EmptyIterator<T> implements Iterator<T> {

    private static EmptyIterator INSTANCE = new EmptyIterator();

    @SuppressWarnings("unchecked")
    static <T> EmptyIterator<T> get() {
        return INSTANCE;
    }

    private EmptyIterator() {}

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        return null;
    }

    @Override
    public void forEachRemaining(final Consumer<? super T> action) {
        // do nothing
    }
}
