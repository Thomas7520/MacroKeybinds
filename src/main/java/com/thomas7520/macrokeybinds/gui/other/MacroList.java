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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Environment(value= EnvType.CLIENT)
public class MacroList
extends ElementListWidget<MacroList.Entry> {
    final Screen parent;
    private List<IMacro> macroList;
    private final boolean isServer;
    int maxKeyNameLength;

    private static final Identifier EDIT_ICON = new Identifier("macrokeybinds", "textures/edit_button.png");
    private static final Identifier DELETE_ICON = new Identifier("macrokeybinds", "textures/delete_button.png");

    private String searchBoxInput = "";

    @Nullable
    private List<IMacro> cachedList;

    public MacroList(Screen parent, MinecraftClient client, List<IMacro> macros, boolean isServer) {
        super(client, parent.width, parent.height - 52 , 40, parent.height - 32, 20);
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
    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 15 + 20;
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


            this.editButton = ButtonImageWidget.builder(Text.empty(), button -> MacroList.this.client.setScreen(new EditMacroScreen(MacroList.this.parent, macro, parent instanceof ServerMacroScreen)))
                    .dimensions(0,0,20,20)
                    .icon(EDIT_ICON)
                    .build();

            this.stateButton = CheckboxEdited.builder(Text.empty(), client.textRenderer)
                    .pos(0,0)
                    .callback((checkbox, checked) -> {
                        String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";

                        macro.setEnable(checked);
                        MacroFlow.writeMacro(macro, new File(FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + directory).getPath());
                    })
                    .checked(macro.isEnable())
                    .build();

            this.deleteButton = ButtonImageWidget.builder(Text.empty(), button -> client.setScreen(new ConfirmScreen((p_170322_)-> {

                if (p_170322_) {
                    if(isMacroServer) {
                        MacroUtil.getServerKeybinds().remove(macro.getUUID());
                    } else {
                        MacroUtil.getGlobalKeybindsMap().remove(macro.getUUID());
                    }

                    String directory = isMacroServer ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                    new File(FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + directory + "/" + macro.getUUID().toString() + ".json").delete();
                }

                client.setScreen(parent);
            }, Text.translatable("text.macro.deleteQuestion"), Text.translatable("text.macro.deleteWarning"), Text.translatable("text.macro.deleteButton"), ScreenTexts.CANCEL)))
                    .dimensions(20,0,20,20)
                    .icon(DELETE_ICON)
                    .build();
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            float f = (float) (x - MacroList.this.maxKeyNameLength);
            context.drawText(client.textRenderer, this.macro.getName(), (int) f, y + 6, 16777215, false);
            this.deleteButton.setX(x + 190 + 20);
            this.deleteButton.setY(y);
            this.editButton.setX(x + 190);
            this.editButton.setY(y);
            this.stateButton.setX(x + 170);
            this.stateButton.setY(y);
            this.editButton.render(context, mouseX, mouseY, tickDelta);
            this.stateButton.render(context, mouseX, mouseY, tickDelta);
            this.deleteButton.render(context, mouseX, mouseY, tickDelta);
            if(stateButton.isHovered()) {
                context.fill(stateButton.getX(), stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 1, Color.WHITE.getRGB());
                context.fill(stateButton.getX(), stateButton.getY() + 19, stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                context.fill(stateButton.getX(), stateButton.getY() + 20, stateButton.getX() + 1, stateButton.getY(), Color.WHITE.getRGB());
                context.fill(stateButton.getX() + 19, stateButton.getY(), stateButton.getX() + 20, stateButton.getY() + 20, Color.WHITE.getRGB());
                context.drawTooltip(client.textRenderer, client.textRenderer.wrapLines(Text.translatable("text.tooltip.editmacro.state"), 150), createPositioner(true,false, stateButton), mouseX, mouseY);

            }

            if(editButton.isHovered()) {
                context.drawTooltip(client.textRenderer, client.textRenderer.wrapLines(Text.translatable("text.tooltip.editmacro.edit"), 150), createPositioner(true,false, editButton), mouseX, mouseY);
            }

            if(deleteButton.isHovered()) {
                context.drawTooltip(client.textRenderer, client.textRenderer.wrapLines(Text.translatable("text.tooltip.editmacro.delete"), 150), createPositioner(true,false, deleteButton), mouseX, mouseY);
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
                    context.drawTooltip(client.textRenderer, Text.translatable("text.tooltip.running"), mouseX, mouseY);
                }
                context.drawText(client.textRenderer, Text.translatable("text.running"), x - 16, y + 6, Color.GREEN.getRGB(), true);
            }
        }

        @Override
        public List<? extends Element> children() {
            return ImmutableList.of(this.editButton, this.deleteButton, this.stateButton);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return ImmutableList.of(this.editButton, this.deleteButton, this.stateButton);
        }

        @Override
        protected void update() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class Entry
            extends ElementListWidget.Entry<MacroList.Entry> {
        abstract void update();
    }

    protected TooltipPositioner createPositioner(boolean hovered, boolean focused, ClickableWidget focus) {
        if (!hovered && focused && MinecraftClient.getInstance().getNavigationType().isKeyboard()) {
            return new FocusedTooltipPositioner(focus);
        }
        return new WidgetTooltipPositioner(focus);
    }
}

