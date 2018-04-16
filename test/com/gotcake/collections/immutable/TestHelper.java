package com.gotcake.collections.immutable;

import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Static helper methods for tests
 * @author Aaron Cake (gotcake)
 */
public final class TestHelper {

    private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateRandomString(final StringBuilder buffer, final Random random, final int minLength, final int maxLength) {
        buffer.setLength(0);
        int length = (int)Math.floor(random.nextFloat() * (maxLength - minLength)) + minLength;
        for (int i = 0; i < length; i++) {
            int index = (int)(random.nextFloat() * VALID_CHARS.length());
            buffer.append(VALID_CHARS.charAt(index));
        }
        return buffer.toString();
    }

    public static String generateRandomString(final StringBuilder buffer, final Random random, final int maxLength) {
        return generateRandomString(buffer, random, 2, maxLength);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> makeMap(final Object... data) {
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException();
        }
        final Map<K, V> map = new LinkedHashMap<>();
        for (int i = 0; i < data.length; i += 2) {
            map.put((K)data[i], (V)data[i+1]);
        }
        return map;
    }

    public static <V> Map<V, Integer> computeValueCounts(final Collection<V> values) {
        final Map<V, Integer> counts = new HashMap<>();
        for (final V value: values) {
            final Integer curCount = counts.get(value);
            if (curCount == null) {
                counts.put(value, 1);
            } else {
                counts.put(value, curCount + 1);
            }
        }
        return counts;
    }

    public static <V> Map<V, Integer> computeValueCounts(final Map<?, V> map) {
        return computeValueCounts(map.values());
    }

    public static <V> void decrementValueCount(final Map<V, Integer> counts, final V value) {
        final Integer curCount = counts.get(value);
        if (curCount == null) {
            fail("too many of value: " + value);
        }
        if (curCount == 1) {
            counts.remove(value);
        } else {
            counts.put(value, curCount - 1);
        }
    }

    private TestHelper() { throw new UnsupportedOperationException(); }

}
