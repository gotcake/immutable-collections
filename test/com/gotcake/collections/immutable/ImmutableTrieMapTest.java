package com.gotcake.collections.immutable;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImmutableTrieMapTest {

    @Test
    public void testEmpty() {
        final ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        assertEquals(0, map.size());
        assertSame(ImmutableTrieMap.of(), ImmutableTrieMap.of());
        map.assertValid();
    }

    @Test
    public void testSingleBasic() {
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of("foo", 2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertNotNull(map.get("foo"));
        assertEquals(2, (long)map.get("foo"));
        map.assertValid();
    }

    @Test
    public void testSingleReplacement() throws Exception {
        final ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of("foo", 2).set("foo", 3);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertNotNull(map.get("foo"));
        assertEquals(3, (long)map.get("foo"));
        map.assertValid();
    }

    @Test
    public void testManyBasic() throws Exception {
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        map = map.set("foo", 1);
        map.assertValid();
        map = map.set("bar", 2);
        map.assertValid();
        map = map.set("null", 5);
        map.assertValid();
        map = map.set("baz", 3);
        map.assertValid();
        map = map.set("redrover", 0);
        map.assertValid();
        assertEquals(5, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(map.containsKey("null"));
        assertTrue(map.containsKey("baz"));
        assertTrue(map.containsKey("redrover"));
        assertNotNull(map.get("foo"));
        assertNotNull(map.get("bar"));
        assertNotNull(map.get("null"));
        assertNotNull(map.get("baz"));
        assertNotNull(map.get("redrover"));
        assertEquals(1, (int)map.get("foo"));
        assertEquals(2, (int)map.get("bar"));
        assertEquals(5, (int)map.get("null"));
        assertEquals(3, (int)map.get("baz"));
        assertEquals(0, (int)map.get("redrover"));
    }

    @Test
    public void testManyReplacement() throws Exception {
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        map = map.set("foo", 1);
        map = map.set("bar", 2);
        map = map.set("null", 5);
        map = map.set("jolly", 9);
        map = map.set("omg", -10);
        map = map.set("zoidberg", 10);
        map = map.set("baz", 3);
        map = map.set("redrover", 0);
        map.assertValid();
        // now replace stuff
        map = map.set("null", -1);
        map = map.set("baz", 100);
        map = map.set("redrover", 8);
        map.assertValid();
        // add some more
        map = map.set("hello", 7);
        map.assertValid();
        // verify expected values
        assertEquals(9, map.size());
        assertTrue(map.containsKey("foo"));
        assertTrue(map.containsKey("bar"));
        assertTrue(map.containsKey("null"));
        assertTrue(map.containsKey("baz"));
        assertTrue(map.containsKey("redrover"));
        assertTrue(map.containsKey("hello"));
        assertNotNull(map.get("foo"));
        assertNotNull(map.get("bar"));
        assertNotNull(map.get("null"));
        assertNotNull(map.get("baz"));
        assertNotNull(map.get("redrover"));
        assertNotNull(map.get("hello"));
        assertEquals(1, (int)map.get("foo"));
        assertEquals(2, (int)map.get("bar"));
        assertEquals(-1, (int)map.get("null"));
        assertEquals(100, (int)map.get("baz"));
        assertEquals(8, (int)map.get("redrover"));
        assertEquals(7, (int)map.get("hello"));
    }

    @Test
    public void testHashCollision() throws Exception {
        assertEquals("Aa".hashCode(), "BB".hashCode());
        assertEquals("AaAa".hashCode(), "BBBB".hashCode());
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.of();
        map = map.set("Aa", 1);
        map = map.set("BB", 2);
        map = map.set("null", 5);
        map = map.set("AaAa", 3);
        map = map.set("BBBB", 0);
        map.assertValid();
        // verify expected values
        assertEquals(5, map.size());
        assertTrue(map.containsKey("Aa"));
        assertTrue(map.containsKey("BB"));
        assertTrue(map.containsKey("null"));
        assertTrue(map.containsKey("AaAa"));
        assertTrue(map.containsKey("BBBB"));
        assertNotNull(map.get("Aa"));
        assertNotNull(map.get("BB"));
        assertNotNull(map.get("null"));
        assertNotNull(map.get("AaAa"));
        assertNotNull(map.get("BBBB"));
        assertEquals(1, (int)map.get("Aa"));
        assertEquals(2, (int)map.get("BB"));
        assertEquals(5, (int)map.get("null"));
        assertEquals(3, (int)map.get("AaAa"));
        assertEquals(0, (int)map.get("BBBB"));
    }

    @Test
    public void testRemoveBasic() throws Exception {
        ImmutableTrieMap<String, Integer> map = ImmutableTrieMap.<String, Integer>of()
                .set("foobar", 1)
                .set("Aa", 2)
                .set("BB", 3)
                .set("BB", 4)
                .set("stuff2", 5)
                .set("helloWorld3", 6);
        map.assertValid();
        assertEquals(5, map.size());
        map = map.delete("foobar");
        map.assertValid();
        assertEquals(4, map.size());
        map.assertValid();
        map = map.set("AaAa", 7)
                .set("BBBB", 8)
                .set("stuff", 9)
                .set("helloWorld", 10);
        map.assertValid();
        assertEquals(8, map.size());
        map = map.delete("helloWorld");
        map.assertValid();
        assertEquals(7, map.size());
        map = map.delete("AaAa");
        map.assertValid();
        assertEquals(6, map.size());
        map = map.delete("stuff");
        map.assertValid();
        assertEquals(5, map.size());
        map.assertValid();
        map = map.set("thisisareallylongstring", 11)
                .set("AaAa", 12);
        map.assertValid();
        assertEquals(7, map.size());
        assertEquals((Integer)2, map.get("Aa"));
        assertEquals((Integer)4, map.get("BB"));
        assertEquals((Integer)5, map.get("stuff2"));
        assertEquals((Integer)12, map.get("AaAa"));
        assertEquals((Integer)8, map.get("BBBB"));
        assertEquals((Integer)11, map.get("thisisareallylongstring"));
        assertEquals((Integer)6, map.get("helloWorld3"));
        map = map.delete("Aa");
        map.assertValid();
        map = map.delete("BB");
        map.assertValid();
        map = map.delete("stuff2");
        map.assertValid();
        map = map.delete("AaAa");
        map.assertValid();
        map = map.delete("BBBB");
        map.assertValid();
        map = map.delete("thisisareallylongstring");
        map.assertValid();
        map = map.delete("helloWorld3");
        map.assertValid();
        assertEquals(0, map.size());
        assertSame(ImmutableTrieMap.of(), map);
    }




}