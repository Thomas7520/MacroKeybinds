package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class WheelSelectMacroScreen extends Screen {

    private final Screen parent;
    private final Consumer<IMacro> onMacroSelected;

    private MacroSelectionList macroList;
    private EditBox searchBox;

    public WheelSelectMacroScreen(Screen parent, Consumer<IMacro> onMacroSelected) {
        super(Component.translatable("text.wheel.select.macro"));
        this.parent = parent;
        this.onMacroSelected = Objects.requireNonNull(onMacroSelected);
    }

    @Override
    protected void init() {
        StringWidget titleWidget = new StringWidget(title, font);
        titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, 10);
        addRenderableOnly(titleWidget);

        List<IMacro> macros = MacroUtil.getGlobalKeybindsMap().values().stream()
                .filter(macro -> !isMacroAlreadyInWheel(macro))
                .sorted(Comparator.comparing(IMacro::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        macroList = new MacroSelectionList(minecraft, width, height - 73, 43, macros);
        addRenderableWidget(macroList);

        searchBox = new EditBox(font, width / 2 - 100, 20, 200, 18, searchBox, Component.translatable("text.searchbox.shadow"));
        searchBox.setResponder(macroList::update);
        addRenderableWidget(searchBox);

        macroList.update(searchBox.getValue());

        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 - 50, height - 27, 100, 20)
                .build());
    }

    private boolean isMacroAlreadyInWheel(IMacro macro) {
        if(MacroUtil.getWheel() == null) return false;

        return MacroUtil.getWheel().getMacros().stream()
                .filter(Objects::nonNull)
                .anyMatch(slot -> macro.getUUID().equals(slot.getMacroId()));
    }

    private void selectMacro(IMacro macro) {
        onMacroSelected.accept(macro);
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if(macroList.children().isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("text.wheel.no.macros"), width / 2, height / 2 - 5, 0xFFAAAAAA);
        }
    }

    private class MacroSelectionList extends ContainerObjectSelectionList<MacroEntry> {

        private final List<IMacro> macros;

        private MacroSelectionList(Minecraft minecraft, int width, int height, int y, List<IMacro> macros) {
            super(minecraft, width, WheelSelectMacroScreen.this.height, y, y + height, 22);
            this.macros = new ArrayList<>(macros);
            update("");
        }

        private void update(String search) {
            String normalizedSearch = search.strip().toLowerCase(Locale.ROOT);

            clearEntries();
            setScrollAmount(0);

            macros.stream()
                    .filter(macro -> macro.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                    .forEach(macro -> addEntry(new MacroEntry(macro)));
        }

        @Override
        public int getRowWidth() {
            return Math.min(260, width - 40);
        }
    }

    private class MacroEntry extends ContainerObjectSelectionList.Entry<MacroEntry> {

        private final Button selectButton;

        private MacroEntry(IMacro macro) {
            this.selectButton = Button.builder(Component.literal(macro.getName()), button -> selectMacro(macro))
                    .bounds(0, 0, 240, 20)
                    .build();
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float partialTick) {
            selectButton.setPosition(left, top);
            selectButton.setWidth(width);
            selectButton.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(selectButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(selectButton);
        }
    }
}
