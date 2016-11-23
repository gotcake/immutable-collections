package com.gotcake.collections.immutable;

import com.gotcake.collections.immutable.ImmutableMap.Entry;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gotcake.collections.immutable.Util.*;

/**
 * @author Aaron Cake
 */
class SingletonImmutableMap<K, V> extends Entry<K, V> implements ImmutableMap<K, V>, Validatable {

    SingletonImmutableMap(K key, V value) {
        super(key, value);
    }

    @Override
    public boolean containsEntry(K key, V value) {
        return this.key.equals(key) && this.value.equals(value);
    }

    @Override
    public Iterator<Entry<K, V>> entryIterator() {
        return Iterators.nonnullSingleton(this);
    }

    @Override
    public Iterator<K> keyIterator() {
        return Iterators.nonnullSingleton(key);
    }

    @Override
    public Iterator<V> valueIterator() {
        return Iterators.nonnullSingleton(value);
    }

    @Override
    public void forEachKey(Consumer<? super K> action) {
        action.accept(key);
    }

    @Override
    public void forEachValue(Consumer<? super V> action) {
        action.accept(value);
    }

    @Override
    public ImmutableMap<K, V> update(K key, BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        if (key.equals(this.key)) {
            final V newValue = mapperFn.apply(key, value);
            if (newValue == null) {
                return ImmutableMap.of();
            }
            if (!value.equals(newValue)) {
                return new SingletonImmutableMap<>(key, newValue);
            }
            return this;
        }
        final V value = mapperFn.apply(key, null);
        if (value == null) {
            return this;
        }
        return new RegularImmutableTrieMap<>(this.key, this.value, key, value);
    }

    @Override
    public ImmutableMap<K, V> updateIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        if (!key.equals(this.key)) {
            return this;
        }
        final V newValue = mapperFn.apply(key, value);
        if (newValue == null) {
            return ImmutableMap.of();
        }
        if (!value.equals(newValue)) {
            return new SingletonImmutableMap<>(key, newValue);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> updateIfAbsent(K key, Function<? super K, ? extends V> computeFn) {
        if (key == null) throw new NullPointerException("key cannot be null");
        if (this.key.equals(key)) {
            return this;
        }
        final V value = computeFn.apply(key);
        if (value == null) {
            return this;
        }
        return new RegularImmutableTrieMap<>(this.key, this.value, key, value);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.key.equals(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.equals(value);
    }

    @Override
    public V get(Object key) {
        return this.key.equals(key) ? value : null;
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return this.key.equals(key) ? value : defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        action.accept(key, value);
    }

    @Override
    public ImmutableMap<K, V> set(K key, V value) {
        if (value == null) throw new NullPointerException();
        if (key.equals(this.key)) {
            return new SingletonImmutableMap<>(key, value);
        }
        return new RegularImmutableTrieMap<>(this.key, this.value, key, value);
    }

    @Override
    public ImmutableMap<K, V> setIfAbsent(K key, V value) {
        if (value == null) throw new NullPointerException();
        if (key.equals(this.key)) {
            return this;
        }
        return new RegularImmutableTrieMap<>(this.key, this.value, key, value);
    }

    @Override
    public ImmutableMap<K, V> setIfPresent(K key, V value) {
        if (value == null) throw new NullPointerException();
        if (key.equals(this.key)) {
            return new SingletonImmutableMap<>(key, value);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> setIfMatch(K key, V oldValue, V newValue) {
        if (key.equals(this.key) && oldValue.equals(this.key)) {
            return new SingletonImmutableMap<>(key, newValue);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> delete(K key) {
        if (this.key.equals(key)) {
            return EmptyImmutableMap.getInstance();
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> deleteIfMatch(K key, V value) {
        if (this.key.equals(key) && this.value.equals(value)) {
            return EmptyImmutableMap.getInstance();
        }
        return this;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return Collections.singleton(this);
    }

    @Override
    public Set<K> keySet() {
        return Collections.singleton(key);
    }

    @Override
    public Collection<V> values() {
        return Collections.singleton(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Map)) return false;
        final Map other = (Map)obj;
        if (other.size() != 1) return false;
        return other.containsKey(key) & other.containsValue(value);
    }

    @Override
    public void assertValid(Writer debugWriter) throws IOException {
        assertNotNull("key was null", key);
        assertNotNull("value was null", value);
    }
}
