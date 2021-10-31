package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.EditMacroScreen;
import com.thomas7520.macrokeybinds.gui.GlobalMacroScreen;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class MacroList extends ContainerObjectSelectionList<MacroList.Entry> {

    final GlobalMacroScreen macroScreen;
    int maxNameWidth;

    public MacroList(GlobalMacroScreen p_97399_, Minecraft p_97400_) {
        super(p_97400_, p_97399_.width + 45, p_97399_.height, 43, p_97399_.height - 30, 20);
        this.macroScreen = p_97399_;

        MacroUtil.getGlobalKeybindsMap().keySet().forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen)));

    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }



    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<MacroList.Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends MacroList.Entry {
        private final IMacro macro;
        private final ImageButton editButton;
        private final Checkbox stateButton;
        private final ImageButton deleteButton;

        KeyEntry(final IMacro p_97451_, final Screen lastScreen) {

            this.macro = p_97451_;

            this.editButton = new ImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/edit_button.png"), (p_97479_) -> {
                MacroList.this.minecraft.setScreen(new EditMacroScreen(MacroList.this.macroScreen, macro));
                System.out.println("tt");
            });

            this.stateButton = new Checkbox(0, 0, 20 , 20, new TextComponent(""), true) {
                @Override
                public void onPress() {

                    super.onPress();
                }
            };

            this.deleteButton = new ImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/delete_button.png"), (p_97479_) -> {
                MacroList.this.minecraft.setScreen(new ConfirmScreen((p_170322_)-> {

                    if (p_170322_) {
                        MacroUtil.getGlobalKeybindsMap().remove(macro);
                    }

                    MacroList.this.minecraft.setScreen(lastScreen);
                }, new TranslatableComponent("text.macro.deleteQuestion"), new TranslatableComponent("text.macro.deleteWarning"), new TranslatableComponent("text.macro.deleteButton"), CommonComponents.GUI_CANCEL));
            });
        }

        public void render(PoseStack p_97463_, int p_97464_, int p_97465_, int p_97466_, int p_97467_, int p_97468_, int p_97469_, int p_97470_, boolean p_97471_, float p_97472_) {
            float f = (float) (p_97466_ - MacroList.this.maxNameWidth);
            MacroList.this.minecraft.font.draw(p_97463_, this.macro.getName(), f, (float) (p_97465_ + p_97468_ / 2 - 9 / 2), 16777215);
            this.deleteButton.x = p_97466_ + 190 + 20;
            this.deleteButton.y = p_97465_;
            this.editButton.x = p_97466_ + 190;
            this.editButton.y = p_97465_;
            this.stateButton.x = p_97466_ + 170;
            this.stateButton.y = p_97465_;
            this.editButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
            this.stateButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
            this.deleteButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
        }

        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.stateButton, this.deleteButton, this.editButton);
        }

        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.stateButton, this.deleteButton, this.editButton);
        }

        public boolean mouseClicked(double p_97459_, double p_97460_, int p_97461_) {
            if (this.stateButton.mouseClicked(p_97459_, p_97460_, p_97461_)) {
                return true;
            } else if(this.deleteButton.mouseClicked(p_97459_, p_97460_, p_97461_)){
                return true;
            } else {
                return this.editButton.mouseClicked(p_97459_, p_97460_, p_97461_);
            }
        }

        public boolean mouseReleased(double p_97481_, double p_97482_, int p_97483_) {
            return this.stateButton.mouseReleased(p_97481_, p_97482_, p_97483_) || this.deleteButton.mouseReleased(p_97481_, p_97482_, p_97483_) || this.editButton.mouseReleased(p_97481_,p_97482_,p_97483_);
        }
    }
}
