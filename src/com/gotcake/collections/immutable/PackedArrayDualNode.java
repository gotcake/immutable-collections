package com.gotcake.collections.immutable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static com.gotcake.collections.immutable.Util.computeHashPrefix;

/**
 * A leaf node for holding more than two entries
 * @author Aaron Cake
 */
class PackedArrayDualNode<K, V> implements Node<K, V> {

    private int mask;
    private final Object[] packedArray;

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
    @SuppressWarnings("unchecked")
    public V get(K key, int prefix) {

        final int bit = 1 << (prefix >>> 27);

        if ((bit & mask) == 0) {
            // bit not in mask, branch does not exist, return null
            return null;
        }

        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;
        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            return ((Node<K, V>)packedArray[offset + 1]).get(key, prefix << 5);
        }

        if (keyOrNull.equals(key)) {
            // key matches, return value
            return (V)packedArray[offset + 1];
        }

        // key does not match, return null
        return null;

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> set(K key, V value, int prefix, int depth, SizeChangeSink size) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, insert entry
            size.sizeChange++;
            return insertEntry(bit, offset, key, value);
        }

        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.set(key, value, prefix << 5, depth + 1, size);
            if (child != newChild) {
                // child changed, replace child
                return replaceEntry(offset, null, newChild);
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
            return replaceEntry(offset, key, value);
        }

        // key does not match, create new branch
        final int ourPrefix = computeHashPrefix(keyOrNull, depth);
        final Node<K, V> newNode = NodeFactory.createNodeWithTwoEntries(
                prefix << 5, key, value,
                ourPrefix << 5, (K)keyOrNull, (V)packedArray[offset + 1]
        );
        size.sizeChange++;
        return replaceEntry(offset, null, newNode);

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> setIfExists(K key, V value, int prefix, int depth) {

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
            final Node<K, V> newChild = child.setIfExists(key, value, prefix << 5, depth + 1);
            if (child != newChild) {
                // child changed, replace child
                return replaceEntry(offset, null, newChild);
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
            return replaceEntry(offset, key, value);
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
            return insertEntry(bit, offset, key, value);
        }

        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.setIfNotExists(key, value, prefix << 5, depth + 1);
            if (child != newChild) {
                // child changed, replace child
                return replaceEntry(offset, null, newChild);
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
                prefix << 5, key, value,
                ourPrefix << 5, (K)keyOrNull, (V)packedArray[offset + 1]
        );
        return replaceEntry(offset, null, newNode);

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> delete(K key, int prefix) {

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
            final Node<K, V> newChild = child.delete(key, prefix << 5);

            if (newChild == null) {
                // child deleted, remove child
                return NodeFactory.createNodeByRemovingOffsetFromPackedArray(mask, packedArray, bit, offset);
            }

            if (child != newChild) {
                // child changed, replace child
                return replaceEntry(offset, null, newChild);
            }

            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches, remove entry
            return NodeFactory.createNodeByRemovingOffsetFromPackedArray(mask, packedArray, bit, offset);
        }

        // key does not match, return this
        return this;

    }

    @Override
    @SuppressWarnings("unchecked")
    public Node<K, V> update(K key, int prefix, int depth, BiFunction<? super K, ? super V, ? extends V> updateFn, SizeChangeSink size) {

        final int bit = 1 << (prefix >>> 27);
        final int offset = Integer.bitCount(mask & (bit - 1)) * 2;

        if ((bit & mask) == 0) {
            // bit not set, branch does not exist, possibly insert entry
            final V newValue = updateFn.apply(key, null);
            if (newValue != null) {
                size.sizeChange++;
                return insertEntry(bit, offset, key, newValue);
            }
            return this;
        }

        final Object keyOrNull = packedArray[offset];

        if (keyOrNull == null) {
            // we have a child, descend further
            final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
            final Node<K, V> newChild = child.update(key, prefix << 5, depth + 1, updateFn, size);
            if (child != newChild) {
                // child changed, replace child
                return replaceEntry(offset, null, newChild);
            }
            // child did not change, return this
            return this;
        }

        if (keyOrNull.equals(key)) {
            // key matches

            final V curValue = (V)packedArray[offset + 1];
            final V newValue = updateFn.apply(key, curValue);

            if (newValue == null) {
                size.sizeChange--;
                return NodeFactory.createNodeByRemovingOffsetFromPackedArray(mask, packedArray, bit, offset);
            }

            if (newValue.equals(curValue)) {
                // value matches, do nothing
                return this;
            }
            // value doesn't match, replace it
            return replaceEntry(offset, key, newValue);
        }

        // key does not match, create new branch
        final V newValue = updateFn.apply(key, null);
        if (newValue != null) {
            size.sizeChange++;
            final int ourPrefix = computeHashPrefix(keyOrNull, depth);
            final Node<K, V> newNode = NodeFactory.createNodeWithTwoEntries(
                    prefix << 5, key, newValue,
                    ourPrefix << 5, (K)keyOrNull, (V)packedArray[offset + 1]
            );
            return replaceEntry(offset, null, newNode);
        }

        return this;

    }

    private PackedArrayDualNode<K, V> insertEntry(final int bit, final int offset, final Object o1, final Object o2) {
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

    private PackedArrayDualNode<K, V> replaceEntry(final int offset, final Object o1, final Object o2) {
        final Object[] newArray = packedArray.clone();
        newArray[offset] = o1;
        newArray[offset + 1] = o2;
        return new PackedArrayDualNode<>(mask, newArray);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEachEntry(final BiConsumer<? super K, ? super V> action) {
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            if (keyOrNull == null) {
                final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
                child.forEachEntry(action);
            } else {
                action.accept((K)keyOrNull, (V)packedArray[offset + 1]);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(final Object value) {
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            if (keyOrNull == null) {
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
    @SuppressWarnings("unchecked")
    public int computeSize() {
        int total = 0;
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            if (keyOrNull == null) {
                final Node<K, V> child = (Node<K, V>)packedArray[offset + 1];
                total += child.computeSize();
            } else {
                total += 1;
            }
        }
        return total;
    }

    @Override
    public void computeIteration(int i, NodeEntryIterator<K, V>.Callback callback) {

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
    public void assertValid(final Assertions a) {
        a.assertNotEqual(".mask", 0, mask);
        a.assertNotNull(".packedArray", packedArray);
        a.assertEqual(".(packedArray.length == bitCount(mask) * 2)", packedArray.length, Integer.bitCount(mask) * 2);
        for (int offset = 0; offset < packedArray.length; offset += 2) {
            final Object keyOrNull = packedArray[offset];
            final Object valueOrChild = packedArray[offset + 1];
            if (keyOrNull == null) {
                final String childContextStr = "[" + (offset + 1) + "]";
                a.assertValidType(childContextStr, valueOrChild, Node.class);
                if (valueOrChild instanceof Validatable) {
                    a.assertValid(childContextStr, (Validatable)valueOrChild);
                }
            } else {
                a.assertFalse("[" + offset + "] is not Node", valueOrChild instanceof Node);
            }
        }
    }
}
