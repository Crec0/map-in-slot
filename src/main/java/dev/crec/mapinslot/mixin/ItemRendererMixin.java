package dev.crec.mapinslot.mixin;

import dev.crec.mapinslot.MapInSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
	@Shadow
	public float blitOffset;

	@Inject(
		method = "renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At("HEAD")
	)
	private void drawMap(Font font, ItemStack stack, int x, int y, String string, CallbackInfo ci) {
		if (stack.is(Items.FILLED_MAP)) {
			MapInSlot.renderMap(Minecraft.getInstance(), this.blitOffset, stack, x, y);
		}
	}
}
