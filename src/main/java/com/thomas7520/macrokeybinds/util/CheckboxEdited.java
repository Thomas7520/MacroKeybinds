package com.thomas7520.macrokeybinds.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

@Environment(value= EnvType.CLIENT)
public class CheckboxEdited
        extends PressableWidget {

    private static final Identifier TEXTURE = new Identifier("textures/gui/checkbox.png");
    private static final int TEXT_COLOR = 0xE0E0E0;
    private static final int field_47105 = 4;
    private static final int field_47106 = 8;
    private boolean checked;
    private final CheckboxEdited.Callback callback;

    CheckboxEdited(int x, int y, Text message, TextRenderer textRenderer, boolean checked, CheckboxEdited.Callback callback) {
        super(x, y, CheckboxEdited.getSize(textRenderer) + 4 + textRenderer.getWidth(message), CheckboxEdited.getSize(textRenderer), message);
        this.checked = checked;
        this.callback = callback;
    }

    public static CheckboxEdited.Builder builder(Text text, TextRenderer textRenderer) {
        return new CheckboxEdited.Builder(text, textRenderer);
    }

    private static int getSize(TextRenderer textRenderer) {
        return 9+8;
    }

    @Override
    public void onPress() {
        this.checked = !this.checked;
        this.callback.onValueChange(this, this.checked);
    }

    public boolean isChecked() {
        return this.checked;
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) {
        builder.put(NarrationPart.TITLE, (Text)this.getNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.checkbox.usage.focused"));
            } else {
                builder.put(NarrationPart.USAGE, (Text)Text.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        RenderSystem.enableDepthTest();
        TextRenderer textRenderer = minecraftClient.textRenderer;
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        RenderSystem.enableBlend();
        int i = CheckboxEdited.getSize(textRenderer) + 3;
        int j = this.getX() + i + 4;
        int k = this.getY() + (this.height >> 1) - (textRenderer.fontHeight >> 1);

        context.drawTexture(TEXTURE, this.getX(), this.getY(), this.isHovered() ? 20.0F : 0.0F, this.checked ? 20.0F : 0.0F, i,i, 64, 64);

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.drawTextWithShadow(textRenderer, this.getMessage(), j, k, 0xE0E0E0 | MathHelper.ceil(this.alpha * 255.0f) << 24);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Callback {
        public static final CheckboxEdited.Callback EMPTY = (checkbox, checked) -> {};

        public void onValueChange(CheckboxEdited var1, boolean var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Text message;
        private final TextRenderer textRenderer;
        private int x = 0;
        private int y = 0;
        private CheckboxEdited.Callback callback = CheckboxEdited.Callback.EMPTY;
        private boolean checked = false;
        @Nullable
        private SimpleOption<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Text message, TextRenderer textRenderer) {
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

        public CheckboxEdited.Builder option(SimpleOption<Boolean> option) {
            this.option = option;
            this.checked = option.getValue();
            return this;
        }

        public CheckboxEdited.Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public CheckboxEdited build() {
            CheckboxEdited.Callback callback = this.option == null ? this.callback : (checkbox, checked) -> {
                this.option.setValue(checked);
                this.callback.onValueChange(checkbox, checked);
            };
            CheckboxEdited checkboxWidget = new CheckboxEdited(this.x, this.y, this.message, this.textRenderer, this.checked, callback);
            checkboxWidget.setTooltip(this.tooltip);
            return checkboxWidget;
        }
    }
}