package com.gotcake.collections.immutable;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A very useful test that checks for parity with HashMap with 1 million entries.
 * This bad boy tends to catch most errors other unit tests miss.
 * @author Aaron Cake (gotcake)
 */
public class ImmutableTrieMapPartiyTest {

    @Test
    public void testMassiveParityWithHashMap() throws Exception {
        final Random random = new Random(0x1024572);
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        final HashMap<String, Integer> hashMap = new HashMap<>();
        final StringBuilder buffer = new StringBuilder(102);
        for (int i = 0; i < 2000000; i++) {
            final String key = TestHelper.generateRandomString(buffer, random);
            final int value = (int)(random.nextFloat() * 1000000);
            final ImmutableTrieMap<String, Integer> temp = map.set(key, value);
            if (!hashMap.containsKey(key)) {
                if (temp == map) {
                    temp.assertValid();
                    assertNotSame("ImmutableTrieMap must return new instance when adding a new key", temp, map);
                }
            }
            hashMap.put(key, value);
            map = temp;
        }
        map.assertValid();
        System.out.println(map.computeDebugInfo());
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
            assertNotNull("[Meta] Sanity check that we can remove key from hash map", hashMap.remove(key));
            final ImmutableTrieMap<String, Integer> temp = map.delete(key);
            if (map == temp) {
                temp.assertValid();
                assertNotSame("ImmutableTrieMap must return new instance when removing an existing key", temp, map);
            }
            map = temp;
        }
        final Set<String> keySet = new HashSet<>(hashMap.keySet());
        final Iterator<ImmutableTrieMap.Entry<String, Integer>> it = map.entryIterator();
        while (it.hasNext()) {
            final ImmutableTrieMap.Entry<String, Integer> entry = it.next();
            assertTrue("Iterator must iterate over existing keys only once", keySet.remove(entry.key));
            assertEquals("Iterator Entries must  have correct value", hashMap.get(entry.key), entry.value);
        }
        map.assertValid();
        System.out.println(map.computeDebugInfo());
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

}
