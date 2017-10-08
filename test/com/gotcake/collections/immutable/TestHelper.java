package com.gotcake.collections.immutable;

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

    private TestHelper() { throw new UnsupportedOperationException(); }

}
