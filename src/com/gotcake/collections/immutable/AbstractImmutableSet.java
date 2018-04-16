package com.gotcake.collections.immutable;

import java.util.Set;

/**
 * Created by aaron on 4/15/18.
 */
abstract class AbstractImmutableSet<T> implements ImmutableSet<T> {

    @Override
    public int hashCode() {
        int h = 0;
        for (final T element: this) {
            h += element.hashCode();
        }
        return h;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object o) {
        if (!(o instanceof Set)) {
            return false;
        }
        final Set set = (Set)o;
        if (set.size() != size()) {
            return false;
        }
        try {
            return set.containsAll(this);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }
}
