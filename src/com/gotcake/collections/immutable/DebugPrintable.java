package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An interface to be implemented by classes that can print out potentially useful debug information
 * @author Aaron Cake
 */
public interface DebugPrintable {

    static void tryPrintDebug(Object obj, OutputStream outputStream) {
        if (obj instanceof DebugPrintable) {
            try {
                final Writer writer = new OutputStreamWriter(outputStream);
                ((DebugPrintable)obj).printDebug(writer);
                writer.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static void tryPrintDebug(Object obj) {
        tryPrintDebug(obj, System.out);
    }

    void printDebug(Writer writer) throws IOException;

}
