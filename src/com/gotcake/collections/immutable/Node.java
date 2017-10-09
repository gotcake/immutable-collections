package com.gotcake.collections.immutable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * An ImmutableTrieMap tree node
 * @author Aaron Cake
 */
interface Node<K, V> extends Validatable {

    V get(K key, int prefix);
    Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size);
    Node<K, V> setIfExists(K key, V value, int prefix, int depth);
    Node<K, V> setIfNotExists(K key, V value, int prefix, int depth);
    Node<K, V> delete(K key, int prefix, int depth);
    Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size);

    void forEachEntry(BiConsumer<? super K, ? super V> action);
    boolean containsValue(Object value);
    int computeSize();
    void computeIteration(int i, NodeEntryIterator<K, V>.Callback callback);

}
