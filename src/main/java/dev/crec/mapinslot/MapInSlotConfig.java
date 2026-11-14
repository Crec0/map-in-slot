package dev.crec.mapinslot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

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
            } catch (Exception ignored) {
            }
        }
        if (INSTANCE == null) {
            INSTANCE = new MapInSlotConfig();
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
        } catch (Exception ignored) {
        }
    }
}
