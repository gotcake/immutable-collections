package com.gotcake.collections.immutable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * A method for efficiently iterating over entries (or keys, or values) in a tree of Nodes
 * @author Aaron Cake
 */
abstract class NodeEntryIterator<K, V> {

    final class Callback {

        void offer(final K key, final V value) {
            nextKey = key;
            nextValue = value;
            didComputeNext = true;
        }

        void enterNode(final Node<K, V> node) {
            final StackEntry<K, V> entry = new StackEntry<>(node);
            stack.push(entry);
            node.computeIteration(entry.index++, this);
        }

        void replaceNode(final Node<K, V> node) {
            final StackEntry entry = stack.peek();
            entry.index = 0;
            entry.node = node;
            node.computeIteration(entry.index++, this);
        }

        void exitNode() {
            stack.pop();
            final StackEntry<K, V> entry = stack.peek();
            if (entry != null) {
                entry.node.computeIteration(entry.index++, this);
            }
        }

    }

    private static class StackEntry<K, V> {
        int index;
        Node<K, V> node;
        StackEntry(final Node<K, V> node) {
            this.index = 0;
            this.node = node;
        }
    }

    private final Deque<StackEntry<K, V>> stack;
    private final Callback callback;
    protected K nextKey;
    protected V nextValue;
    protected boolean didComputeNext;

    protected NodeEntryIterator(final Node<K, V> node) {
        stack = new ArrayDeque<>(10);
        callback = new Callback();
        stack.push(new StackEntry<>(node));
    }

    public boolean hasNext() {
        return didComputeNext || tryComputeNext();
    }

    protected boolean tryComputeNext() {
        final StackEntry<K, V> entry = stack.peek();
        if (entry == null) {
            return false;
        }
        entry.node.computeIteration(entry.index++, callback);
        return didComputeNext;
    }

    static class KeyIterator<K, V> extends NodeEntryIterator<K, V> implements Iterator<K> {

        KeyIterator(final Node<K, V> node) {
            super(node);
        }

        @Override
        public K next() {
            if (didComputeNext || tryComputeNext()) {
                final K key = nextKey;
                nextKey = null;
                nextValue = null;
                didComputeNext = false;
                return key;
            }
            return null;
        }
    }

    static class ValueIterator<K, V> extends NodeEntryIterator<K, V> implements Iterator<V> {

        ValueIterator(final Node<K, V> node) {
            super(node);
        }

        @Override
        public V next() {
            if (didComputeNext || tryComputeNext()) {
                final V value = nextValue;
                nextKey = null;
                nextValue = null;
                didComputeNext = false;
                return value;
            }
            return null;
        }
    }

    static class EntryIterator<K, V> extends NodeEntryIterator<K, V> implements Iterator<ImmutableMap.Entry<K, V>> {

        EntryIterator(final Node<K, V> node) {
            super(node);
        }

        @Override
        public ImmutableMap.Entry<K, V> next() {
            if (didComputeNext || tryComputeNext()) {
                final K key = nextKey;
                final V value = nextValue;
                nextKey = null;
                nextValue = null;
                didComputeNext = false;
                return new ImmutableMap.Entry<>(key, value);
            }
            return null;
        }
    }

}
