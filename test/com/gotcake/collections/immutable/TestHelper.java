package com.gotcake.collections.immutable;

import java.util.Random;

/**
 * Static helper methods for tests
 * @author Aaron Cake (gotcake)
 */
public final class TestHelper {

    private static final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateRandomString(final StringBuilder buffer, final Random random) {
        buffer.setLength(0);
        int length = (int)(random.nextFloat() * 100) + 2;
        for (int i = 0; i < length; i++) {
            int index = (int)(random.nextFloat() * VALID_CHARS.length());
            buffer.append(VALID_CHARS.charAt(index));
        }
        return buffer.toString();
    }

    private TestHelper() { throw new UnsupportedOperationException(); }

}
