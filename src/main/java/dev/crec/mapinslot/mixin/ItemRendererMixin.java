package dev.crec.mapinslot.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    
    @Shadow public abstract Matrix3x2fStack pose();
    
    @Shadow
    public abstract void submitMapRenderState(MapRenderState mapRenderState);
    
    @Unique
    private final MapRenderState mapRenderState = new MapRenderState();

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "HEAD")
    )
    private void drawMap(Font font, ItemStack stack, int i, int j, String string, CallbackInfo ci) {
        if (!stack.is(Items.FILLED_MAP)) return;

        var mapId = stack.get(DataComponents.MAP_ID);
        var savedData = MapItem.getSavedData(mapId, this.minecraft.level);

        if (savedData == null) return;

        this.pose().pushMatrix();
        this.pose().translate(i, j);
        this.pose().scale(0.125F, 0.125F);

        this.minecraft.getMapRenderer().extractRenderState(mapId, savedData, this.mapRenderState);
        this.submitMapRenderState(this.mapRenderState);

        this.pose().popMatrix();
    }
}
