package com.thomas7520.macrokeybinds.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

@Environment(value= EnvType.CLIENT)
public class ButtonImageWidget
        extends PressableWidget {
    public static final int DEFAULT_WIDTH_SMALL = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int field_46856 = 8;
    protected static final ButtonImageWidget.NarrationSupplier DEFAULT_NARRATION_SUPPLIER = textSupplier -> (MutableText)textSupplier.get();
    protected final ButtonImageWidget.PressAction onPress;
    protected final ButtonImageWidget.NarrationSupplier narrationSupplier;

    private final Identifier icon;

    public static ButtonImageWidget.Builder builder(Text message, ButtonImageWidget.PressAction onPress) {
        return new ButtonImageWidget.Builder(message, onPress);
    }

    protected ButtonImageWidget(int x, int y, int width, int height, Text message, ButtonImageWidget.PressAction onPress, ButtonImageWidget.NarrationSupplier narrationSupplier, Identifier icon) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.narrationSupplier = narrationSupplier;
        this.icon = icon;
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }

    @Override
    protected MutableText getNarrationMessage() {
        return this.narrationSupplier.createNarrationMessage(super::getNarrationMessage);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        this.appendDefaultNarrations(builder);
    }


    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        if(icon != null) {

            int i = 0;
            if(isHovered()) {
                i = getHeight();
            }
            context.drawTexture(icon, this.getX(), this.getY(),  0, i, getWidth(), getHeight());
        }
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.drawMessage(context, minecraftClient.textRenderer, i | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final ButtonImageWidget.PressAction onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private ButtonImageWidget.NarrationSupplier narrationSupplier = DEFAULT_NARRATION_SUPPLIER;
        private Identifier icon;

        public Builder(Text message, ButtonImageWidget.PressAction onPress) {
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

        public ButtonImageWidget.Builder icon(Identifier icon) {
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

    @Environment(value=EnvType.CLIENT)
    public static interface PressAction {
        public void onPress(ButtonImageWidget var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface NarrationSupplier {
        public MutableText createNarrationMessage(Supplier<MutableText> var1);
    }
}
