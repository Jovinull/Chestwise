package dev.chestwise.core;

/** Compact, locale-neutral quantities for the terminal's 16-pixel item overlay. */
public final class AggregateCountFormat {
    private AggregateCountFormat() {
    }

    public static String compact(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        if (count < 1_000) {
            return Long.toString(count);
        }
        if (count < 1_000_000) {
            return scaled(count, 1_000, "k");
        }
        if (count < 1_000_000_000) {
            return scaled(count, 1_000_000, "m");
        }
        if (count < 1_000_000_000_000L) {
            return scaled(count, 1_000_000_000, "b");
        }
        return scaled(count, 1_000_000_000_000L, "t");
    }

    private static String scaled(long count, long unit, String suffix) {
        long whole = count / unit;
        long tenth = count % unit * 10 / unit;
        return whole < 10 && tenth > 0 ? whole + "." + tenth + suffix : whole + suffix;
    }
}
