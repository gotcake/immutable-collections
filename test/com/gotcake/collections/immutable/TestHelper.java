package com.gotcake.collections.immutable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
        final Map<K, V> map = new HashMap<>();
        for (int i = 0; i < data.length; i += 2) {
            map.put((K)data[i], (V)data[i+1]);
        }
        return map;
    }

    private TestHelper() { throw new UnsupportedOperationException(); }

}
