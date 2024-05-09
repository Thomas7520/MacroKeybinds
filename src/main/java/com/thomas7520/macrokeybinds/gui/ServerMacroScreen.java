package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.ButtonImageWidget;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.IconWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ServerMacroScreen extends Screen {

    public static final Identifier STOP_ICON = new Identifier(MacroMod.MODID, "textures/stop_icon.png");
    private final Screen parent;
    private MacroList macroList;
    private TextFieldWidget searchBox;
    private ButtonWidget stopMacroButton;

    public ServerMacroScreen(Screen parent) {
        super(Text.translatable("text.servermacros.title"));

        this.parent = parent;
    }

    public void init() {
        double scrollAmount = 0;

        if(macroList != null) {
            scrollAmount = macroList.getScrollAmount();
        }

        this.macroList = new MacroList(this, client, new ArrayList<>(MacroUtil.getServerKeybinds().values()), true);

        if(searchBox != null) {
            macroList.updateList(new ArrayList<>(MacroUtil.getServerKeybinds().values()));
            macroList.update(() -> searchBox.getText(), false);
            macroList.setScrollAmount(scrollAmount);
        }

        this.addDrawableChild(this.macroList);

        addDrawableChild(ButtonWidget.builder(Text.translatable("text.createmacro"), button -> client.setScreen(new EditMacroScreen(this, null, true)))
                .dimensions(this.width / 2 - 155, this.height - 25, 150, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, button -> client.setScreen(parent))
                .dimensions(this.width / 2 - 155 + 160, this.height - 25, 150, 20)
                .build());

        this.searchBox = new TextFieldWidget(textRenderer, this.width / 2 - 100, 20, 200, 18, this.searchBox, Text.translatable("text.searchbox.shadow"));


        this.searchBox.setChangedListener((p_101362_) -> this.macroList.update(() -> p_101362_, true));


        addDrawableChild(searchBox);

        stopMacroButton = addDrawableChild(new ButtonWidget(searchBox.getX() - 25, searchBox.getY() - 1, 20, 20, Text.empty(), button -> {
            for (IMacro macro : MacroUtil.getServerKeybinds().values()) {
                if(macro instanceof DelayedMacro delayedMacro) {
                    if(delayedMacro.isStart()) {
                        delayedMacro.setStart(false);
                    }
                }

                if(macro instanceof RepeatMacro repeatMacro) {
                    if(repeatMacro.isRepeat()) {
                        repeatMacro.setRepeat(false);
                    }
                }
            }
            stopMacroButton.active = false;
        }, Supplier::get) {

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
                int i = 16;
                int j = 16;
                super.renderWidget(context, mouseX, mouseY, delta);
                context.drawTexture(STOP_ICON, this.getX() + 2, this.getY() + 2, 0.0F, 0.0F, i, j, i, j);
            }
        });


        stopMacroButton.setTooltip(Tooltip.of(Text.translatable("text.stopmacro")));

        stopMacroButton.active = false;

        for (IMacro macro : MacroUtil.getServerKeybinds().values()) {
            if(macro instanceof DelayedMacro delayedMacro) {
                if(delayedMacro.isStart()) {
                    stopMacroButton.active = true;
                    break;
                }
            }

            if(macro instanceof RepeatMacro repeatMacro) {
                if(repeatMacro.isRepeat()) {
                    stopMacroButton.active = true;
                    break;
                }
            }
        }
    }

    @Override
    public void tick() {

        if(stopMacroButton.active) return;

        for (IMacro macro : MacroUtil.getServerKeybinds().values()) {
            if(macro instanceof DelayedMacro delayedMacro) {
                if(delayedMacro.isStart()) {
                    stopMacroButton.active = true;
                    break;
                }
            }

            if(macro instanceof RepeatMacro repeatMacro) {
                if(repeatMacro.isRepeat()) {
                    stopMacroButton.active = true;
                    break;
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, this.title, this.width / 2 - textRenderer.getWidth(title) / 2, 8, 16777215, false);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackgroundTexture(context);
    }
}
