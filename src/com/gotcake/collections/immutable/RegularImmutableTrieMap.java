package com.gotcake.collections.immutable;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.gotcake.collections.immutable.Util.*;

/**
 * An immutable data structure based on the Hash Array Mapped Trie described at
 * http://lampwww.epfl.ch/papers/idealhashtrees.pdf
 * @author Aaron Cake
 */
final class RegularImmutableTrieMap<K, V> implements ImmutableMap<K, V>, Validatable {

    final Node<K, V> root;
    final int size;
    Integer computedHash = null;

    private RegularImmutableTrieMap(int size, Node<K, V> root) {
        this.size = size;
        this.root = root;
    }

    RegularImmutableTrieMap(K key, V value) {
        if (key == null || value == null) { throw new NullPointerException(); }
        size = 1;
        this.root = NodeFactory.createNodeWithSingleEntry(
                computeSmearHash(key) >>> 27,
                key,
                value
        );
    }

    RegularImmutableTrieMap(K key1, V value1, K key2, V value2) {
        if (key1 == null || value1 == null || key2 == null || value2 == null) { throw new NullPointerException(); }
        if (key1.equals(key2)) {
            size = 1;
            this.root = NodeFactory.createNodeWithSingleEntry(
                    computeSmearHash(key1) >>> 27,
                    key1,
                    value2
            );
        } else {
            size = 2;
            this.root = NodeFactory.createNodeWithTwoEntries(
                    0,
                    computeSmearHash(key2), key2, value2,
                    computeSmearHash(key1), key1, value1
            );
        }
    }

    RegularImmutableTrieMap(final Map<? extends K, ? extends  V> sourceMap) {
        if (sourceMap.isEmpty()) {
            throw new IllegalArgumentException("sourceMap must not be empty");
        }
        @SuppressWarnings("unchecked")
        final Iterator<Map.Entry<K, V>> it = (Iterator)sourceMap.entrySet().iterator();
        Map.Entry<K, V> entry = it.next();
        final int bitIndex = computeSmearHash(entry.getKey()) >>> 27;
        Node<K, V> root = NodeFactory.createNodeWithSingleEntry(bitIndex, entry.getKey(), entry.getValue());
        while (it.hasNext()) {
            entry = it.next();
            final int hash = computeSmearHash(entry.getKey());
            root = root.setIfNotExists(entry.getKey(), entry.getValue(), hash, 0);
        }
        this.root = root;
        this.size = sourceMap.size();
    }

