package com.gotcake.collections.immutable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * A set of micro-benchmarks for ImmutableTrieMap vs other types of maps
 * @author Aaron Cake (gotcake)
 */
public class ImmutableTrieMapPerformanceTest {

    private static final String[] KEYS = new String[1000000];
    private static final String[] KEYS_SMALL = new String[100];

    @BeforeClass
    public static void setupSuite() {
        final StringBuilder buffer = new StringBuilder(102);
        final Random random = new Random(231435363);
        for (int i = 0; i < KEYS.length; i++) {
            TestHelper.generateRandomString(buffer, random);
            KEYS[i] = buffer.toString();
        }
        for (int i = 0; i < KEYS_SMALL.length; i++) {
            TestHelper.generateRandomString(buffer, random);
            KEYS_SMALL[i] = buffer.toString();
        }
    }

    @Before
    public void waitForGC() throws InterruptedException {
        Runtime.getRuntime().gc();
        Thread.sleep(1000);
    }

    @Test
    public void testTrieMap() {
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        long time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map = map.set(KEYS[i], i);
        }
        System.out.println("ImmutableTrieMap PUT: " + (System.nanoTime() - time) / 1000000f);
        time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map.get(KEYS[i]);
        }
        System.out.println("ImmutableTrieMap GET: " + (System.nanoTime() - time) / 1000000f);
    }

    @Test
    public void testHashMap() {
        HashMap<String, Integer> map = new HashMap<>();
        long time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map.put(KEYS[i], i);
        }
        System.out.println("HashMap PUT: " + (System.nanoTime() - time) / 1000000f);
        time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map.get(KEYS[i]);
        }
        System.out.println("HashMap GET: " + (System.nanoTime() - time) / 1000000f);
    }

    @Test
    public void testTreeMap() {
        TreeMap<String, Integer> map = new TreeMap<>();
        long time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map.put(KEYS[i], i);
        }
        System.out.println("TreeMap PUT: " + (System.nanoTime() - time) / 1000000f);
        time = System.nanoTime();
        for (int i = 0; i < KEYS.length; i++) {
            map.get(KEYS[i]);
        }
        System.out.println("TreeMap GET: " + (System.nanoTime() - time) / 1000000f);
    }

    @Test
    public void testTrieMapSmall() {
        long elapsed = 0;
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        for (int j = 0; j < 10000; j++) {
            map = ImmutableTrieMap.of();
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map = map.set(KEYS_SMALL[i], i);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("ImmutableTrieMap (small x 10000) PUT: " + elapsed / 1000000f);
        elapsed = 0;
        for (int j = 0; j < 10000; j++) {
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map.get(KEYS_SMALL[i]);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("ImmutableTrieMap (small x 10000) GET: " + elapsed / 1000000f);
    }

    @Test
    public void testTreeMapSmall() {
        long elapsed = 0;
        TreeMap<String, Integer> map = new TreeMap<>();
        for (int j = 0; j < 10000; j++) {
            map = new TreeMap<>();
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map.put(KEYS_SMALL[i], i);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("TreeMap (small x 10000) PUT: " + elapsed / 1000000f);
        elapsed = 0;
        for (int j = 0; j < 10000; j++) {
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map.get(KEYS_SMALL[i]);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("TreeMap (small x 10000) GET: " + elapsed / 1000000f);
    }

    @Test
    public void testHashMapSmall() {
        long elapsed = 0;
        HashMap<String, Integer> map = new HashMap<>();
        for (int j = 0; j < 10000; j++) {
            map = new HashMap<>();
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map.put(KEYS_SMALL[i], i);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("HashMap (small x 10000) PUT: " + elapsed / 1000000f);
        elapsed = 0;
        for (int j = 0; j < 10000; j++) {
            long time = System.nanoTime();
            for (int i = 0; i < KEYS_SMALL.length; i++) {
                map.get(KEYS_SMALL[i]);
            }
            elapsed += (System.nanoTime() - time);
        }
        System.out.println("HashMap (small x 10000) GET: " + elapsed / 1000000f);
    }

}
