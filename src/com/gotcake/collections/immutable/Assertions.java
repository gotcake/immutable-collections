package com.gotcake.collections.immutable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A class that supports asserting various conditions keeps track of all failed assertions
 * @author Aaron Cake
 */
final class Assertions {

    private final List<String> failedAssertions;
    private final String context;

    Assertions(final String context) {
        this.context = context;
        this.failedAssertions = new ArrayList<>();
    }

    private Assertions(final String context, final List<String> failedAssertions) {
        this.context = context;
        this.failedAssertions = failedAssertions;
    }

    private void fail(final String error, final String refinedContext) {
        failedAssertions.add(error + ": " + context + refinedContext);
    }

    private static String binaryStr(int num) {
        String str = Integer.toBinaryString(num);
        if (str.length() < 32) {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 32 - str.length(); i++) {
                sb.append('0');
            }
            sb.append(str);
            str = sb.toString();
        }
        return str.substring(0, 8) + ',' + str.substring(9, 16) +
                ',' + str.substring(17, 24) + ',' + str.substring(25);
    }

    void assertTrue(final String context, final boolean value) {
        if (!value) {
            fail("Expected true, but got false", context);
        }
    }

    void assertFalse(final String context, final boolean value) {
        if (value) {
            fail("Expected false, but got true", context);
        }
    }

    void assertEqual(final String context, final int expected, final int value) {
        if (expected != value) {
            fail("Expected " + expected + ", but got " + value, context);
        }
    }

    void assertValidType(final String context, final Object value, final Class<?>... validTypes) {
        for (final Class<?> clazz: validTypes) {
            if (clazz.isInstance(value)) {
                return;
            }
        }
        final String typeStr = Stream.of(validTypes)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
        final String actualType = value == null ? "null" : value.getClass().getSimpleName();
        fail("Expected one of type [" + typeStr + "], but got " + actualType, context);
    }

    void assertNotEqual(final String context, final int expected, final int value) {
        if (expected == value) {
            fail("Expected value other than " + expected, context);
        }
    }

    void assertNotNull(final String context, final Object value) {
        if (value == null) {
            fail("Expected non-null value, but got null", context);
        }
    }

    void assertNull(final String context, final Object value) {
        if (value != null) {
            fail("Expected null value, but got " + value, context);
        }
    }

    void assertEqual(final String context, final Object expected, final Object value) {
        if (!Objects.equals(expected, value)) {
            fail("Expected " + expected + ", but got " + value, context);
        }
    }

    void assertEqualBinary(final String message, final int expected, final int value) {
        if (expected != value) {
            fail("Expected " + expected + "(" + binaryStr(expected) + "), but got " + value + "(" + binaryStr(value) + ")", message);
        }
    }

    void assertValid(final String context, final Validatable object) {
        object.assertValid(new Assertions(this.context + "." + context, failedAssertions));
    }

    void printFailedAssertions(final PrintStream out) {
        failedAssertions.forEach(out::println);
    }

    void throwIfFailedAssertions() {
        if (failedAssertions.size() == 1) {
            throw new AssertionError("Failed 1 assertion during validation");
        }
        if (failedAssertions.size() > 1) {
            throw new AssertionError("Failed " + failedAssertions.size() + " assertions during validation");
        }
    }
}
