package com.gotcake.collections.immutable;

import junit.framework.Test;

import java.io.*;
import java.util.*;

/**
 * Created by aaron on 9/4/17.
 */
public class GenerateParityCase {

    public static void main(String[] args) throws IOException {

        final int compareEvery = 20;
        final int totalSteps = 200000;

        final float addChance = 2;
        final float overwriteChance = 1;
        final float removeChance = 1;
        final float collisionChance = 0.3f;
        final float nonRandomChance = addChance + overwriteChance + removeChance + collisionChance;
        final float totalChance = nonRandomChance + 5;

        final String fileName = "test/resources/parity_test_data_3.txt";
        final Random r = new Random();

        final Map<Integer, List<String>> hashCollisions = new HashMap<>();
        List<String> collidingStrings;

        try (final BufferedReader reader = new BufferedReader(new FileReader("test/resources/hash_collisions.txt"))) {
            final Set<String> collidingStringsSet = new HashSet<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                final String[] split = line.split("\\s+");
                if (split.length < 2) {
                    continue;
                }
                int hash = Util.computeSmearHash(split[0]);
                List<String> list = Arrays.asList(split);
                hashCollisions.put(hash, list);
                collidingStringsSet.addAll(list);
            }
            collidingStrings = new ArrayList<>(collidingStringsSet);
        }

        final Map<String, String> reference = new HashMap<>();
        ArrayList<String> arrayRef = new ArrayList<>();

        final StringBuilder sb = new StringBuilder(10);

        try (final Writer w = new FileWriter(fileName)) {

            for (int i = 0; i < totalSteps; i++) {

                if ((i % compareEvery) == (compareEvery - 1)) {
                    w.write("c\n");
                }

                if (reference.size() > 100 && reference.size() / arrayRef.size() < 0.75) {
                    arrayRef = new ArrayList<>(reference.keySet());
                }

                float t = r.nextFloat() * totalChance;

                if (reference.isEmpty() || t < addChance) {
                    String key = null;
                    while (key == null || reference.containsKey(key)) {
                        key = TestHelper.generateRandomString(sb, r, 7);
                    }
                    put(w, reference, arrayRef, key, TestHelper.generateRandomString(sb, r, 3));
                } else if (t < (addChance + overwriteChance + removeChance)){
                    String key = null;
                    while (key == null || !reference.containsKey(key)) {
                        int n = (int)Math.floor(r.nextFloat() * arrayRef.size());
                        key = arrayRef.get(n);
                    }
                    if (t < (addChance + overwriteChance)) {
                        String val = null;
                        while (val == null || reference.get(key).equals(val)) {
                            val = TestHelper.generateRandomString(sb, r, 3);
                        }
                        put(w, reference, arrayRef, key, val);
                    } else {
                        remove(w, reference, key);
                    }
                } else if (t < nonRandomChance) {
                    int n = (int)Math.floor(r.nextFloat() * collidingStrings.size());
                    String str = collidingStrings.get(n);
                    put(w, reference, arrayRef, str, TestHelper.generateRandomString(sb, r, 3));
                    List<String> colliding = hashCollisions.get(Util.computeSmearHash(str));
                    String key = null;
                    while (key == null || key.equals(str)) {
                        int n2 = (int)Math.floor(r.nextFloat() * colliding.size());
                        key = colliding.get(n2);
                    }
                    put(w, reference, arrayRef, key, TestHelper.generateRandomString(sb, r, 3));
                } else {
                    if (r.nextFloat() < 0.5) {
                        remove(w, reference, TestHelper.generateRandomString(sb, r, 7));
                    } else {
                        put(w, reference, arrayRef, TestHelper.generateRandomString(sb, r, 7), TestHelper.generateRandomString(sb, r, 3));
                    }
                }

            }

            w.write("c\n");

        }



    }

    private static void put(Writer w, Map<String, String> reference, List<String> arrayRef, String key, String val) throws IOException {
        w.write("p ");
        w.write(key);
        w.write(' ');
        w.write(val);
        w.write('\n');
        arrayRef.add(key);
        reference.put(key, val);
    }

    private static void remove(Writer w,  Map<String, String> reference, String key) throws IOException {
        w.write("r ");
        w.write(key);
        w.write('\n');
        reference.remove(key);
    }

}
