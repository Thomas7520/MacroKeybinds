package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.EditMacroScreen;
import com.thomas7520.macrokeybinds.gui.ServerMacroScreen;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class MacroList extends ContainerObjectSelectionList<MacroList.Entry> {

    final Screen macroScreen;
    private final boolean isServer;
    int maxNameWidth;

    @Nullable
    private List<IMacro> cachedList;

    private List<IMacro> macroList;

    public MacroList(Screen p_97399_, Minecraft p_97400_, List<IMacro> macros, boolean isServer) {
        super(p_97400_, p_97399_.width + 45, p_97399_.height, 43, p_97399_.height - 30, 20);
        this.macroScreen = p_97399_;


        macros.sort(Comparator.comparingLong(IMacro::getCreatedTime));

        macroList = macros;
        this.isServer = isServer;
        macros.forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen, isServer)));

    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public void refreshList(Supplier<String> p_101677_, boolean p_101678_) {
        this.clearEntries();
   //     LevelStorageSource levelstoragesource = this.minecraft.getLevelSource();
        if (this.cachedList == null || p_101678_) {
            this.cachedList = macroList;


            macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        }

        if (this.cachedList.isEmpty()) {
            //this.minecraft.setScreen(CreateWorldScreen.create(null));
        } else {
            String s = p_101677_.get().toLowerCase(Locale.ROOT);

            for (IMacro macro : this.cachedList) {
                if (macro.getName().toLowerCase(Locale.ROOT).contains(s) || macro.getName().toLowerCase(Locale.ROOT).contains(s)) {
                    this.addEntry(new KeyEntry(macro, MacroList.this.macroScreen, isServer));
                }
            }

        }
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

        KeyEntry(final IMacro p_97451_, final Screen lastScreen, final boolean isMacroServer) {

            this.macro = p_97451_;

            this.editButton = new ImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/edit_button.png"), (p_97479_) ->
                    MacroList.this.minecraft.setScreen(new EditMacroScreen(MacroList.this.macroScreen, macro, lastScreen instanceof ServerMacroScreen)));

            this.stateButton = new Checkbox(0, 0, 20 , 20, Component.empty(), macro.isEnable()) {
                @Override
                public void onPress() {
                    macro.setEnable(!selected());
                    super.onPress();
                }
            };

            this.deleteButton = new ImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/delete_button.png"), (p_97479_) -> MacroList.this.minecraft.setScreen(new ConfirmScreen((p_170322_)-> {

                if (p_170322_) {
                    if(isMacroServer) {
                        MacroUtil.getServerKeybinds().remove(macro.getUUID());
                    } else {
                        MacroUtil.getGlobalKeybindsMap().remove(macro.getUUID());
                    }

                    String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                    new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + directory + "/" + macro.getUUID().toString() + ".json").delete();
                }

                MacroList.this.minecraft.setScreen(lastScreen);
            }, Component.translatable("text.macro.deleteQuestion"), Component.translatable("text.macro.deleteWarning"), Component.translatable("text.macro.deleteButton"), CommonComponents.GUI_CANCEL)));
        }

        public void render(PoseStack p_97463_, int p_97464_, int p_97465_, int p_97466_, int p_97467_, int p_97468_, int p_97469_, int p_97470_, boolean p_97471_, float p_97472_) {
            float f = (float) (p_97466_ - MacroList.this.maxNameWidth);
            MacroList.this.minecraft.font.draw(p_97463_, this.macro.getName(), f, (float) (p_97465_ + p_97468_ / 2 - 9 / 2), 16777215);
            this.deleteButton.setX(p_97466_ + 190 + 20);
            this.deleteButton.setY(p_97465_);
            this.editButton.setX(p_97466_ + 190);
            this.editButton.setY(p_97465_);
            this.stateButton.setX(p_97466_ + 170);
            this.stateButton.setY(p_97465_);
            this.editButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
            this.stateButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
            this.deleteButton.render(p_97463_, p_97469_, p_97470_, p_97472_);
            if(stateButton.isHoveredOrFocused()) {
                fill(p_97463_, stateButton.getX(), stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 1, Color.WHITE.getRGB());
                fill(p_97463_, stateButton.getX(), stateButton.getY() + 19, stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                fill(p_97463_, stateButton.getX(), stateButton.getY() + 20, stateButton.getX() + 1, stateButton.getY(), Color.WHITE.getRGB());
                fill(p_97463_, stateButton.getX() + 19, stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.state"), 150), p_97469_, p_97470_);
            }

            if(editButton.isHoveredOrFocused()) {
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.edit"), 150), p_97469_, p_97470_);
            }

            if(deleteButton.isHoveredOrFocused()) {
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.delete"), 150), p_97469_, p_97470_);
            }


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
