package com.thomas7520.macrokeybinds.gui.wheel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class WheelSelectIconScreen extends Screen {

    private static final int ICON_SIZE = 20;
    private static final int ICON_GAP = 4;

    private final Screen parent;
    private final Consumer<Identifier> onIconSelected;

    private IconButton selectedIcon;
    private IconButton hoveredIcon;
    private Button doneButton;

    public WheelSelectIconScreen(Screen parent, Consumer<Identifier> onIconSelected) {
        super(Component.translatable("text.wheel.select.icon"));
        this.parent = parent;
        this.onIconSelected = Objects.requireNonNull(onIconSelected);
    }

    @Override
    protected void init() {
        StringWidget titleWidget = new StringWidget(title, font);
        titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, 10);
        addRenderableWidget(titleWidget);

        addRenderableWidget(new IconGrid(minecraft, width, height - 100, 30));

        Component doneText = selectedIcon == null ? Component.translatable("text.wheel.remove.icon") : CommonComponents.GUI_DONE;

        doneButton = addRenderableWidget(Button.builder(doneText, button -> confirmSelection())
                .bounds(width / 2 - 102, height - 27, 100, 20)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 + 2, height - 27, 100, 20)
                .build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        hoveredIcon = null;
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        IconButton preview = hoveredIcon != null ? hoveredIcon : selectedIcon;
        if(preview == null) return;

        int previewY = height - 55;
        graphics.item(preview.stack, width / 2 - 60, previewY - 4);
        graphics.text(font, preview.stack.getHoverName(), width / 2 - 38, previewY, 0xFFFFFFFF);
    }

    private void selectIcon(IconButton icon) {
        selectedIcon = icon;
        doneButton.setMessage(CommonComponents.GUI_DONE);
    }

    private void confirmSelection() {
        onIconSelected.accept(selectedIcon == null ? null : selectedIcon.iconId);
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private class IconGrid extends ContainerObjectSelectionList<IconRow> {

        private final int columns;

        public IconGrid(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, ICON_SIZE + ICON_GAP);
            this.columns = Math.max(1, Math.min(12, (width - 40) / (ICON_SIZE + ICON_GAP)));

            List<IconButton> icons = BuiltInRegistries.ITEM.entrySet().stream()
                    .sorted(Comparator.comparing(entry -> entry.getKey().identifier()))
                    .map(entry -> new IconButton(entry.getValue().getDefaultInstance(), entry.getKey().identifier()))
                    .filter(icon -> !icon.stack.isEmpty())
                    .toList();

            for(int index = 0; index < icons.size(); index += columns) {
                int end = Math.min(index + columns, icons.size());
                addEntry(new IconRow(new ArrayList<>(icons.subList(index, end))));
            }
        }

        @Override
        public int getRowWidth() {
            return columns * (ICON_SIZE + ICON_GAP);
        }
    }

    private class IconRow extends ContainerObjectSelectionList.Entry<IconRow> {

        private final List<IconButton> icons;

        public IconRow(List<IconButton> icons) {
            this.icons = icons;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            for(int index = 0; index < icons.size(); index++) {
                IconButton icon = icons.get(index);
                icon.setPosition(getX() + index * (ICON_SIZE + ICON_GAP), getY() + 2);
                icon.extractRenderState(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return icons;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return icons;
        }
    }

    private class IconButton extends AbstractWidget {

        private final ItemStack stack;
        private final Identifier iconId;

        public IconButton(ItemStack stack, Identifier iconId) {
            super(0, 0, ICON_SIZE, ICON_SIZE, stack.getHoverName());
            this.stack = stack;
            this.iconId = iconId;
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            if(isHovered()) {
                hoveredIcon = this;
            }

            int backgroundColor = selectedIcon == this ? 0xA0FFFFFF : 0x60000000;
            int borderColor = selectedIcon == this ? 0xFFFFFF55 : isHovered() ? 0xFFFFFFFF : 0xFF777777;

            graphics.fill(getX(), getY(), getRight(), getBottom(), backgroundColor);
            graphics.outline(getX(), getY(), getWidth(), getHeight(), borderColor);
            graphics.item(stack, getX() + 2, getY() + 2);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            selectIcon(this);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
