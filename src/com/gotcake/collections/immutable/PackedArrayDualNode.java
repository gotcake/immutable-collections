package com.gotcake.collections.immutable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.gotcake.collections.immutable.Util.*;

/**
 * A leaf node for holding more than two entries
 * @author Aaron Cake
 */
class PackedArrayDualNode<K, V> implements Node<K, V> {

    int mask;
    final Object[] packedArray;

    PackedArrayDualNode(final int bitIndex, final K key, final V value) {
        this.mask = 1 << bitIndex;
        this.packedArray = new Object[]{ key, value };
    }

    PackedArrayDualNode(final int bitIndex, final Node<K, V> child) {
        this.mask = 1 << bitIndex;
        this.packedArray = new Object[]{ null, child };
    }

    PackedArrayDualNode(final int bitIndex1, final K key1, final V value1,
                        final int bitIndex2, final K key2, final V value2) {
        this.mask = (1 << bitIndex1) | (1 << bitIndex2);
        if (bitIndex1 > bitIndex2) {
            this.packedArray = new Object[]{ key2, value2, key1, value1 };
        } else {
            this.packedArray = new Object[]{ key1, value1, key2, value2 };
        }
    }

    PackedArrayDualNode(final int mask, final Object[] packedArray) {
        this.mask = mask;
        this.packedArray = packedArray;
    }

    @Override
    public V get(K key, int prefix) {

        final int bit = 1 << (prefix >>> 27);

        if ((bit & mask) == 0) {
            // bit not in mask, branch does not exist, return null
            return null;
        }

        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;
        @SuppressWarnings("unchecked")
        final K keyOrNull = (K)packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            @SuppressWarnings("unchecked")
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            return child.get(key, prefix << 5);
        }

        if (keyOrNull.equals(key)) {
            // key matches, return value
            @SuppressWarnings("unchecked")
            final V value = (V)packedArray[offset + 1];
            return value;
        }

