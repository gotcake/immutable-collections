# immutable-collections
A colleciton of immutable collections

## ImmutableTrieMap
A specialized immutable Map implemented as a Hash Array Mapped Trie or (HAMT).
This map is best used when you need to immutability but also need to perform lots of operations on the Map. During inserts/updates/removals most nodes of the tree are reused in the new instance, minimizing object allocation, overhead, and memory usage.

## ImmutableTrieSet
Like the ImmutableTrieMap, but a set. Not yet implemented.

