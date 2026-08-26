package dev.chestwise.core;

public record ExtractionStep(PhysicalSlotRef slot, long amount) {
    public ExtractionStep {
        if (amount <= 0 || amount > slot.count()) {
            throw new IllegalArgumentException("Invalid extraction amount");
        }
    }
}

