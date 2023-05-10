package dev.crec.mapinslot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapInSlot {

	public static void renderMap(PoseStack poseStack, Minecraft minecraft, float blitOffset, ItemStack itemStack, int x, int y) {
		if (minecraft.player == null) {
			return;
		}

		Integer mapId = MapItem.getMapId(itemStack);
		MapItemSavedData savedData = MapItem.getSavedData(mapId, minecraft.player.level);

		if (mapId != null && savedData != null) {
			MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			poseStack.pushPose();
			poseStack.translate(x, y, blitOffset + 150);
			poseStack.scale(1 / 8F, 1 / 8F, 1);
			minecraft.gameRenderer.getMapRenderer().render(poseStack, bufferSource, mapId, savedData, true, 0xF000D2);
			bufferSource.endBatch();
			poseStack.popPose();
		}
	}
}
