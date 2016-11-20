package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Aaron Cake (acake)
 */
abstract class BaseImmutableMap<K, V, SelfType extends BaseImmutableMap<K, V, SelfType>> implements Map<K, V> {

    public static class Entry<K, V> implements Map.Entry<K, V> {

        public final K key;
        public final V value;

        public Entry(K key, V value) {
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
            return (key == otherKey || (key != null && key.equals(otherKey))) &&
                    (value == otherValue || (value != null && value.equals(otherValue)));
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }
    }

    // we cache the entry set because if this map does not have fast entry iteration,
    // the entry set will cache entries for us and save object allocation
    protected ImmutableMapEntrySet<K, V> cachedEntrySet = null;
    protected Integer cachedHashCode = null;

    public abstract boolean containsEntry(final K key, final V value);
    protected abstract Iterator<Entry<K, V>> entryIterator();
    public abstract Iterator<K> keyIterator();
    public abstract Iterator<V> valueIterator();
    public abstract void forEachKey(Consumer<? super K> action);
    public abstract void forEachValue(Consumer<? super V> action);
    protected abstract boolean hasFastEntryIteration();
    protected abstract int computeHashCode();
    public abstract SelfType update(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn);
    public abstract SelfType updateIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn);
    public abstract SelfType updateIfAbsent(final K key, final Function<? super K, ? extends V> computeFn);

    /**
     * Computes the a new map with the given key-value pair.
     * If this map already contains the key-value pair, this map is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    public SelfType set(final K key, final V value) {
        Objects.requireNonNull(value, "value cannot be null");
        return update(key, (K k, V v) -> value);
    }


    /**
     * Computes the a new map with the given key-value pair only if there is no value for the given key.
     * If this map already contains the given key, this map is returned,
     * no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    public SelfType setIfAbsent(final K key, final V value) {
        Objects.requireNonNull(value, "value cannot be null");
        return updateIfAbsent(key, (K k) -> value);
    }


    /**
     * Computes the a new map with the given key-value pair only if there is already a value for the given key.
     * If this map already contains the given key-value pair or a value for the given key is missing,
     * this map is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param value the value to set
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or value are null
     */
    public SelfType setIfPresent(final K key, final V value) {
        Objects.requireNonNull(value, "value cannot be null");
        return updateIfPresent(key, (K k, V v) -> value);
    }


    /**
     * Computes the a new map with the given key-value pair only if the current value equals oldValue.
     * If this map already contains the given key-value pair or the given key is missing or has a value not equal to oldValue,
     * this map is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param oldValue oldValue the value to match
     * @param newValue the value to set if the current value equals oldValue
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or newValue are null
     */
    @SuppressWarnings("unchecked")
    public SelfType setIfMatch(final K key, final V oldValue, final V newValue) {
        Objects.requireNonNull(newValue, "newValue cannot be null");
        if (oldValue == null || oldValue.equals(newValue)) {
            return (SelfType)this;
        }
        return updateIfPresent(key, (K k, V existing) -> existing.equals(oldValue) ? newValue : oldValue);
    }

    /**
     * Deletes an entry with the given key, if it exists.
     * If a modification is required, a new map is returned, otherwise this object is returned.
     * @param key the key
     * @return the new map, or this object if no modification was required
     */
    @SuppressWarnings("unchecked")
    public SelfType delete(final K key) {
        if (key == null) {
            return (SelfType)this;
        }
        return updateIfPresent(key, (K k, V v) -> null);
    }

    /**
     * Deletes an entry only if it matches the given key and value.
     * If a modification is required, a new map is returned, otherwise this object is returned.
     * @param key the key
     * @param value the value
     * @return the new map, or this object if no modification was required
     */
    @SuppressWarnings("unchecked")
    public SelfType deleteIfMatch(final K key, final V value) {
        if (key == null || value == null) {
            return (SelfType)this;
        }
        return updateIfPresent(key, (K k, V existing) -> existing.equals(value) ? null : existing);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (cachedEntrySet == null) {
            cachedEntrySet = new ImmutableMapEntrySet<>(this);
        }
        return cachedEntrySet;
    }

    @Override
    @Deprecated
    public V put(K key, V value) {
        throw new UnsupportedOperationException("update is not supported");
    }

    @Override
    @Deprecated
    public V remove(Object key) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    @Deprecated
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("putAll is not supported");
    }

    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException("clear is not supported");
    }

    @Override
    public Set<K> keySet() {
        return new ImmutableMapKeySet<>(this);
    }

    @Override
    public Collection<V> values() {
        return new ImmutableMapValueCollection<>(this);
    }

    @Override
    @Deprecated
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException("replaceAll is not supported");
    }

    @Override
    @Deprecated
    public V putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException("putIfAbsent is not supported");
    }

    @Override
    @Deprecated
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException("remove is not supported");
    }

    @Override
    @Deprecated
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException("replace is not supported");
    }

    @Override
    @Deprecated
    public V replace(K key, V value) {
        throw new UnsupportedOperationException("replace is not supported");
    }

    @Override
    @Deprecated
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException("computeIfAbsent is not supported");
    }

    @Override
    @Deprecated
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("computeIfPresent is not supported");
    }

    @Override
    @Deprecated
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("compute is not supported");
    }

    @Override
    @Deprecated
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException("merge is not supported");
    }

    /**
     * Gets a Spliterator over the keys of this map
     * @return a key spliterator
     */
    public Spliterator<K> keySpliterator() {
        return Spliterators.spliterator(keyIterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.CONCURRENT | Spliterator.DISTINCT | Spliterator.SIZED);
    }

    /**
     * Gets a Spliterator over the values of this map
     * @return a value spliterator
     */
    public Spliterator<V> valueSpliterator() {
        return Spliterators.spliterator(valueIterator(), size(),
                Spliterator.IMMUTABLE | Spliterator.CONCURRENT | Spliterator.SIZED);
    }

    @Override
    public int hashCode() {
        if (cachedHashCode == null) {
            cachedHashCode = computeHashCode();
        }
        return cachedHashCode;
    }

}
