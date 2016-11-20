package com.gotcake.collections.immutable;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
            TrieUtil.assertEqual("must equal hash", TrieUtil.computeSmearHash(obj), TrieUtil.computeHashPrefix(obj, 0));
            TrieUtil.assertEqual("lower 5 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 1) & 0b11111);
            TrieUtil.assertEqual("lower 10 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 2) & 0b1111111111);
            TrieUtil.assertEqual("lower 15 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 3) & 0b111111111111111);
            TrieUtil.assertEqual("lower 20 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 4) & 0b11111111111111111111);
            TrieUtil.assertEqual("lower 25 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 5) & 0b1111111111111111111111111);
            TrieUtil.assertEqual("lower 30 bits must be 0", 0, TrieUtil.computeHashPrefix(obj, 6) & 0b111111111111111111111111111111);
        }
    }

    @Test
    public void testComputeHashSuffix() {
        for (final Object obj: OBJECTS) {
            final int hash = TrieUtil.computeSmearHash(obj);
            TrieUtil.assertEqual("must equal 0", 0, TrieUtil.computeHashSuffix(obj, 0));
            TrieUtil.assertEqual("must equal ", hash & ~0b111111111111111111111111111, TrieUtil.computeHashSuffix(obj, 1));
            TrieUtil.assertEqual("must equal ", hash & ~0b1111111111111111111111, TrieUtil.computeHashSuffix(obj, 2));
            TrieUtil.assertEqual("must equal ", hash & ~0b11111111111111111, TrieUtil.computeHashSuffix(obj, 3));
            TrieUtil.assertEqual("must equal ", hash & ~0b111111111111, TrieUtil.computeHashSuffix(obj, 4));
            TrieUtil.assertEqual("must equal ", hash & ~0b1111111, TrieUtil.computeHashSuffix(obj, 5));
            TrieUtil.assertEqual("must equal ", hash & ~0b11, TrieUtil.computeHashSuffix(obj, 6));
        }
    }

    @Test
    public void testComputeChildHashSuffix() {
        TrieUtil.assertEqual("must equal", 0b00100000000000000000000000000000, TrieUtil.computeChildHashSuffix(0, 0b00100, 0));
        TrieUtil.assertEqual("must equal", 0b10011011000000000000000000000000, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b01100, 1));
        TrieUtil.assertEqual("must equal", 0b10011000000000000000000000000000, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b00000, 2));
        TrieUtil.assertEqual("must equal", 0b10011000001000100000000000000000, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b10001, 2));
        TrieUtil.assertEqual("must equal", 0b10011000000000010001000000000000, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b10001, 3));
        TrieUtil.assertEqual("must equal", 0b10011000000000000000100010000000, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b10001, 4));
        TrieUtil.assertEqual("must equal", 0b10011000000000000000000001000100, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b10001, 5));
        TrieUtil.assertEqual("must equal", 0b10011000000000000000000000000011, TrieUtil.computeChildHashSuffix(0b10011 << 27, 0b11000, 6));
    }

}
