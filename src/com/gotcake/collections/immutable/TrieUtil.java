package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by aaron on 7/2/16.
 */
public final class TrieUtil {

    private static final int C1 = 0xcc9e2d51;
    private static final int C2 = 0x1b873593;
    private static final int ALL_BITS = ~0;
    private static final int ALL_BITS_EXCEPT_TWO = ~0b11;
    private static final char[] INDENT = new char[1024];
    private static final char[] ZEROS = new char[32];

    static {
        Arrays.fill(ZEROS, '0');
        Arrays.fill(INDENT, ' ');
        for (int i = 0; i < INDENT.length; i += 4) {
            INDENT[i] = '|';
        }
    }

    /**
     * Computes a smeared hash for the object. This allows more even distribution, especially for integral numbers.
     * @param obj the object to hash
     * @return the smeared hash
     */
    public static int computeSmearHash(final Object obj) {
        return C2 * Integer.rotateLeft(obj.hashCode() * C1, 15);
    }

    /**
     * Computes the hash prefix of an object after a certain depth of traversal
     * @param obj the object to contains the hash prefix of
     * @param depth the current tree depth
     * @return the hash prefix for the object and current tree depth
     */
    public static int computeHashPrefix(final Object obj, final int depth) {
        if (depth < 0 || depth > 6) {
            throw new IllegalArgumentException("Invalid depth " + depth + ". Must be in range [0, 6]");
        }
        return computeSmearHash(obj) << (5 * depth);
    }

    /**
     * Computes the hash suffix of an object after a certain depth of traversal
     * @param obj the object to contains the hash prefix of
     * @param depth the current tree depth
     * @return the hash prefix for the object and current tree depth
     */
    public static int computeHashSuffix(final Object obj, final int depth) {
        if (depth < 0 || depth > 7) {
            throw new IllegalArgumentException("Invalid depth " + depth + ". Must be in range [0, 7]");
        }
        if (depth == 0) { // handle 0 separately since shifting by 32 does nothing
            return 0;
        }
        if (depth > 6) {
            return computeSmearHash(obj);
        }
        int shift = 32 - (5 * depth);
        return (computeSmearHash(obj) >>> shift) << shift;
    }

    /**
     * Gets the position of the nth set bit in mask, where 0 is the least-significant bit.
     * This is not an optimized implementation and is not suitable for any performance-critical operations.
     * @param mask the mask
     * @param n n
     * @return the position of the nth set bit
     */
    public static int nthSetBitPosition(int mask, int n) {
        if (n < 0 || n >= Integer.bitCount(mask)) {
            throw new IndexOutOfBoundsException("Invalid bit position " + n + " for integer " + Integer.toHexString(mask));
        }
        int pos = -1;
        while(n >= 0) {
            if ((mask & 0x01) == 1) n--;
            mask >>= 1;
            pos++;
        }
        return pos;
    }

    /**
     * Computes the hash suffix of a child given the current hash suffix and the child's bitIndex
     * @param suffix the current node's hash suffix
     * @param bitIndex the bit index of the child
     * @return the child's hash suffix
     */
    public static int computeChildHashSuffix(final int suffix, final int bitIndex, int curDepth) {
        if (bitIndex < 0 || bitIndex > 31) {
            throw new IndexOutOfBoundsException("Invalid bit index " + bitIndex);
        }
        if (curDepth < 0 || curDepth > 6) {
            throw new IndexOutOfBoundsException("Invalid curDepth " + curDepth);
        }
        if (curDepth == 6) {
            return suffix | bitIndex >> 3;
        }
        return suffix | (bitIndex << (27 - (curDepth * 5)));
    }

    /**
     * Computes the logical index for an item given the mask and bitIndex
     * @param mask the mask
     * @param bitIndex the bitIndex
     * @return the logical index
     */
    public static int computeLogicalIndex(final int mask, final int bitIndex) {
        return Integer.bitCount(mask & ((1 << bitIndex) - 1));
    }

    public static void assertThat(final String msg, final boolean test) {
        if (!test) {
            throw new IllegalStateException("Assertion Failed: " + msg);
        }
    }

    public static void assertEqual(final String msg, final int expected, int actual) {
        if (expected != actual) {
            throw new IllegalStateException("Assertion Failed: " + msg +
                    "; expected=" + expected + " (" + toBinaryString(expected) +
                    "), actual=" + actual + " (" + toBinaryString(actual) + ")");
        }
    }

    public static void assertValidType(final String baseMsg, final Object value, final boolean nullable, final Class<?>... validClasses) {
        if (value == null) {
            if (!nullable) {
                throw new IllegalStateException(baseMsg + " cannot be null");
            }
            return;
        }
        for (final Class<?> validClass: validClasses) {
            if (validClass.equals(value.getClass())) {
                return;
            }
        }
        throw new IllegalStateException(baseMsg + " cannot be an instance of " + value.getClass().getSimpleName());
    }

    public static void printIndent(final Writer writer, int i) throws IOException {
        if (i > 256) {
            writer.write(INDENT, 0, INDENT.length);
            printIndent(writer, i - 256);
        } else {
            writer.write(INDENT, 0, i * 4);
        }
    }

    public static String toBinaryString(int i) {
        String str = Integer.toBinaryString(i);
        if (str.length() < 32) {
            str = new String(ZEROS, 0, 32 - str.length()) + str;
        }
        return str + 'b';
    }

    public static void printIntBitsHighlight(Writer writer, int i, int start, int length, int trimLen) throws IOException {
        String str = Integer.toBinaryString(i);
        if (str.length() < trimLen) {
            str = new String(ZEROS, 0, trimLen - str.length()) + str;
        } else if (str.length() > trimLen) {
            str = str.substring(0, trimLen);
        }
        if (start > 0) {
            writer.write(str, 0, start);
        } else if (start < 0) {
            writer.write(str);
            return;
        }
        if (start + length <= trimLen) {
            writer.write('(');
            writer.write(str, start, length);
            writer.write(')');
        }

        if (start + length < trimLen) {
            writer.write(str, start + length, trimLen - start - length);
        }

        writer.write('b');
    }

    public static <K, V> Function<K, V> functionThatReturns(final V value) {
        return (K key) -> value;
    }

    public static <K, V> BiFunction<K, V, V> functionThatReturnsIfMatchOrParam(final V matchValue, final V returnVal) {
        return (K key, V value) -> Objects.equals(value, matchValue) ? returnVal : value;
    }

    private TrieUtil() { throw new UnsupportedOperationException(); }

}