    RegularImmutableTrieMap(final Set<? extends K> keys, final V valueForAll) {
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("key set must not be empty");
        }
        @SuppressWarnings("unchecked")
        final Iterator<K> it = (Iterator)keys.iterator();
        K key = it.next();
        final int bitIndex = computeSmearHash(key) >>> 27;
        Node<K, V> root = NodeFactory.createNodeWithSingleEntry(bitIndex, key, valueForAll);
        while (it.hasNext()) {
            key = it.next();
            final int hash = computeSmearHash(key);
            root = root.setIfNotExists(key, valueForAll, hash, 0);
        }
        this.root = root;
        this.size = keys.size();
    }

    /**
     * Checks if this map contains the given key
     * @param key the key to check for
     * @return true if this map contains the key, false otherwise
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(final Object key) {
        if (key == null) {
            return false;
        }
        final int prefix = computeSmearHash(key);
        return root.get((K)key, prefix) != null;
    }

    /**
     * Checks if this map contains the key-value pair
     * @param key the key to check for
     * @param value the value to check for
     * @return true if this map contains the entry pair, false otherwise
     */
    public boolean containsEntry(final K key, final V value) {
        if (key == null || value == null) {
            return false;
        }
        final int prefix = computeSmearHash(key);
        final V existingValue = root.get(key, prefix);
        return existingValue.equals(value);
    }

    /**
     * Gets the value with the given key, or null if no entry exists
     * @param key the key
     * @return the value, or null if no entry exists
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
        final int prefix = computeSmearHash(key);
        return root.get((K)key, prefix);
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Always returns false since RegularImmutableTrieMap cannot be empty
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns true if the map contains the given value.
     * WARNING: this executes in linear O(n) time.
     * @param value the value to search for
     * @return true if the map contains the value, false otherwise
     */
    @Override
    public boolean containsValue(final Object value) {
        return root.containsValue(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)  return true;
        if (!(other instanceof Map)) return false;
        final Map<?, ?> map = (Map<?, ?>)other;
        if (size != map.size()) return false;
        // if it's an RegularImmutableTrieMap do a structural comparison
        if (other instanceof RegularImmutableTrieMap) {
            final RegularImmutableTrieMap<?, ?> iMap = (RegularImmutableTrieMap<?, ?>) other;
            return root.equals(iMap.root);
        }
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            if (!containsEntry((K) entry.getKey(), (V) entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the value with the given key, or the default value if no entry exists.
     * @param key the key to look up
     * @param defaultValue the default value
     * @return the value, or defaultValue if no entry with the given key exists
     */
    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(final Object key, final V defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        final int prefix = computeSmearHash(key);
        final V existingValue = root.get((K)key, prefix);
        return existingValue == null ? defaultValue : existingValue;
    }

    /**
     * Calls the action for all entries of this map
     * @param action the action to call
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        root.forEachEntry(action);
    }


    /**
     * Gets an Iterator over the keys of this map
     * @return a key iterator
     */
    @Override
    public Iterator<K> keyIterator() {
        return new NodeEntryIterator.KeyIterator<>(root);
    }

    /**
     * Gets an Iterator over the values of this map
     * @return a value iterator
     */
    @Override
    public Iterator<V> valueIterator() {
        return new NodeEntryIterator.ValueIterator<>(root);
    }

    /**
     * Returns an iterator over all of the entries in this map.
     * WARNING: This will create a new Map.Entry object for each entry iterated over.
     * @return an iterator over entries
     */
    @Override
    public Iterator<ImmutableMap.Entry<K, V>> entryIterator() {
        return new NodeEntryIterator.EntryIterator<>(root);
    }

    /**
     * Calls action for every key of this map
     * @param action the Consumer to call
     */
    public void forEachKey(final Consumer<? super K> action) {
        root.forEachEntry((key, value) -> action.accept(key));
    }

    /**
     * Calls action for every value of this map
     * @param action the Consumer to call
     */
    public void forEachValue(final Consumer<? super V> action) {
        root.forEachEntry((key, value) -> action.accept(value));
    }

    @Override
    public ImmutableMap<K, V> update(K key, BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        if (key == null || mapperFn == null) throw new NullPointerException();
        final SizeChangeSink sink = new SizeChangeSink();
        final Node<K, V> newRoot = root.update(key, computeSmearHash(key), 0, mapperFn, sink);
        if (newRoot == null) {
            return EmptyImmutableMap.getInstance();
        }
        if (newRoot != root) {
            return new RegularImmutableTrieMap<>(size + sink.sizeChange, newRoot);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> set(K key, V value) {
        if (key == null || value == null) { throw new NullPointerException(); }
        final SizeChangeSink sink = new SizeChangeSink();
        final Node<K, V> newRoot = root.set(key, value, computeSmearHash(key), 0, sink);
        if (newRoot != root) {
            return new RegularImmutableTrieMap<>(size + sink.sizeChange, newRoot);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> setIfAbsent(K key, V value) {
        if (key == null || value == null) { throw new NullPointerException(); }
        final Node<K, V> newRoot = root.setIfNotExists(key, value, computeSmearHash(key), 0);
        if (newRoot != root) {
            return new RegularImmutableTrieMap<>(size + 1, newRoot);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> setIfPresent(K key, V value) {
        if (key == null || value == null) { throw new NullPointerException(); }
        final Node<K, V> newRoot = root.setIfExists(key, value, computeSmearHash(key), 0);
        if (newRoot != root) {
            return new RegularImmutableTrieMap<>(size, newRoot);
        }
        return this;
    }

    @Override
    public ImmutableMap<K, V> delete(K key) {
        if (key == null) {
            return this;
        }
        final Node<K, V> newRoot = root.delete(key, computeSmearHash(key), 0);
        if (newRoot == null) {
            return EmptyImmutableMap.getInstance();
        }
        if (newRoot != root) {
            return new RegularImmutableTrieMap<>(size - 1, newRoot);
        }
        return this;
    }

    @Override
    public int hashCode() {
        if (computedHash == null) {
            computedHash = root.hashCode();
        }
        return computedHash;
    }

    @Override
    public void assertValid() {
        assertValidType("root", root, false, PackedArrayDualNode.class);
        int computedSize = root.assertValidAndComputeSize(0, 0);
        assertEqual("Size must equal computed size", size, computedSize);
    }
}
