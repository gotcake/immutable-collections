package com.gotcake.collections.immutable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * A node for holding multiple entries with the same hash value
 * @author Aaron Cake
 */
public class PackedArrayCollisionNode<K, V> implements Node<K, V> {

    private final Object[] packedArray;

    public PackedArrayCollisionNode(final K key1, final V value1,
                                     final K key2, final V value2) {
        packedArray = new Object[]{ key1, value1, key2, value2 };
    }

    private PackedArrayCollisionNode(final Object[] array) {
        packedArray = array;
    }

    @Override
    public V get(K key, int prefix) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[i + 1];
                return value;
            }
        }
        return null;
    }

    private Node<K, V> nodeByReplacingValue(int index, V newValue) {
        Object[] newArray = packedArray.clone();
        newArray[index] = newValue;
        return new PackedArrayCollisionNode<>(newArray);
    }

    private Node<K, V> nodeByAddingEntry(K key, V value) {
        int len = packedArray.length;
        Object[] newArray = new Object[len + 2];
        System.arraycopy(packedArray, 0, newArray, 0, len);
        newArray[len] = key;
        newArray[len + 1] = value;
        return new PackedArrayCollisionNode<>(newArray);
    }

    private Node<K, V> nodeByRemovingEntry(int offset) {
        int lenMinus2 = packedArray.length - 2;
        if (lenMinus2 == 2) {
            if (offset == 0) {
                @SuppressWarnings("unchecked")
                final K key = (K)packedArray[2];
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[3];
                return new SingleEntryNode<>(key, value);
            }
            if (offset == 2) {
                @SuppressWarnings("unchecked")
                final K key = (K)packedArray[0];
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[1];
                return new SingleEntryNode<>(key, value);
            }
            throw new IndexOutOfBoundsException();
        }
        final Object[] newArray = new Object[lenMinus2];
        if (offset > 0) {
            System.arraycopy(packedArray, 0, newArray, 0, offset);
        }
        if (offset < lenMinus2) {
            System.arraycopy(packedArray, offset + 2, newArray, offset, lenMinus2 - offset);
        }
        return new PackedArrayCollisionNode<>(newArray);
    }

    @Override
    public Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                if (packedArray[i + 1].equals(value)) {
                    return this;
                }
                return nodeByReplacingValue(i + 1, value);
            }
        }
        size.sizeChange++;
        return nodeByAddingEntry(key, value);
    }

    @Override
    public Node<K, V> setIfExists(K key, V value, int prefix, int depth) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                if (packedArray[i + 1].equals(value)) {
                    return this;
                }
                return nodeByReplacingValue(i + 1, value);
            }
        }
        return this;
    }

    @Override
    public Node<K, V> setIfNotExists(K key, V value, int prefix, int depth) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                return this;
            }
        }
        return nodeByAddingEntry(key, value);
    }

    @Override
    public Node<K, V> delete(K key, int prefix, int depth) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                return nodeByRemovingEntry(i);
            }
        }
        return this;
    }

    @Override
    public Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size) {
        for (int i = 0; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(key)) {
                @SuppressWarnings("unchecked")
                final V curVal = (V)packedArray[i + 1];
                final V newVal = updateFn.apply(key, curVal);
                if (newVal == null) {
                    size.sizeChange--;
                    return nodeByRemovingEntry(i);
                } else if (!newVal.equals(curVal)) {
                    return nodeByReplacingValue(i + 1, newVal);
                }
                return this;
            }
        }
        final V newVal = updateFn.apply(key, null);
        if (newVal == null) {
            return this;
        }
        size.sizeChange++;
        return nodeByAddingEntry(key, newVal);
    }

    @Override
    public void forEachEntry(BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < packedArray.length; i += 2) {
            @SuppressWarnings("unchecked")
            final K key = (K)packedArray[i];
            @SuppressWarnings("unchecked")
            final V value = (V)packedArray[i + 1];
            action.accept(key, value);
        }
    }

    @Override
    public boolean containsValue(Object value) {
        for (int i = 1; i < packedArray.length; i += 2) {
            if (packedArray[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int computeSize() {
        return packedArray.length / 2;
    }

    @Override
    public void computeIteration(int i, NodeEntryIterator<K, V>.Callback callback) {
        final int i2 = i * 2;
        if (i2 == packedArray.length) {
            callback.exitNode();
        } else {
            @SuppressWarnings("unchecked")
            final K key = (K)packedArray[i2];
            @SuppressWarnings("unchecked")
            final V value = (V)packedArray[i2 + 1];
            callback.offer(key, value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackedArrayCollisionNode)) return false;
        PackedArrayCollisionNode<?, ?> that = (PackedArrayCollisionNode<?, ?>) o;
        if (packedArray.length != that.packedArray.length) return false;
        for (int i = 0; i < packedArray.length; i++) {
            if (!packedArray[i].equals(that.packedArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (final Object el : packedArray) {
            result = result * 31 + el.hashCode();
        }
        return result;
    }

    @Override
    public void assertValid(Assertions a) {
        // TODO: check that hashes are equal
    }
}
