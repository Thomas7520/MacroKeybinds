package com.thomas7520.macrokeybinds.gui.other;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OldImageButton extends Button {
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffTex;
    protected final int textureWidth;
    protected final int textureHeight;

    public OldImageButton(int p_169011_, int p_169012_, int p_169013_, int p_169014_, int p_169015_, int p_169016_, ResourceLocation p_169017_, OnPress p_169018_) {
        this(p_169011_, p_169012_, p_169013_, p_169014_, p_169015_, p_169016_, p_169014_, p_169017_, 256, 256, p_169018_);
    }

    public OldImageButton(int p_94269_, int p_94270_, int p_94271_, int p_94272_, int p_94273_, int p_94274_, int p_94275_, ResourceLocation p_94276_, OnPress p_94277_) {
        this(p_94269_, p_94270_, p_94271_, p_94272_, p_94273_, p_94274_, p_94275_, p_94276_, 256, 256, p_94277_);
    }

    public OldImageButton(int p_94230_, int p_94231_, int p_94232_, int p_94233_, int p_94234_, int p_94235_, int p_94236_, ResourceLocation p_94237_, int p_94238_, int p_94239_, OnPress p_94240_) {
        this(p_94230_, p_94231_, p_94232_, p_94233_, p_94234_, p_94235_, p_94236_, p_94237_, p_94238_, p_94239_, p_94240_, CommonComponents.EMPTY);
    }

    public OldImageButton(int p_94256_, int p_94257_, int p_94258_, int p_94259_, int p_94260_, int p_94261_, int p_94262_, ResourceLocation p_94263_, int p_94264_, int p_94265_, OnPress p_94266_, Component p_94267_) {
        super(p_94256_, p_94257_, p_94258_, p_94259_, p_94267_, p_94266_, DEFAULT_NARRATION);
        this.textureWidth = p_94264_;
        this.textureHeight = p_94265_;
        this.xTexStart = p_94260_;
        this.yTexStart = p_94261_;
        this.yDiffTex = p_94262_;
        this.resourceLocation = p_94263_;
    }

    public void renderWidget(GuiGraphics p_283502_, int p_281473_, int p_283021_, float p_282518_) {

        int i = yTexStart;
        if (!this.isActive()) {
            i = yTexStart + yDiffTex * 2;
        } else if (this.isHoveredOrFocused()) {
            i = yTexStart + yDiffTex;
        }

        RenderSystem.enableDepthTest();
        p_283502_.blit(this.resourceLocation, this.getX(), this.getY(), this.xTexStart, i, this.width, this.height, this.textureWidth, this.textureHeight);
    }
}