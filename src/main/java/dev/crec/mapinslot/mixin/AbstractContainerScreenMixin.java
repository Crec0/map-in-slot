package dev.crec.mapinslot.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.crec.mapinslot.MapInSlot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	protected AbstractContainerScreenMixin(Component component) {
		super(component);
	}

	@Inject(
		method = "renderSlot",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
		)
	)
	private void renderMap(PoseStack poseStack, Slot slot, CallbackInfo ci) {
		ItemStack stack = slot.getItem();
		if (stack.is(Items.FILLED_MAP)) {
			MapInSlot.renderMap(this.minecraft, this.getBlitOffset(), slot);
		}
	}
}
