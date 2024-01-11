package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class GlobalMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;

    public GlobalMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, Component.translatable("text.globalmacros.title"));
    }

    protected void init() {
        this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);
        this.addWidget(this.macroList);

        addRenderableWidget(Button.builder(Component.translatable("text.createmacro"), p_93751_ -> minecraft.setScreen(new EditMacroScreen(this, null, false)))
                .bounds(this.width / 2 - 155, this.height - 29, 150, 20)
                .build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p_93751_ -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 155 + 160, this.height - 29, 150, 20)
                .build());

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, this.searchBox, Component.translatable("selectWorld.search"));
        this.searchBox.setResponder((p_101362_) -> this.macroList.refreshList(() -> p_101362_, false));

        addWidget(searchBox);

    }


    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        this.renderBackground(p_281549_);
        this.macroList.render(p_281549_, p_281550_, p_282878_, p_282465_);
        this.searchBox.render(p_281549_, p_281550_, p_282878_, p_282465_);
        p_281549_.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(p_281549_, p_281550_, p_282878_, p_282465_);
    }


}
