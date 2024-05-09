package com.thomas7520.macrokeybinds.gui;

import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Supplier;


public class MainMacroScreen extends Screen {


    private int guiLeft;
    private int guiTop;
    private ButtonWidget serverMacrosButton;

    public MainMacroScreen() {
        super(Text.translatable("text.config.mainscreen"));
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        Text globalMacros = Text.translatable("text.config.globalmacros");
        Text serverMacros = Text.translatable("text.config.servermacros");
        Text discordLink = Text.translatable("text.config.needhelp");

        addDrawableChild(createButton(globalMacros, guiLeft - 100, guiTop / 2, 200, 20, () -> new GlobalMacroScreen(this)));

        addDrawableChild(serverMacrosButton = createButton(serverMacros, guiLeft - 100, guiTop / 2 + 35, 200, 20, () -> new ServerMacroScreen(this)));

        addDrawableChild(createUrlButton(discordLink, guiLeft - 100, guiTop / 2 + 70, 200, 20, "https://discord.gg/xTqj3ZSeH4"));




        super.init();
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);

        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, title, width / 2 - textRenderer.getWidth(title) / 2, 8, 16777215, false);

        if(serverMacrosButton.isHovered() && MacroUtil.getServerIP().isEmpty()) {
            context.drawTooltip(textRenderer, textRenderer.wrapLines(Text.translatable("text.tooltip.main.noserver"), 150), createPositioner(true,false, serverMacrosButton), mouseX, mouseY);
        }

        if(serverMacrosButton.active && MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = false;
        }

        if(!serverMacrosButton.active && !MacroUtil.getServerIP().isEmpty()) {
            serverMacrosButton.active = true;
        }
    }



    private ButtonWidget createButton(Text text, int x, int y, int width, int height, Supplier<Screen> screenSupplier) {
        return ButtonWidget.builder(text, button -> this.client.setScreen(screenSupplier.get()))
                .dimensions(x,y,width,height)
                .build();
    }

    private ButtonWidget createUrlButton(Text text, int x, int y, int width, int height, String url) {
        return ButtonWidget.builder(text, ConfirmLinkScreen.opening(url, this, true))
                .dimensions(x,y,width,height)
                .build();
    }

    protected TooltipPositioner createPositioner(boolean hovered, boolean focused, ClickableWidget focus) {
        if (!hovered && focused && MinecraftClient.getInstance().getNavigationType().isKeyboard()) {
            return new FocusedTooltipPositioner(focus);
        }
        return new WidgetTooltipPositioner(focus);
    }
}
