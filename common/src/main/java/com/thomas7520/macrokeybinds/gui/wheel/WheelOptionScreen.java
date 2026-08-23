package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.object.wheel.Wheel;
import com.thomas7520.macrokeybinds.object.wheel.WheelMode;
import com.thomas7520.macrokeybinds.object.wheel.WheelSlot;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import com.thomas7520.macrokeybinds.util.WheelFlow;
import com.thomas7520.macrokeybinds.util.widget.ButtonImageWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;

public class WheelOptionScreen extends Screen {

    private static final ResourceLocation DELETE_ICON = new ResourceLocation("macrokeybinds", "textures/delete_button.png");

    private static final int MAX_PREVIEW_RADIUS = 70;
    private static final int CONTROLS_DISTANCE_FROM_CIRCLE = 14;
    private static final int DELETE_BUTTON_SIZE = 20;
    private static final int ICON_BUTTON_SIZE = 20;
    private static final int ADD_BUTTON_SIZE = 18;

    private final Screen parent;
    private final Wheel wheel;

    private Button modeButton;

    public WheelOptionScreen(Screen parent) {
        super(Component.translatable("text.wheel.options.title"));
        this.parent = parent;
        this.wheel = MacroUtil.getWheel() == null ? new Wheel() : MacroUtil.getWheel();
        MacroUtil.setWheel(this.wheel);
    }

    @Override
    protected void init() {
        StringWidget titleWidget = new StringWidget(title, font);
        titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, 8);
        addRenderableOnly(titleWidget);

        int wheelTop = 22;
        int wheelBottom = height - 30;
        int previewRadius = Math.min(MAX_PREVIEW_RADIUS, Math.min((wheelBottom - wheelTop) / 2, (width - 20) / 2));
        int wheelCenterX = width / 2;
        int wheelCenterY = wheelTop + (wheelBottom - wheelTop) / 2;

        addRenderableOnly(new WheelWidget(wheelCenterX - previewRadius, wheelCenterY - previewRadius, previewRadius,
                wheel.getMacros(), true, false, false, 0, slot -> {}));

        for(int slotIndex = 0; slotIndex < WheelWidget.SLOTS_MAX; slotIndex++) {
            addSlotControls(slotIndex, wheelCenterX, wheelCenterY, previewRadius);
        }

