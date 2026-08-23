package com.thomas7520.macrokeybinds.gui;

import com.thomas7520.macrokeybinds.gui.wheel.WheelOptionScreen;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;


public class MainMacroScreen extends Screen {


    private final Screen parent;
    private int guiLeft;
    private int guiTop;
    private Button serverMacrosButton;

    public MainMacroScreen() {
        this(null);
    }

    public MainMacroScreen(Screen parent) {
        super(Component.translatable("text.config.mainscreen"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        Component globalMacros = Component.translatable("text.config.globalmacros");
        Component serverMacros = Component.translatable("text.config.servermacros");
        Component wheelOptions = Component.translatable("text.config.wheeloptions");
        Component discordLink = Component.translatable("text.config.needhelp");

        StringWidget titleWidget = new StringWidget(title, font);
        titleWidget.setPosition(width / 2 - titleWidget.getWidth() / 2, 8);
        addRenderableWidget(titleWidget);

        addRenderableWidget(createButton(globalMacros, guiLeft - 100, guiTop / 2, 200, 20, () -> new GlobalMacroScreen(this)));

        addRenderableWidget(serverMacrosButton = createButton(serverMacros, guiLeft - 100, guiTop / 2 + 35, 200, 20, () -> new ServerMacroScreen(this)));

        serverMacrosButton.active = !MacroUtil.getServerIP().isEmpty();

        Button wheelOptionsButton = createButton(wheelOptions, guiLeft - 100, guiTop / 2 + 70, 200, 20,
                () -> new WheelOptionScreen(this));

        wheelOptionsButton.active = minecraft.level != null;
        if(!wheelOptionsButton.active) {
            wheelOptionsButton.setTooltip(Tooltip.create(Component.translatable("text.tooltip.main.wheel.no_world")));
        }
        addRenderableWidget(wheelOptionsButton);

        addRenderableWidget(createUrlButton(discordLink, guiLeft - 100, guiTop / 2 + 105, 200, 20, "https://discord.gg/xTqj3ZSeH4"));

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(guiLeft - 100, height - 27, 200, 20)
                .build());

        super.init();
    }


    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (parent != null) {
            this.renderBackground(context);
        }

        super.render(context, mouseX, mouseY, delta);

        if(serverMacrosButton.isHovered() && MacroUtil.getServerIP().isEmpty()) {
            this.setTooltipForNextRenderPass(font.split(Component.translatable("text.tooltip.main.noserver"), 150));
        }
    }



    private Button createButton(Component text, int x, int y, int width, int height, Supplier<Screen> screenSupplier) {
        return Button.builder(text, button -> this.minecraft.setScreen(screenSupplier.get()))
                .bounds(x,y,width,height)
                .build();
    }

    private Button createUrlButton(Component text, int x, int y, int width, int height, String url) {
        return Button.builder(text, button -> ConfirmLinkScreen.confirmLinkNow(url, this, true))
                .bounds(x,y,width,height)
                .build();
    }

    @Override
    public void onClose() {
        if(parent == null) {
            super.onClose();
            return;
        }

        minecraft.setScreen(parent);
    }

    protected ClientTooltipPositioner createPositioner(boolean hovered, boolean focused, AbstractWidget focus) {
        if (!hovered && focused && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            return DefaultTooltipPositioner.INSTANCE;
        }

        return new BelowOrAboveWidgetTooltipPositioner(focus);
    }
}
