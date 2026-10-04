package dev.chestwise.core;

public record RestockPlayerSlot(int slot, ItemIdentity identity, int count, boolean protectedSlot) {
    public RestockPlayerSlot {
        if (slot < 0 || slot >= 36 || count < 0 || (count == 0) != (identity == null)) {
            throw new IllegalArgumentException("Invalid restock inventory slot");
        }
    }
}
