package com.gotcake.collections.immutable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by aaron on 9/5/17.
 */
public class GenerateHashCollisions {

    public static void main(String[] args) throws IOException {

        final String fileName = "test/resources/hash_collisions.txt";
        final Random r = new Random();

        final Map<Integer, Set<String>> data = new HashMap<>();

        final StringBuilder sb = new StringBuilder(10);

        long start = System.currentTimeMillis();

        long count = 0;
        long dupe = 0;

        while ((System.currentTimeMillis() - start) < (1000 * 60 * 10) && data.size() < Integer.MAX_VALUE - 1) {

            final String s = TestHelper.generateRandomString(sb, r, 3, 7);

            int hash = Util.computeSmearHash(s);

            Set<String> set = data.get(hash);
            if (set == null) {
                set = new HashSet<>();
                data.put(hash, set);
            }
            if (set.add(s)) {
                count++;
            } else {
                dupe++;
            }

        }


        int collidingHashes = 0;


        try (final Writer w = new FileWriter(fileName, false)) {

            for (final Integer key: data.keySet()) {

                Set<String> set = data.get(key);

                if (set.size() > 2) {

                    collidingHashes++;

                    boolean first = true;
                    for (final String val: set) {
                        if (!first) {
                            w.write(' ');
                        } else {
                            first = false;
                        }
                        w.write(val);
                    }

                    w.write('\n');

                }

            }

        }

        System.out.println("Total strings generated: " + count);
        System.out.println("Num dupes: " + dupe);
        System.out.println("Total unique hashes: " + data.size());
        System.out.println("Num hashes w/ collisions: " + collidingHashes);

    }

}
