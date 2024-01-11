package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class MainMacroScreen extends Screen {


    private int guiLeft;
    private int guiTop;
    private Button serverMacrosButton;

    public MainMacroScreen() {
        super(Component.translatable("text.config.mainscreen"));
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        Component globalMacros = Component.translatable("text.config.globalmacros");
        Component serverMacros = Component.translatable("text.config.servermacros");
        Component discordLink = Component.translatable("text.config.needhelp");

        addRenderableWidget(Button.builder(globalMacros, p_93751_ ->  Minecraft.getInstance().setScreen(new GlobalMacroScreen(this, minecraft.options)))
                .bounds(guiLeft - 100, guiTop / 2, 200, 20)
                .build());

        addRenderableWidget(serverMacrosButton = Button.builder(serverMacros, p_93751_ -> Minecraft.getInstance().setScreen(new ServerMacroScreen(this, minecraft.options)))
                .bounds(guiLeft - 100, guiTop / 2 + 35, 200, 20)
                .build());

        addRenderableWidget(Button.builder(discordLink, p_93751_ -> {
            this.minecraft.setScreen(new ConfirmLinkScreen((p_169337_) -> {
                if (p_169337_) {
                    Util.getPlatform().openUri("https://discord.gg/xTqj3ZSeH4");
                }

                this.minecraft.setScreen(this);
            }, "https://discord.gg/xTqj3ZSeH4", true));
        }).bounds(guiLeft - 100, guiTop / 2 + 70, 200, 20)
                .build());



        super.init();
    }


    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        renderBackground(p_281549_);

        p_281549_.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);

        if(serverMacrosButton.isHoveredOrFocused() && MacroUtil.getServerIP().isEmpty()) {
            p_281549_.renderTooltip(font, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.main.noserver").withStyle(ChatFormatting.RED), 150), p_281550_, p_282878_);
        }

        if(serverMacrosButton.active && MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = false;
        }

        if(!serverMacrosButton.active && !MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = true;
        }
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);
    }



    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
