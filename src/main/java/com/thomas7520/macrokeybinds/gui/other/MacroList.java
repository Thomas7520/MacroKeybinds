package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.EditMacroScreen;
import com.thomas7520.macrokeybinds.gui.ServerMacroScreen;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.MacroFlow;
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
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.opengl.GL11;

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
    private String searchBoxInput = "";

    public MacroList(Screen p_97399_, Minecraft p_97400_, List<IMacro> macros, boolean isServer) {
        super(p_97400_, p_97399_.width + 45, p_97399_.height, 43, p_97399_.height - 30, 20);
        this.macroScreen = p_97399_;


        macros.sort(Comparator.comparingLong(IMacro::getCreatedTime));

        macroList = macros;
        this.isServer = isServer;
        macros.forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen, isServer)));

    }

    protected int getScrollbarPosition() {
        return width - 60;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public void refreshList(Supplier<String> p_101677_, boolean p_101678_) {

        if(searchBoxInput.equalsIgnoreCase(p_101677_.get()) || (searchBoxInput.isEmpty() && p_101677_.get().isEmpty())) return;
        searchBoxInput = p_101677_.get();

        this.clearEntries();

        if (this.cachedList == null || p_101678_) {
            this.cachedList = macroList;


            macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        }

        if (!this.cachedList.isEmpty()) {
            String s = p_101677_.get().toLowerCase(Locale.ROOT);

            for (IMacro macro : this.cachedList) {
                if (macro.getName().toLowerCase(Locale.ROOT).contains(s) || macro.getName().toLowerCase(Locale.ROOT).contains(s)) {
                    this.addEntry(new KeyEntry(macro, MacroList.this.macroScreen, isServer));
                }
            }

        }
    }

    public void updateList(List<IMacro> list) {
        macroList = list;
        if(cachedList != null) cachedList.clear();
        this.clearEntries();
        macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        macroList.forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen, isServer)));
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

            this.stateButton = new Checkbox(0, 0, 20 , 20, new TextComponent(""), macro.isEnable()) {
                @Override
                public void onPress() {
                    macro.setEnable(!selected());

                    String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                    MacroFlow.writeMacro(macro, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + directory);
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
            }, new TranslatableComponent("text.macro.deleteQuestion"), new TranslatableComponent("text.macro.deleteWarning"), new TranslatableComponent("text.macro.deleteButton"), CommonComponents.GUI_CANCEL)));
        }

        public void render(PoseStack p_97463_, int p_97464_, int y, int x, int p_97467_, int p_97468_, int mouseX, int mouseY, boolean p_97471_, float p_97472_) {
            float f = (float) (x - MacroList.this.maxNameWidth);
            MacroList.this.minecraft.font.draw(p_97463_, this.macro.getName(), f, y + 6, 16777215);
            this.deleteButton.x = x + 190 + 20;
            this.deleteButton.y = y;
            this.editButton.x = x + 190;
            this.editButton.y = y;
            this.stateButton.x = x + 170;
            this.stateButton.y = y;
            this.editButton.render(p_97463_, mouseX, mouseY, p_97472_);
            this.stateButton.render(p_97463_, mouseX, mouseY, p_97472_);
            this.deleteButton.render(p_97463_, mouseX, mouseY, p_97472_);
            if(stateButton.isHoveredOrFocused()) {
                fill(p_97463_, stateButton.x, stateButton.y, stateButton.x + 20, stateButton.y + 1, Color.WHITE.getRGB());
                fill(p_97463_, stateButton.x, stateButton.y + 19, stateButton.x + 20, stateButton.y + 20, Color.WHITE.getRGB());
                fill(p_97463_, stateButton.x, stateButton.y + 20, stateButton.x + 1, stateButton.y, Color.WHITE.getRGB());
                fill(p_97463_, stateButton.x + 19, stateButton.y, stateButton.x + 20, stateButton.y + 20, Color.WHITE.getRGB());
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.editmacro.state"), 150), mouseX, mouseY);
            }


            if(editButton.isHoveredOrFocused()) {
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.editmacro.edit"), 150), mouseX, mouseY);
            }

            if(deleteButton.isHoveredOrFocused()) {
                MacroList.this.macroScreen.renderTooltip(p_97463_, Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.editmacro.delete"), 150), mouseX, mouseY);
            }



            boolean running = false;

            if(macro instanceof DelayedMacro delayedMacro) {
                if(delayedMacro.isStart()) {
                    running = true;
                }
            }

            if(macro instanceof RepeatMacro repeatMacro) {
                if(repeatMacro.isRepeat()) {
                    running = true;
                }
            }



            if(running) {
                if(mouseX >= x - 20 && mouseX <= x - 5 && mouseY >= y + 3 && mouseY < y+12) {
                    MacroList.this.macroScreen.renderTooltip(p_97463_, new TranslatableComponent("text.tooltip.running"), mouseX, mouseY);
                }
                MacroList.this.minecraft.font.draw(p_97463_, new TranslatableComponent("text.running"), x - 16, y + 6, Color.GREEN.getRGB());
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
