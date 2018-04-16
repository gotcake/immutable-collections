package com.gotcake.collections.immutable;

import org.junit.Test;

import java.util.*;

import static com.gotcake.collections.immutable.TestHelper.computeValueCounts;
import static com.gotcake.collections.immutable.TestHelper.decrementValueCount;
import static com.gotcake.collections.immutable.TestHelper.makeMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * A
 * @author Aaron Cake
 */
public class TestImmutableMapMethods {

    @Test
    public void testCreateEmpty() {
        final ImmutableMap<String, Integer> map = ImmutableMap.of();
        assertSame(map, ImmutableMap.of()); // should always return the same instance
        assertBasics(map);
    }

    @Test
    public void testCreateSingleEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2);
        assertBasics(map, "foo", 2);
    }

    @Test
    public void testCreateTwoEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2, "bar", 3);
        assertBasics(map, "foo", 2, "bar", 3);
    }

    @Test
    public void testCreateThreeEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2, "bar", 3, "baz", 4);
        assertBasics(map, "foo", 2, "bar", 3, "baz", 4);
    }

    @Test
    public void testDuplicateValues() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 1, "bar", 1, "baz", 1);
        assertBasics(map, "foo", 1, "bar", 1, "baz", 1);
    }

    @Test
    public void testHashCollisions() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("xor", 1, "GKm", 2, "yQS", 3)
                .set("Fjm", 4)
                .set("ABC", 5);
        assertBasics(
                map,
                "xor", 1,
                "GKm", 2,
                "yQS", 3,
                "Fjm", 4,
                "ABC", 5
        );
    }

    // TODO: other methods not covered by other tests

    /**
     * Check all getting or state-checking and equality methods for correctness
     */
    private static <K, V> void assertBasics(final ImmutableMap<K, V> map, Object... data) {
        final Map<K, V> expected = makeMap(data);

        assertEquals("size must match (.size)", expected.size(), map.size());
        for (final Map.Entry<K, V> entry: expected.entrySet()) {
            assertEquals("value for each key must match (.get)", entry.getValue(), map.get(entry.getKey()));
            assertTrue("must contain each key (.containsKey)", map.containsKey(entry.getKey()));
            assertTrue("must contain each value (.containsValue)", map.containsValue(entry.getValue()));
            assertTrue("must contain each entry (.containsEntry)", map.containsEntry(entry.getKey(), entry.getValue()));
        }
        assertTrue("must be equal (expected.equals(map))", expected.equals(map));
        assertTrue("must be equal (map.equals(expected))", map.equals(expected));

        assertEquals("must have same hash equal",
                expected.hashCode(),
                map.hashCode());


        // validate key-based iteration methods
        {
            final Iterator<K> keyIterator = map.keyIterator();
            final Set<K> keysRemaining = new HashSet<>(expected.keySet());
            while (keyIterator.hasNext()) {
                assertTrue(keysRemaining.remove(keyIterator.next()));
            }
            assertTrue(keysRemaining.isEmpty());
        }
        {
            final Set<K> keysRemaining = new HashSet<>(expected.keySet());
            map.forEachKey(key -> assertTrue(keysRemaining.remove(key)));
            assertTrue(keysRemaining.isEmpty());
        }

        // validate entry-based iteration methods
        {
            final Iterator<Map.Entry<K, V>> entryIterator = map.entryIterator();
            final Set<K> keysRemaining = new HashSet<>(expected.keySet());
            while (entryIterator.hasNext()) {
                final Map.Entry<K, V> entry = entryIterator.next();
                assertTrue(keysRemaining.remove(entry.getKey()));
                assertEquals("iterated entries must contain the correct value", expected.get(entry.getKey()), entry.getValue());
            }
            assertTrue(keysRemaining.isEmpty());
        }
        {
            final Set<K> keysRemaining = new HashSet<>(expected.keySet());
            map.forEach((key, value) -> {
                assertTrue(keysRemaining.remove(key));
                assertEquals("iterated entries must contain the correct value", expected.get(key), value);
            });
            assertTrue(keysRemaining.isEmpty());
        }

        // validate value-based iteration methods
        {
            final Iterator<V> valueIterator = map.valueIterator();
            final Map<V, Integer> counts = computeValueCounts(expected);
            while (valueIterator.hasNext()) {
                decrementValueCount(counts, valueIterator.next());
            }
            assertTrue(counts.isEmpty());
        }
        {
            final Map<V, Integer> counts = computeValueCounts(expected);
            map.forEachValue(value -> decrementValueCount(counts, value));
            assertTrue(counts.isEmpty());
        }

        // validate derivative collections

        assertTrue("keySet must be equal (expected.keySet().equals(map.keySet()))", expected.keySet().equals(map.keySet()));
        assertTrue("keySet must be equal (map.keySet().equals(expected.keySet()))", map.keySet().equals(expected.keySet()));

        assertEquals("keySet hashes must be equal",
                expected.keySet().hashCode(),
                map.keySet().hashCode());

        assertTrue("entrySet must be equal (expected.entrySet().equals(map.entrySet()))", expected.entrySet().equals(map.entrySet()));
        assertTrue("entrySet must be equal (map.entrySet().equals(expected.entrySet()))", map.entrySet().equals(expected.entrySet()));

        assertEquals("entrySet hashes must be equal",
                expected.entrySet().hashCode(),
                map.entrySet().hashCode());

        final Collection<V> expectedValues = expected.values();
        final Collection<V> actualValues = map.values();
        assertEquals("actual and expected value collections must be equal", computeValueCounts(expectedValues), computeValueCounts(actualValues));

    }


}
