package dev.chestwise.minecraft.client;

import dev.chestwise.core.SortMode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Small loader-independent client preference file. It never contains server gameplay rules. */
public final class ChestwiseClientPreferences {
    private static final String SORT_MODE = "sortMode";
    private static final String MOUSE_WHEEL = "mouseWheelTransfer";
    private static final String PROTECTED_SLOTS = "protectedSlots";

    private final Path file;
    private SortMode sortMode;
    private boolean mouseWheelTransfer;
    private final Set<Integer> protectedSlots;

    private ChestwiseClientPreferences(
        Path file,
        SortMode sortMode,
        boolean mouseWheelTransfer,
        Set<Integer> protectedSlots
    ) {
        this.file = file;
        this.sortMode = sortMode;
        this.mouseWheelTransfer = mouseWheelTransfer;
        this.protectedSlots = new TreeSet<>(protectedSlots);
    }

    public static ChestwiseClientPreferences load(Path file) {
        Properties properties = new Properties();
        if (Files.isRegularFile(file)) {
            try (InputStream input = Files.newInputStream(file)) {
                properties.load(input);
            } catch (IOException ignored) {
                return defaults(file);
            }
        }
        SortMode sort = parseSortMode(properties.getProperty(SORT_MODE));
        boolean wheel = Boolean.parseBoolean(properties.getProperty(MOUSE_WHEEL, "false"));
        return new ChestwiseClientPreferences(file, sort, wheel, parseProtectedSlots(properties.getProperty(PROTECTED_SLOTS)));
    }

    private static ChestwiseClientPreferences defaults(Path file) {
        return new ChestwiseClientPreferences(file, SortMode.QUANTITY, false, Set.of());
    }

    private static SortMode parseSortMode(String value) {
        if (value == null) {
            return SortMode.QUANTITY;
        }
        try {
            return SortMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SortMode.QUANTITY;
        }
    }

    public SortMode sortMode() {
        return sortMode;
    }

    public boolean mouseWheelTransfer() {
        return mouseWheelTransfer;
    }

    public Set<Integer> protectedSlots() {
        return Set.copyOf(protectedSlots);
    }

    public void toggleProtectedSlot(int slot) {
        if (slot < 0 || slot >= 36) {
            return;
        }
        if (!protectedSlots.add(slot)) {
            protectedSlots.remove(slot);
        }
        save();
    }

    public void setSortMode(SortMode updated) {
        sortMode = updated;
        save();
    }

    private void save() {
        Properties properties = new Properties();
        properties.setProperty(SORT_MODE, sortMode.name().toLowerCase(Locale.ROOT));
        properties.setProperty(MOUSE_WHEEL, Boolean.toString(mouseWheelTransfer));
        properties.setProperty(PROTECTED_SLOTS, protectedSlots.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",")));
        try {
            Files.createDirectories(file.getParent());
            Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "Chestwise client-only preferences");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) {
            // A read-only config directory must not make the terminal unusable.
        }
    }

    private static Set<Integer> parseProtectedSlots(String value) {
        Set<Integer> parsed = new TreeSet<>();
        if (value == null || value.isBlank()) {
            return parsed;
        }
        for (String part : value.split(",")) {
            try {
                int slot = Integer.parseInt(part.trim());
                if (slot >= 0 && slot < 36) {
                    parsed.add(slot);
                }
            } catch (NumberFormatException ignored) {
                // Ignore one malformed entry without losing the remaining valid preferences.
            }
        }
        return parsed;
    }
}
