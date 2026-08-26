package dev.chestwise.core;

public record InsertionStep(int playerSlot, String sourceId, int storageSlot, long sourceRevision, long amount) {
    public InsertionStep {
        if (playerSlot < 0 || sourceId.isBlank() || storageSlot < 0 || sourceRevision < 0 || amount <= 0) {
            throw new IllegalArgumentException("Invalid insertion step");
        }
    }
}

