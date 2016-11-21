package com.gotcake.collections.immutable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aaron on 7/1/16.
 */
public class DebugInfo {

    private Map<Class, Integer> nodeCounts;
    private int maxDepth;
    private int size;

    public DebugInfo(int size) {
        nodeCounts = new HashMap<>();
        maxDepth = 0;
        this.size = size;
    }

    public void registerNode(final Class<?> nodeClass, int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        final Integer currentCount = nodeCounts.get(nodeClass);
        nodeCounts.put(nodeClass, currentCount == null ? 1 : currentCount + 1);
    }
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DebugInfo[\n");
        sb.append("    size = ").append(size).append('\n');
        sb.append("    maxDepth = ").append(maxDepth).append('\n');
        nodeCounts.forEach((clazz, count) -> {
            sb.append("    nodeCount(").append(clazz.getSimpleName()).append(") = ").append(count).append('\n');
        });
        sb.append(']');
        return sb.toString();
    }
}
