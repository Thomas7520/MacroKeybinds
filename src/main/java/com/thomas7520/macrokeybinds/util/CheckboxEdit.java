package com.thomas7520.macrokeybinds.util;


import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class CheckboxEdit extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = new ResourceLocation("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = new ResourceLocation("widget/checkbox");
    private static final int TEXT_COLOR = 14737632;
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final OnValueChange onValueChange;

    CheckboxEdit(int p_93826_, int p_93827_, Component p_93830_, Font p_312622_, boolean p_93831_, OnValueChange p_309427_) {
        super(p_93826_, p_93827_, boxSize(p_312622_) + 4 + p_312622_.width(p_93830_), boxSize(p_312622_), p_93830_);
        this.selected = p_93831_;
        this.onValueChange = p_309427_;
    }

    public static Builder builder(Component p_309446_, Font p_309998_) {
        return new Builder(p_309446_, p_309998_);
    }

    private static int boxSize(Font p_310239_) {
        return 9 + 8;
    }

    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    public void updateWidgetNarration(NarrationElementOutput p_260253_) {
        p_260253_.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                p_260253_.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    public void renderWidget(GuiGraphics p_283124_, int p_282925_, int p_282705_, float p_282612_) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font font = minecraft.font;
        p_283124_.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation resourcelocation;
        if (this.selected) {
            resourcelocation = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            resourcelocation = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int i = boxSize(font);
        int j = this.getX() + i + 4;
        int k = this.getY() + (this.height >> 1) - (9 >> 1);
        p_283124_.blitSprite(resourcelocation, this.getX(), this.getY(), i+3, i+3);
        p_283124_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        p_283124_.drawString(font, this.getMessage(), j, k, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int x = 0;
        private int y = 0;
        private OnValueChange onValueChange = OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component p_312515_, Font p_311430_) {
            this.message = p_312515_;
            this.font = p_311430_;
        }

        public Builder pos(int p_313014_, int p_311548_) {
            this.x = p_313014_;
            this.y = p_311548_;
            return this;
        }

        public Builder onValueChange(OnValueChange p_312502_) {
            this.onValueChange = p_312502_;
            return this;
        }

        public Builder selected(boolean p_310957_) {
            this.selected = p_310957_;
            this.option = null;
            return this;
        }

        public Builder selected(OptionInstance<Boolean> p_310610_) {
            this.option = p_310610_;
            this.selected = p_310610_.get();
            return this;
        }

        public Builder tooltip(Tooltip p_309712_) {
            this.tooltip = p_309712_;
            return this;
        }

        public CheckboxEdit build() {
            OnValueChange checkbox$onvaluechange = this.option == null ? this.onValueChange : (p_311135_, p_313032_) -> {
                this.option.set(p_313032_);
                this.onValueChange.onValueChange(p_311135_, p_313032_);
            };
            CheckboxEdit checkbox = new CheckboxEdit(this.x, this.y, this.message, this.font, this.selected, checkbox$onvaluechange);
            checkbox.setTooltip(this.tooltip);
            return checkbox;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        OnValueChange NOP = (p_310417_, p_311975_) -> {
        };

        void onValueChange(CheckboxEdit p_309925_, boolean p_310656_);
    }
}