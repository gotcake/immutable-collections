package com.gotcake.collections.immutable;

import org.junit.Test;

import static org.junit.Assert.*;

public class BasicImmutableMapTest {

    @Test
    public void testEmpty() {
        final ImmutableMap<String, Integer> map = ImmutableMap.of();
        assertEquals(0, map.size());
        assertSame(ImmutableMap.of(), ImmutableMap.of());
    }

    @Test
    public void testSingleBasic() {
        ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertNotNull(map.get("foo"));
        assertEquals(2, (long)map.get("foo"));
        Validatable.tryAssertValid(map);
    }

    @Test
    public void testSingleReplacement() throws Exception {
        final ImmutableMap<String, Integer> map = ImmutableMap.of("foo", 2).set("foo", 3);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("foo"));
        assertNotNull(map.get("foo"));
        assertEquals(3, (long)map.get("foo"));
        Validatable.tryAssertValid(map);
    }

    @Test
    public void testManyBasic() throws Exception {
        ImmutableMap<String, Integer> map = ImmutableMap.of();
        map = map.set("foo", 1);
        Validatable.tryAssertValid(map);
        map = map.set("bar", 2);
        Validatable.tryAssertValid(map);
        map = map.set("null", 5);
        Validatable.tryAssertValid(map);
        map = map.set("baz", 3);
        Validatable.tryAssertValid(map);
        map = map.set("redrover", 0);
        Validatable.tryAssertValid(map);
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
        ImmutableMap<String, Integer> map = ImmutableMap.of();
        map = map.set("foo", 1);
        map = map.set("bar", 2);
        map = map.set("null", 5);
        map = map.set("jolly", 9);
        map = map.set("omg", -10);
        map = map.set("zoidberg", 10);
        map = map.set("baz", 3);
        map = map.set("redrover", 0);
        Validatable.tryAssertValid(map);
        // now replace stuff
        map = map.set("null", -1);
        map = map.set("baz", 100);
        map = map.set("redrover", 8);
        Validatable.tryAssertValid(map);
        // add some more
        map = map.set("hello", 7);
        Validatable.tryAssertValid(map);
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
        ImmutableMap<String, Integer> map = ImmutableMap.of();
        map = map.set("Aa", 1);
        map = map.set("BB", 2);
        map = map.set("null", 5);
        map = map.set("AaAa", 3);
        map = map.set("BBBB", 0);
        Validatable.tryAssertValid(map);
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
        ImmutableMap<String, Integer> map = ImmutableMap.<String, Integer>of();
        map = map.set("foobar", 1);
        Validatable.tryAssertValid(map);
        assertEquals(1, map.size());
        map = map.set("Aa", 2);
        Validatable.tryAssertValid(map);
        assertEquals(2, map.size());
        map = map.set("BB", 3);
        Validatable.tryAssertValid(map);
        assertEquals(3, map.size());
        map = map.set("BB", 4);
        Validatable.tryAssertValid(map);
        assertEquals(3, map.size());
        map = map.set("stuff2", 5);
        Validatable.tryAssertValid(map);
        assertEquals(4, map.size());
        map = map.set("helloWorld3", 6);
        Validatable.tryAssertValid(map);
        assertEquals(5, map.size());
        map = map.delete("foobar");
        Validatable.tryAssertValid(map);
        assertEquals(4, map.size());
        Validatable.tryAssertValid(map);
        map = map.set("AaAa", 7)
                .set("BBBB", 8)
                .set("stuff", 9)
                .set("helloWorld", 10);
        Validatable.tryAssertValid(map);
        assertEquals(8, map.size());
        map = map.delete("helloWorld");
        Validatable.tryAssertValid(map);
        assertEquals(7, map.size());
        map = map.delete("AaAa");
        Validatable.tryAssertValid(map);
        assertEquals(6, map.size());
        map = map.delete("stuff");
        Validatable.tryAssertValid(map);
        assertEquals(5, map.size());
        Validatable.tryAssertValid(map);
        map = map.set("thisisareallylongstring", 11)
                .set("AaAa", 12);
        Validatable.tryAssertValid(map);
        assertEquals(7, map.size());
        assertEquals((Integer)2, map.get("Aa"));
        assertEquals((Integer)4, map.get("BB"));
        assertEquals((Integer)5, map.get("stuff2"));
        assertEquals((Integer)12, map.get("AaAa"));
        assertEquals((Integer)8, map.get("BBBB"));
        assertEquals((Integer)11, map.get("thisisareallylongstring"));
        assertEquals((Integer)6, map.get("helloWorld3"));
        map = map.delete("Aa");
        Validatable.tryAssertValid(map);
        map = map.delete("BB");
        Validatable.tryAssertValid(map);
        map = map.delete("stuff2");
        Validatable.tryAssertValid(map);
        map = map.delete("AaAa");
        Validatable.tryAssertValid(map);
        map = map.delete("BBBB");
        Validatable.tryAssertValid(map);
        map = map.delete("thisisareallylongstring");
        Validatable.tryAssertValid(map);
        map = map.delete("helloWorld3");
        assertEquals(0, map.size());
        assertSame(ImmutableMap.of(), map);
    }




}