        // key does not match, return null
        return null;

    }

    @Override
    public Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, insert entry
            size.sizeChange++;
            return nodeByInsertingAtOffset(bit, offset, key, value);
        }

        @SuppressWarnings("unchecked")
        final K keyOrNull = (K)packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            @SuppressWarnings("unchecked")
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.set(key, value, prefix << 5, depth + 1, size);
            if (child != newChild) {
                // child changed, replace child
                return nodeByReplacingOffset(offset, null, newChild);

            }
            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches
            if (packedArray[offset + 1].equals(value)) {
                // value matches, do nothing
                return this;
            }
            // value doesn't match, replace it
            return nodeByReplacingOffset(offset, key, value);
        }

        // key does not match, create new branch
        final int ourPrefix = computeHashPrefix(keyOrNull, depth);
        @SuppressWarnings("unchecked")
        final V ourValue = (V)packedArray[offset + 1];
        final Node<K, V> newNode = NodeFactory.createNodeWithTwoEntries(
                depth + 1,
                prefix << 5, key, value,
                ourPrefix << 5, keyOrNull, ourValue
        );
        size.sizeChange++;
        return nodeByReplacingOffset(offset, null, newNode);

    }

    @Override
    public Node<K, V> setIfExists(K key, V value, int prefix, int depth) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, return this
            return this;
        }

        @SuppressWarnings("unchecked")
        final K keyOrNull = (K)packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            @SuppressWarnings("unchecked")
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.setIfExists(key, value, prefix << 5, depth + 1);
            if (child != newChild) {
                // child changed, replace child
                return nodeByReplacingOffset(offset, null, newChild);
            }
            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches
            if (packedArray[offset + 1].equals(value)) {
                // value matches, do nothing
                return this;
            }
            // value doesn't match, replace it
            return nodeByReplacingOffset(offset, key, value);
        }

        // key does not match, return this
        return this;

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> setIfNotExists(K key, V value, int prefix, int depth) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, insert entry
            return nodeByInsertingAtOffset(bit, offset, key, value);
        }

        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.setIfNotExists(key, value, prefix << 5, depth + 1);
            if (child != newChild) {
                // child changed, replace child
                return nodeByReplacingOffset(offset, null, newChild);
            }
            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches, do nothing
            return this;
        }

        // key does not match, create new branch
        final int ourPrefix = computeHashPrefix(keyOrNull, depth);
        final Node<K, V> newNode = NodeFactory.createNodeWithTwoEntries(
                depth + 1,
                prefix << 5, key, value,
                ourPrefix << 5, (K)keyOrNull, (V)packedArray[offset + 1]
        );
        return nodeByReplacingOffset(offset, null, newNode);

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> delete(K key, int prefix, int depth) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, return this
            return this;
        }

        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.delete(key, prefix << 5, depth + 1);

            if (child != newChild) {

                if (newChild == null) {
                    // child deleted, remove child
                    return nodeByRemovingOffset(offset, bit, depth);
                }

                if (newChild instanceof SingleEntryNode) {
                    // collapse entry into this node
                    final SingleEntryNode<K, V> entryToCollapse = (SingleEntryNode<K, V>)newChild;
                    return nodeByReplacingOffset(offset, entryToCollapse.key, entryToCollapse.value);
                }

                // child changed, replace child
                return nodeByReplacingOffset(offset, null, newChild);
            }

            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches, remove entry
            return nodeByRemovingOffset(offset, bit, depth);
        }

        // key does not match, return this
        return this;

    }

    @Override
    public Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, possibly insert entry
            final V newValue = updateFn.apply(key, null);
            if (newValue != null) {
                size.sizeChange++;
                return nodeByInsertingAtOffset(bit, offset, key, newValue);
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        final K keyOrNull = (K)packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            @SuppressWarnings("unchecked")
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.update(key, prefix << 5, depth + 1, updateFn, size);
            if (child != newChild) {

                if (newChild == null) {
                    // child removed
                    return nodeByRemovingOffset(offset, bit, depth);
                }

                if (newChild instanceof SingleEntryNode) {
                    // collapse entry into this node
                    final SingleEntryNode<K, V> entryToCollapse = (SingleEntryNode<K, V>)newChild;
                    return nodeByReplacingOffset(offset, entryToCollapse.key, entryToCollapse.value);
                }

                // child changed, replace child
                return nodeByReplacingOffset(offset, null, newChild);
            }
            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches

            @SuppressWarnings("unchecked")
            final V curValue = (V)packedArray[offset + 1];
            final V newValue = updateFn.apply(key, curValue);

            if (newValue == null) {
                size.sizeChange--;
                return nodeByRemovingOffset(offset, bit, depth);
            }

            if (newValue.equals(curValue)) {
                // value matches, do nothing
                return this;
            }
            // value doesn't match, replace it
            return nodeByReplacingOffset(offset, key, newValue);
        }

        // key does not match, create new branch
        final V newValue = updateFn.apply(key, null);
        if (newValue != null) {
            size.sizeChange++;
            final int ourPrefix = computeHashPrefix(keyOrNull, depth);
            @SuppressWarnings("unchecked")
            final V ourValue = (V)packedArray[offset + 1];
            final Node<K, V> newNode = NodeFactory.createNodeWithTwoEntries(
                    depth + 1,
                    prefix << 5, key, newValue,
                    ourPrefix << 5, keyOrNull, ourValue
            );
            return nodeByReplacingOffset(offset, null, newNode);
        }

        return this;

    }

    private PackedArrayDualNode<K, V> nodeByInsertingAtOffset(final int bit, final int offset, final Object o1, final Object o2) {
        final Object[] newArray = new Object[packedArray.length + 2];
        if (offset > 0) {
            System.arraycopy(packedArray, 0, newArray, 0, offset);
        }
        newArray[offset] = o1;
        newArray[offset + 1] = o2;
        if (offset < packedArray.length) {
            System.arraycopy(packedArray, offset, newArray, offset + 2, packedArray.length - offset);
        }
        return new PackedArrayDualNode<>(mask | bit, newArray);
    }

    private PackedArrayDualNode<K, V> nodeByReplacingOffset(final int offset, final Object o1, final Object o2) {
        final Object[] newArray = packedArray.clone();
        newArray[offset] = o1;
        newArray[offset + 1] = o2;
        return new PackedArrayDualNode<>(mask, newArray);
    }

    private Node<K, V> nodeByRemovingOffset(final int offset, final int bit, final int depth) {
        final int lenMinus2 = packedArray.length - 2;
        if (lenMinus2 <= 0) {
            return null;
        }
        // if there's only one element left, check to see if it's an entry.
        // if it's an entry, return a SingleEntryNode instead of a PackedArrayDualNode
        if (lenMinus2 == 2 && depth > 0) {
            if (offset == 0 && packedArray[2] != null) {
                @SuppressWarnings("unchecked")
                final K key = (K)packedArray[2];
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[3];
                return new SingleEntryNode<>(key, value);
            }
            if (offset == 2 && packedArray[0] != null) {
                @SuppressWarnings("unchecked")
                final K key = (K)packedArray[0];
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[1];
                return new SingleEntryNode<>(key, value);
            }
        }
        final Object[] newArray = new Object[lenMinus2];
        if (offset > 0) {
            System.arraycopy(packedArray, 0, newArray, 0, offset);
        }
        if (offset < lenMinus2) {
            System.arraycopy(packedArray, offset + 2, newArray, offset, lenMinus2 - offset);
        }
        return new PackedArrayDualNode<>(mask & ~bit, newArray);
    }

    @Override
    public void forEachEntry(final BiConsumer<? super K, ? super V> action) {
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            @SuppressWarnings("unchecked")
            final K keyOrNull = (K)packedArray[offset];
            if (keyOrNull == null) {
                @SuppressWarnings("unchecked")
                final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
                child.forEachEntry(action);
            } else {
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[offset + 1];
                action.accept(keyOrNull, value);
            }
        }
    }

    @Override
    public boolean containsValue(final Object value) {
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            if (keyOrNull == null) {
                @SuppressWarnings("unchecked")
                final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
                if (child.containsValue(value)) {
                    return true;
                }
            } else if (packedArray[offset + 1].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void computeIteration(int i, NodeEntryIterator<K, V>.Callback callback) {
        final int i2 = i * 2;
        if (i2 == packedArray.length) {
            callback.exitNode();
        } else {
            @SuppressWarnings("unchecked")
            final K keyOrNull = (K)packedArray[i2];
            if (keyOrNull == null) {
                @SuppressWarnings("unchecked")
                final Node<K, V> node = (Node<K, V>)packedArray[i2 + 1];
                callback.enterNode(node);
            } else {
                @SuppressWarnings("unchecked")
                final V value = (V)packedArray[i2 + 1];
                callback.offer(keyOrNull, value);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof PackedArrayDualNode)) return false;
        PackedArrayDualNode<?, ?> that = (PackedArrayDualNode<?, ?>) o;
        if (mask != that.mask) return false;
        for (int i = 0; i < packedArray.length; i++) {
            if (!Objects.equals(packedArray[i], that.packedArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = result * 31 + mask;
        for (final Object el : packedArray) {
            result = result * 31 + (el == null ? 0 : el.hashCode());
        }
        return result;
    }

    @Override
    public int assertValidAndComputeSize(int suffix, int depth) {

        // suffix math notes
        // hash = 00001_00010_00011_00100_00101_00110_00
        // bit0 = 1, bit0 = 2 ..., bit7 = 0
        // individual parts:
        // (hash >>> (27 - 5 * 0)) << 27 => 00001
        // (hash >>> (27 - 5 * 1)) << 27 >>> (5 * 1) => 00000_00010
        // whole:
        // (hash >>> (27 - 5 * 1)) << (27 - 5 * 1) => 00001_00010
        // structural:
        // 00001 >>> 5 * 0
        // 00010 >>> 5 * 1

        int total = 0;
        assertNotEqual("mask must not be 0", 0, mask);
        assertNotNull("packedArray must not be null", packedArray);
        assertEqual("packedArray length must match set mask bits", packedArray.length, Integer.bitCount(mask) * 2);
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            final Object valueOrChild = packedArray[offset + 1];
            if (keyOrNull == null) {
                assertValidType("child", valueOrChild, false, PackedArrayDualNode.class, PackedArrayCollisionNode.class);
                @SuppressWarnings("unchecked")
                final Node<K, V> child = (Node<K, V>)valueOrChild;
                int bitIndex = nthSetBitPosition(mask, offset / 2);
                total += child.assertValidAndComputeSize(computeChildHashSuffix(suffix, bitIndex, depth), depth + 1);
            } else {
                total++;
                assertThat("if key is not null, value must not be a node", !(valueOrChild instanceof Node));
                assertEqualBinary("computed hash suffix must match structural location", suffix, computeHashSuffix(keyOrNull, depth));
            }
        }
        return total;
    }
}
