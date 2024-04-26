package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.gui.other.OldImageButton;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GlobalMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;
    private OldImageButton stopMacroButton;

    public GlobalMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, Component.translatable("text.globalmacros.title"));
    }

    protected void init() {
        if(macroList == null) {
            this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);
        } else {
            macroList.updateList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            macroList.refreshList(() -> searchBox.getValue(), true);
            macroList.setWidth(width + 45);
            macroList.setHeight(height - 52 - 33);
            macroList.setScrollAmount(macroList.getScrollAmount());
        }

        this.addWidget(this.macroList);

        addRenderableWidget(Button.builder(Component.translatable("text.createmacro"), p_93751_ -> minecraft.setScreen(new EditMacroScreen(this, null, false)))
                .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
                .build());


        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p_93751_ -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                .build());

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, this.searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder((p_101362_) -> this.macroList.refreshList(() -> p_101362_, false));

        addRenderableWidget(searchBox);

        if(!searchBox.getValue().isEmpty()) {
            this.macroList.refreshList(() -> searchBox.getValue(), false);
        }



        stopMacroButton = addRenderableWidget(new OldImageButton(searchBox.getX() - 25, searchBox.getY() - 1, 20, 20, 0, 0, 20, new ResourceLocation(MacroMod.MODID, "textures/stop_icon.png"), (p_97479_) -> {
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

        }) {
            @Override
            public void renderWidget(GuiGraphics p_283502_, int p_281473_, int p_283021_, float p_282518_) {
                p_283502_.setColor(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                p_283502_.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
                p_283502_.blit(this.resourceLocation, this.getX() + 2, this.getY() + 2, 0, 0, 16,16, 16,16);

                p_283502_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                super.renderWidget(p_283502_, p_281473_, p_283021_, p_282518_);
            }
        });

        stopMacroButton.setTooltip(Tooltip.create(Component.translatable("text.stopmacro")));

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
        super.tick();
    }

    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {

        this.renderDirtBackground(p_281549_);

        this.macroList.render(p_281549_, p_281550_, p_282878_, p_282465_);
        p_281549_.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);

        for(Renderable renderable : this.renderables) {
            renderable.render(p_281549_, p_281550_, p_282878_, p_282465_);
        }
    }
}