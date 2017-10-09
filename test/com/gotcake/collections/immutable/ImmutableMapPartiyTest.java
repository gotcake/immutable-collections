package com.gotcake.collections.immutable;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * A very useful test that checks for parity with HashMap with 1 million entries.
 * This bad boy tends to catch most errors other unit tests miss.
 * @author Aaron Cake (gotcake)
 */
public class ImmutableMapPartiyTest {

    @Test
    public void testMassiveParityWithHashMap() throws Exception {
        final Random random = new Random(0x1024572);
        ImmutableMap<String, Integer> map = ImmutableMap.of();
        final HashMap<String, Integer> hashMap = new HashMap<>();
        final StringBuilder buffer = new StringBuilder(102);
        for (int i = 0; i < 2000000; i++) {
            final String key = TestHelper.generateRandomString(buffer, random, 102);
            final int value = (int)(random.nextFloat() * 1000000);
            final ImmutableMap<String, Integer> temp = map.set(key, value);
            if (!hashMap.containsKey(key)) {
                if (temp == map) {
                    //Validatable.tryAssertValid(map);
                    assertNotSame("ImmutableTrieMap must return new instance when adding a new key", temp, map);
                }
            }
            hashMap.put(key, value);
            map = temp;
        }
        //Validatable.tryAssertValid(map);
        //DebugPrintable.tryPrintDebug(map);
        assertEquals(hashMap.size(), map.size());
        final HashSet<String> keysToRemove = new HashSet<>();
        for (final Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            final String key = entry.getKey();
            assertTrue("ImmutableTrieMap must contain all keys in HashMap", map.containsKey(key));
        }
        for (final Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            final String key = entry.getKey();
            final int value = entry.getValue();
            assertTrue("ImmutableTrieMap must contain all keys in HashMap", map.containsKey(key));
            assertEquals("ImmutableTrieMap must values must match that of HashMap", (Integer)value, map.get(key));
            if (random.nextFloat() > 0.6f) {
                keysToRemove.add(key);
            }
        }
        for (final String key: keysToRemove) {
            assertNotNull("[Meta] Sanity check that we can remove key from hash internal", hashMap.remove(key));
            final ImmutableMap<String, Integer> temp = map.delete(key);
            if (map == temp) {
                //Validatable.tryAssertValid(map);
                assertNotSame("ImmutableTrieMap must return new instance when removing an existing key", temp, map);
            }
            map = temp;
        }
        final Set<String> keySet = new HashSet<>(hashMap.keySet());
        final Iterator<ImmutableMap.Entry<String, Integer>> it = map.entryIterator();
        while (it.hasNext()) {
            final ImmutableMap.Entry<String, Integer> entry = it.next();
            assertTrue("Iterator must iterate over existing keys only once", keySet.remove(entry.key));
            assertEquals("Iterator Entries must  have correct value", hashMap.get(entry.key), entry.value);
        }
        //Validatable.tryAssertValid(map);
        //DebugPrintable.tryPrintDebug(map);
        assertEquals(hashMap.size(), map.size());
        for (final Map.Entry<String, Integer> entry: hashMap.entrySet()) {
            final String key = entry.getKey();
            final int value = entry.getValue();
            assertEquals("ImmutableTrieMap values must match that of HashMap after removing keys", (Integer)value, map.get(key));
        }
        keySet.clear();
        keySet.addAll(hashMap.keySet());
        map.forEach((key, value) -> {
            assertTrue("forEach must iterate over existing keys only once", keySet.remove(key));
            assertEquals("forEach must call Consumer with correct value", hashMap.get(key), value);
        });
        assertTrue("forEach must iterate over all keys", keySet.isEmpty());
        keySet.clear();
        keySet.addAll(hashMap.keySet());
        map.forEachKey((key) ->
            assertTrue("forEachKey must iterate over existing keys only once", keySet.remove(key))
        );
        assertTrue("forEachKey must iterate over all keys", keySet.isEmpty());

    }

    @Test
    public void testParity1() throws Exception {
        runParityTest("test/resources/parity_test_data_1.txt", ImmutableMap.of(), new HashMap<>());
    }

    @Test
    public void testParity2() throws Exception {
        runParityTest("test/resources/parity_test_data_2.txt", ImmutableMap.of(), new HashMap<>());
    }

    @Test
    public void testParity3() throws Exception {
        runParityTest("test/resources/parity_test_data_3.txt", ImmutableMap.of(), new HashMap<>());
    }

    private static void runParityTest(final String file, ImmutableMap<String, String> map, final Map<String, String> reference) throws IOException {
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line, key, val;
            ImmutableMap<String, String> tmp;
            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                final String[] parts = trimmed.split("\\s+");
                if (parts.length == 0) {
                    continue;
                }
                switch (parts[0]) {
                    case "c":
                        Validatable.tryAssertValid(map);
                        assertTrue(map.equals(reference));
                        break;
                    case "r":
                        key = parts[1];
                        tmp = map.delete(key);
                        if (reference.remove(key) == null) {
                            assertSame(map, tmp);
                        } else {
                            assertNotSame(map, tmp);
                            map = tmp;
                        }
                        break;
                    case "p":
                        key = parts[1];
                        val = parts[2];
                        tmp = map.set(key, val);
                        if (val.equals(reference.put(key, val))) {
                            assertSame(map, tmp);
                        } else {
                            assertNotSame(map, tmp);
                            map = tmp;
                        }
                        break;
                }
            }
        }
    }

}
