package dev.chestwise.minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ChestwiseConfig(
    int scanRadius,
    int maxInventories,
    int discoveryIntervalTicks,
    int inventoriesPolledPerTick,
    double interactionDistance
) {
    private static final Logger LOGGER = LoggerFactory.getLogger("chestwise");
    public static final ChestwiseConfig DEFAULT = new ChestwiseConfig(16, 128, 100, 8, 8.0);

    public static ChestwiseConfig load(Path file) {
        if (Files.notExists(file)) {
            try {
                writeDefaults(file);
            } catch (IOException exception) {
                LOGGER.warn("Could not create {}; using defaults", file, exception);
            }
            return DEFAULT;
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(file)) {
            properties.load(input);
            return new ChestwiseConfig(
                integer(properties, "scanRadius", DEFAULT.scanRadius),
                integer(properties, "maxInventories", DEFAULT.maxInventories),
                integer(properties, "discoveryIntervalTicks", DEFAULT.discoveryIntervalTicks),
                integer(properties, "inventoriesPolledPerTick", DEFAULT.inventoriesPolledPerTick),
                decimal(properties, "interactionDistance", DEFAULT.interactionDistance)
            );
        } catch (IOException | IllegalArgumentException exception) {
            LOGGER.error("Invalid Chestwise configuration at {}; using safe defaults", file, exception);
            return DEFAULT;
        }
    }

    private static int integer(Properties properties, String key, int fallback) {
        return Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim());
    }

    private static double decimal(Properties properties, String key, double fallback) {
        return Double.parseDouble(properties.getProperty(key, Double.toString(fallback)).trim());
    }

    private static void writeDefaults(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent == null) {
            throw new IOException("Configuration path has no parent: " + file);
        }
        Files.createDirectories(parent);
        Properties properties = new Properties();
        properties.setProperty("scanRadius", Integer.toString(DEFAULT.scanRadius));
        properties.setProperty("maxInventories", Integer.toString(DEFAULT.maxInventories));
        properties.setProperty("discoveryIntervalTicks", Integer.toString(DEFAULT.discoveryIntervalTicks));
        properties.setProperty("inventoriesPolledPerTick", Integer.toString(DEFAULT.inventoriesPolledPerTick));
        properties.setProperty("interactionDistance", Double.toString(DEFAULT.interactionDistance));
        Path temporary = Files.createTempFile(parent, "chestwise-config-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporary)) {
                properties.store(output, "Chestwise server/common settings. Times are in game ticks (20 ticks = 1 second).");
            }
            try {
                Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, file);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public ChestwiseConfig {
        if (scanRadius < 1 || scanRadius > 64) {
            throw new IllegalArgumentException("scanRadius must be between 1 and 64");
        }
        if (maxInventories < 1 || maxInventories > 1024) {
            throw new IllegalArgumentException("maxInventories must be between 1 and 1024");
        }
        if (discoveryIntervalTicks < 20 || discoveryIntervalTicks > 72000
            || inventoriesPolledPerTick < 1 || inventoriesPolledPerTick > 1024
            || !Double.isFinite(interactionDistance) || interactionDistance < 1 || interactionDistance > 64) {
            throw new IllegalArgumentException("Invalid polling or distance configuration");
        }
    }
}
