package dev.crec.mapinslot.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin implements ResourceManagerReloadListener {

	@Shadow @Final
	private Minecraft minecraft;

	@Inject(
		method = "tryRenderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;IILnet/minecraft/client/resources/model/BakedModel;)V",
			shift = Shift.AFTER
		)
	)
	private void drawMap(PoseStack poseStack, LivingEntity livingEntity, Level level, ItemStack stack, int x, int y, int k, int l, CallbackInfo ci) {
		var player = this.minecraft.player;
		if (player == null || !stack.is(Items.FILLED_MAP)) return;

		var mapId = MapItem.getMapId(stack);
		var savedData = MapItem.getSavedData(mapId, player.level);

		if (savedData == null) return;

		var bufferSource = this.minecraft.renderBuffers().bufferSource();
		poseStack.pushPose();
		// Item model is rendered at offset of 100, we render map on top at 101 in order for it to not have overlapping issues
		poseStack.translate(x, y, 101);
		poseStack.scale(0.125F, 0.125F, 1F);
		this.minecraft.gameRenderer.getMapRenderer().render(poseStack, bufferSource, mapId, savedData, true, 0xF000F0);
		bufferSource.endBatch();
		poseStack.popPose();
	}
}
