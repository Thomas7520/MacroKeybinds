package com.thomas7520.macrokeybinds.gui;

import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;


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

        addRenderableWidget(createButton(globalMacros, guiLeft - 100, guiTop / 2, 200, 20, () -> new GlobalMacroScreen(this)));

        addRenderableWidget(serverMacrosButton = createButton(serverMacros, guiLeft - 100, guiTop / 2 + 35, 200, 20, () -> new ServerMacroScreen(this)));

        addRenderableWidget(createUrlButton(discordLink, guiLeft - 100, guiTop / 2 + 70, 200, 20, "https://discord.gg/xTqj3ZSeH4"));

        super.init();
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawString(font, title, width / 2 - font.width(title) / 2, 8, 16777215, false);

        if(serverMacrosButton.isHovered() && MacroUtil.getServerIP().isEmpty()) {
            context.renderTooltip(font, font.split(Component.translatable("text.tooltip.main.noserver"), 150), mouseX, mouseY);
        }

        if(serverMacrosButton.active && MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = false;
        }

        if(!serverMacrosButton.active && !MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = true;
        }
    }



    private Button createButton(Component text, int x, int y, int width, int height, Supplier<Screen> screenSupplier) {
        return Button.builder(text, button -> this.minecraft.setScreen(screenSupplier.get()))
                .bounds(x,y,width,height)
                .build();
    }

    private Button createUrlButton(Component text, int x, int y, int width, int height, String url) {
        return Button.builder(text, ConfirmLinkScreen.confirmLinkNow(this, url))
                .bounds(x,y,width,height)
                .build();
    }

    protected ClientTooltipPositioner createPositioner(boolean hovered, boolean focused, AbstractWidget focus) {
        if (!hovered && focused && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            return new DefaultTooltipPositioner();
        }

        return new BelowOrAboveWidgetTooltipPositioner(focus.getRectangle());
    }
}
