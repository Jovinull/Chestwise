package dev.chestwise.minecraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class ChestwiseConfigTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void createsAndReloadsValidatedDefaults() {
        Path file = temporaryDirectory.resolve("nested/chestwise-server.properties");
        assertEquals(ChestwiseConfig.DEFAULT, ChestwiseConfig.load(file));
        assertTrue(Files.isRegularFile(file));
        assertEquals(ChestwiseConfig.DEFAULT, ChestwiseConfig.load(file));
    }

    @Test
    void rejectsUnsafeValuesAsOneAtomicConfiguration() throws IOException {
        Path file = temporaryDirectory.resolve("chestwise-server.properties");
        Files.writeString(file, "scanRadius=9999\nmaxInventories=128\n");
        assertEquals(ChestwiseConfig.DEFAULT, ChestwiseConfig.load(file));
    }
}
