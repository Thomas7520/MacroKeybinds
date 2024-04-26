package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GlobalMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;
    private Button stopMacroButton;

    public GlobalMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, Component.translatable("text.globalmacros.title"));
    }

    protected void init() {
        if(macroList == null) {
            this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);
        } else {
            macroList.updateList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            macroList.refreshList(() -> searchBox.getValue(), true);
            macroList.updateSize(width + 45, height, 43, height - 30);
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

        stopMacroButton = addRenderableWidget(new Button(searchBox.getX() - 25, searchBox.getY() - 1, 20, 20, Component.empty(), (p_97479_) -> {
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
            public void renderWidget(PoseStack p_268099_, int p_267992_, int p_267950_, float p_268076_) {

                RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                blitNineSliced(p_268099_, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                RenderSystem.setShaderTexture(0, new ResourceLocation(MacroMod.MODID, "textures/stop_icon.png"));
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                blit(p_268099_, this.getX() + 2, this.getY() + 2, 0, 0, 0, 16, 16, 16, 16);
            }

            private int getTextureY() {
                int i = 1;
                if (!this.active) {
                    i = 0;
                } else if (this.isHoveredOrFocused()) {
                    i = 2;
                }

                return 46 + i * 20;
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


    public void render(PoseStack p_97530_, int p_97531_, int p_97532_, float p_97533_) {
        this.renderDirtBackground(p_97530_);
        this.macroList.render(p_97530_, p_97531_, p_97532_, p_97533_);

        drawCenteredString(p_97530_, this.font, this.title, this.width / 2, 8, 16777215);

        super.render(p_97530_, p_97531_, p_97532_, p_97533_);
    }
}
