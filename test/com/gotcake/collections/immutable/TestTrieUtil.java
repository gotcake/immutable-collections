package com.gotcake.collections.immutable;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.gotcake.collections.immutable.Util.assertEqual;
import static com.gotcake.collections.immutable.Util.assertEqualBinary;
import static com.gotcake.collections.immutable.Util.computeChildHashSuffix;

/**
 * Created by aaron on 11/19/16.
 */
public class TestTrieUtil {

    private static final List<Object> OBJECTS = new ArrayList<>(1000);
    static {
        for (int i = 0; i < 1000; i++) {
            OBJECTS.add(new Object());
        }
    }


    @Test
    public void testComputeHashPrefix() {
        for (final Object obj: OBJECTS) {
            assertEqualBinary("must equal hash", Util.computeSmearHash(obj), Util.computeHashPrefix(obj, 0));
            assertEqualBinary("lower 5 bits must be 0", 0, Util.computeHashPrefix(obj, 1) & 0b11111);
            assertEqualBinary("lower 10 bits must be 0", 0, Util.computeHashPrefix(obj, 2) & 0b1111111111);
            assertEqualBinary("lower 15 bits must be 0", 0, Util.computeHashPrefix(obj, 3) & 0b111111111111111);
            assertEqualBinary("lower 20 bits must be 0", 0, Util.computeHashPrefix(obj, 4) & 0b11111111111111111111);
            assertEqualBinary("lower 25 bits must be 0", 0, Util.computeHashPrefix(obj, 5) & 0b1111111111111111111111111);
            assertEqualBinary("lower 30 bits must be 0", 0, Util.computeHashPrefix(obj, 6) & 0b111111111111111111111111111111);
        }
    }

    @Test
    public void testComputeHashSuffix() {
        for (final Object obj: OBJECTS) {
            final int hash = Util.computeSmearHash(obj);
            assertEqualBinary("must equal 0", 0, Util.computeHashSuffix(obj, 0));
            assertEqualBinary("must equal ", hash & ~0b111111111111111111111111111, Util.computeHashSuffix(obj, 1));
            assertEqualBinary("must equal ", hash & ~0b1111111111111111111111, Util.computeHashSuffix(obj, 2));
            assertEqualBinary("must equal ", hash & ~0b11111111111111111, Util.computeHashSuffix(obj, 3));
            assertEqualBinary("must equal ", hash & ~0b111111111111, Util.computeHashSuffix(obj, 4));
            assertEqualBinary("must equal ", hash & ~0b1111111, Util.computeHashSuffix(obj, 5));
            assertEqualBinary("must equal ", hash & ~0b11, Util.computeHashSuffix(obj, 6));
        }
    }

    @Test
    @SuppressWarnings("NumericOverflow")
    public void testComputeChildHashSuffix() {
        assertEqualBinary("must equal", 0b00100000000000000000000000000000, computeChildHashSuffix(0, 0b00100, 0));
        assertEqualBinary("must equal", 0b10011011000000000000000000000000, computeChildHashSuffix(0b10011 << 27, 0b01100, 1));
        assertEqualBinary("must equal", 0b10011000000000000000000000000000, computeChildHashSuffix(0b10011 << 27, 0b00000, 2));
        assertEqualBinary("must equal", 0b10011000001000100000000000000000, computeChildHashSuffix(0b10011 << 27, 0b10001, 2));
        assertEqualBinary("must equal", 0b10011000000000010001000000000000, computeChildHashSuffix(0b10011 << 27, 0b10001, 3));
        assertEqualBinary("must equal", 0b10011000000000000000100010000000, computeChildHashSuffix(0b10011 << 27, 0b10001, 4));
        assertEqualBinary("must equal", 0b10011000000000000000000001000100, computeChildHashSuffix(0b10011 << 27, 0b10001, 5));
        assertEqualBinary("must equal", 0b10011000000000000000000000000011, computeChildHashSuffix(0b10011 << 27, 0b11000, 6));
    }

}
