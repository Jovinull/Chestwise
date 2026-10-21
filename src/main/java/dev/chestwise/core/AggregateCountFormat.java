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

    /**
     * Compact label sized for the terminal's 18-pixel slot spacing. Exact
     * totals remain available in the slot tooltip.
     */
    public static String compactForSlot(long count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        if (count < 1_000) {
            return Long.toString(count);
        }
        if (count < 100_000) {
            if (count >= 99_500) {
                return ".1m";
            }
            return count / 1_000 + "k";
        }
        if (count < 1_000_000) {
            return roundedTenths(count, 1_000_000, "m");
        }
        if (count < 100_000_000) {
            if (count >= 99_500_000) {
                return ".1b";
            }
            return count / 1_000_000 + "m";
        }
        if (count < 1_000_000_000) {
            return roundedTenths(count, 1_000_000_000, "b");
        }
        if (count < 100_000_000_000L) {
            if (count >= 99_500_000_000L) {
                return ".1t";
            }
            return count / 1_000_000_000 + "b";
        }
        if (count < 1_000_000_000_000L) {
            return roundedTenths(count, 1_000_000_000_000L, "t");
        }
        if (count < 100_000_000_000_000L) {
            if (count >= 99_500_000_000_000L) {
                return ".1q";
            }
            return count / 1_000_000_000_000L + "t";
        }
        if (count < 1_000_000_000_000_000L) {
            return roundedTenths(count, 1_000_000_000_000_000L, "q");
        }
        if (count < 100_000_000_000_000_000L) {
            if (count >= 99_500_000_000_000_000L) {
                return ".1Q";
            }
            return count / 1_000_000_000_000_000L + "q";
        }
        if (count < 1_000_000_000_000_000_000L) {
            return roundedTenths(count, 1_000_000_000_000_000_000L, "Q");
        }
        return count / 1_000_000_000_000_000_000L + "Q";
    }

    private static String roundedTenths(long count, long unit, String suffix) {
        long tenths = (count % unit + unit / 20) / (unit / 10);
        return tenths == 10 ? "1" + suffix : "." + tenths + suffix;
    }

    private static String scaled(long count, long unit, String suffix) {
        long whole = count / unit;
        long tenth = count % unit * 10 / unit;
        return whole < 10 && tenth > 0 ? whole + "." + tenth + suffix : whole + suffix;
    }
}
