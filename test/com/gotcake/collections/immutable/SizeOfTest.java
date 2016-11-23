package com.gotcake.collections.immutable;

import com.javamex.classmexer.MemoryUtil;
import org.junit.Test;

import java.util.Random;

/**
 * @author Aaron Cake
 */
public class SizeOfTest {

    @Test
    public void testSize() {
        // WARNING: this code assumes that the only public fields in the object tree are the keys and values
        ImmutableMap<Integer, Integer> map = ImmutableMap.of();
        final Random random = new Random(0x45181145);
        System.out.println("sizeof(0): " + MemoryUtil.deepMemoryUsageOf(map));
        map = map.set(random.nextInt(), random.nextInt());
        System.out.println("sizeof(1): " + MemoryUtil.deepMemoryUsageOf(map));
        for (int i = 1; i < 150000; i++) {
            map = map.set(random.nextInt(), random.nextInt());
            double log10 = Math.log10(map.size());
            if (Math.floor(log10) == log10) {
                System.out.println("sizeof(" + map.size() + "): " + MemoryUtil.deepMemoryUsageOf(map));
            }
        }
    }

}
