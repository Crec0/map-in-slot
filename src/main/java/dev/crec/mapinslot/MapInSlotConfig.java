package dev.crec.mapinslot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MapInSlotConfig {
    public enum PreviewMode {
        AS_MAP_ITEM,
        FULL_SLOT,
        OFF
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("map-in-slot");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("map-in-slot.json");
    private static MapInSlotConfig INSTANCE;

    public boolean enabled = true;
    public PreviewMode mode = PreviewMode.AS_MAP_ITEM;
    public boolean lockedMapGlass = true;

    public static MapInSlotConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (Files.exists(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                INSTANCE = GSON.fromJson(reader, MapInSlotConfig.class);
            } catch (Exception e) {
                LOGGER.warn("Failed to read Map in Slot config from {}, using defaults", PATH, e);
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new MapInSlotConfig();
            save();
        } else if (INSTANCE.mode == null) {
            INSTANCE.mode = PreviewMode.AS_MAP_ITEM;
            save();
        }
    }

    public static void save() {
        if (INSTANCE == null) return;
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to save Map in Slot config to {}", PATH, e);
        }
    }
}
