package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class MainMacroScreen extends Screen {


    private int guiLeft;
    private int guiTop;

    public MainMacroScreen() {
        super(new TextComponent("text.config.mainscreen"));
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        Component globalMacros = new TranslatableComponent("text.config.globalmacros");
        Component serverMacros = new TranslatableComponent("text.config.servermacros");
        Component discordLink = new TranslatableComponent("text.config.needhelp");

        addRenderableWidget(new Button(guiLeft - 100, guiTop / 2 - 35, 200, 20,globalMacros, p_93751_ -> {
            Minecraft.getInstance().setScreen(new GlobalMacroScreen(this, minecraft.options));
        }));

        addRenderableWidget(new Button(guiLeft - 100, guiTop / 2, 200, 20, serverMacros, p_93751_ -> {
            // todo open gui
        }));

        addRenderableWidget(new Button(guiLeft - 100, guiTop / 2 + 35, 200, 20, discordLink, p_93751_ -> {
            // todo open discord link

        }));




        super.init();
    }

    @Override
    public void render(PoseStack stack, int x, int y, float zFloat) {
        renderBackground(stack);

        super.render(stack, x, y, zFloat);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
