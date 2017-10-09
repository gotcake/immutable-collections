package com.gotcake.collections.immutable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * A temporary node used when only a single entry is left and it should be collapsed into the parent node
 * @author Aaron Cake
 */
class SingleEntryNode<K, V> implements Node<K, V> {

    final K key;
    final V value;

    SingleEntryNode(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public V get(K key, int prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<K, V> setIfExists(K key, V value, int prefix, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<K, V> setIfNotExists(K key, V value, int prefix, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<K, V> delete(K key, int prefix, int depth) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachEntry(BiConsumer<? super K, ? super V> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void computeIteration(int i, NodeEntryIterator<K, V>.Callback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int assertValidAndComputeSize(int suffix, int depth) {
        throw new UnsupportedOperationException();
    }

}
