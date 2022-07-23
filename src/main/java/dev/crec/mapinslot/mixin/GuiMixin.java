package dev.crec.mapinslot.mixin;

import dev.crec.mapinslot.MapInSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin extends GuiComponent {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(
		method = "renderSlot",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"
		)
	)
	private void renderMap(int x, int y, float f, Player player, ItemStack stack, int k, CallbackInfo ci) {
		if (stack.is(Items.FILLED_MAP)) {
			MapInSlot.renderMap(this.minecraft, this.getBlitOffset(), stack, x, y);
		}
	}
}
