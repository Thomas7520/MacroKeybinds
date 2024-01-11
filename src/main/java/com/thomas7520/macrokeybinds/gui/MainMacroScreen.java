package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class MainMacroScreen extends Screen {


    private int guiLeft;
    private int guiTop;
    private Button serverMacrosButton;

    public MainMacroScreen() {
        super(new TranslatableComponent("text.config.mainscreen"));
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        Component globalMacros = new TranslatableComponent("text.config.globalmacros");
        Component serverMacros = new TranslatableComponent("text.config.servermacros");
        Component discordLink = new TranslatableComponent("text.config.needhelp");

        addRenderableWidget(new Button(guiLeft - 100, guiTop / 2, 200, 20,globalMacros, p_93751_ ->
                Minecraft.getInstance().setScreen(new GlobalMacroScreen(this, minecraft.options))));

        addRenderableWidget(serverMacrosButton = new Button(guiLeft - 100, guiTop / 2 + 35, 200, 20, serverMacros, p_93751_ ->
                Minecraft.getInstance().setScreen(new ServerMacroScreen(this, minecraft.options))));

        addRenderableWidget(new Button(guiLeft - 100, guiTop / 2 + 70, 200, 20, discordLink, p_93751_ -> {
            this.minecraft.setScreen(new ConfirmLinkScreen((p_169337_) -> {
                if (p_169337_) {
                    Util.getPlatform().openUri("https://discord.gg/xTqj3ZSeH4");
                }

                this.minecraft.setScreen(this);
            }, "https://discord.gg/xTqj3ZSeH4", true));
        }));




        super.init();
    }

    @Override
    public void render(PoseStack stack, int x, int y, float zFloat) {
        renderBackground(stack);

        drawCenteredString(stack, this.font, this.title, this.width / 2, 8, 16777215);

        if(serverMacrosButton.isHoveredOrFocused() && MacroUtil.getServerIP().isEmpty()) {
            renderTooltip(stack, Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.main.noserver").withStyle(ChatFormatting.RED), 150), x, y);
        }

        if(serverMacrosButton.active && MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = false;
        }

        if(!serverMacrosButton.active && !MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = true;
        }

        super.render(stack, x, y, zFloat);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
