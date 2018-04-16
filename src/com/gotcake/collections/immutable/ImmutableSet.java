package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.Function;
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
        int size = elements.length;
        if (size == 0) {
            return EmptyImmutableSet.getInstance();
        } else if (size == 1) {
            return of(elements[0]);
        } else {
            final Set<T> keys = new HashSet<>();
            Collections.addAll(keys, elements);
            if (keys.contains(null)) {
                throw new NullPointerException();
            }
            return new MapBackedImmutableTrieSet<>(
                    new RegularImmutableTrieMap<>(keys, Boolean.TRUE)
            );
        }
    }

    static <T> ImmutableSet<T> of(final Collection<? extends T> elements) {
        int size = elements.size();
        if (size == 0) {
            return EmptyImmutableSet.getInstance();
        } else if (size == 1) {
            return of(elements.iterator().next());
        } else {
            if (elements instanceof Set) {
                @SuppressWarnings("unchecked")
                final Set<T> keys = (Set)elements;
                if (keys.contains(null)) {
                    throw new NullPointerException();
                }
                return new MapBackedImmutableTrieSet<>(
                        new RegularImmutableTrieMap<>(keys, Boolean.TRUE)
                );
            } else {
                final Set<T> keys = new HashSet<>(elements);
                if (keys.contains(null)) {
                    throw new NullPointerException();
                }
                return new MapBackedImmutableTrieSet<>(
                        new RegularImmutableTrieMap<>(keys, Boolean.TRUE)
                );
            }
        }
    }

    ImmutableSet<T> insert(final T element);
    ImmutableSet<T> delete(final T element);


    /**
     * Filters the elements of this set with the given predicate
     * @param predicate a function which should return true to keep and element, or false to remove it
     * @return the new set, or this set if not changes were required
     */
    ImmutableSet<T> filter(final Predicate<T> predicate);

    /**
     * Computes a new set by inserting all the given elements.
     * Returns a new set if modifications were required, otherwise this set is returned.
     * @param elements the elements to insert
     * @return the new set, or this set if no changes were required
     */
    default ImmutableSet<T> insertAll(final Collection<? extends T> elements) {
        ImmutableSet<T> set = this;
        for (final T element: elements) {
            set = set.insert(element);
        }
        return set;
    }

    /**
     * Computes a new set by keeping only the given elements. Other elements are deleted if present.
     * Returns a new set if modifications were required, otherwise this set is returned.
     * @param elements the elements to keep
     * @return the new set, or this set if no changes were required
     */
    default ImmutableSet<T> keepAll(final Collection<? extends T> elements) {
        final Set set;
        // if input is not a set, make a copy so contains check if O(1)
        if (elements instanceof Set) {
            set = (Set)elements;
        } else {
            set = new HashSet<>(elements);
        }
        return this.filter(set::contains);
    }

    /**
     * Computes a new set by deleting all the given elements if present.
     * Returns a new set if modifications were required, otherwise this set is returned.
     * @param elements the elements to insert
     * @return the new set, or this set if no changes were required
     */
    default ImmutableSet<T> deleteAll(final Collection<? extends T> elements) {
        ImmutableSet<T> set = this;
        for (final T element: elements) {
            set = set.delete(element);
        }
        return set;
    }

    @Override
    default Object[] toArray() {
        return Iterators.toObjectArray(size(), iterator());
    }

    @Override
    default <A> A[] toArray(final A[] a) {
        return Iterators.toArrayTypeChecked(size(), iterator(), a);
    }

    @Override
    @Deprecated
    default boolean add(final T item) {
        throw new UnsupportedOperationException("add is not supported, use insert instead");
    }

    @Override
    @Deprecated
    default boolean remove(final Object o) {
        throw new UnsupportedOperationException("remove is not supported, use delete instead");
    }

    @Override
    default boolean containsAll(final Collection<?> c) {
        for (final Object o: c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Deprecated
    default boolean addAll(final Collection<? extends T> c) {
        throw new UnsupportedOperationException("addAll is not supported, use insertAll instead");
    }

    @Override
    @Deprecated
    default boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("retainAll is not supported, use keepAll instead");
    }

    @Override
    @Deprecated
    default boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("removeAll is not supported, use deleteAll instead");
    }

    @Override
    @Deprecated
    default boolean removeIf(final Predicate<? super T> filter) {
        throw new UnsupportedOperationException("removeIf is not supported, use filter instead");
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException("clear is not supported, use ImmutableMap.of() instead");
    }


    @Override
    default Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED);
    }

    default HashSet<T> asHashSet() {
        // size the map so that it won't need to expand
        final HashSet<T> map = new HashSet<>((int)Math.ceil(size() / 0.75), 0.75f);
        // faster than new HashSet(this) O(n) vs O(n * log32(n))
        forEach(map::add);
        return map;
    }

    default TreeSet<T> asTreeSet() {
        final TreeSet<T> set = new TreeSet<>();
        // faster than new TreeSet(this) O(n * log2(n)) vs O(n * log2(n) * log32(n))
        // and also prevents creation of n Map.Entry instances
        forEach(set::add);
        return set;
    }
}
