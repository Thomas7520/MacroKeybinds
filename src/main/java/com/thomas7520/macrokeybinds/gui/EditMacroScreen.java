package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroCMDSuggestions;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.UUID;

public class EditMacroScreen extends Screen {


    private final Screen lastScreen;
    private final String[] macrosType = {"text.type.simple", "text.type.toggle", "text.type.repeat", "text.type.delayed"};
    private final String[] actionsType = {"text.action.message", "text.action.command", "text.action.fillchat"};

    private EditBox nameBox;
    private Button macroActionButton;
    private EditBox macroActionBox;

    private Button macroTypeButton;
    private EditBox timeBox;
    private Button macroKeyButton;

    private Button confirmButton;

    private boolean listenMacroBind;
    private int keySelect = -1;
    private byte macroTypeSelectId;
    private byte actionTypeSelectId;
    private String keyName;

    private int guiLeft;
    private int guiTop;
    private final IMacro macroData;
    private final boolean serverMacro;
    private MacroCMDSuggestions commandSuggestions;

    private MacroModifier macroModifierSelect = MacroModifier.NONE;
    private InputConstants.Key inputSelected;

    public EditMacroScreen(Screen lastScreen, IMacro macro, boolean serverMacro) {
        super(Component.translatable((macro == null) ? (serverMacro ? "text.createservermacros.title" : "text.createglobalmacros.title") : (serverMacro ? "text.createservermacros.title" : "text.editglobalmacro.title")));

        this.lastScreen = lastScreen;
        this.macroData = macro;
        this.serverMacro = serverMacro;
    }


    public void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;



        this.addRenderableWidget(nameBox = new EditBox(font, guiLeft - 200, guiTop / 2, 150, 20, Component.nullToEmpty(null)));
        nameBox.setCanLoseFocus(true);

        addRenderableWidget(macroActionButton = Button.builder(Component.translatable(actionsType[0]), onPress -> {
            if(actionTypeSelectId == 2) {
                macroActionBox.setMessage(macroActionBox.getMessage().copy().withStyle(ChatFormatting.WHITE));
                actionTypeSelectId = 0;
            } else {
                actionTypeSelectId++;
            }

            macroActionButton.setMessage(Component.translatable(actionsType[actionTypeSelectId]));
        }).bounds(guiLeft - 200, guiTop / 2 + 40, 150, 20)
        .tooltip(Tooltip.create(Component.translatable("text.tooltip.actiontype")))
        .build());

        this.addRenderableWidget(macroActionBox = new EditBox(font, guiLeft - 200, guiTop / 2 + 80, 150, 20, Component.nullToEmpty(null)));


        this.commandSuggestions = new MacroCMDSuggestions(this.minecraft, this, this.macroActionBox, this.font, false, false, 10, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        macroActionBox.setResponder(this::onEdited);
        macroActionBox.setFilter(s -> actionTypeSelectId == 1 || actionTypeSelectId == 2 || !s.startsWith("/"));
        macroActionBox.setMaxLength(256);
        macroActionBox.setCanLoseFocus(true);


        addRenderableWidget(macroTypeButton = Button.builder(Component.translatable(macrosType[0]), onPress -> {
            if(macroTypeSelectId == 3) {
                macroTypeSelectId = 0;
            } else {
                macroTypeSelectId++;
            }

            macroTypeButton.setMessage(Component.translatable(macrosType[macroTypeSelectId]));

            if(macroTypeSelectId == 0) {
                timeBox.visible = false;
                macroKeyButton.setY(guiTop / 2 + 40);
            } else {
                timeBox.visible = true;
                macroKeyButton.setY(guiTop / 2 + 80);
            }
        }).bounds(guiLeft + 55, guiTop / 2, 150, 20)
                .tooltip(Tooltip.create(Component.translatable("text.tooltip.macrotype")))
                .build());


        this.addRenderableWidget(timeBox = new EditBox(font, guiLeft + 55, guiTop / 2 + 40, 150, 20, Component.nullToEmpty(null)));
        timeBox.setFilter(MacroUtil::isNumeric);
        timeBox.setCanLoseFocus(true);


        addRenderableWidget(macroKeyButton = Button.builder(Component.translatable("text.key"), onPress -> {
            if(listenMacroBind) return;
            listenMacroBind = true;

            macroKeyButton.setMessage((Component.literal("> ")).append(macroKeyButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));

        }).bounds(guiLeft + 55, guiTop / 2 + 80, 150, 20)
                .tooltip(Tooltip.create((macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect)) ?
                        Component.translatable("text.tooltip.editmacro.keyalreadyassigned").withStyle(ChatFormatting.RED)
                        : Component.translatable("text.tooltip.keybind")))
                .build());



        this.addRenderableWidget(Button.builder(Component.translatable("text.globalmacros.back"), p_93751_ -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 - 155 + 160, this.height - 38, 150, 20).build());


