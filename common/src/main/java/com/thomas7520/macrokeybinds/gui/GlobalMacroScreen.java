package com.thomas7520.macrokeybinds.gui;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.object.macro.CountedRepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.DelayedMacro;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.RepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.ToggleMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.function.Supplier;

public class GlobalMacroScreen extends Screen {

    public static final ResourceLocation STOP_ICON = new ResourceLocation(MacroMod.MODID, "textures/stop_icon.png");
    private final Screen parent;
    private MacroList macroList;
    private EditBox searchBox;
    private Button stopMacroButton;

    public GlobalMacroScreen(Screen parent) {
        super(Component.translatable("text.globalmacros.title"));

        this.parent = parent;
    }

    public void init() {
        double scrollAmount = 0;

        if(macroList != null) {
            scrollAmount = macroList.getScrollAmount();
        }

        this.macroList = new MacroList(this, minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);

        if(searchBox != null) {
            macroList.updateList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            macroList.update(() -> searchBox.getValue(), false);
            macroList.setScrollAmount(scrollAmount);
        }

        this.addRenderableWidget(this.macroList);

        addRenderableWidget(Button.builder(Component.translatable("text.createmacro"), button -> minecraft.setScreen(new EditMacroScreen(this, null, false)))
                .bounds(this.width / 2 - 155, this.height - 25, 150, 20)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> minecraft.setScreen(parent))
                .bounds(this.width / 2 - 155 + 160, this.height - 25, 150, 20)
                .build());

        this.searchBox = new EditBox(font, this.width / 2 - 100, 20, 200, 18, this.searchBox, Component.translatable("text.searchbox.shadow"));


        this.searchBox.setResponder((p_101362_) -> this.macroList.update(() -> p_101362_, true));


        addRenderableWidget(searchBox);

        stopMacroButton = addRenderableWidget(new Button(searchBox.getX() - 25, searchBox.getY() - 1, 20, 20, Component.empty(), button -> {
            for (IMacro macro : MacroUtil.getGlobalKeybindsMap().values()) {
                if(macro instanceof DelayedMacro delayedMacro) {
                    if(delayedMacro.isStart()) {
                        delayedMacro.setStart(false);
                    }
                }

                if(macro instanceof ToggleMacro toggleMacro) {
                    if(toggleMacro.isToggled()) {
                        toggleMacro.setToggled(false);
                    }
                }

                if(macro instanceof CountedRepeatMacro countedRepeatMacro) {
                    countedRepeatMacro.cancel();
                }

                if(macro instanceof RepeatMacro repeatMacro) {
                    repeatMacro.setRepeat(false);
                }
            }
            stopMacroButton.active = false;
        }, Supplier::get) {

            @Override
            protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
                super.renderWidget(context, mouseX, mouseY, delta);
                int i = 16;
                int j = 16;
                context.blit(STOP_ICON, this.getX() + 2, this.getY() + 2, 0.0F, 0.0F, i, j, i, j);
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

            if(macro instanceof ToggleMacro toggleMacro) {
                if(toggleMacro.isToggled()) {
                    stopMacroButton.active = true;
                    break;
                }
            }

            if(macro instanceof CountedRepeatMacro countedRepeatMacro) {
                if(countedRepeatMacro.isRunning()) {
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
        stopMacroButton.active = false;

        for (IMacro macro : MacroUtil.getGlobalKeybindsMap().values()) {
            if(macro instanceof DelayedMacro delayedMacro) {
                if(delayedMacro.isStart()) {
                    stopMacroButton.active = true;
                    break;
                }
            }

            if(macro instanceof ToggleMacro toggleMacro) {
                if(toggleMacro.isToggled()) {
                    stopMacroButton.active = true;
                    break;
                }
            }

            if(macro instanceof CountedRepeatMacro countedRepeatMacro) {
                if(countedRepeatMacro.isRunning()) {
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
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawString(font, this.title, this.width / 2 - font.width(title) / 2, 8, 16777215, false);
    }


}
