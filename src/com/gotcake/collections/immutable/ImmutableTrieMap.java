package com.gotcake.collections.immutable;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An immutable map-like data structure based on the Hash Array Mapped Trie described at
 * http://lampwww.epfl.ch/papers/idealhashtrees.pdf
 * @author Aaron Cake
 */
public final class ImmutableTrieMap<K, V> extends BaseImmutableMap<K, V, ImmutableTrieMap<K, V>> {

    /**
     * PRECONDITION: existingBitIndex != bitIndex
     * Merges an existing node with a new node by creating a new DoubleNode
     */
    private static <K, V> Node<K, V> createCombinedNodeDifferentBitIndexes(final Node<K, V> existing,
                                                                           final int existingBitIndex,
                                                                           final K key,
                                                                           final V value,
                                                                           final int bitIndex) {
        final int mask = (1 << existingBitIndex) | (1 << bitIndex);
        if (existingBitIndex < bitIndex) {
            return new DoubleNode<>(mask, existing, new LeafNode<>(key, value));
        } else {
            return new DoubleNode<>(mask, new LeafNode<>(key, value), existing);
        }
    }

    /**
     * PRECONDITION: existingPrefix != prefix
     * Merges an existing node with a new node by creating a new DoubleNode and SingleNodes if necessary
     */
    private static <K, V> Node<K, V> createCombinedNodeDifferentPrefixes(final Node<K, V> existing,
                                                                         final int existingPrefix,
                                                                         final K key,
                                                                         final V value,
                                                                         final int prefix) {
        final int existingBitIndex = existingPrefix >>> 27;
        final int bitIndex = prefix >>> 27;
        if (existingBitIndex == bitIndex) {
            return new SingleNode<>(bitIndex, createCombinedNodeDifferentPrefixes(existing, existingPrefix << 5, key, value, prefix << 5));
        } else {
            return createCombinedNodeDifferentBitIndexes(existing, existingBitIndex, key, value, bitIndex);
        }
    }

    private final Node<K, V> root;
    private final int size;

    @SuppressWarnings("unchecked")
    private static final ImmutableTrieMap EMPTY_INSTANCE = new ImmutableTrieMap(0, null);

    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableTrieMap<K, V> of() {
        return EMPTY_INSTANCE;
    }

    public static <K, V> ImmutableTrieMap<K, V> of(final K key, final V value) {
        if (key == null) throw new NullPointerException("key cannot be null");
        if (value == null) throw new NullPointerException("value cannot be null");
        return new ImmutableTrieMap<>(1, new LeafNode<>(key, value));
    }

    private ImmutableTrieMap(final int size, final Node<K, V> root) {
        this.size = size;
        this.root = root;
    }

    /**
     * Checks if the map contains the key
     * @param key the key to check for
     * @return true if the map contains the key, false otherwise
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean containsKey(final Object key) {
        if (root == null || key == null) {
            return false;
        }
        final int prefix = TrieUtil.computeSmearHash(key);
        return root.get((K)key, prefix) != null;
    }

    /**
     * Checks if the map contains the key-value pair
     * @param key the key to check for
     * @param value the value to check for
     * @return true if the map contains the entry pair, false otherwise
     */
    public boolean containsEntry(final K key, final V value) {
        if (root == null || key == null || value == null) {
            return false;
        }
        final int prefix = TrieUtil.computeSmearHash(key);
        final V existingValue = root.get(key, prefix);
        return existingValue.equals(value);
    }

    /**
     * Gets the value with the given key, or null if no entry exists
     * @param key the key
     * @return the value, or null if no entry exists
     */
    @Override
    @SuppressWarnings("unchecked")
    public V get(final Object key) {
        if (root == null) {
            return null;
        }
        final int prefix = TrieUtil.computeSmearHash(key);
        return root.get((K)key, prefix);
    }

