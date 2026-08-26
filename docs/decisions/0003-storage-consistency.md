# 0003: Storage consistency and transaction model

- Status: accepted
- Date: 2026-08-25

Chestwise never owns the indexed items. Every UI entry is an immutable snapshot
containing an exact item identity and physical slot references. Item identity
includes the item id plus a canonical version-provided payload containing NBT
or data components, damage, names, enchantments, potions, and modded data.

Mutations are server-authoritative intentions. The server validates player,
terminal, distance, loaded chunks, access, source revision, and current slot
contents. A transfer is simulated in full, then applied. If any optimistic
precondition changes, the operation aborts and the affected sources are marked
dirty. Partial results are reported explicitly; the client never submits a
physical slot-removal plan.

The index is incremental. Discovery is periodic and bounded; known sources are
fingerprinted on a staggered schedule, and unloaded, removed, or replaced
sources are invalidated when polling or discovery observes them. No task
force-loads a chunk and no full inventory scan runs each tick.
