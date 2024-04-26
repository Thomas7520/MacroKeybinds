package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.EditMacroScreen;
import com.thomas7520.macrokeybinds.gui.ServerMacroScreen;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.CheckboxEdit;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
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
import org.checkerframework.checker.units.qual.C;

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
        super(p_97400_, p_97399_.width + 45, p_97399_.height - 52 - 33, 43, 20);
        this.macroScreen = p_97399_;


        macros.sort(Comparator.comparingLong(IMacro::getCreatedTime));

        macroList = macros;
        this.isServer = isServer;
        macros.forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen, isServer)));

    }


    public int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.height - 30 + 20));
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 15 + 20;
    }

    public int getRowWidth() {
        return super.getRowWidth() + 32;
    }

    public void refreshList(Supplier<String> p_101677_, boolean update) {

        if(!update && (searchBoxInput.equalsIgnoreCase(p_101677_.get()) || (searchBoxInput.isEmpty() && p_101677_.get().isEmpty()))) return;
        searchBoxInput = p_101677_.get();

        this.clearEntries();
        setScrollAmount(0);

        if (this.cachedList == null) {
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
        cachedList = null;
        this.clearEntries();
        macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        macroList.forEach((IMacro p_97451_) -> addEntry(new KeyEntry(p_97451_, macroScreen, isServer)));
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    @OnlyIn(Dist.CLIENT)
    public class KeyEntry extends Entry {
        private final IMacro macro;
        private final OldImageButton editButton;
        private final CheckboxEdit stateButton;
        private final OldImageButton deleteButton;

        KeyEntry(final IMacro p_97451_, final Screen lastScreen, final boolean isMacroServer) {

            this.macro = p_97451_;

            this.editButton = new OldImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/edit_button.png"), (p_97479_) ->
                    MacroList.this.minecraft.setScreen(new EditMacroScreen(MacroList.this.macroScreen, macro, lastScreen instanceof ServerMacroScreen)));


            this.stateButton = CheckboxEdit.builder(Component.empty(), minecraft.font)
                    .pos(0,0)
                    .onValueChange((p_309925_, p_310656_) ->  {
                        macro.setEnable(p_310656_);

                        String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                        MacroFlow.writeMacro(macro, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + directory);
                    })
                    .selected(macro.isEnable())
                    .build();



            this.deleteButton = new OldImageButton(0, 0, 20, 20, 0, 0, 20,new ResourceLocation(MacroMod.MODID, "textures/delete_button.png"), (p_97479_) -> MacroList.this.minecraft.setScreen(new ConfirmScreen((p_170322_)-> {

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

        @Override
        public void render(GuiGraphics guiGraphics, int p_281298_, int y, int x, int p_283433_, int p_281932_, int mouseX, int mouseY, boolean p_282605_, float p_281432_) {
            float f = (float) (x - MacroList.this.maxNameWidth);
            guiGraphics.drawString(minecraft.font, this.macro.getName(), f, (float) y + 6, 16777215, false);
            this.deleteButton.setX(x + 190 + 20);
            this.deleteButton.setY(y);
            this.editButton.setX(x + 190);
            this.editButton.setY(y);
            this.stateButton.setX(x + 170);
            this.stateButton.setY(y);
            this.editButton.render(guiGraphics, mouseX, mouseY, p_281432_);
            this.stateButton.render(guiGraphics, mouseX, mouseY, p_281432_);
            this.deleteButton.render(guiGraphics, mouseX, mouseY, p_281432_);
            if(stateButton.isHoveredOrFocused()) {
                guiGraphics.fill(stateButton.getX(), stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 1, Color.WHITE.getRGB());
                guiGraphics.fill(stateButton.getX(), stateButton.getY() + 19, stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                guiGraphics.fill(stateButton.getX(), stateButton.getY() + 20, stateButton.getX() + 1, stateButton.getY(), Color.WHITE.getRGB());
                guiGraphics.fill(stateButton.getX() + 19, stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                guiGraphics.renderTooltip(minecraft.font, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.state"), 150), mouseX, mouseY);
            }

            if(editButton.isHoveredOrFocused()) {
                guiGraphics.renderTooltip(minecraft.font, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.edit"), 150), mouseX, mouseY);
            }

            if(deleteButton.isHoveredOrFocused()) {
                guiGraphics.renderTooltip(minecraft.font, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.delete"), 150), mouseX, mouseY);
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
                    guiGraphics.renderTooltip(minecraft.font, Component.translatable("text.tooltip.running"), mouseX, mouseY);
                }
                guiGraphics.drawString(minecraft.font, Component.translatable("text.running"), x - 16, y + 6, Color.GREEN.getRGB());
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
