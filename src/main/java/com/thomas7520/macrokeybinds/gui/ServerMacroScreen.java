package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;

public class ServerMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;
    private Button stopMacroButton;
    public ServerMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, new TranslatableComponent("text.servermacros.title"));
    }

    protected void init() {
        if(macroList == null) {
            this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getServerKeybinds().values()), true);
        } else {
            macroList.updateList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            macroList.updateSize(width + 45, height, 43, height - 30);
            macroList.setScrollAmount(macroList.getScrollAmount());
        }

        this.addWidget(this.macroList);

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("text.createmacro"), (p_97538_) -> {
            minecraft.setScreen(new EditMacroScreen(this, null, true));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (p_97535_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }));

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, this.searchBox, new TranslatableComponent("selectWorld.search"));
        this.searchBox.setResponder((p_101362_) -> this.macroList.refreshList(() -> p_101362_, false));

        addRenderableWidget(searchBox);

        stopMacroButton = addRenderableWidget(new Button(searchBox.x - 25, searchBox.y - 1, 20, 20, TextComponent.EMPTY, (p_97535_) -> {
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

        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, new TranslatableComponent("text.stopmacro"), p_93755_, p_93756_)) {

            @Override
            public void renderButton(PoseStack p_93746_, int p_93747_, int p_93748_, float p_93749_) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
                int i = this.getYImage(this.isHovered());
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                this.blit(p_93746_, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
                this.blit(p_93746_, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
                this.renderBg(p_93746_, minecraft, p_93747_, p_93748_);


                RenderSystem.setShaderTexture(0, new ResourceLocation(MacroMod.MODID, "textures/stop_icon.png"));
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                blit(p_93746_, x + 2, y + 2, 0, 0, 0, 16, 16, 16, 16);

                if (this.isHovered()) {
                    this.renderToolTip(p_93746_, p_93747_, p_93748_);
                }
            }

        });

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
        super.tick();
    }


    public void render(PoseStack p_97530_, int p_97531_, int p_97532_, float p_97533_) {
        this.renderBackground(p_97530_);
        this.macroList.render(p_97530_, p_97531_, p_97532_, p_97533_);
        drawCenteredString(p_97530_, this.font, this.title, this.width / 2, 8, 16777215);

        super.render(p_97530_, p_97531_, p_97532_, p_97533_);
    }
}