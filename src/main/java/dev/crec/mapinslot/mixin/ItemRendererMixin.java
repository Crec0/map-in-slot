package dev.crec.mapinslot.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.crec.mapinslot.MapInSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements ResourceManagerReloadListener {

	@Unique
	private float blitOffset;

	@Shadow
	public abstract BakedModel getModel(ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i);


	@Inject(
			method = "tryRenderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
			at = @At("HEAD")
	)
	private void drawMap(PoseStack poseStack, LivingEntity livingEntity, Level level, ItemStack stack, int x, int y, int z, int l, CallbackInfo ci) {
		if (stack.is(Items.FILLED_MAP)) {
			BakedModel bakedModel = this.getModel(stack, null, livingEntity, z);
			this.blitOffset = (float)(50 + (bakedModel.isGui3d() ? l : 0));
		}
	}


	@Inject(
		method = "renderGuiItemDecorations(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
		at = @At("HEAD")
	)
	private void drawMap(PoseStack poseStack, Font font, ItemStack stack, int x, int y, String string, CallbackInfo ci) {
		if (stack.is(Items.FILLED_MAP)) {
			MapInSlot.renderMap(poseStack, Minecraft.getInstance(), this.blitOffset, stack, x, y);
		}
	}
}
