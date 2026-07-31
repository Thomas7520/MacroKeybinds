package com.thomas7520.macrokeybinds.gui.other;

import com.google.common.collect.ImmutableList;
import com.thomas7520.macrokeybinds.gui.EditMacroScreen;
import com.thomas7520.macrokeybinds.gui.ServerMacroScreen;
import com.thomas7520.macrokeybinds.object.DelayedMacro;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.RepeatMacro;
import com.thomas7520.macrokeybinds.util.ButtonImageWidget;
import com.thomas7520.macrokeybinds.util.CheckboxEdited;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Environment(value= EnvType.CLIENT)
public class MacroList
extends ContainerObjectSelectionList<MacroList.Entry> {
    final Screen parent;
    private List<IMacro> macroList;
    private final boolean isServer;
    int maxKeyNameLength;

    private static final Identifier EDIT_ICON = Identifier.fromNamespaceAndPath("macrokeybinds", "textures/edit_button.png");
    private static final Identifier DELETE_ICON = Identifier.fromNamespaceAndPath("macrokeybinds", "textures/delete_button.png");

    private String searchBoxInput = "";

    @Nullable
    private List<IMacro> cachedList;

    public MacroList(Screen parent, Minecraft client, List<IMacro> macros, boolean isServer) {
        super(client, parent.width, parent.height - 20 - 53, 43, 20);
        this.parent = parent;
        this.macroList = macros;
        this.isServer = isServer;
        macros.sort(Comparator.comparingLong(IMacro::getCreatedTime));


        macros.stream().map(bind -> new KeyBindingEntry(bind, parent, isServer)).forEach(this::addEntry);
    }

    public void update(Supplier<String> p_101677_, boolean update) {

        if(!update && (searchBoxInput.equalsIgnoreCase(p_101677_.get()) || (searchBoxInput.isEmpty() && p_101677_.get().isEmpty()))) return;
        searchBoxInput = p_101677_.get();

        this.clearEntries();
        this.setScrollAmount(0);

        if (this.cachedList == null) {
            this.cachedList = macroList;

            macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        }

        if (!this.cachedList.isEmpty()) {
            String s = p_101677_.get().toLowerCase(Locale.ROOT);

            for (IMacro macro : this.cachedList) {
                if (macro.getName().toLowerCase(Locale.ROOT).contains(s) || macro.getName().toLowerCase(Locale.ROOT).contains(s)) {
                    this.addEntry(new MacroList.KeyBindingEntry(macro, parent, isServer));
                }
            }
        }
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 15;
    }

    public void updateList(List<IMacro> list) {
        macroList = list;
        cachedList = null;
        this.clearEntries();
        macroList.sort(Comparator.comparingLong(IMacro::getCreatedTime));
        macroList.forEach((IMacro p_97451_) -> addEntry(new MacroList.KeyBindingEntry(p_97451_, parent, isServer)));
    }

    @Override
    protected int scrollBarX() {
        return super.scrollBarX() + 15 + 20;
    }


    @Environment(value=EnvType.CLIENT)
    public class KeyBindingEntry
            extends MacroList.Entry {
        private final IMacro macro;
        private final ButtonImageWidget editButton;
        private final CheckboxEdited stateButton;
        private final ButtonImageWidget deleteButton;


        public KeyBindingEntry(IMacro bind, Screen parent, boolean isMacroServer) {
            this.macro = bind;


            this.editButton = ButtonImageWidget.builder(Component.empty(), button -> MacroList.this.minecraft.setScreenAndShow(new EditMacroScreen(MacroList.this.parent, macro, parent instanceof ServerMacroScreen)))
                    .dimensions(0,0,20,20)
                    .icon(EDIT_ICON)
                    .build();

            this.stateButton = CheckboxEdited.builder(Component.empty(), minecraft.font)
                    .pos(0,0)
                    .callback((checkbox, checked) -> {
                        String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";

                        macro.setEnable(checked);
                        MacroFlow.writeMacro(macro, new File(FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + directory).getPath());
                    })
                    .checked(macro.isEnable())
                    .build();

            this.deleteButton = ButtonImageWidget.builder(Component.empty(), button -> minecraft.setScreenAndShow(new ConfirmScreen((p_170322_)-> {

                if (p_170322_) {
                    if(isMacroServer) {
                        MacroUtil.getServerKeybinds().remove(macro.getUUID());
                    } else {
                        MacroUtil.getGlobalKeybindsMap().remove(macro.getUUID());
                    }

                    String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                    new File(FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + directory + "/" + macro.getUUID().toString() + ".json").delete();
                }

                minecraft.setScreenAndShow(parent);
            }, Component.translatable("text.macro.deleteQuestion"), Component.translatable("text.macro.deleteWarning"), Component.translatable("text.macro.deleteButton"), CommonComponents.GUI_CANCEL)))
                    .dimensions(20,0,20,20)
                    .icon(DELETE_ICON)
                    .build();
        }


        @Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = getX();
            int y = getY();

            float f = (float) (x - MacroList.this.maxKeyNameLength);

            context.text(minecraft.font, Component.literal(this.macro.getName()), (int) f, y + 6, 0xffffffff);
            this.deleteButton.setX(x + 190 + 20);
            this.deleteButton.setY(y);
            this.editButton.setX(x + 190);
            this.editButton.setY(y);
            this.stateButton.setX(x + 170);
            this.stateButton.setY(y);
            this.editButton.extractRenderState(context, mouseX, mouseY, tickDelta);
            this.stateButton.extractRenderState(context, mouseX, mouseY, tickDelta);
            this.deleteButton.extractRenderState(context, mouseX, mouseY, tickDelta);
            if(stateButton.isHovered()) {
                context.fill(stateButton.getX(), stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 1, Color.WHITE.getRGB());
                context.fill(stateButton.getX(), stateButton.getY() + 19, stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                context.fill(stateButton.getX(), stateButton.getY() + 20, stateButton.getX() + 1, stateButton.getY(), Color.WHITE.getRGB());
                context.fill(stateButton.getX() + 19, stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                context.setTooltipForNextFrame(minecraft.font, minecraft.font.split(Component.translatable("text.tooltip.editmacro.state"), 150), mouseX, mouseY);

            }

            if(editButton.isHovered()) {
                context.setTooltipForNextFrame(minecraft.font, minecraft.font.split(Component.translatable("text.tooltip.editmacro.edit"), 150), mouseX, mouseY);
            }

            if(deleteButton.isHovered()) {
                context.setTooltipForNextFrame(minecraft.font, minecraft.font.split(Component.translatable("text.tooltip.editmacro.delete"), 150), mouseX, mouseY);
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
                    context.setTooltipForNextFrame(minecraft.font, Component.translatable("text.tooltip.running"), mouseX, mouseY);
                }
                context.text(minecraft.font, Component.translatable("text.running"), x - 16, y + 6, Color.GREEN.getRGB(), true);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton, this.deleteButton, this.stateButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editButton, this.deleteButton, this.stateButton);
        }

        @Override
        protected void update() {
        }


    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
            extends ContainerObjectSelectionList.Entry<MacroList.Entry> {
        abstract void update();
    }

    protected ClientTooltipPositioner createPositioner(boolean hovered, boolean focused, AbstractWidget focus) {
        if (!hovered && focused && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            return DefaultTooltipPositioner.INSTANCE;
        }
        return new BelowOrAboveWidgetTooltipPositioner(focus.getRectangle());
    }
}
