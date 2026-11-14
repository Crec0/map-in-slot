package dev.crec.mapinslot.mixin;

import dev.crec.mapinslot.MapInSlotConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.AtlasManager;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow public abstract Matrix3x2fStack pose();

    @Shadow
    public abstract void map(MapRenderState mapRenderState);

    @Shadow
    public abstract void blit(Identifier location, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1);

    @Unique
    private static final SpriteId FILLED_MAP_SPRITE = new SpriteId(
            TextureAtlas.LOCATION_ITEMS,
            Identifier.withDefaultNamespace("item/filled_map")
    );

    @Unique
    private static final Identifier FRAME_16 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame.png");
    @Unique
    private static final Identifier FRAME_32 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame_32x.png");
    @Unique
    private static final Identifier FRAME_64 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame_64x.png");

    @Unique
    private static final Identifier FRAME_LOCKED_16 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame_locked.png");
    @Unique
    private static final Identifier FRAME_LOCKED_32 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame_locked_32x.png");
    @Unique
    private static final Identifier FRAME_LOCKED_64 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/map_cutout_frame_locked_64x.png");

    @Unique
    private static final Identifier FULL_GLASS_16 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/locked_glass_overlay_full.png");
    @Unique
    private static final Identifier FULL_GLASS_32 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/locked_glass_overlay_full_32x.png");
    @Unique
    private static final Identifier FULL_GLASS_64 = Identifier.fromNamespaceAndPath("map-in-slot", "textures/gui/locked_glass_overlay_full_64x.png");

    @Unique
    private static final MapRenderState MAP_RENDER_STATE = new MapRenderState();

    @Unique
    private static TextureAtlasSprite lastFilledMapSprite;
    @Unique
    private static int cachedResolution = 16;

    // Atlas reloads construct a fresh sprite instance, so identity tracks pack toggles, reorders, and F3+T after upload completes
    @Unique
    private static int getResolution(Minecraft minecraft) {
        AtlasManager atlasManager = minecraft.getAtlasManager();
        if (atlasManager == null) return cachedResolution;

        TextureAtlasSprite sprite;
        try {
            sprite = atlasManager.get(FILLED_MAP_SPRITE);
        } catch (RuntimeException ignored) {
            return cachedResolution;
        }

        if (sprite != lastFilledMapSprite) {
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

    // Swap filled map item model with blank map to prevent static map art from bleeding under live preview
    @ModifyVariable(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack replaceFilledMapWithBlank(ItemStack itemStack) {
        if (!itemStack.is(Items.FILLED_MAP)) return itemStack;

        var config = MapInSlotConfig.get();
        if (!config.enabled || config.mode == MapInSlotConfig.PreviewMode.OFF) return itemStack;

        if (this.minecraft.level == null) return itemStack;

        var mapId = itemStack.get(DataComponents.MAP_ID);
        if (mapId == null) return itemStack;

        var savedData = MapItem.getSavedData(mapId, this.minecraft.level);
        if (savedData == null) return itemStack;

        return Items.MAP.getDefaultInstance();
    }

    @Inject(
            method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "HEAD")
    )
    private void drawMap(Font font, ItemStack itemStack, int x, int y, String countText, CallbackInfo ci) {
        if (!itemStack.is(Items.FILLED_MAP)) return;

        var config = MapInSlotConfig.get();
        if (!config.enabled || config.mode == MapInSlotConfig.PreviewMode.OFF) return;

        if (this.minecraft.level == null) return;

        var mapId = itemStack.get(DataComponents.MAP_ID);
        if (mapId == null) return;

        var savedData = MapItem.getSavedData(mapId, this.minecraft.level);
        if (savedData == null) return;

        if (config.mode == MapInSlotConfig.PreviewMode.AS_MAP_ITEM) {
            this.pose().pushMatrix();
            try {
                this.pose().translate(x + 7.5F, y + 7.5F);
                this.pose().rotate((float) Math.toRadians(8.8));
                this.pose().scale(13.0F / 128.0F, 13.0F / 128.0F);
                this.pose().translate(-64.0F, -64.0F);

                this.minecraft.getMapRenderer().extractRenderState(mapId, savedData, MAP_RENDER_STATE);
                this.map(MAP_RENDER_STATE);
            } finally {
                this.pose().popMatrix();
            }

            int res = getResolution(this.minecraft);
            boolean isLocked = config.lockedMapGlass && savedData.locked;
            Identifier frameLocation = switch (res) {
                case 64 -> isLocked ? FRAME_LOCKED_64 : FRAME_64;
                case 32 -> isLocked ? FRAME_LOCKED_32 : FRAME_32;
                default -> isLocked ? FRAME_LOCKED_16 : FRAME_16;
            };

            this.blit(frameLocation, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
        } else {
            this.pose().pushMatrix();
            try {
                this.pose().translate(x, y);
                this.pose().scale(0.125F, 0.125F);

                this.minecraft.getMapRenderer().extractRenderState(mapId, savedData, MAP_RENDER_STATE);
                this.map(MAP_RENDER_STATE);
            } finally {
                this.pose().popMatrix();
            }

            if (config.lockedMapGlass && savedData.locked) {
                int res = getResolution(this.minecraft);
                Identifier fullGlassLocation = switch (res) {
                    case 64 -> FULL_GLASS_64;
                    case 32 -> FULL_GLASS_32;
                    default -> FULL_GLASS_16;
                };
                this.blit(fullGlassLocation, x, y, x + 16, y + 16, 0.0F, 1.0F, 0.0F, 1.0F);
            }
        }
    }
}
