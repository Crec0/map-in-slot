package dev.crec.mapinslot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class MapInSlotConfigScreen extends OptionsSubScreen {

    public MapInSlotConfigScreen(Screen parent) {
        super(parent, Minecraft.getInstance().options, Component.literal("Map in Slot Settings"));
    }

    // Use Button instead of CycleButton because CycleButton.OnValueChange moved to AbstractCycleButton in 26.4
    @Override
    protected void addContents() {
        LinearLayout content = this.layout.addToContents(LinearLayout.vertical().spacing(8));
        var config = MapInSlotConfig.get();

        content.addChild(Button.builder(
                CommonComponents.optionStatus(Component.literal("Enabled"), config.enabled),
                btn -> {
                    config.enabled = !config.enabled;
                    btn.setMessage(CommonComponents.optionStatus(Component.literal("Enabled"), config.enabled));
                }).build());

        content.addChild(Button.builder(
                styleLabel(config.mode),
                btn -> {
                    config.mode = config.mode == MapInSlotConfig.PreviewMode.FULL_SLOT
                            ? MapInSlotConfig.PreviewMode.AS_MAP_ITEM
                            : MapInSlotConfig.PreviewMode.FULL_SLOT;
                    btn.setMessage(styleLabel(config.mode));
                }).build());

        content.addChild(Button.builder(
                CommonComponents.optionStatus(Component.literal("Locked Map Glass"), config.lockedMapGlass),
                btn -> {
                    config.lockedMapGlass = !config.lockedMapGlass;
                    btn.setMessage(CommonComponents.optionStatus(Component.literal("Locked Map Glass"), config.lockedMapGlass));
                }).build());
    }

    private static Component styleLabel(MapInSlotConfig.PreviewMode mode) {
        String value = mode == MapInSlotConfig.PreviewMode.FULL_SLOT ? "Full Slot" : "Map Item";
        return CommonComponents.optionNameValue(Component.literal("Style"), Component.literal(value));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    public void onClose() {
        MapInSlotConfig.save();
        // Delegate screen transition so 26.1-26.2 (Minecraft.setScreen) and 26.3+ (Minecraft.gui.setScreen) both work
        super.onClose();
    }
}
