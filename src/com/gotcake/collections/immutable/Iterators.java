package com.gotcake.collections.immutable;

import java.util.*;
import java.lang.reflect.Array;
import java.util.function.Consumer;

/**
 * Util methods for iterators
 * @author Aaron Cake (gotcake)
 */
public class Iterators {

    @SuppressWarnings("unchecked")
    private static Iterator EMPTY_INSTANCE = new EmptyIterator();

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> empty() {
        return EMPTY_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> nonnullSingleton(T value) {
        return new NonnullSingletonIterator<>(value);
    }

    public static <T> List<T> toList(final int size, final Iterator<T> it) {
        final ArrayList<T> list = new ArrayList<>(size);
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
    public static <T> Set<T> toSet(final int size, final Iterator<T> it) {
        final HashSet<T> list = new HashSet<>(size);
        while (it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArrayTypeChecked(final int size, final Iterator<?> it, T[] array) {
        final Class<?> elementType = array.getClass().getComponentType();
        if (array.length != size) {
            array = (T[]) Array.newInstance(elementType, size);
        }
        int i = 0;
        while (it.hasNext()) {
            final Object el = it.next();
            if (!elementType.isInstance(el)) {
                throw new ArrayStoreException(el + " is not an instance of " + elementType.getSimpleName());
            }
            array[i++] = (T)el;
        }
        return array;
    }

    public static Object[] toObjectArray(final int size, final Iterator<?> it) {
        final Object[] array = new Object[size];
        int i = 0;
        while (it.hasNext()) {
            array[i++] = it.next();
        }
        return array;
    }

    private static final class EmptyIterator<T> implements Iterator<T> {

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

    private static final class NonnullSingletonIterator<T> implements Iterator<T> {

        private T value;

        private NonnullSingletonIterator(T value) {
            this.value = value;
        }

        @Override
        public boolean hasNext() {
            return value != null;
        }

        @Override
        public T next() {
            if (value == null) {
                throw new NoSuchElementException();
            }
            T temp = value;
            value = null;
            return temp;
        }

        @Override
        public void forEachRemaining(final Consumer<? super T> action) {
            if (value != null) {
                action.accept(value);
                value = null;
            }
        }
    }

}
