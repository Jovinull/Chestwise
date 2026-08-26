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
        initial.toggleProtectedSlot(0);
        initial.toggleProtectedSlot(27);
        ChestwiseClientPreferences reloaded = ChestwiseClientPreferences.load(file);
        assertEquals(SortMode.NAMESPACE, reloaded.sortMode());
        assertEquals(java.util.Set.of(0, 27), reloaded.protectedSlots());
    }

    @Test
    void malformedValuesFallBackIndependently() throws IOException {
        Path file = temporaryDirectory.resolve("chestwise-client.properties");
        Files.writeString(file,
            "sortMode=not-a-mode\nmouseWheelTransfer=also-not-a-boolean\nprotectedSlots=-1,three,8,40\n");
        ChestwiseClientPreferences preferences = ChestwiseClientPreferences.load(file);
        assertEquals(SortMode.QUANTITY, preferences.sortMode());
        assertFalse(preferences.mouseWheelTransfer());
        assertEquals(java.util.Set.of(8), preferences.protectedSlots());
    }

}
