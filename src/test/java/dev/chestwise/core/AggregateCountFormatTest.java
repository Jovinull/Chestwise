package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class AggregateCountFormatTest {
    @Test
    void formatsLargeCountsWithoutHidingExactSmallCounts() {
        assertEquals("999", AggregateCountFormat.compact(999));
        assertEquals("1.2k", AggregateCountFormat.compact(1_250));
        assertEquals("64k", AggregateCountFormat.compact(64_000));
        assertEquals("3.4m", AggregateCountFormat.compact(3_400_000));
        assertEquals("2b", AggregateCountFormat.compact(2_000_000_000L));
        assertEquals("9.2t", AggregateCountFormat.compact(9_223_000_000_000L));
        assertThrows(IllegalArgumentException.class, () -> AggregateCountFormat.compact(-1));
    }
}
