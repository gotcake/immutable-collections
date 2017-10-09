package com.gotcake.collections.immutable;

/**
 * A simple interface for items that support validation
 * @author Aaron Cake
 */
interface Validatable {

    static void tryAssertValid(final Object o) {
        if (o instanceof Validatable) {
            ((Validatable)o).assertValid();
        }
    }

    void assertValid();

}
