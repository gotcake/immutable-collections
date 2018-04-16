package com.gotcake.collections.immutable;

import org.junit.Test;

import java.util.Map;

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
        assertMapEquals(map);
    }

    @Test
    public void testCreateSingleEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2);
        assertMapEquals(map, "foo", 2);
    }

    @Test
    public void testCreateTwoEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2, "bar", 3);
        assertMapEquals(map, "foo", 2, "bar", 3);
    }

    @Test
    public void testCreateThreeEntry() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2, "bar", 3, "baz", 4);
        assertMapEquals(map, "foo", 2, "bar", 3, "baz", 4);
    }

    // TODO: other methods not covered by other tests

    /**
     * Check all getting or state-checking and equality methods for correctness
     */
    private static <K, V> void assertMapEquals(final ImmutableMap<K, V> map, Object... data) {
        Map<K, V> expected = makeMap(data);
        assertEquals("size must match (.size)", expected.size(), map.size());
        for (Map.Entry<K, V> entry: expected.entrySet()) {
            assertEquals("value for each key must match (.get)", entry.getValue(), map.get(entry.getKey()));
            assertTrue("must contain each key (.containsKey)", map.containsKey(entry.getKey()));
            assertTrue("must contain each value (.containsValue)", map.containsValue(entry.getValue()));
            assertTrue("must contain each entry (.containsEntry)", map.containsEntry(entry.getKey(), entry.getValue()));
        }
        assertTrue("must be equal (expected.equals(map))", expected.equals(map));
        assertTrue("must be equal (map.equals(expected))", map.equals(expected));
    }

}
