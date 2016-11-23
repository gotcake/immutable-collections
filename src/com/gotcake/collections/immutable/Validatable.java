package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An interface to be implemented by classes that can assert their own validity,
 * and possibly print out debug info if there is a validation error.
 * @author Aaron Cake
 */
interface Validatable {
    
    static void tryAssertValid(final Object obj, final OutputStream outputStream) {
        if (obj instanceof Validatable) {
            try {
                final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                ((Validatable)obj).assertValid(writer);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void tryAssertValid(final Object obj) {
        tryAssertValid(obj, System.err);
    }

    void assertValid(final Writer debugWriter) throws IOException;

}
