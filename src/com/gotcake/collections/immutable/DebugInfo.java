package com.gotcake.collections.immutable;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Helps keep track of some info including node counts and compute
 * @author Aaron Cake
 */
class DebugInfo {

    private static final Function<Object, Long> SIZE_CALC_FUNC = tryGetSizeCalculationFunc();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat(".##");

    private static Function<Object, Long> tryGetSizeCalculationFunc() {
        try {
            Class<?> memoryUtilClass = DebugInfo.class.getClassLoader().loadClass("com.javamex.classmexer.MemoryUtil");
            Method m = memoryUtilClass.getMethod("deepMemoryUsageOf", Object.class);
            // test method first
            m.invoke(null, new Object());
            return (obj) -> {
                try {
                    return (Long)m.invoke(null, obj);
                } catch (Exception e) {
                    return null;
                }
            };
        } catch (Exception e) {
            return (obj) -> null;
        }
    }

    private Class clazz = null;
    private Map<Class, Integer> nodeCounts = new HashMap<>();
    private int maxDepth = 0;
    private int size = 0;
    private Long memUsage = null;

    DebugInfo registerInstance(Object instance) {
        this.clazz = instance.getClass();
        this.memUsage = SIZE_CALC_FUNC.apply(instance);
        return this;
    }

    DebugInfo setSize(int size) {
        this.size = size;
        return this;
    }

    DebugInfo registerNode(Class<?> nodeClass, int depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
        Integer currentCount = nodeCounts.get(nodeClass);
        nodeCounts.put(nodeClass, currentCount == null ? 1 : currentCount + 1);
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DebugInfo[\n");
        if (clazz != null) {
            sb.append("    type = ").append(clazz.getSimpleName()).append('\n');
        }
        sb.append("    size = ").append(size).append('\n');
        if (maxDepth > 0) {
            sb.append("    maxDepth = ").append(maxDepth).append('\n');
        }
        nodeCounts.forEach((clazz, count) -> {
            sb.append("    nodeCount(").append(clazz.getSimpleName()).append(") = ").append(count).append('\n');
        });
        if (memUsage != null) {
            sb.append("    memUsage = ").append(memUsage).append(" bytes");
            if (memUsage > 1024) {
                double mem = memUsage / 1024.0;
                String unit = " kB)\n";
                if (mem > 1024) {
                    mem /= 1024.0;
                    unit = " MB)\n";
                }
                sb.append(" (");
                synchronized (DECIMAL_FORMAT) {
                    sb.append(DECIMAL_FORMAT.format(mem));
                }
                sb.append(unit);
            } else {
                sb.append('\n');
            }
        }
        sb.append("]\n");
        return sb.toString();
    }
}