        addRenderableWidget(confirmButton = Button.builder(Component.translatable(macroData == null ? "text.createmacro" : "text.editmacro"), p_93751_ -> {
            UUID macroUUID = macroData == null ? UUID.randomUUID() : macroData.getUUID();
            IMacro macro = switch (macroTypeSelectId) {
                case 0 ->
                        new SimpleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                case 1 ->
                        new ToggleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                case 2 ->
                        new RepeatMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                case 3 ->
                        new DelayedMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), keyName, true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                default -> throw new IllegalStateException("Unexpected value: " + macroTypeSelectId);
            };
            if(serverMacro) {
                MacroUtil.getServerKeybinds().put(macroUUID, macro);
            } else {
                MacroUtil.getGlobalKeybindsMap().put(macroUUID, macro);
            }
            String directory = serverMacro ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
            MacroFlow.writeMacro(macro, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + directory);

            this.minecraft.setScreen(this.lastScreen);
        })
                .bounds(this.width / 2 - 155, this.height - 38, 150, 20)
                .build());

        timeBox.visible = false;
        macroKeyButton.setY(guiTop / 2 + 40);

        if(macroData != null) initDataMacro();



        super.init();
    }


    @Override
    public void tick() {
        super.tick();
    }

    private void onEdited(String p_95611_) {
        String s = this.macroActionBox.getValue();
        this.commandSuggestions.setAllowSuggestions(!s.equals("") && actionTypeSelectId == 1);
        if(actionTypeSelectId != 1) return;
        this.commandSuggestions.updateCommandInfo();

    }

    @Override
    public void render(GuiGraphics p_281549_, int p_281550_, int p_282878_, float p_282465_) {
        renderDirtBackground(p_281549_);
        p_281549_.drawCenteredString(this.font, this.title, this.width / 2, 16, 16777215);


        if(macroTypeSelectId != 0) {
            p_281549_.drawString(font, Component.translatable("text.tooltip.timebox"), guiLeft + 47, guiTop / 2 + 21, Color.WHITE.getRGB());
            p_281549_.drawString(font, Component.translatable("text.tooltip.timeboxsub"), guiLeft + 90, guiTop / 2 + 31, Color.WHITE.getRGB());
        }

        nameBox.render(p_281549_, p_281550_, p_282878_, p_282465_);
        timeBox.render(p_281549_, p_281550_, p_282878_, p_282465_);

        confirmButton.active = !nameBox.getValue().isEmpty() && !macroActionBox.getValue().isEmpty()
                && (macroTypeSelectId == 0 || !timeBox.getValue().isEmpty()) && keySelect != -1;

        if(!confirmButton.active && confirmButton.isHoveredOrFocused()) {
            p_281549_.renderTooltip(font, Minecraft.getInstance().font.split(Component.translatable("text.tooltip.editmacro.forgotvalue").withStyle(ChatFormatting.RED), 150), p_281550_, p_282878_);
        }

        if(actionTypeSelectId == 1 && macroActionBox.isFocused() && !macroActionBox.getValue().isEmpty() && macroActionBox.getValue().startsWith("/")) {
            nameBox.visible = false;
            macroActionButton.visible = false;
            p_281549_.drawString(font, Component.translatable("text.tooltip.actionbox"), guiLeft - 45, guiTop / 2 + 85, Color.WHITE.getRGB());

            this.commandSuggestions.render(p_281549_, p_281550_, p_282878_, p_282465_);
            Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(p_281550_, p_282878_);
            if (style != null && style.getHoverEvent() != null) {
                p_281549_.renderComponentHoverEffect(font, style, p_281550_, p_282878_);
            }
        } else {
            p_281549_.drawString(font, Component.translatable("text.tooltip.namebox"), guiLeft - 140, guiTop / 2 - 15, Color.WHITE.getRGB());
            p_281549_.drawString(font, Component.translatable("text.tooltip.actionbox"), guiLeft - 142, guiTop / 2 + 65, Color.WHITE.getRGB());
            nameBox.visible = true;
            macroActionButton.visible = true;
        }

        for(Renderable renderable : this.renderables) {
            renderable.render(p_281549_, p_281550_, p_282878_, p_282465_);
        }
    }



    @Override
    public boolean isPauseScreen() {
        return false;
    }


    @Override
    public boolean mouseClicked(double p_97522_, double p_97523_, int p_97524_) {
        if (this.commandSuggestions.mouseClicked((int)p_97522_, (int)p_97523_, p_97524_)) {
            return true;
        }

        if(!macroActionBox.isMouseOver(p_97522_, p_97523_) && macroActionBox.isFocused()) {
            macroActionBox.setFocused(false);
        }

        if(!nameBox.isMouseOver(p_97522_, p_97523_) && nameBox.isFocused()) {
            nameBox.setFocused(false);
        }

        if (this.listenMacroBind) {
            InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(p_97524_);
            keySelect = key.getValue();
            keyName = key.getDisplayName().getString();
            listenMacroBind = false;
            if(macroData != null) {
                macroData.setKey(keySelect);
                macroData.setModifier(MacroModifier.NONE);
            }

            macroModifierSelect = MacroModifier.NONE;

            if(hasConflictKey()) {
                macroKeyButton.setMessage(Component.literal(keyName).withStyle(ChatFormatting.RED));
            } else {
                macroKeyButton.setMessage(Component.literal(keyName));
            }
            return true;
        } else {
            return super.mouseClicked(p_97522_, p_97523_, p_97524_);
        }

    }

    @Override
    public boolean keyPressed(int p_97526_, int p_97527_, int p_97528_) {
        if (this.commandSuggestions.keyPressed(p_97526_, p_97527_, p_97528_)) {
            return true;
        }
        if (this.listenMacroBind) {
            if (p_97526_ == GLFW.GLFW_KEY_ESCAPE) {
                return false;
            } else {
                InputConstants.Key key = InputConstants.getKey(p_97526_, p_97527_);

                inputSelected = key;

                keySelect = key.getValue();
                keyName = key.getDisplayName().getString();


                switch (net.minecraftforge.client.settings.KeyModifier.getActiveModifier()) {
                    case SHIFT -> macroModifierSelect = MacroModifier.SHIFT;
                    case ALT -> macroModifierSelect = MacroModifier.ALT;
                    case CONTROL -> macroModifierSelect = MacroModifier.CONTROL;
                    case NONE -> macroModifierSelect = MacroModifier.NONE;
                }

                if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(inputSelected)) {
                    listenMacroBind = false;

                    if (macroData != null) {
                        macroData.setModifier(macroModifierSelect);
                        macroData.setKey(keySelect);
                    }

                    if (hasConflictKey()) {
                        macroKeyButton.setMessage(Component.literal(getKeyName()).withStyle(ChatFormatting.RED));
                    } else {
                        macroKeyButton.setMessage(Component.literal(getKeyName()));
                    }

                } else {
                    macroKeyButton.setMessage(Component.literal("> ").append(Component.literal(keyName).withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
                }
            }
            return true;
        } else {
            return super.keyPressed(p_97526_, p_97527_, p_97528_);
        }
    }

    @Override
    public boolean keyReleased(int p_94715_, int p_94716_, int p_94717_) {
        if(listenMacroBind) {
            if (macroData != null) {
                macroData.setKey(keySelect);
                macroData.setModifier(macroModifierSelect);
            }


            if (hasConflictKey()) {
                macroKeyButton.setMessage(Component.literal(keyName).withStyle(ChatFormatting.RED));
            } else {
                macroKeyButton.setMessage(Component.literal(keyName));
            }

            listenMacroBind = false;
        }
        return super.keyReleased(p_94715_, p_94716_, p_94717_);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_, double p_299502_) {
        if (this.commandSuggestions.mouseScrolled(p_94688_)) {
            return true;
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_, p_299502_);
    }

    @Override
    public void resize(Minecraft p_96575_, int p_96576_, int p_96577_) {
        Minecraft.getInstance().setScreen(new EditMacroScreen(lastScreen, macroData, serverMacro));
        super.resize(p_96575_, p_96576_, p_96577_);
    }

    public void initDataMacro() {
        nameBox.insertText(macroData.getName());
        actionTypeSelectId = (byte) macroData.getAction().ordinal();
        macroActionBox.insertText(macroData.getActionText());
        keySelect = macroData.getKey();
        keyName = macroData.getKeyName();
        macroTypeSelectId = 0;
        macroModifierSelect = macroData.getModifier();

        if(macroData instanceof ToggleMacro) {
            timeBox.insertText(String.valueOf(((ToggleMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 1;
        }

        if(macroData instanceof RepeatMacro) {
            timeBox.insertText(String.valueOf(((RepeatMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 2;
        }

        if(macroData instanceof DelayedMacro) {
            timeBox.insertText(String.valueOf(((DelayedMacro) macroData).getDelayedTime()));
            macroTypeSelectId = 3;
        }

        macroActionButton.setMessage(Component.translatable(actionsType[actionTypeSelectId]));
        macroTypeButton.setMessage(Component.translatable(macrosType[macroTypeSelectId]));


        if (MacroUtil.isCombinationAssigned(macroData)){
            macroKeyButton.setMessage(Component.literal(getKeyName()).withStyle(ChatFormatting.RED));
        } else {
            macroKeyButton.setMessage(Component.literal(getKeyName()));
        }

        if(macroTypeSelectId == 0) {
            timeBox.visible = false;
            macroKeyButton.setY(guiTop / 2 + 40);
        } else {
            timeBox.visible = true;
            macroKeyButton.setY(guiTop / 2 + 80);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        super.onClose();
    }

    private String getKeyName() {
        return macroModifierSelect != MacroModifier.NONE ? macroModifierSelect.name() + " + " + keyName : keyName;
    }
    private boolean hasConflictKey() {
        return macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect);
    }
}
