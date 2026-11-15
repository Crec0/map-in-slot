package dev.crec.mapinslot.mixin;

import dev.crec.mapinslot.MapInSlotRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class ItemRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @ModifyVariable(
            method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack replaceFilledMapWithBlank(ItemStack itemStack) {
        return MapInSlotRenderer.resolveItemStack(this.minecraft, itemStack);
    }

    @Inject(
            method = "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At(value = "HEAD")
    )
    private void drawMap(Font font, ItemStack itemStack, int x, int y, String countText, CallbackInfo ci) {
        MapInSlotRenderer.renderMap((GuiGraphicsExtractor) (Object) this, this.minecraft, itemStack, x, y);
    }
}
