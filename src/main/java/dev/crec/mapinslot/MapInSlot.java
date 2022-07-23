package dev.crec.mapinslot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class MapInSlot {
	public static void renderMap(Minecraft mc, float blitOffset, Slot slot) {
		renderMap(mc, blitOffset, slot.getItem(), slot.x, slot.y);
	}

	public static void renderMap(Minecraft mc, float blitOffset, ItemStack stack, int x, int y) {
		if (mc.player == null) return;

		Integer mapId = MapItem.getMapId(stack);
		MapItemSavedData saveData = MapItem.getSavedData(mapId, mc.player.level);

		if (mapId != null && saveData != null) {
			MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
			PoseStack poseStack = new PoseStack();
			poseStack.translate(x, y, blitOffset + 190);
			poseStack.scale(1/8F, 1/8F, 1);
			mc.gameRenderer.getMapRenderer().render(poseStack, bufferSource, mapId, saveData, true, 0xF000D2);
			bufferSource.endBatch();
		}
	}
}
