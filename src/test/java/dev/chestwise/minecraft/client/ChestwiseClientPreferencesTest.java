package dev.chestwise.minecraft.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import dev.chestwise.core.SortMode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ChestwiseClientPreferencesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void defaultsAreSafeAndSortPreferencePersists() {
        Path file = temporaryDirectory.resolve("config/chestwise-client.properties");
        ChestwiseClientPreferences initial = ChestwiseClientPreferences.load(file);
        assertEquals(SortMode.QUANTITY, initial.sortMode());
        assertFalse(initial.mouseWheelTransfer());

        initial.setSortMode(SortMode.NAMESPACE);
        assertEquals(SortMode.NAMESPACE, ChestwiseClientPreferences.load(file).sortMode());
    }

    @Test
    void malformedValuesFallBackIndependently() throws IOException {
        Path file = temporaryDirectory.resolve("chestwise-client.properties");
        Files.writeString(file, "sortMode=not-a-mode\nmouseWheelTransfer=also-not-a-boolean\n");
        ChestwiseClientPreferences preferences = ChestwiseClientPreferences.load(file);
        assertEquals(SortMode.QUANTITY, preferences.sortMode());
        assertFalse(preferences.mouseWheelTransfer());
    }
}
