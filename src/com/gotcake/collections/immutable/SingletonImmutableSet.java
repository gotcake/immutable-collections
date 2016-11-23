package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.gotcake.collections.immutable.Util.assertNotNull;

/**
 *
 * @author Aaron Cake
 */
class SingletonImmutableSet<T> implements ImmutableSet<T>, Validatable {

    private final T element;

    SingletonImmutableSet(T element) {
        this.element = element;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean contains(Object o) {
        return element.equals(o);
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.nonnullSingleton(element);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        // do nothing
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(element);
    }

    @Override
    public Stream<T> parallelStream() {
        return Stream.of(element);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof Set)) {
            return false;
        }
        final Set other = (Set)obj;
        return other.size() == 1 && other.contains(element);
    }

    @Override
    public ImmutableSet<T> insert(T element) {
        if (element.equals(this.element)) {
            return this;
        }
        return new MapBackedImmutableTrieSet<T>(this.element, element);
    }

    @Override
    public ImmutableSet<T> delete(T element) {
        if (this.element.equals(element)) {
            return EmptyImmutableSet.getInstance();
        }
        return this;
    }

    @Override
    public Object[] toArray() {
        return new Object[]{element};
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(T1[] a) {
        final Class<?> componentType = a.getClass().getComponentType();
        if (!componentType.isInstance(element)) {
            throw new ArrayStoreException(element + " is not instance of " + componentType.getSimpleName());
        }
        if (a.length != 1) {
            a = (T1[])Array.newInstance(componentType, 1);
        }
        a[0] = (T1)element;
        return a;
    }

    @Override
    public void assertValid(Writer debugWriter) throws IOException {
        assertNotNull("element was null", element);
    }
}