    /**
     * Computes the a new map with the given key and value returned by computeFn only if there is no value for the given key.
     * If this map already contains the given key, computeFn is never called.
     * If this map already contains the given key or computeFn returns null,
     * this map is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param computeFn a function which computes the value
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or computeFn is null
     */
    @Override
    public ImmutableTrieMap<K, V> updateIfAbsent(final K key, final Function<? super K, ? extends V> computeFn) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(computeFn, "computeFn cannot be null");
        if (root == null) {
            final V value = computeFn.apply(key);
            if (value == null) {
                return this;
            }
            return new ImmutableTrieMap<>(1, new LeafNode<>(key, value));
        }
        return updateInternal(key, new SingleUpdateConfig<>(computeFn, false, true));
    }

    /**
     * Computes the a new map with the given key and value returned by remapperFn only if there is already a value for the given key.
     * If this map doesn't contain the given key, mapperFn is never called.
     * If this map doesn't contain the given key or remapperFn returns the existing value,
     * this map is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or mapperFn is null
     */
    @Override
    public ImmutableTrieMap<K, V> updateIfPresent(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(mapperFn, "mapperFn cannot be null");
        if (root == null) {
            return this;
        }
        return updateInternal(key, new SingleUpdateConfig<>(mapperFn, true, false));
    }

    /**
     * Computes the a new map with the given key and value returned by remapperFn.
     * If computeFn returns the existing value,
     * this map is returned, no modifications are made, and no new instances are created.
     * @param key the key
     * @param mapperFn a function which maps the value
     * @return the new map instance, or the this instance if no modifications were necessary
     * @throws NullPointerException if key and/or mapperFn is null
     */
    @Override
    public ImmutableTrieMap<K, V> update(final K key, final BiFunction<? super K, ? super V, ? extends V> mapperFn) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(mapperFn, "mapperFn cannot be null");
        if (root == null) {
            final V value = mapperFn.apply(key, null);
            if (value == null) {
                return this;
            }
            return new ImmutableTrieMap<>(1, new LeafNode<>(key, value));
        }
        return updateInternal(key, new SingleUpdateConfig<>(mapperFn, true, true));
    }

    /**
     * Internal update method
     * @param key the key
     * @param singleUpdateConfig update config
     * @return the new map if any modifications were made, or this instance if not
     */
    @SuppressWarnings("unchecked")
    private ImmutableTrieMap<K, V> updateInternal(final K key, final SingleUpdateConfig<K, V> singleUpdateConfig) {
        final int prefix = TrieUtil.computeSmearHash(key);
        final Node<K, V> newRoot = root.update(singleUpdateConfig, key, prefix, 0);
        if (newRoot != root) {
            if (newRoot == null) {
                return EMPTY_INSTANCE;
            }
            // something changed, return new map
            return new ImmutableTrieMap<>(size + singleUpdateConfig.sizeChange, newRoot);
        }
        // no change
        return this;
    }

    /**
     * Returns true if this map is empty
     * @return true if this map is empty, false otherwise
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns true if the map contains the given value.
     * WARNING: this executes in linear O(n) time.
     * @param value the value to search for
     * @return true if the map contains the value, false otherwise
     */
    @Override
    public boolean containsValue(final Object value) {
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode && Objects.equals(value, ((LeafNode<K, V>)current).value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)  return true;
        if (!(other instanceof Map)) return false;
        final Map<?, ?> map = (Map<?, ?>)other;
        if (size != map.size()) return false;
        // if it's an ImmutableTrieMap do a structural comparison
        if (other instanceof ImmutableTrieMap) {
            final ImmutableTrieMap<?, ?> iMap = (ImmutableTrieMap<?, ?>) other;
            return root.equals(iMap.root);
        }
        // if we have faster contains access than other map, iterate over other map
        if (other instanceof TreeMap) {
            for (final Map.Entry<?, ?> entry : map.entrySet()) {
                if (!containsEntry((K) entry.getKey(), (V) entry.getValue())) {
                    return false;
                }
            }
            return true;
        }
        // otherwise iterate other this map and compare to other
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode) {
                final LeafNode<K, V> leaf = (LeafNode<K, V>)current;
                if (!Objects.equals(map.get(leaf.key), leaf.value)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Gets the value with the given key, or the default value if no entry exists.
     * @param key the key to look up
     * @param defaultValue the default value
     * @return the value, or defaultValue if no entry with the given key exists
     */
    @Override
    @SuppressWarnings("unchecked")
    public V getOrDefault(final Object key, final V defaultValue) {
        if (root == null || key == null) {
            return defaultValue;
        }
        final int prefix = TrieUtil.computeSmearHash(key);
        final V existingValue = root.get((K)key, prefix);
        return existingValue == null ? defaultValue : existingValue;
    }

    /**
     * Calls the action for all entries in the map
     * @param action the action to call
     */
    @Override
    public void forEach(final BiConsumer<? super K, ? super V> action) {
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode) {
                final LeafNode<K, V> leaf = (LeafNode<K, V>)current;
                action.accept(leaf.key, leaf.value);
            }
        }
    }


    /**
     * Gets an Iterator over the keys of this map
     * @return a key iterator
     */
    @Override
    public Iterator<K> keyIterator() {
        if (root == null) {
            return EmptyIterator.get();
        }
        return new KeyIteratorImpl<>(root);
    }

    /**
     * Gets an Iterator over the values of this map
     * @return a value iterator
     */
    @Override
    public Iterator<V> valueIterator() {
        if (root == null) {
            return EmptyIterator.get();
        }
        return new ValueIteratorImpl<>(root);
    }

    /**
     * Calls action for every key in the map
     * @param action the Consumer to call
     */
    public void forEachKey(final Consumer<? super K> action) {
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode) {
                action.accept(((LeafNode<K, V>)current).key);
            }
        }
    }

    /**
     * Calls action for every value in the map
     * @param action the Consumer to call
     */
    public void forEachValue(final Consumer<? super V> action) {
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode) {
                action.accept(((LeafNode<K, V>)current).value);
            }
        }
    }

    @Override
    protected boolean hasFastEntryIteration() {
        return true;
    }

    @Override
    protected int computeHashCode() {
        if (root == null) {
            return 0;
        }
        int hash = 0;
        final Stack<Node<K, V>> stack = new Stack<>();
        stack.push(root);
        while (!stack.empty()) {
            final Node<K, V> current = stack.pop();
            current.pushChildren(stack);
            if (current instanceof LeafNode) {
                final LeafNode<K, V> leaf = (LeafNode<K, V>)current;
                hash += leaf.key.hashCode() ^ leaf.value.hashCode();
            }
        }
        return hash;
    }


    DebugInfo computeDebugInfo() {
        final DebugInfo info = new DebugInfo(size, root == null ? 0 : root.computeSize());
        if (root != null) {
            root.computeDebugInfo(info, 1);
        }
        return info;
    }

    void assertSanity() {
        try {
            if (root != null) {
                TrieUtil.assertThat("computed size must equal stored size", root.computeSize() == size);
                root.assertSanity(null, 0, 0);
            }
        } catch (IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println(computeDebugInfo());
            throw e;
        }
    }

    void printTree() {
        if (root != null) {
            final StringWriter stringWriter = new StringWriter();
            try {
                root.printTree(stringWriter, 0, 0, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(stringWriter.toString());
        }
    }

    @Override
    public int size() {
        return size;
    }


    /**
     * Returns an iterator over all of the entries in this map
     * @return an iterator over entries
     */
    @Override
    protected Iterator<Entry<K, V>> entryIterator() {
        if (root == null) {
            return EmptyIterator.get();
        }
        return new EntryIteratorImpl<>(root);
    }

    /**
     * A class describing how to perform a single update and keeps track of the change in size
     */
    private static final class SingleUpdateConfig<K, V> {

        private byte sizeChange = 0;
        private final boolean updateIfExists;
        private final boolean updateIfNotExists;
        private final Function<? super K, ? extends V> computeFn;
        private final BiFunction<? super K, ? super V, ? extends V> mapperFn;
        private boolean didCallFn = false;

        private SingleUpdateConfig(final Function<? super K, ? extends V> computeFn, final boolean updateIfExists, final boolean updateIfNotExists) {
            this.updateIfExists = updateIfExists;
            this.updateIfNotExists = updateIfNotExists;
            this.computeFn = computeFn;
            this.mapperFn = null;
        }

        private SingleUpdateConfig(final BiFunction<? super K, ? super V, ? extends V> mapperFn, final boolean updateIfExists, final boolean updateIfNotExists) {
            this.updateIfExists = updateIfExists;
            this.updateIfNotExists = updateIfNotExists;
            this.computeFn = null;
            this.mapperFn = mapperFn;
        }

        private V computeNotExist(final K key) {
            TrieUtil.assertThat("updateIfNotExists must be true", updateIfNotExists);
            TrieUtil.assertThat("this is the first time calling a fn", !didCallFn);
            didCallFn = true;
            if (computeFn != null) {
                return computeFn.apply(key);
            }
            Objects.requireNonNull(mapperFn, "either computeFn or mapperFn must not be null");
            return mapperFn.apply(key, null);

        }

        private V computeExists(final K key, final V value) {
            TrieUtil.assertThat("updateIfNotExists must be true", updateIfExists);
            Objects.requireNonNull(mapperFn, "mapperFn must not be null");
            TrieUtil.assertThat("this is the first time calling a fn", !didCallFn);
            didCallFn = true;
            return mapperFn.apply(key, value);
        }

        private void informNodeRemoved() {
            TrieUtil.assertThat("this is the first time informing about a node operation", sizeChange == 0);
            sizeChange = -1;
        }

        private void informNodeInserted() {
            TrieUtil.assertThat("this is the first time informing about a node operation", sizeChange == 0);
            sizeChange = 1;
        }

    }

    /**
     * An interface implemented by all Nodes
     */
    private interface Node<K, V>  {
        Node<K, V> update(final SingleUpdateConfig<K, V> singleUpdateConfig, final K key, final int prefix, final int depth);
        V get(final K key, final int prefix);
        int computeSize();
        void computeDebugInfo(final DebugInfo info, int curDepth);
        void assertSanity(final Node<K, V> parent, int prefix, int curDepth);
        void pushChildren(final Stack<Node<K, V>> stack);
        boolean equals(final Node<?, ?> other);
        void printTree(Writer writer, int printDepth, int suffix, int nodeDepth) throws IOException;
    }

    /**
     * A simplified array mapped hash trie node where there is only one child
     */
    private static final class SingleNode<K, V> implements Node<K, V> {

        private final Node<K, V> child;
        private final byte childBitIndex;

        private SingleNode(final int childBitIndex, final Node<K, V> child) {
            this.child = child;
            this.childBitIndex = (byte)childBitIndex;
        }

        @Override
        public Node<K, V> update(final SingleUpdateConfig<K, V> updateConfig, final K key, final int prefix, final int depth) {
            final int bitIndex = prefix >>> 27;
            if (bitIndex == childBitIndex) {
                // bit index matches, key possibly contained under child. update child
                final Node<K, V> newChild = child.update(updateConfig, key, prefix << 5, depth + 1);
                if (child != newChild) {
                    if (newChild == null) {
                        // descendant removed, this node is no longer needed
                        return null;
                    }
                    if (newChild instanceof LeafNode) {
                        // new child is leaf node, this node is no longer needed
                        return newChild;
                    }
                    // descendant replaced, replace this node too
                    return new SingleNode<>(childBitIndex, newChild);
                }
            } else if (updateConfig.updateIfNotExists) {
                // bit index does not match, a new node will need to be inserted and a map node created
                final V value = updateConfig.computeNotExist(key);
                if (value != null) {
                    updateConfig.informNodeInserted();
                    return createCombinedNodeDifferentBitIndexes(child, childBitIndex, key, value, bitIndex);
                }
            }
            return this;
        }

        @Override
        public V get(final K key, final int prefix) {
            return prefix >>> 27 == childBitIndex ? child.get(key, prefix << 5) : null;
        }

        @Override
        public int computeSize() {
            return child.computeSize();
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(SingleNode.class, curDepth);
            child.computeDebugInfo(info, curDepth + 1);
        }

        @Override
        public void assertSanity(final Node<K, V> parent, int suffix, int curDepth) {
            TrieUtil.assertValidType("SingleNode parent", parent, true,
                    SingleNode.class, DoubleNode.class, TripleNode.class, MapNode.class);
            TrieUtil.assertThat("SingleNode depth cannot be greater than 6", curDepth <= 6);
            TrieUtil.assertThat("SingleNode child must not be null", child != null);
            TrieUtil.assertThat("SingleNode childBitIndex must be non-negative", childBitIndex >= 0);
            TrieUtil.assertThat("SingleNode childBitIndex must be less than 32", childBitIndex < 32);
            child.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, childBitIndex, curDepth), curDepth + 1);
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            stack.add(child);
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (!(other instanceof SingleNode)) return false;
            final SingleNode<?, ?> node = (SingleNode<?, ?>) other;
            return node.childBitIndex == childBitIndex && child.equals(node.child);
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ SingleNode");
            if (curDepth > 0) {
                writer.write(" prefix = ");
                TrieUtil.printIntBitsHighlight(writer, suffix, (curDepth - 1) * 5, 5, curDepth * 5);
            }
            writer.write('\n');
            child.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, childBitIndex, curDepth), curDepth + 1);
        }

    }

    /**
     * Base class for our map nodes
     */
    private static abstract class AbstractMapNode<K, V> implements Node<K, V> {

        protected final int mask;

        protected AbstractMapNode(final int mask) {
            this.mask = mask;
        }

        protected boolean hasChild(final int bitIndex) {
            return ((mask >>> bitIndex) & 1) == 1;
        }

        /**
         * PRECONDITION: hasChild(index) == true
         */
        abstract protected Node<K, V> getChild(final int logicalIndex);

        /**
         * PRECONDITION: hasChild(index) == true
         */
        abstract protected AbstractMapNode<K, V> replaceChild(final int logicalIndex, final Node<K, V> node);

        /**
         * PRECONDITION: hasChild(index) == true
         */
        abstract protected Node<K, V> removeChild(final int newMask, final int logicalIndex);

        /**
         * PRECONDITION: hasChild(index) == false
         */
        abstract protected AbstractMapNode<K, V> insertChild(final int newMask, final int logicalIndex, final Node<K, V> node);


        /**
         * PRECONDITION: contains(key, prefix) != value
         */
        @Override
        public Node<K, V> update(final SingleUpdateConfig<K, V> updateConfig, final K key, final int prefix, int depth) {
            final int bitIndex = prefix >>> 27;
            if (hasChild(bitIndex)) {
                // this map node has the given bit index, the key might exist in a descendant
                final int logicalIndex = TrieUtil.computeLogicalIndex(mask, bitIndex);
                final Node<K, V> child = getChild(logicalIndex);
                final Node<K, V> newChild = child.update(updateConfig, key, prefix << 5, depth + 1);
                if (child != newChild) {
                    // if a descendant was modified, we'll need to return a new node
                    if (newChild == null) {
                        // child removed
                        final int newMask = (1 << bitIndex) ^ mask;
                        return removeChild(newMask, logicalIndex);
                    }
                    // child replaced
                    return replaceChild(logicalIndex, newChild);
                }
            } else if (updateConfig.updateIfNotExists) {
                // this map node does not contain the bit index, a new node must be inserted
                final V value = updateConfig.computeNotExist(key);
                if (value != null) {
                    updateConfig.informNodeInserted();
                    final int logicalIndex = TrieUtil.computeLogicalIndex(mask, bitIndex);
                    final int newMask = mask | (1 << bitIndex);
                    return insertChild(newMask, logicalIndex, new LeafNode<>(key, value));
                }
            }
            return this;
        }

        @Override
        public V get(final K key, final int prefix) {
            final int index = prefix >>> 27;
            return hasChild(index) ? getChild(TrieUtil.computeLogicalIndex(mask, index)).get(key, prefix << 5) : null;
        }

    }

    /**
     * An bitmap hash array mapped trie node valid from size 4-32
     */
    private static final class MapNode<K, V> extends AbstractMapNode<K, V> {

        private final Node<K, V>[] children;

        @SuppressWarnings("unchecked")
        private MapNode(final int mask, final int numChildren) {
            super(mask);
            this.children = new Node[numChildren];
        }

        @Override
        protected Node<K, V> getChild(final int logicalIndex) {
            return children[logicalIndex];
        }

        @Override
        protected AbstractMapNode<K, V> replaceChild(final int logicalIndex, final Node<K, V> node) {
            final MapNode<K, V> newNode = new MapNode<>(mask, children.length);
            System.arraycopy(children, 0, newNode.children, 0, children.length);
            newNode.children[logicalIndex] = node;
            return newNode;
        }

        @Override
        protected AbstractMapNode<K, V> insertChild(final int newMask, final int logicalIndex, final Node<K, V> node) {
            final MapNode<K, V> newNode = new MapNode<>(newMask, children.length + 1);
            // copy children to new array
            if (logicalIndex > 0) {
                System.arraycopy(children, 0, newNode.children, 0, logicalIndex);
            }
            newNode.children[logicalIndex] = node;
            if (logicalIndex < children.length) {
                System.arraycopy(children, logicalIndex, newNode.children, logicalIndex + 1, children.length - logicalIndex);
            }
            return newNode;
        }

        @Override
        protected Node<K, V> removeChild(final int newMask, final int logicalIndex) {
            if (children.length > 4) {
                final MapNode<K, V> newNode = new MapNode<>(newMask, children.length - 1);
                if (logicalIndex > 0) {
                    System.arraycopy(children, 0, newNode.children, 0, logicalIndex);
                }
                if (logicalIndex < children.length - 1) {
                    System.arraycopy(children, logicalIndex + 1, newNode.children, logicalIndex, children.length - logicalIndex - 1);
                }
                return newNode;
            }
            switch (logicalIndex) {
                case 0:
                    return new TripleNode<>(newMask, children[1], children[2], children[3]);
                case 1:
                    return new TripleNode<>(newMask, children[0], children[2], children[3]);
                case 2:
                    return new TripleNode<>(newMask, children[0], children[1], children[3]);
                case 3:
                    return new TripleNode<>(newMask, children[0], children[1], children[2]);
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
        }

        @Override
        public int computeSize() {
            int total = 0;
            for (final Node<K, V> child: children) {
                total += child.computeSize();
            }
            return total;
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(MapNode.class, curDepth);
            for (final Node<K, V> child: children) {
                child.computeDebugInfo(info, curDepth + 1);
            }
        }

        @Override
        public void assertSanity(final Node<K, V> parent, final int suffix, final int curDepth) {
            TrieUtil.assertThat("MapNode must have size of at least 4", children.length >= 4);
            TrieUtil.assertThat("MapNode mask bit count must match size", Integer.bitCount(mask) == children.length);
            TrieUtil.assertValidType("MapNode parent", parent, true,
                    SingleNode.class, DoubleNode.class, TripleNode.class, MapNode.class);
            TrieUtil.assertThat("MapNode depth cannot be greater than 6", curDepth <= 6);
            for (final Node<K, V> child: children) {
                TrieUtil.assertThat("MapNode children must not be null", child != null);
            }
            for (int i = 0; i < children.length; i++) {
                children[i].assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, i), curDepth), curDepth + 1);
            }
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ MapNode");
            if (curDepth > 0) {
                writer.write(" prefix = ");
                TrieUtil.printIntBitsHighlight(writer, suffix, (curDepth - 1) * 5, 5, curDepth * 5);
            }
            writer.write('\n');
            for (int i = 0; i < children.length; i++) {
                children[i].printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, i), curDepth), curDepth + 1);
            }
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            Collections.addAll(stack, children);
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (!(other instanceof MapNode)) return false;
            final MapNode<?, ?> mapNode = (MapNode<?, ?>)other;
            if (mapNode.mask != mask) return false;
            for (int i = 0; i < children.length; i++) {
                if (!children[i].equals(mapNode.children[i])) return false;
            }
            return true;
        }

    }

    /**
     * A simplified array mapped hash trie node where there are two children
     */
    private static final class DoubleNode<K, V> extends AbstractMapNode<K, V> {

        private final Node<K, V> childA;
        private final Node<K, V> childB;

        private DoubleNode(final int mask, final Node<K, V> childA, final Node<K, V> childB) {
            super(mask);
            this.childA = childA;
            this.childB = childB;
        }

        @Override
        protected Node<K, V> getChild(final int logicalIndex) {
            return logicalIndex == 0 ? childA : childB;
        }

        @Override
        protected AbstractMapNode<K, V> replaceChild(final int logicalIndex, final Node<K, V> node) {
            if (logicalIndex == 0) {
                return new DoubleNode<>(mask, node, childB);
            } else {
                return new DoubleNode<>(mask, childA, node);
            }
        }

        @Override
        protected Node<K, V> removeChild(int newMask, int logicalIndex) {
            if (logicalIndex == 0) {
                if (childB instanceof LeafNode) {
                    return childB;
                }
                int bitIndexB = Integer.numberOfTrailingZeros(newMask);
                return new SingleNode<>(bitIndexB, childB);
            } else {
                if (childA instanceof LeafNode) {
                    return childA;
                }
                int bitIndexA = Integer.numberOfTrailingZeros(newMask);
                return new SingleNode<>(bitIndexA, childA);
            }
        }

        /**
         * PRECONDITION: hasChild(index) == false
         */
        @Override
        protected AbstractMapNode<K, V> insertChild(final int newMask, final int logicalIndex, final Node<K, V> node) {
            switch (logicalIndex) {
                case 0:
                    return new TripleNode<>(newMask, node, childA, childB);
                case 1:
                    return new TripleNode<>(newMask, childA, node, childB);
                case 2:
                    return new TripleNode<>(newMask, childA, childB, node);
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
        }

        @Override
        public int computeSize() {
            return childA.computeSize() + childB.computeSize();
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(DoubleNode.class, curDepth);
            childA.computeDebugInfo(info, curDepth + 1);
            childB.computeDebugInfo(info, curDepth + 1);
        }

        @Override
        public void assertSanity(final Node<K, V> parent, final int suffix, final int curDepth) {
            TrieUtil.assertThat("DoubleNode mask bit count must be 2", Integer.bitCount(mask) == 2);
            TrieUtil.assertValidType("DoubleNode parent", parent, true,
                    SingleNode.class, DoubleNode.class, TripleNode.class, MapNode.class);
            TrieUtil.assertThat("DoubleNode depth cannot be greater than 6", curDepth <= 6);
            TrieUtil.assertThat("DoubleNode children must not be null", childA != null && childB != null);
            childA.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 0), curDepth), curDepth + 1);
            childB.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 1), curDepth), curDepth + 1);
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            stack.add(childA);
            stack.add(childB);
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ DoubleNode");
            if (curDepth > 0) {
                writer.write(" prefix = ");
                TrieUtil.printIntBitsHighlight(writer, suffix, (curDepth - 1) * 5, 5, curDepth * 5);
            }
            writer.write('\n');
            childA.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 0), curDepth), curDepth + 1);
            childB.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 1), curDepth), curDepth + 1);
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (!(other instanceof DoubleNode)) return false;
            final DoubleNode<?, ?> node = (DoubleNode<?, ?>) other;
            return node.mask == mask && childA.equals(node.childA) && childB.equals(node.childB);
        }

    }

    /**
     * A simplified array mapped hash trie node where there are three children
     */
    private static final class TripleNode<K, V> extends AbstractMapNode<K, V> {

        private final Node<K, V> childA;
        private final Node<K, V> childB;
        private final Node<K, V> childC;

        private TripleNode(final int mask, final Node<K, V> childA, final Node<K, V> childB, final Node<K, V> childC) {
            super(mask);
            this.childA = childA;
            this.childB = childB;
            this.childC = childC;
        }

        @Override
        protected Node<K, V> getChild(final int logicalIndex) {
            switch (logicalIndex) {
                case 0:
                    return childA;
                case 1:
                    return childB;
                case 2:
                    return childC;
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
        }

        @Override
        protected AbstractMapNode<K, V> replaceChild(final int logicalIndex, final Node<K, V> node) {
            switch (logicalIndex) {
                case 0:
                    return new TripleNode<>(mask, node, childB, childC);
                case 1:
                    return new TripleNode<>(mask, childA, node, childC);
                case 2:
                    return new TripleNode<>(mask, childA, childB, node);
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
        }

        @Override
        protected Node<K, V> removeChild(int newMask, int logicalIndex) {
            switch (logicalIndex) {
                case 0:
                    return new DoubleNode<>(newMask, childB, childC);
                case 1:
                    return new DoubleNode<>(newMask, childA, childC);
                case 2:
                    return new DoubleNode<>(newMask, childA, childB);
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
        }

        @Override
        protected AbstractMapNode<K, V> insertChild(final int newMask, final int logicalIndex, final Node<K, V> node) {
            final MapNode<K, V> newNode = new MapNode<>(newMask, 4);
            switch (logicalIndex) {
                case 0:
                    newNode.children[0] = node;
                    newNode.children[1] = childA;
                    newNode.children[2] = childB;
                    newNode.children[3] = childC;
                    break;
                case 1:
                    newNode.children[0] = childA;
                    newNode.children[1] = node;
                    newNode.children[2] = childB;
                    newNode.children[3] = childC;
                    break;
                case 2:
                    newNode.children[0] = childA;
                    newNode.children[1] = childB;
                    newNode.children[2] = node;
                    newNode.children[3] = childC;
                    break;
                case 3:
                    newNode.children[0] = childA;
                    newNode.children[1] = childB;
                    newNode.children[2] = childC;
                    newNode.children[3] = node;
                    break;
                default:
                    throw new IllegalStateException("bad logical index: " + logicalIndex);
            }
            return newNode;
        }

        @Override
        public int computeSize() {
            return childA.computeSize() + childB.computeSize() + childC.computeSize();
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(TripleNode.class, curDepth);
            childA.computeDebugInfo(info, curDepth + 1);
            childB.computeDebugInfo(info, curDepth + 1);
            childC.computeDebugInfo(info, curDepth + 1);
        }

        @Override
        public void assertSanity(final Node<K, V> parent, final int suffix, final int curDepth) {
            TrieUtil.assertThat("TripleNode mask bit count must be 3", Integer.bitCount(mask) == 3);
            TrieUtil.assertValidType("TripleNode parent", parent, true,
                    SingleNode.class, DoubleNode.class, TripleNode.class, MapNode.class);
            TrieUtil.assertThat("TripleNode children must not be null", childA != null && childB != null && childC != null);
            childA.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 0), curDepth), curDepth + 1);
            childB.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 1), curDepth), curDepth + 1);
            childC.assertSanity(this, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 2), curDepth), curDepth + 1);
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ TripleNode");
            if (curDepth > 0) {
                writer.write(" prefix = ");
                TrieUtil.printIntBitsHighlight(writer, suffix, (curDepth - 1) * 5, 5, curDepth * 5);
            }
            writer.write('\n');
            childA.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 0), curDepth), curDepth + 1);
            childB.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 1), curDepth), curDepth + 1);
            childC.printTree(writer, printDepth + 1, TrieUtil.computeChildHashSuffix(suffix, TrieUtil.nthSetBitPosition(mask, 2), curDepth), curDepth + 1);
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            stack.add(childA);
            stack.add(childB);
            stack.add(childC);
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (!(other instanceof TripleNode)) return false;
            final TripleNode<?, ?> node = (TripleNode<?, ?>) other;
            return node.mask == mask && childA.equals(node.childA) && childB.equals(node.childB) && childC.equals(node.childC);
        }

    }

    /**
     * A leaf node with a single value and no colliding values
     */
    private static class LeafNode<K, V> extends Entry<K, V> implements Node<K, V> {

        private LeafNode(final K key, final V value) {
            super(key, value);
        }

        protected LeafNode<K, V> updateLeaf(final SingleUpdateConfig<K, V> updateConfig, final K key) {
            if (this.key.equals(key)) {
                // found the entry
                if (updateConfig.updateIfExists) {
                    final V newValue = updateConfig.computeExists(key, value);
                    if (newValue == null) {
                        // null returned, remove node
                        updateConfig.informNodeRemoved();
                        return null;
                    }
                    if (!value.equals(newValue)) {
                        // different value returned, replace node
                        return new LeafNode<>(key, newValue);
                    }
                }
            } else if (updateConfig.updateIfNotExists) {
                // the key does not exist in the tree, insert a new node
                final V newValue = updateConfig.computeNotExist(key);
                if (newValue != null) {
                    updateConfig.informNodeInserted();
                    return new LinkedLeafNode<>(key, newValue, this);
                }
            }
            return this;
        }

        @Override
        public Node<K, V> update(final SingleUpdateConfig<K, V> updateConfig, final K key, final int prefix, final int depth) {
            if (this.key.equals(key)) {
                // found the entry
                if (updateConfig.updateIfExists) {
                    final V newValue = updateConfig.computeExists(key, value);
                    if (newValue == null) {
                        // null returned, remove node
                        updateConfig.informNodeRemoved();
                        return null;
                    }
                    if (!value.equals(newValue)) {
                        // different value returned, replace node
                        return new LeafNode<>(key, newValue);
                    }
                }
            } else if (updateConfig.updateIfNotExists) {
                // the key does not exist in the tree, insert a new node
                final V newValue = updateConfig.computeNotExist(key);
                if (newValue != null) {
                    updateConfig.informNodeInserted();
                    if (depth < 6) {
                        // we have some hash bits left over, use them to determine if we can insert a new map node
                        final int thisPrefix = TrieUtil.computeHashPrefix(this.key, depth);
                        if (prefix == thisPrefix) { // if the prefixes are the same it's a hash collision, chain a new LinkedLeafNode
                            return new LinkedLeafNode<>(key, newValue, this);
                        }
                        // the hash prefixes are different, which means the two nodes fall under different indices in a map node
                        return createCombinedNodeDifferentPrefixes(this, thisPrefix, key, newValue, prefix);
                    } else {
                        // we've run out of hash bits, chain a new LinkedLeafNode
                        return new LinkedLeafNode<>(key, newValue, this);
                    }
                }
            }
            return this;
        }

        @Override
        public V get(final K key, final int prefix) {
            return this.key.equals(key) ? this.value : null;
        }

        @Override
        public int computeSize() {
            return 1;
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(LeafNode.class, curDepth);
        }

        @Override
        public void assertSanity(final Node<K, V> parent, final int suffix, final int curDepth) {
            TrieUtil.assertValidType("LeafNode parent", parent, true,
                    DoubleNode.class, TripleNode.class, MapNode.class, LinkedLeafNode.class);
            TrieUtil.assertEqual("LeafNode (" + key + "->" + value + ") suffix mismatch", suffix, TrieUtil.computeHashSuffix(key, curDepth));
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            // do nothing
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ LeafNode (" + key + "->"+ value + ") hash = ");
            TrieUtil.printIntBitsHighlight(writer, TrieUtil.computeSmearHash(key), (curDepth - 1) * 5, 5, 32);
            writer.write('\n');
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (other == null || other.getClass() != LeafNode.class) return false;
            final LeafNode<?, ?> node = (LeafNode<?, ?>) other;
            return Objects.equals(key, node.key) && Objects.equals(value, node.value);
        }

        protected boolean hasEntry(final Object key, final Object value) {
            return Objects.equals(this.key, key) && Objects.equals(this.value, value);
        }

    }

    /**
     * A leaf node that is created when there is a hash collision. Creates a linked list of LeafNodes.
     */
    private static final class LinkedLeafNode<K, V> extends LeafNode<K, V> {

        final LeafNode<K, V> next;

        private LinkedLeafNode(final K key, final V value, final LeafNode<K, V> next) {
            super(key, value);
            this.next = next;
        }

        protected LeafNode<K, V> updateLeaf(final SingleUpdateConfig<K, V> updateConfig, final K key) {
            if (this.key.equals(key)) {
                // found the entry
                if (updateConfig.updateIfExists) {
                    final V newValue = updateConfig.computeExists(key, value);
                    if (newValue == null) {
                        // null returned, remove node
                        updateConfig.informNodeRemoved();
                        return next;
                    }
                    if (!value.equals(newValue)) {
                        // different value returned, replace node
                        return new LinkedLeafNode<>(key, newValue, next);
                    }
                }
            } else {
                final LeafNode<K, V> newNext = next.updateLeaf(updateConfig, key);
                if (newNext == null) {
                    // remove next node
                    return new LeafNode<>(this.key, value);
                }
                if (newNext != next) {
                    // replace next node
                    return new LinkedLeafNode<>(this.key, value, newNext);
                }
            }
            // no change
            return this;
        }

        @Override
        public Node<K, V> update(final SingleUpdateConfig<K, V> updateConfig, final K key, final int prefix, final int depth) {
            if (this.key.equals(key)) {
                if (updateConfig.updateIfExists) {
                    final V newValue = updateConfig.computeExists(key, value);
                    if (newValue == null) {
                        // remove this entry
                        updateConfig.informNodeRemoved();
                        return next;
                    }
                    if (!value.equals(newValue)) {
                        // replace this entry
                        return new LinkedLeafNode<>(key, newValue, next);
                    }
                }
            } else {
                if (depth < 6) {
                    // we have some bits left, check if we need to create a map node
                    final int thisPrefix = TrieUtil.computeHashPrefix(this.key, depth);
                    if (prefix != thisPrefix) {
                        if (!updateConfig.updateIfNotExists) {
                            // key does not exist, quit early if updateIfNotExists is false
                            return this;
                        }
                        // not a hash collision, we must create a map node and insert a new node
                        final V newValue = updateConfig.computeNotExist(key);
                        if (newValue != null) {
                            updateConfig.informNodeInserted();
                            return createCombinedNodeDifferentPrefixes(this, thisPrefix, key, newValue, prefix);
                        }
                    }
                }
                // prefixes are the same or we ran out of bits: it's a hash collision, key guaranteed to be in this chain
                final LeafNode<K, V> newNext = next.updateLeaf(updateConfig, key);
                if (newNext == null) {
                    // remove next node
                    return new LeafNode<>(this.key, value);
                }
                if (newNext != next) {
                    // replace next node
                    return new LinkedLeafNode<>(this.key, value, newNext);
                }
            }
            // no change
            return this;
        }

        @Override
        public V get(final K key, final int prefix) {
            if (this.key.equals(key)) {
                return value;
            }
            return next.get(key, prefix);
        }

        @Override
        public int computeSize() {
            return 1 + next.computeSize();
        }

        @Override
        public void computeDebugInfo(DebugInfo info, int curDepth) {
            info.registerNode(LinkedLeafNode.class, curDepth);
            next.computeDebugInfo(info, curDepth + 1);
        }

        @Override
        public void assertSanity(final Node<K, V> parent, final int suffix, final int curDepth) {
            TrieUtil.assertValidType("LinkedLeafNode parent", parent, true,
                    DoubleNode.class, TripleNode.class, MapNode.class, LinkedLeafNode.class);
            TrieUtil.assertThat("LinkedLeafNode next must not be null", next != null);
            TrieUtil.assertEqual("LinkedLeafNode (" + key + "->" + value + ") suffix mismatch", suffix, TrieUtil.computeHashSuffix(key, curDepth));
            next.assertSanity(this, suffix, curDepth);
        }

        @Override
        public void printTree(Writer writer, int printDepth, int suffix, int curDepth) throws IOException {
            TrieUtil.printIndent(writer, printDepth);
            writer.write("+ LinkedLeafNode (" + key + "->"+ value + ") hash = ");
            TrieUtil.printIntBitsHighlight(writer, TrieUtil.computeSmearHash(key), (curDepth - 1) * 5, 5, 32);
            writer.write('\n');
            next.printTree(writer, printDepth + 1, suffix, curDepth);
        }

        @Override
        public void pushChildren(final Stack<Node<K, V>> stack) {
            stack.add(next);
        }

        @Override
        public boolean equals(Node<?, ?> other) {
            if (!(other instanceof LinkedLeafNode)) return false;
            LeafNode<?, ?> node = (LeafNode<?, ?>)other;
            while (true) {
                if (!hasEntry(node.key, node.value)) return false;
                if (node instanceof LinkedLeafNode) {
                    node = ((LinkedLeafNode) node).next;
                } else {
                    return true;
                }
            }
        }

        @Override
        protected boolean hasEntry(final Object key, final Object value) {
            return super.hasEntry(key, value) || next.hasEntry(key, value);
        }

    }

    /**
     * An iterator implementation for entries
     */
    private static class EntryIteratorImpl<K, V> implements Iterator<Entry<K, V>> {

        private final Stack<Node<K, V>> stack;

        @SuppressWarnings("unchecked")
        private EntryIteratorImpl(final Node<K, V> root) {
            stack = new Stack<>();
            stack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        @SuppressWarnings("unchecked")
        public Entry<K, V> next() {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof Entry) {
                    return (Entry)current;
                }
            }
            return null;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void forEachRemaining(final Consumer<? super Entry<K, V>> action) {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof Entry) {
                    action.accept((Entry<K, V>)current);
                }
            }
        }
    }

    /**
     * An iterator implementation for keys
     */
    private static final class KeyIteratorImpl<K, V> implements Iterator<K> {

        private final Stack<Node<K, V>> stack;

        private KeyIteratorImpl(final Node<K, V> root) {
            stack = new Stack<>();
            stack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public K next() {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof LeafNode) {
                    return ((LeafNode<K, V>)current).key;
                }
            }
            return null;
        }

        @Override
        public void forEachRemaining(final Consumer<? super K> action) {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof LeafNode) {
                    action.accept(((LeafNode<K, V>)current).key);
                }
            }
        }
    }

    /**
     * An iterator implementation for values
     */
    private static final class ValueIteratorImpl<K, V> implements Iterator<V> {

        private final Stack<Node<K, V>> stack;

        private ValueIteratorImpl(final Node<K, V> root) {
            stack = new Stack<>();
            stack.add(root);
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public V next() {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof LeafNode) {
                    return ((LeafNode<K, V>)current).value;
                }
            }
            return null;
        }

        @Override
        public void forEachRemaining(final Consumer<? super V> action) {
            while (!stack.empty()) {
                final Node<K, V> current = stack.pop();
                current.pushChildren(stack);
                if (current instanceof LeafNode) {
                    action.accept(((LeafNode<K, V>)current).value);
                }
            }
        }
    }

}
