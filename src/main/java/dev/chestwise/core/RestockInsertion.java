package dev.chestwise.core;

public record RestockInsertion(int playerSlot, int count) {
    public RestockInsertion {
        if (playerSlot < 0 || playerSlot >= 36 || count < 1) {
            throw new IllegalArgumentException("Invalid restock insertion");
        }
    }
}
