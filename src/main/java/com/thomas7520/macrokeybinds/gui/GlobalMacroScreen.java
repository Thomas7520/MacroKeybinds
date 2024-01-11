package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.gui.other.MacroList;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.ArrayList;

public class GlobalMacroScreen extends OptionsSubScreen {

    private MacroList macroList;
    private EditBox searchBox;

    public GlobalMacroScreen(Screen p_97519_, Options p_97520_) {
        super(p_97519_, p_97520_, new TranslatableComponent("text.globalmacros.title"));
    }

    protected void init() {
        this.macroList = new MacroList(this, this.minecraft, new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()), false);
        this.addWidget(this.macroList);

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 29, 150, 20, new TranslatableComponent("text.createmacro"), (p_97538_) -> {
            minecraft.setScreen(new EditMacroScreen(this, null, false));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 29, 150, 20, CommonComponents.GUI_DONE, (p_97535_) -> {
            this.minecraft.setScreen(this.lastScreen);
        }));

        this.searchBox = new EditBox(this.font, this.width / 2 - 100, 20, 200, 18, this.searchBox, new TranslatableComponent("selectWorld.search"));
        this.searchBox.setResponder((p_101362_) -> this.macroList.refreshList(() -> p_101362_, false));

        addWidget(searchBox);

    }

    public void render(PoseStack p_97530_, int p_97531_, int p_97532_, float p_97533_) {
        this.renderBackground(p_97530_);
        this.macroList.render(p_97530_, p_97531_, p_97532_, p_97533_);
        this.searchBox.render(p_97530_, p_97531_, p_97532_, p_97533_);
        drawCenteredString(p_97530_, this.font, this.title, this.width / 2, 8, 16777215);

        super.render(p_97530_, p_97531_, p_97532_, p_97533_);
    }
}
