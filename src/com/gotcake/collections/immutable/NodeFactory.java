package com.gotcake.collections.immutable;

/**
 * A single place to handle creating new types of nodes - to make it easier to test out specialized types of nodes
 * @author Aaron Cake
 */
class NodeFactory {

    /**
     * Creates a new node containing the given keys and values where it is not known how many bits of the
     * prefixes collide.
     */
    static <K, V> Node<K, V> createNodeWithTwoEntries(final int prefix1, final K key1, final V value1,
                                                      final int prefix2, final K key2, final V value2) {
        int bitIndex1 = prefix1 >>> 27;
        if (prefix1 == prefix2) {
            return new LinkedCollisionNode<>(
                    bitIndex1,
                    key1,
                    value1,
                    new PackedArrayDualNode<>(bitIndex1, key2, value2)
            );
        }

        int bitIndex2 = prefix2 >>> 27;
        if (bitIndex1 == bitIndex2) {
            return new PackedArrayDualNode<>(
                    bitIndex1,
                    createNodeWithTwoEntries(
                        prefix1 << 5, key1, value1,
                        prefix2 << 5, key2, value2
                    )
            );
        }

        return new PackedArrayDualNode<>(
                bitIndex1, key1, value1,
                bitIndex2, key2, value2
        );

    }

    static <K, V> Node<K, V> createNodeByRemovingOffsetFromPackedArray(final int mask,
                                                                       final Object[] packedArray,
                                                                       final int bit,
                                                                       final int offset) {


        final int lenMinus2 = packedArray.length - 2;
        if (lenMinus2 <= 0) {
            return null;
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

    static <K, V> Node<K, V> createNodeWithSingleEntry(final int bitIndex, final K key, final V value) {
        return new PackedArrayDualNode<>(bitIndex, key, value);
    }

    static <K, V> Node<K, V> createNodeWithTwoEntriesNonColliding(final int bitIndex1, final K key1, final V value1,
                                                                  final int bitIndex2, final K key2, final V value2) {
        return new PackedArrayDualNode<>(
                bitIndex1, key1, value1,
                bitIndex2, key2, value2
        );
    }

}
