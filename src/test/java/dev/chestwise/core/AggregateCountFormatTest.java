package dev.chestwise.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void slotLabelsFitWithinThreeGlyphsAndKeepExactTotalsForTooltips() {
        assertEquals("999", AggregateCountFormat.compactForSlot(999));
        assertEquals("1k", AggregateCountFormat.compactForSlot(1_250));
        assertEquals("99k", AggregateCountFormat.compactForSlot(99_499));
        assertEquals(".1m", AggregateCountFormat.compactForSlot(99_500));
        assertEquals(".1m", AggregateCountFormat.compactForSlot(100_000));
        assertEquals("1m", AggregateCountFormat.compactForSlot(999_999));
        assertEquals("1m", AggregateCountFormat.compactForSlot(1_250_000));
        assertEquals(".1b", AggregateCountFormat.compactForSlot(99_500_000));
        assertEquals("1b", AggregateCountFormat.compactForSlot(999_999_999));
        assertEquals(".1b", AggregateCountFormat.compactForSlot(100_000_000));
        assertEquals(".1t", AggregateCountFormat.compactForSlot(99_500_000_000L));
        assertEquals("1t", AggregateCountFormat.compactForSlot(999_999_999_999L));
        assertEquals(".1t", AggregateCountFormat.compactForSlot(100_000_000_000L));
        assertEquals(".1q", AggregateCountFormat.compactForSlot(99_500_000_000_000L));
        assertEquals("1q", AggregateCountFormat.compactForSlot(999_999_999_999_999L));
        assertEquals(".1Q", AggregateCountFormat.compactForSlot(99_500_000_000_000_000L));
        assertEquals("9Q", AggregateCountFormat.compactForSlot(Long.MAX_VALUE));
        assertTrue(java.util.stream.LongStream.of(
            0, 1, 999, 1_000, 99_500, 999_999,
            99_500_000, 999_999_999, 99_500_000_000L, 999_999_999_999L,
            99_500_000_000_000L, 999_999_999_999_999L,
            99_500_000_000_000_000L, Long.MAX_VALUE
        )
            .mapToObj(AggregateCountFormat::compactForSlot)
            .allMatch(label -> label.length() <= 3));
        assertThrows(IllegalArgumentException.class, () -> AggregateCountFormat.compactForSlot(-1));
    }
}
