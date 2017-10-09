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
     * Computes a new value for the entry with the given key, if it exists.
     * If computeFn returns the existing value,
     * this instance is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or mapperFn is null
     */
    ImmutableMap<K, V> update(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn);

    /**
     * Computes new values for all the entries in this map.
     * If computeFn returns the existing value,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param mapperFn a function which maps the value
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or mapperFn is null
     */
    default ImmutableMap<K, V> updateAll(final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        ImmutableMap<K, V> map = this;
        final Iterator<K> it = this.keyIterator();
        while (it.hasNext()) {
            final K key = it.next();
            map = map.update(key, mapperFn);
        }
        return map;
    }

    /**
     * Computes the a new map with the given key and value returned by remapperFn only if there is already a value for the given key.
     * If this internal doesn't contain the given key, mapperFn is never called.
     * If this internal doesn't contain the given key or remapperFn returns the existing value,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new map instance, or the this instance if no modifications were necessary
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
     * Computes the a new map with the given key and value returned by computeFn only if there is no value for the given key.
     * If this internal already contains the given key, computeFn is never called.
     * If this internal already contains the given key or computeFn returns null,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param computeFn a function which computes the value
     * @return the new map instance, or the this instance if no modifications were necessary
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
     * Computes a new map with the given key-value pair.
     * If this internal already contains the key-value pair, this internal is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> set(final K key, final V value);


    /**
     * Computes a new map by adding all the key value paris in the given map.
     * If no modifications were necessary, this instance is returned, otherwise a new map is created.
     * @param sourceMap the map containing all the entries to add to this map
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException the map, or a key or value in the map were empty
     */
    default ImmutableMap<K, V> setAll(final Map<K, V> sourceMap) {
        ImmutableMap<K, V> map = this;
        for (final K key: sourceMap.keySet()) {
            map = map.set(key, sourceMap.get(key));
        }
        return map;
    }


    /**
     * Computes the a new map with the given key-value pair only if there is no value for the given key.
     * If this internal already contains the given key, this internal is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> setIfAbsent(final K key, final V value);


    /**
     * Computes the a new map with the given key-value pair only if there is already a value for the given key.
     * If this internal already contains the given key-value pair or a value for the given key is missing,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    ImmutableMap<K, V> setIfPresent(final K key, final V value);


    /**
     * Computes the a new map with the given key-value pair only if the current value equals oldValue.
     * If this internal already contains the given key-value pair or the given key is missing or has a value not equal to oldValue,
     * this internal is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param matchValue the value to match
     * @param newValue the value to set if the current value equals oldValue
     * @return the new map instance, or the this instance if no modifications were necessary
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
     * If a modification is required, a new map is returned, otherwise this object is returned.
     * @param key the key
     * @return the new map, or this object if no modification was required
     */
    ImmutableMap<K, V> delete(final K key);

    /**
     * Deletes all entiries specified by the given keys, if they exist.
     * If a modification is required, a new map is returned, otherwise this object is returned.
     * @param keys the keys to delete
     * @return the new map, or this object if no modification was required
     */
    default ImmutableMap<K, V> deleteAll(final Iterable<K> keys) {
        ImmutableMap<K, V> map = this;
        for (final K key: keys) {
            map = map.delete(key);
        }
        return map;
    }

    /**
     * Deletes an entry only if it matches the given key and value.
     * If a modification is required, a new map is returned, otherwise this object is returned.
     * @param key the key
     * @param matchValue the value
     * @return the new map, or this object if no modification was required
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
        throw new UnsupportedOperationException("remove is not supported, use delete instead");
    }

    @Override
    @Deprecated
    default void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("putAll is not supported, use setAll instead");
    }

    @Override
    @Deprecated
    default void clear() {
        throw new UnsupportedOperationException("clear is not supported, use ImmutableMap.of() instead");
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
        throw new UnsupportedOperationException("replaceAll is not supported, use updateAll instead");
    }

    @Override
    @Deprecated
    default V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("putIfAbsent is not supported");
    }

    @Override
    @Deprecated
    default boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("remove is not supported, use deleteIfMatch instead");
    }

    @Override
    @Deprecated
    default boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("replace is not supported, use setIfMatch instead");
    }

    @Override
    @Deprecated
    default V replace(K key, V value) {
        throw new UnsupportedOperationException("replace is not supported, use setIfExists instead");
    }

    @Override
    @Deprecated
    default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("computeIfAbsent is not supported, use updateIfAbsent instead");
    }

    @Override
    @Deprecated
    default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("computeIfPresent is not supported, use updateIfPresent instead");
    }

    @Override
    @Deprecated
    default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("compute is not supported, use update instead");
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

    default HashMap<K, V> asHashMap() {
        // size the map so that it won't need to expand
        final HashMap<K, V> map = new HashMap<>((int)Math.ceil(size() / 0.75), 0.75f);
        // faster than new HashMap(this) O(n) vs O(n * log32(n))
        // and also prevents creation of n Map.Entry instances
        forEach(map::put);
        return map;
    }

    default TreeMap<K, V> asTreeMap() {
        final TreeMap<K, V> map = new TreeMap<>();
        // faster than new TreeMap(this) O(n * log2(n)) vs O(n * log2(n) * log32(n))
        // and also prevents creation of n Map.Entry instances
        forEach(map::put);
        return map;
    }

}
