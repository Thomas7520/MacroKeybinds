package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class EditMacroFormList extends ContainerObjectSelectionList<EditMacroFormList.Entry> {

    private static final int FORM_WIDTH = 310;
    private static final int ROW_HEIGHT = 40;
    private static final int LABEL_COLOR = 0xFFE0E0E0;

    private final Screen screen;

    public EditMacroFormList(Screen screen, Minecraft client) {
        super(client, screen.width, screen.height, 30, screen.height - 32, ROW_HEIGHT);
        this.screen = screen;
        this.centerListVertically = false;
    }

    public void clearRows() {
        clearEntries();
    }

    public void addRow(Field... fields) {
        addEntry(new Entry(screen, fields));
    }

    @Override
    public int getRowWidth() {
        return FORM_WIDTH;
    }

    @Override
    protected int getScrollbarPosition() {
        return screen.width / 2 + FORM_WIDTH / 2 + 6;
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        Entry previousFocused = getFocused();
        if(previousFocused != focused && previousFocused != null) {
            previousFocused.setFocused(null);
        }
        super.setFocused(focused);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if(!focused) setFocused((GuiEventListener) null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Entry entry = getEntryAtPosition(mouseX, mouseY);
        boolean clickedWidget = entry != null && entry.children().stream()
                .anyMatch(widget -> widget.isMouseOver(mouseX, mouseY));
        boolean handled = super.mouseClicked(mouseX, mouseY, button);

        if(!clickedWidget) setFocused(null);

        return handled;
    }

    public record Field(Component label, AbstractWidget widget, int xOffset) {
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final Screen screen;
        private final List<Field> fields;
        private final List<AbstractWidget> widgets;

        public Entry(Screen screen, Field... fields) {
            this.screen = screen;
            this.fields = Arrays.asList(fields);
            this.widgets = Arrays.stream(fields).map(Field::widget).toList();
        }

        @Override
        public void render(GuiGraphics context, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int formLeft = screen.width / 2 - FORM_WIDTH / 2;
            int y = top;

            for(Field field : fields) {
                int x = formLeft + field.xOffset();
                context.drawString(Minecraft.getInstance().font, field.label(), x, y, LABEL_COLOR, false);
                field.widget().setPosition(x, y + 11);
                field.widget().render(context, mouseX, mouseY, tickDelta);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.copyOf(widgets);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.copyOf(widgets);
        }
    }
}
