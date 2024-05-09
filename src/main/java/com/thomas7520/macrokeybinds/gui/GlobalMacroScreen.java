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
import net.minecraft.client.option.GameOptions;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Supplier;

public class GlobalMacroScreen extends GameOptionsScreen {

    public static final Identifier STOP_ICON = new Identifier(MacroMod.MODID, "textures/stop_icon.png");
    private MacroList macroList;
    private EditBoxWidget searchBox;
    private ButtonWidget stopMacroButton;

    public GlobalMacroScreen(Screen p_97519_, GameOptions p_97520_) {
        super(p_97519_, p_97520_, Text.translatable("text.globalmacros.title"));
    }

    public void init() {
        if(macroList == null) {
            this.macroList = new MacroList(this, client, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);
        } else {
            macroList.updateList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            macroList.update(() -> searchBox.getText(), true);
            macroList.updateSize(width + 45, 52, 40, height - 30);
            macroList.setScrollAmount(macroList.getScrollAmount());
        }

        this.addDrawableChild(this.macroList);

        addDrawableChild(createButton(Text.translatable("text.createmacro"), this.width / 2 - 155, this.height - 29, 150, 20, () -> new EditMacroScreen(this, null, false)));

        addDrawableChild(createButton(ScreenTexts.DONE, this.width / 2 - 155 + 160, this.height - 29, 150, 20, () -> parent));

        if(this.searchBox == null) {
            this.searchBox = new EditBoxWidget(textRenderer, this.width / 2 - 100, 20, 200, 18, Text.translatable("text.searchbox.shadow"), Text.empty());
        } else {
            searchBox.setPosition(this.width / 2 - 100, 20);
        }
        this.searchBox.setChangeListener((p_101362_) -> this.macroList.update(() -> p_101362_, false));


        addDrawableChild(searchBox);

        stopMacroButton = addDrawableChild(new ButtonWidget(searchBox.getX() - 25, searchBox.getY() - 1, 20, 20, Text.empty(), button -> {
            for (IMacro macro : MacroUtil.getGlobalKeybindsMap().values()) {
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
            public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                int i = 16;
                int j = 16;
                super.renderButton(context, mouseX, mouseY, delta);
                context.drawTexture(STOP_ICON, this.getX() + 2, this.getY() + 2, 0.0F, 0.0F, i, j, i, j);
            }
        });


        stopMacroButton.setTooltip(Tooltip.of(Text.translatable("text.stopmacro")));

        stopMacroButton.active = false;

        for (IMacro macro : MacroUtil.getGlobalKeybindsMap().values()) {
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
        searchBox.tick();

        if(stopMacroButton.active) return;

        for (IMacro macro : MacroUtil.getGlobalKeybindsMap().values()) {
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
        renderBackgroundTexture(context);


        this.macroList.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, this.title, this.width / 2 - textRenderer.getWidth(title) / 2, 8, 16777215, false);

    }


    private ButtonWidget createButton(Text text, int x, int y, int width, int height, Supplier<Screen> screenSupplier) {
        return ButtonWidget.builder(text, button -> this.client.setScreen(screenSupplier.get()))
                .dimensions(x,y,width,height)
                .build();
    }



}
