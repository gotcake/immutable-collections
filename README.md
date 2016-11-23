# immutable-collections
A collection of immutable collections

This project is still very much in flux. Expect anything and everything to change prior to a 1.0 release.

## ImmutableMap
A specialized immutable Map implemented as a Hash Array Mapped Trie or (HAMT).
This map is best used when you need to immutability but also need to perform lots of operations on the Map.
During inserts/updates/removals most nodes of the tree are reused in the new instance, minimizing object allocation,
overhead, and memory usage.

## ImmutableSet
Like the ImmutableMap, but a set. Uses ImmutableMap for storage.

## Upcoming
Test an implementation of ImmutableMap that collapses leaves into the map nodes against the current implementation.
Theory is it will decrease the memory used by a single instance, but but it may allocate more new memory when doing
mutations and might have an impact on mutation performance.

Once settled on a optimal implementation for the map, implement the ImmutableSet without ImmutableMap as a backing store.
