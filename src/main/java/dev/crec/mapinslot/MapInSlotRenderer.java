package dev.crec.mapinslot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public final class MapInSlotRenderer {

    private static final Identifier FILLED_MAP_SPRITE = Identifier.withDefaultNamespace("item/filled_map");

    private static final Identifier FRAME_16 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame.png");
    private static final Identifier FRAME_32 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame_32x.png");
    private static final Identifier FRAME_64 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame_64x.png");

    private static final Identifier FRAME_LOCKED_16 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame_locked.png");
    private static final Identifier FRAME_LOCKED_32 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame_locked_32x.png");
    private static final Identifier FRAME_LOCKED_64 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/map_cutout_frame_locked_64x.png");

    private static final Identifier FULL_GLASS_16 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/locked_glass_overlay_full.png");
    private static final Identifier FULL_GLASS_32 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/locked_glass_overlay_full_32x.png");
    private static final Identifier FULL_GLASS_64 = Identifier.fromNamespaceAndPath("map-in-slot",
            "textures/gui/locked_glass_overlay_full_64x.png");

    private static final MapRenderState MAP_RENDER_STATE = new MapRenderState();

    private static TextureAtlasSprite lastFilledMapSprite;
    private static int cachedResolution = 16;

    private MapInSlotRenderer() {
    }

    private record MapPreviewData(MapId id, MapItemSavedData savedData) {
    }

    // Swap filled map model with blank map to prevent static art from bleeding under live preview
    public static ItemStack resolveItemStack(Minecraft minecraft, ItemStack itemStack) {
        return resolvePreviewData(minecraft, itemStack) != null ? Items.MAP.getDefaultInstance() : itemStack;
    }

    public static void renderMap(GuiGraphicsExtractor graphics, Minecraft minecraft, ItemStack itemStack, int x, int y) {
        var preview = resolvePreviewData(minecraft, itemStack);
        if (preview == null) return;

        var config = MapInSlotConfig.get();
        boolean isLocked = config.lockedMapGlass && preview.savedData().locked;

        if (config.mode == MapInSlotConfig.PreviewMode.FULL_SLOT) {
            drawMapTexture(graphics, minecraft, preview, x, y, 0.125F, 0.0F, false);
            if (isLocked) {
                Identifier glass = selectByResolution(getResolution(minecraft), FULL_GLASS_16, FULL_GLASS_32, FULL_GLASS_64);
                graphics.blit(glass, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
            }
            return;
        }

        drawMapTexture(graphics, minecraft, preview, x + 7.5F, y + 7.5F, 13.0F / 128.0F, (float) Math.toRadians(8.8), true);
        int res = getResolution(minecraft);
        Identifier frame = isLocked
                ? selectByResolution(res, FRAME_LOCKED_16, FRAME_LOCKED_32, FRAME_LOCKED_64)
                : selectByResolution(res, FRAME_16, FRAME_32, FRAME_64);
        graphics.blit(frame, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
    }

    private static MapPreviewData resolvePreviewData(Minecraft minecraft, ItemStack itemStack) {
        if (minecraft == null || minecraft.level == null || itemStack == null || !itemStack.is(Items.FILLED_MAP)) {
            return null;
        }

        var config = MapInSlotConfig.get();
        if (!config.enabled || config.mode == MapInSlotConfig.PreviewMode.OFF) {
            return null;
        }

        var mapId = itemStack.get(DataComponents.MAP_ID);
        if (mapId == null) return null;

        var savedData = MapItem.getSavedData(mapId, minecraft.level);
        return savedData != null ? new MapPreviewData(mapId, savedData) : null;
    }

    private static void drawMapTexture(GuiGraphicsExtractor graphics, Minecraft minecraft, MapPreviewData preview,
                                       float tx, float ty, float scale, float rotationRadians, boolean centered) {
        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(tx, ty);
            if (rotationRadians != 0.0F) {
                graphics.pose().rotate(rotationRadians);
            }
            graphics.pose().scale(scale, scale);
            if (centered) {
                graphics.pose().translate(-64.0F, -64.0F);
            }

            minecraft.getMapRenderer().extractRenderState(preview.id(), preview.savedData(), MAP_RENDER_STATE);
            graphics.map(MAP_RENDER_STATE);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    private static Identifier selectByResolution(int res, Identifier tex16, Identifier tex32, Identifier tex64) {
        return switch (res) {
            case 64 -> tex64;
            case 32 -> tex32;
            default -> tex16;
        };
    }

    // Atlas reloads construct a fresh sprite instance, so identity tracks pack toggles, reorders, and F3+T after upload completes
    private static int getResolution(Minecraft minecraft) {
        AtlasManager atlasManager = minecraft.getAtlasManager();
        if (atlasManager == null) return cachedResolution;

        TextureAtlasSprite sprite;
        try {
            sprite = atlasManager.getAtlasOrThrow(AtlasIds.ITEMS).getSprite(FILLED_MAP_SPRITE);
        } catch (RuntimeException ignored) {
            return cachedResolution;
        }

        if (sprite != null && sprite != lastFilledMapSprite && sprite.contents() != null) {
            lastFilledMapSprite = sprite;
            int width = sprite.contents().width();
            if (width >= 64) {
                cachedResolution = 64;
            } else if (width >= 32) {
                cachedResolution = 32;
            } else {
                cachedResolution = 16;
            }
        }
        return cachedResolution;
    }
}
