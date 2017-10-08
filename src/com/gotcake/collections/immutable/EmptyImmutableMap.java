package com.gotcake.collections.immutable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Aaron Cake
 */
final class EmptyImmutableMap<K, V> implements ImmutableMap<K, V> {

    private static EmptyImmutableMap INSTANCE = new EmptyImmutableMap();

    @SuppressWarnings("unchecked")
    static <K, V> EmptyImmutableMap<K, V> getInstance() {
        return INSTANCE;
    }

    private EmptyImmutableMap() { }

    @Override
    public boolean containsEntry(final K key, final V value) {
        return false;
    }

    @Override
    public Iterator<ImmutableMap.Entry<K, V>> entryIterator() {
        return Iterators.empty();
    }

    @Override
    public Iterator<K> keyIterator() {
        return Iterators.empty();
    }

    @Override
    public Iterator<V> valueIterator() {
        return Iterators.empty();
    }

    @Override
    public void forEachKey(final Consumer<? super K> action) {
        // do nothing
    }

    @Override
    public void forEachValue(final Consumer<? super V> action) {
        // do nothing
    }

    @Override
    public ImmutableMap<K, V> update(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        if (key == null) throw new NullPointerException("key cannot be null");
        final V value = mapperFn.apply(key, null);
        return value == null ? this : ImmutableMap.of(key, value);
    }

    @Override
    public ImmutableMap<K, V> updateIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        return this;
    }

    @Override
    public ImmutableMap<K, V> updateIfAbsent(final K key, final Function<? super K, ? extends V> computeFn) {
        final V value = computeFn.apply(key);
        return value == null ? this : ImmutableMap.of(key, value);
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(final Object key) {
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public V get(final Object key) {
        return null;
    }

    @Override
    public V getOrDefault(final Object key, final V defaultValue) {
        return defaultValue;
    }

    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        // do nothing
    }

    public Spliterator<K> keySpliterator() {
        return Spliterators.emptySpliterator();
    }

    public Spliterator<V> valueSpliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public ImmutableMap<K, V> set(final K key, final V value) {
        return ImmutableMap.of(key, value);
    }

    @Override
    public ImmutableMap<K, V> setIfAbsent(final K key, final V value) {
        return ImmutableMap.of(key, value);
    }

    @Override
    public ImmutableMap<K, V> setIfPresent(final K key, final V value) {
        return this;
    }

    @Override
    public ImmutableMap<K, V> setIfMatch(final K key, final V oldValue, final V newValue) {
        return this;
    }

    @Override
    public ImmutableMap<K, V> delete(final K key) {
        return this;
    }

    @Override
    public ImmutableMap<K, V> deleteIfMatch(final K key, final V value) {
        return this;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || (obj instanceof Map && ((Map)obj).isEmpty());
    }

}
