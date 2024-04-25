package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class GlobalMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;

    public GlobalMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, Component.translatable("text.globalmacros.title"));
    }

    protected void init() {
        double scrollAmount = macroList == null ? 0 : macroList.getScrollAmount();

        this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);

        if(searchBox != null) {
            this.macroList.refreshList(() -> searchBox.getValue(), true);
        }

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder((p_101362_) -> this.macroList.refreshList(() -> p_101362_, true));


        addRenderableWidget(searchBox);
        this.addWidget(this.macroList);

        addRenderableWidget(Button.builder(Component.translatable("text.createmacro"), p_93751_ -> minecraft.setScreen(new EditMacroScreen(this, null, false)))
                .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p_93751_ -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                .build());

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
