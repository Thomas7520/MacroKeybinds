package com.thomas7520.macrokeybinds.util.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

import java.util.function.Supplier;

public class ButtonImageWidget
        extends AbstractButton {
    public static final int DEFAULT_WIDTH_SMALL = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int field_46856 = 8;
    protected static final ButtonImageWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableComponent)textSupplier.get();
    protected final ButtonImageWidget.PressAction onPress;
    protected final ButtonImageWidget.NarrationSupplier narrationSupplier;

    private final ResourceLocation icon;

    public static ButtonImageWidget.Builder builder(Component message, ButtonImageWidget.PressAction onPress) {
        return new ButtonImageWidget.Builder(message, onPress);
    }

    protected ButtonImageWidget(int x, int y, int width, int height, Component message, ButtonImageWidget.PressAction onPress, ButtonImageWidget.NarrationSupplier narrationSupplier, ResourceLocation icon) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = narrationSupplier;
        this.icon = icon;
    }



    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationSupplier.createNarrationMessage(super::createNarrationMessage);
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput builder) {
        this.defaultButtonNarrationText(builder);
    }


    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.renderWidget(context, mouseX, mouseY, delta);

        if (icon != null) {
            int i = 0;
            if (isHovered()) {
                i = getHeight();
            }

            context.setColor(1.0F, 1.0F, 1.0F, this.alpha);
            context.blit(icon, this.getX(), this.getY(), 0, (float)i, getWidth(), getHeight(), 256, 256);
            context.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static class Builder {
        private final Component message;
        private final ButtonImageWidget.PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private ButtonImageWidget.NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;
        private ResourceLocation icon;

        public Builder(Component message, ButtonImageWidget.PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public ButtonImageWidget.Builder position(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public ButtonImageWidget.Builder width(int width) {
            this.width = width;
            return this;
        }

        public ButtonImageWidget.Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public ButtonImageWidget.Builder dimensions(int x, int y, int width, int height) {
            return this.position(x, y).size(width, height);
        }

        public ButtonImageWidget.Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public ButtonImageWidget.Builder tooltip(@Nullable Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public ButtonImageWidget.Builder narrationSupplier(ButtonImageWidget.NarrationSupplier narrationSupplier) {
            this.narrationSupplier = narrationSupplier;
            return this;
        }

        public ButtonImageWidget build() {
            ButtonImageWidget buttonWidget = new ButtonImageWidget(this.x, this.y, this.width, this.height, this.message, this.onPress, this.narrationSupplier, this.icon);
            buttonWidget.setTooltip(this.tooltip);
            return buttonWidget;
        }
    }

    public static interface PressAction {
        public void onPress(ButtonImageWidget var1);
    }

    public static interface NarrationSupplier {
        public MutableComponent createNarrationMessage(Supplier<MutableComponent> var1);
    }
}
