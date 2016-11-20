package com.gotcake.collections.immutable;

import java.util.*;
import java.lang.reflect.Array;

/**
 * Util methods for iterators
 * @author Aaron Cake (gotcake)
 */
public class Iterators {
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
    public static <T> T[] toArray(final int size, final Iterator<?> it, T[] array) {
        if (array.length != size) {
            array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
        }
        int i = 0;
        while (it.hasNext()) {
            array[i++] = (T)it.next();
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
}
