package com.gotcake.collections.immutable;

/**
 * A simple interface for items that support validation
 * @author Aaron Cake
 */
interface Validatable {

    static void tryAssertValid(final String context, final Object o) {
        final Assertions assertions = new Assertions(context);
        if (o instanceof Validatable) {
            ((Validatable)o).assertValid(assertions);
        }
        assertions.printFailedAssertions(System.err);
        assertions.throwIfFailedAssertions();
    }

    void assertValid(Assertions a);

}
