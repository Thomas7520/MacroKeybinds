package com.thomas7520.macrokeybinds.util.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import javax.annotation.Nullable;

public class CheckboxEdited
        extends AbstractButton {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int field_47105 = 4;
    private static final int field_47106 = 8;
    private boolean checked;
    private final CheckboxEdited.Callback callback;

    CheckboxEdited(int x, int y, Component message, Font textRenderer, boolean checked, CheckboxEdited.Callback callback) {
        super(x, y, CheckboxEdited.getSize(textRenderer) + 4 + textRenderer.width(message), CheckboxEdited.getSize(textRenderer), message);
        this.checked = checked;
        this.callback = callback;
    }

    public static CheckboxEdited.Builder builder(Component text, Font textRenderer) {
        return new CheckboxEdited.Builder(text, textRenderer);
    }

    private static int getSize(Font textRenderer) {
        return 9+8;
    }



    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {
        builder.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }


    @Override
    public void onPress() {
        this.checked = !this.checked;
        this.callback.onValueChange(this, this.checked);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        Minecraft minecraftClient = Minecraft.getInstance();
        Font textRenderer = minecraftClient.font;
        int i = CheckboxEdited.getSize(textRenderer) + 3;
        int j = this.getX() + i + 4;
        int k = this.getY() + (this.height >> 1) - (textRenderer.lineHeight >> 1);

        context.blit(TEXTURE, this.getX(), this.getY(), this.isHovered() ? 20.0F : 0.0F,
                this.checked ? 20.0F : 0.0F, i, i, 64, 64);
        context.drawString(textRenderer, this.getMessage(), j, k, 0xE0E0E0 | Mth.ceil(this.alpha * 255.0f) << 24);
    }



    public static interface Callback {
        public static final CheckboxEdited.Callback EMPTY = (checkbox, checked) -> {};

        public void onValueChange(CheckboxEdited var1, boolean var2);
    }

    public static class Builder {
        private final Component message;
        private final Font textRenderer;
        private int x = 0;
        private int y = 0;
        private CheckboxEdited.Callback callback = CheckboxEdited.Callback.EMPTY;
        private boolean checked = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component message, Font textRenderer) {
            this.message = message;
            this.textRenderer = textRenderer;
        }

        public CheckboxEdited.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public CheckboxEdited.Builder callback(CheckboxEdited.Callback callback) {
            this.callback = callback;
            return this;
        }

        public CheckboxEdited.Builder checked(boolean checked) {
            this.checked = checked;
            this.option = null;
            return this;
        }

        public CheckboxEdited.Builder option(OptionInstance<Boolean> option) {
            this.option = option;
            this.checked = option.get();
            return this;
        }

        public CheckboxEdited.Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CheckboxEdited build() {
            CheckboxEdited.Callback callback = this.option == null ? this.callback : (checkbox, checked) -> {
                this.option.set(checked);
                this.callback.onValueChange(checkbox, checked);
            };
            CheckboxEdited checkboxWidget = new CheckboxEdited(this.x, this.y, this.message, this.textRenderer, this.checked, callback);
            checkboxWidget.setTooltip(this.tooltip);
            return checkboxWidget;
        }
    }
}
