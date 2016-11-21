package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A immutable Set backed by an ImmutableTrieMap
 * @author Aaron Cake
 */
public abstract class ImmutableTrieSet<T> extends ImmutableSet<T, ImmutableTrieSet<T>> {

    private static final ImmutableTrieSet EMPTY_INSTANCE = new Empty();

    @SuppressWarnings("unchecked")
    public static <T> ImmutableTrieSet<T> of() {
        return EMPTY_INSTANCE;
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableTrieSet<T> of(T element) {
        return new NonEmpty<>(ImmutableTrieMap.of(element, Boolean.TRUE));
    }

    private static final class Empty<T> extends ImmutableTrieSet<T> {

        private Empty() { }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<T> iterator() {
            return EmptyIterator.INSTANCE;
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            // do nothing
        }

        @Override
        public Stream<T> stream() {
            return Stream.empty();
        }

        @Override
        public Stream<T> parallelStream() {
            return Stream.empty();
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof Set && ((Set)obj).isEmpty());
        }

        @Override
        public ImmutableTrieSet<T> insert(T element) {
            return ImmutableTrieSet.of(element);
        }

        @Override
        public ImmutableTrieSet<T> delete(T element) {
            return this;
        }
    }

    private static final class NonEmpty<T> extends ImmutableTrieSet<T> {

        private final ImmutableTrieMap<T, Boolean> map;

        private NonEmpty(final ImmutableTrieMap<T, Boolean> map) {
            this.map = map;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public Iterator<T> iterator() {
            return map.keyIterator();
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            map.forEachKey(action);
        }

        @Override
        public int hashCode() {
            return map.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (!(obj instanceof Set)) return false;
            Set<?> other = (Set)obj;
            if (map.size() != other.size()) {
                return false;
            }
            if (obj instanceof NonEmpty) {
                return map.equals(((NonEmpty)obj).map);
            }
            Iterator<T> it = map.keyIterator();
            while (it.hasNext()) {
                if (!other.contains(it.next())) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ImmutableTrieSet<T> insert(T element) {
            ImmutableTrieMap<T, Boolean> newMap = map.set(element, Boolean.TRUE);
            return newMap != map ? new NonEmpty<>(newMap) : this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ImmutableTrieSet<T> delete(T element) {
            ImmutableTrieMap<T, Boolean> newMap = map.delete(element);
            if (newMap.isEmpty()) {
                return EMPTY_INSTANCE;
            }
            return newMap != map ? new NonEmpty<>(newMap) : this;
        }
    }

}
