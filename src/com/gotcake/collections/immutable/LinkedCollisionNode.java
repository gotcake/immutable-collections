package com.gotcake.collections.immutable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * A node to be used when hash collisions occur for keys in the tree
 * @author Aaron Cake
 */
public class LinkedCollisionNode<K, V> implements Node<K, V> {

    private final byte bitIndex;
    private final K key;
    private final V value;
    private final Node<K, V> next;

    public LinkedCollisionNode(final int bitIndex, final K key, final V value, final Node<K, V> next) {
        this.bitIndex = (byte)bitIndex;
        this.key = key;
        this.value = value;
        this.next = next;
    }

    @Override
    public V get(K key, int prefix) {
        if (this.key.equals(key)) {
            return value;
        }
        return next.get(key, prefix);
    }

    @Override
    public Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size) {
        if (this.key.equals(key)) {
            if (this.value.equals(value)) {
                return this;
            }
            return new LinkedCollisionNode<>(bitIndex, key, value, next);
        }
        final Node<K, V> newNext = next.set(key, value, prefix, depth, size);
        if (newNext != next) {
            return new LinkedCollisionNode<>(bitIndex, this.key, this.value, newNext);
        }
        return this;
    }

    @Override
    public Node<K, V> setIfExists(K key, V value, int prefix, int depth) {
        if (this.key.equals(key)) {
            if (this.value.equals(value)) {
                return this;
            }
            return new LinkedCollisionNode<>(bitIndex, key, value, next);
        }
        final Node<K, V> newNext = next.setIfExists(key, value, prefix, depth);
        if (newNext != next) {
            return new LinkedCollisionNode<>(bitIndex, this.key, this.value, newNext);
        }
        return this;
    }

    @Override
    public Node<K, V> setIfNotExists(K key, V value, int prefix, int depth) {
        if (this.key.equals(key)) {
            return this;
        }
        final Node<K, V> newNext = next.setIfNotExists(key, value, prefix, depth);
        if (newNext != next) {
            return new LinkedCollisionNode<>(bitIndex, this.key, this.value, newNext);
        }
        return this;
    }

    @Override
    public Node<K, V> delete(K key, int prefix) {
        if (this.key.equals(key)) {
            return next;
        }
        final Node<K, V> newNext = next.delete(key, prefix);
        if (newNext == null) {
            return NodeFactory.createNodeWithSingleEntry(bitIndex, this.key, this.value);
        }
        if (newNext != next) {
            return new LinkedCollisionNode<>(bitIndex, this.key, this.value, newNext);
        }
        return this;
    }

    @Override
    public Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size) {
        if (this.key.equals(key)) {
            final V newValue = updateFn.apply(key, value);
            if (newValue == null) {
                size.sizeChange--;
                return next;
            }
            if (value.equals(newValue)) {
                return this;
            }
            return new LinkedCollisionNode<>(bitIndex, key, newValue, next);
        }
        final Node<K, V> newNext = next.update(key, prefix, depth, updateFn, size);
        if (newNext == null) {
            return NodeFactory.createNodeWithSingleEntry(bitIndex, this.key, this.value);
        }
        if (newNext != next) {
            return new LinkedCollisionNode<>(bitIndex, this.key, this.value, newNext);
        }
        return this;
    }

    @Override
    public void forEachEntry(BiConsumer<? super K, ? super V> action) {
        action.accept(key, value);
        next.forEachEntry(action);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.equals(value) || next.containsValue(value);
    }

    @Override
    public int computeSize() {
        return 1 + next.computeSize();
    }

    @Override
    public void computeIteration(final int i, final NodeEntryIterator<K, V>.Callback callback) {
        if (i == 0) {
            callback.offer(key, value);
        } else {
            callback.replaceNode(next);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LinkedCollisionNode)) return false;
        LinkedCollisionNode<?, ?> that = (LinkedCollisionNode<?, ?>) o;
        return bitIndex == that.bitIndex &&
                key.equals(that.key) &&
                value.equals(that.value) &&
                next.equals(that.next);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + key.hashCode();
        result = result * 31 + value.hashCode();
        result = result * 31 + next.hashCode();
        return result;
    }

    @Override
    public void assertValid(final Assertions a) {
        a.assertNotNull(".key", key);
        a.assertNotNull(".value", value);
        a.assertValid(".next", next);
    }
}
