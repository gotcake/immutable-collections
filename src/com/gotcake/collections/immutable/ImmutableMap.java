package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Aaron Cake (acake)
 */
public interface ImmutableMap<K, V> extends Map<K, V> {

    @SuppressWarnings("unchecked")
    static <K, V> ImmutableMap<K, V> of() {
        return EmptyImmutableMap.getInstance();
    }

    static <K, V> ImmutableMap<K, V> of(K key, V value) {
        return new RegularImmutableTrieMap<>(key, value);
    }

    static <K, V> ImmutableMap<K, V> of(K key1, V value1, K key2, V value2) {
        return new RegularImmutableTrieMap<>(key1, value1, key2, value2);
    }

    static <K, V> ImmutableMap<K, V> of(K key1, V value1, K key2, V value2, K key3, V value3) {
        return of(key1, value1, key2, value2).set(key3, value3);
    }

    /**
     * A generic Entry class
     * @author Aaron Cake
     */
    class Entry<K, V> implements Map.Entry<K, V> {

        public final K key;
        public final V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("setValue is not supported");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            final Object otherKey = entry.getKey();
            final Object otherValue = entry.getValue();
            return key.equals(otherKey) && value.equals(otherValue);
        }

        @Override
        public int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }

    }

    boolean containsEntry(final K key, final V value);
    Iterator<ImmutableMap.Entry<K, V>> entryIterator();
    Iterator<K> keyIterator();
    Iterator<V> valueIterator();
    void forEachKey(Consumer<? super K> action);
    void forEachValue(Consumer<? super V> action);

    /**
     * Computes the a new internal with the given key and value returned by remapperFn.
     * If computeFn returns the existing value,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or mapperFn is null
     */
    ImmutableMap<K, V> update(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn);

    /**
     * Computes the a new internal with the given key and value returned by remapperFn only if there is already a value for the given key.
     * If this internal doesn't contain the given key, mapperFn is never called.
     * If this internal doesn't contain the given key or remapperFn returns the existing value,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or computeFn is null
     */
    default ImmutableMap<K, V> updateIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        return update(key, (theKey, value) -> {
            if (value != null) {
                return mapperFn.apply(theKey, value);
            }
            return null;
        });
    }

    /**
     * Computes the a new internal with the given key and value returned by computeFn only if there is no value for the given key.
     * If this internal already contains the given key, computeFn is never called.
     * If this internal already contains the given key or computeFn returns null,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param computeFn a function which computes the value
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or computeFn is null
     */
    default ImmutableMap<K, V> updateIfAbsent(final K key, final Function<? super K, ? extends V> computeFn) {
        return update(key, (theKey, value) -> {
            if (value == null) {
                return computeFn.apply(theKey);
            }
            return value;
        });
    }

    /**
     * Computes the a new internal with the given key-value pair.
     * If this internal already contains the key-value pair, this internal is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> set(final K key, final V value);


    /**
     * Computes the a new internal with the given key-value pair only if there is no value for the given key.
     * If this internal already contains the given key, this internal is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> setIfAbsent(final K key, final V value);


    /**
     * Computes the a new internal with the given key-value pair only if there is already a value for the given key.
     * If this internal already contains the given key-value pair or a value for the given key is missing,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> setIfPresent(final K key, final V value);


    /**
     * Computes the a new internal with the given key-value pair only if the current value equals oldValue.
     * If this internal already contains the given key-value pair or the given key is missing or has a value not equal to oldValue,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param matchValue the value to match
     * @param newValue the value to set if the current value equals oldValue
     * @return the new internal instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or newValue are null
     */
    @SuppressWarnings("unchecked")
    default ImmutableMap<K, V> setIfMatch(final K key, final V matchValue, final V newValue) {
        if (newValue == null) { throw new NullPointerException(); }
        if (matchValue == null || matchValue.equals(newValue)) {
            return this;
        }
        return update(key, (theKey, value) -> {
            if (value.equals(matchValue)) {
                return newValue;
            }
            return value;
        });
    }

    /**
     * Deletes an entry with the given key, if it exists.
     * If a modification is required, a new internal is returned, otherwise this object is returned.
     * @param key the key
     * @return the new internal, or this object if no modification was required
     */
    ImmutableMap<K, V> delete(final K key);

    /**
     * Deletes an entry only if it matches the given key and value.
     * If a modification is required, a new internal is returned, otherwise this object is returned.
     * @param key the key
     * @param matchValue the value
     * @return the new internal, or this object if no modification was required
     */
    @SuppressWarnings("unchecked")
    default ImmutableMap<K, V> deleteIfMatch(final K key, final V matchValue) {
        if (matchValue == null) {
            return this;
        }
        return update(key, (theKey, value) -> {
            if (value.equals(matchValue)) {
                return null;
            }
            return value;
        });
    }

    @Override
    @Deprecated
    default V put(K key, V value) {
        throw new UnsupportedOperationException("update is not supported");
    }

    @Override
    @Deprecated
    default V remove(Object key) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    @Deprecated
    default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("putAll is not supported");
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException("clear is not supported");
    }

    @Override
    default Set<Map.Entry<K, V>> entrySet() {
        return new ImmutableMapEntrySet<>(this);
    }

    @Override
    default Set<K> keySet() {
        return new ImmutableMapKeySet<>(this);
    }

    @Override
    default Collection<V> values() {
        return new ImmutableMapValueCollection<>(this);
    }

    @Override
    @Deprecated
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException("replaceAll is not supported");
    }

    @Override
    @Deprecated
    default V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("putIfAbsent is not supported");
    }

    @Override
    @Deprecated
    default boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    @Deprecated
    default boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("replace is not supported");
    }

    @Override
    @Deprecated
    default V replace(K key, V value) {
        throw new UnsupportedOperationException("replace is not supported");
    }

    @Override
    @Deprecated
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("computeIfAbsent is not supported");
    }

    @Override
    @Deprecated
    default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("computeIfPresent is not supported");
    }

    @Override
    @Deprecated
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("compute is not supported");
    }

    @Override
    @Deprecated
    default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("merge is not supported");
    }

    /**
     * Gets a Spliterator over the keys of this internal
     * @return a key spliterator
     */
    default Spliterator<K> keySpliterator() {
        return Spliterators.spliterator(keyIterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED);
    }

    /**
     * Gets a Spliterator over the values of this internal
     * @return a value spliterator
     */
    default Spliterator<V> valueSpliterator() {
        return Spliterators.spliterator(valueIterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.SIZED);
    }

}