        modeButton = addRenderableWidget(Button.builder(modeText(), button -> changeMode())
                .bounds(width / 2 - 102, height - 27, 100, 20)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 + 2, height - 27, 100, 20)
                .build());
    }

    private void addSlotControls(int slotIndex, int wheelCenterX, int wheelCenterY, int radius) {
        WheelSlot slot = getWheelSlot(slotIndex);
        double angle = Math.toRadians(WheelWidget.getSlotMiddleAngle(slotIndex));
        int controlsCenterX = wheelCenterX + (int) Math.round(Math.cos(angle) * (radius + CONTROLS_DISTANCE_FROM_CIRCLE));
        int controlsCenterY = wheelCenterY + (int) Math.round(Math.sin(angle) * (radius + CONTROLS_DISTANCE_FROM_CIRCLE));

        if(slot == null || slot.getMacroId() == null) {
            int contentX = wheelCenterX + (int) Math.round(Math.cos(angle) * radius * 0.62d);
            int contentY = wheelCenterY + (int) Math.round(Math.sin(angle) * radius * 0.62d);

            StringWidget emptyText = new StringWidget(Component.translatable("text.wheel.empty"), font);
            emptyText.setPosition(contentX - emptyText.getWidth() / 2, contentY - font.lineHeight / 2);
            addRenderableOnly(emptyText);

            Button addButton = Button.builder(Component.literal("+"), button -> openMacroSelector(slotIndex))
                    .bounds(controlsCenterX - ADD_BUTTON_SIZE / 2, controlsCenterY - ADD_BUTTON_SIZE / 2, ADD_BUTTON_SIZE, ADD_BUTTON_SIZE)
                    .build();
            addButton.setTooltip(Tooltip.create(Component.translatable("text.wheel.add")));
            addRenderableWidget(addButton);
            return;
        }

        double tangentX = -Math.sin(angle);
        double tangentY = Math.cos(angle);
        double tangentBoxSize = Math.abs(tangentX) + Math.abs(tangentY);
        double controlsOffset = (DELETE_BUTTON_SIZE / 2d * tangentBoxSize + ICON_BUTTON_SIZE / 2d * tangentBoxSize + 2d) / 2d;

        int deleteCenterX = controlsCenterX - (int) Math.round(tangentX * controlsOffset);
        int deleteCenterY = controlsCenterY - (int) Math.round(tangentY * controlsOffset);
        int iconCenterX = controlsCenterX + (int) Math.round(tangentX * controlsOffset);
        int iconCenterY = controlsCenterY + (int) Math.round(tangentY * controlsOffset);

        addRenderableWidget(ButtonImageWidget.builder(Component.empty(), button -> removeMacro(slotIndex))
                .dimensions(deleteCenterX - DELETE_BUTTON_SIZE / 2, deleteCenterY - DELETE_BUTTON_SIZE / 2, DELETE_BUTTON_SIZE, DELETE_BUTTON_SIZE)
                .icon(DELETE_ICON)
                .tooltip(Tooltip.create(Component.translatable("text.wheel.remove")))
                .build());

        addRenderableWidget(new SlotIconButton(iconCenterX - ICON_BUTTON_SIZE / 2, iconCenterY - ICON_BUTTON_SIZE / 2, ICON_BUTTON_SIZE, ICON_BUTTON_SIZE, slot));
    }

    private void openMacroSelector(int slotIndex) {
        minecraft.setScreen(new WheelSelectMacroScreen(this, macro -> {
            setWheelSlot(slotIndex, new WheelSlot(macro.getUUID(), null));
            saveWheel();
        }));
    }

    private void removeMacro(int slotIndex) {
        setWheelSlot(slotIndex, null);
        saveWheel();
        rebuildWidgets();
    }

    private WheelSlot getWheelSlot(int slotIndex) {
        return slotIndex < wheel.getMacros().size() ? wheel.getMacros().get(slotIndex) : null;
    }

    private void setWheelSlot(int slotIndex, WheelSlot slot) {
        while(wheel.getMacros().size() <= slotIndex) {
            wheel.getMacros().add(null);
        }

        wheel.getMacros().set(slotIndex, slot);
    }

    private void changeMode() {
        wheel.setMode(wheel.getMode() == WheelMode.CLICK ? WheelMode.HOLD : WheelMode.CLICK);
        modeButton.setMessage(modeText());
        saveWheel();
    }

    private Component modeText() {
        Component translatedMode = Component.translatable(wheel.getMode() == WheelMode.CLICK ? "text.wheel.mode.click" : "text.wheel.mode.hold");
        return Component.translatable("text.wheel.mode", translatedMode);
    }

    private void changeIcon(WheelSlot slot) {
        minecraft.setScreen(new WheelSelectIconScreen(this, iconId -> {
            slot.setIconId(iconId);
            saveWheel();
        }));
    }

    private void saveWheel() {
        MacroUtil.setWheel(wheel);

        try {
            WheelFlow.writeWheel(wheel);
        } catch(IOException exception) {
            MacroMod.LOGGER.error("Failed to save wheel options", exception);
        }
    }

    @Override
    public void onClose() {
        if(parent == null) {
            super.onClose();
        } else {
            minecraft.setScreen(parent);
        }
    }

    private class SlotIconButton extends AbstractButton {

        private final WheelSlot slot;

        private SlotIconButton(int x, int y, int width, int height, WheelSlot slot) {
            super(x, y, width, height, Component.empty());
            this.slot = slot;
            setTooltip(Tooltip.create(iconActionText(slot)));
        }

        @Override
        public void onPress() {
            changeIcon(slot);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);

            ItemStack icon = getIcon(slot);
            if(icon.isEmpty()) {
                graphics.drawCenteredString(font, Component.literal("..."), getX() + getWidth() / 2, getY() + (getHeight() - font.lineHeight) / 2, 0xFFFFFFFF);
            } else {
                graphics.renderItem(icon, getX() + 2, getY() + 2);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private static Component iconActionText(WheelSlot slot) {
        return Component.translatable(slot.getIconId() == null ? "text.wheel.add.icon" : "text.wheel.change.icon");
    }

    private ItemStack getIcon(WheelSlot slot) {
        if(slot.getIconId() == null) return ItemStack.EMPTY;

        return BuiltInRegistries.ITEM.getOptional(slot.getIconId())
                .map(Item::getDefaultInstance)
                .orElse(ItemStack.EMPTY);
    }
}
