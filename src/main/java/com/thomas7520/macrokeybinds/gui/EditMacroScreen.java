package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.CommandSuggestions;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
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
    private CommandSuggestions commandSuggestions;

    private MacroModifier macroModifierSelect = MacroModifier.NONE;
    private InputConstants.Key inputSelected;

    public EditMacroScreen(Screen lastScreen, IMacro macro, boolean serverMacro) {
        super(new TranslatableComponent((macro == null) ? (serverMacro ? "text.createservermacros.title" : "text.createglobalmacros.title") : (serverMacro ? "text.createservermacros.title" : "text.editglobalmacro.title")));

        this.lastScreen = lastScreen;
        this.macroData = macro;
        this.serverMacro = serverMacro;
    }


    public void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;



        this.addRenderableWidget(nameBox = new EditBox(font, guiLeft - 200, guiTop / 2, 150, 20, Component.nullToEmpty(null)));

        this.addRenderableWidget(macroActionButton = new Button(guiLeft - 200, guiTop / 2 + 40, 150, 20, new TranslatableComponent(actionsType[0]), p_93751_ -> {
            if(actionTypeSelectId == 2) {
                macroActionBox.setMessage(macroActionBox.getMessage().copy().withStyle(ChatFormatting.WHITE));
                actionTypeSelectId = 0;
            } else {
                actionTypeSelectId++;
            }

            macroActionButton.setMessage(new TranslatableComponent(actionsType[actionTypeSelectId]));

        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.actiontype"), 150), p_93755_, p_93756_)));

        this.addRenderableWidget(macroActionBox = new EditBox(font, guiLeft - 200, guiTop / 2 + 80, 150, 20, Component.nullToEmpty(null)));
        macroActionBox.setMaxLength(256);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.macroActionBox, this.font, false, false, 10, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        macroActionBox.setResponder(this::onEdited);
        macroActionBox.setFilter(s -> actionTypeSelectId == 1 || actionTypeSelectId == 2 || !s.startsWith("/"));

        this.addRenderableWidget(macroTypeButton = new Button(guiLeft + 55, guiTop / 2, 150, 20, new TranslatableComponent(macrosType[0]), (p_96099_)
                -> {
            if(macroTypeSelectId == 3) {
                macroTypeSelectId = 0;
            } else {
                macroTypeSelectId++;
            }

            macroTypeButton.setMessage(new TranslatableComponent(macrosType[macroTypeSelectId]));

            if(macroTypeSelectId == 0) {
                timeBox.visible = false;
                macroKeyButton.y = guiTop / 2 + 40;
            } else {
                timeBox.visible = true;
                macroKeyButton.y = guiTop / 2 + 80;
            }
        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.macrotype"), 150), p_93755_, p_93756_)));


        this.addRenderableWidget(timeBox = new EditBox(font, guiLeft + 55, guiTop / 2 + 40, 150, 20, Component.nullToEmpty(null)));
        timeBox.setFilter(MacroUtil::isNumeric);

        this.addRenderableWidget(macroKeyButton = new Button(guiLeft + 55, guiTop / 2 + 80, 150, 20, new TranslatableComponent("text.key"), p_93751_ -> {
            if(listenMacroBind) return;
            listenMacroBind = true;

            macroKeyButton.setMessage((new TextComponent("> ")).append(macroKeyButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> {
            if(macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect)) {
                renderTooltip(p_93754_,Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.editmacro.keyalreadyassigned").withStyle(ChatFormatting.RED), 150), p_93755_, p_93756_);
            } else {
                renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.keybind"), 150), p_93755_, p_93756_);

            }
        }));



        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, new TranslatableComponent("text.globalmacros.back"), (p_96099_)
                -> this.minecraft.setScreen(this.lastScreen)));

        this.addRenderableWidget(confirmButton = new Button(this.width / 2 - 155, this.height - 38, 150, 20, new TranslatableComponent(macroData == null ? "text.createmacro" : "text.editmacro"), (p_96099_)
                -> {
            UUID macroUUID = macroData == null ? UUID.randomUUID() : macroData.getUUID();
            IMacro macro;
            switch (macroTypeSelectId) {
                case 0 :
                    macro = new SimpleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                    break;
                case 1 :
                    macro = new ToggleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                    break;
                case 2 :
                    macro = new RepeatMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                    break;
                case 3 :
                    macro = new DelayedMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), keyName, true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                    break;
                default :
                    throw new IllegalStateException("Unexpected value: " + macroTypeSelectId);

            }
            if(serverMacro) {
                MacroUtil.getServerKeybinds().put(macroUUID, macro);
            } else {
                MacroUtil.getGlobalKeybindsMap().put(macroUUID, macro);
            }
            String directory = serverMacro ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
            MacroFlow.writeMacro(macro, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + directory);

            if(lastScreen instanceof GlobalMacroScreen globalMacroScreen) {
                globalMacroScreen.updateMacroList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            } else if (lastScreen instanceof  ServerMacroScreen serverMacroScreen) {
                serverMacroScreen.updateMacroList(new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values()));
            }

            this.minecraft.setScreen(this.lastScreen);
        }));

        timeBox.visible = false;
        macroKeyButton.y = guiTop / 2 + 40;

        if(macroData != null) initDataMacro();




        super.init();
    }


    @Override
    public void tick() {
        nameBox.tick();
        timeBox.tick();
        macroActionBox.tick();
        super.tick();
    }

    private void onEdited(String p_95611_) {
        String s = this.macroActionBox.getValue();
        this.commandSuggestions.setAllowSuggestions(!s.equals("") && actionTypeSelectId == 1);
        if(actionTypeSelectId != 1) return;
        this.commandSuggestions.updateCommandInfo();
    }

    public void render(PoseStack p_96089_, int p_96090_, int p_96091_, float p_96092_) {
        renderDirtBackground(0);
        drawCenteredString(p_96089_, this.font, this.title, this.width / 2, 16, 16777215);


        if(macroTypeSelectId != 0) {
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.timebox"), guiLeft + 47, guiTop / 2 + 21, Color.WHITE.getRGB());
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.timeboxsub"), guiLeft + 90, guiTop / 2 + 31, Color.WHITE.getRGB());
        }

        nameBox.render(p_96089_, p_96090_, p_96090_, p_96090_);
        timeBox.render(p_96089_, p_96090_, p_96090_, p_96090_);

        confirmButton.active = !nameBox.getValue().isEmpty() && !macroActionBox.getValue().isEmpty()
                && (macroTypeSelectId == 0 || !timeBox.getValue().isEmpty()) && keySelect != -1;

        if(!confirmButton.active && confirmButton.isHoveredOrFocused()) {
            renderTooltip(p_96089_, Minecraft.getInstance().font.split(new TranslatableComponent("text.tooltip.editmacro.forgotvalue").withStyle(ChatFormatting.RED), 150), p_96090_, p_96091_);
        }

        if(actionTypeSelectId == 1 && macroActionBox.isFocused() && !macroActionBox.getValue().isEmpty() && macroActionBox.getValue().startsWith("/")) {
            nameBox.visible = false;
            macroActionButton.visible = false;
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.actionbox"), guiLeft - 45, guiTop / 2 + 85, Color.WHITE.getRGB());

            this.commandSuggestions.render(p_96089_, p_96090_, p_96091_);
            Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(p_96090_, p_96091_);
            if (style != null && style.getHoverEvent() != null) {
                this.renderComponentHoverEffect(p_96089_, style, p_96090_, p_96091_);
            }
        } else {
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.namebox"), guiLeft - 140, guiTop / 2 - 15, Color.WHITE.getRGB());
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.actionbox"), guiLeft - 142, guiTop / 2 + 65, Color.WHITE.getRGB());
            nameBox.visible = true;
            macroActionButton.visible = true;
        }

        super.render(p_96089_, p_96090_, p_96091_, p_96092_);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public boolean mouseClicked(double p_97522_, double p_97523_, int p_97524_) {
        if (this.commandSuggestions.mouseClicked((int)p_97522_, (int)p_97523_, p_97524_)) {
            return true;
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
                macroKeyButton.setMessage(new TextComponent(keyName).withStyle(ChatFormatting.RED));
            } else {
                macroKeyButton.setMessage(new TextComponent(keyName));
            }
            return true;
        } else {
            return super.mouseClicked(p_97522_, p_97523_, p_97524_);
        }
    }

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
                        macroKeyButton.setMessage(new TextComponent(getKeyName()).withStyle(ChatFormatting.RED));
                    } else {
                        macroKeyButton.setMessage(new TextComponent(getKeyName()));
                    }
                } else {
                    macroKeyButton.setMessage((new TextComponent("> ")).append(new TextComponent(keyName).withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
                }
            }
            return true;
        }  else {
            return super.keyPressed(p_97526_, p_97527_, p_97528_);
        }
    }

    @Override
    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        if(listenMacroBind) {
            if (macroData != null) {
                macroData.setKey(keySelect);
                macroData.setModifier(macroModifierSelect);
            }


            if (hasConflictKey()) {
                macroKeyButton.setMessage(new TextComponent(keyName).withStyle(ChatFormatting.RED));
            } else {
                macroKeyButton.setMessage(new TextComponent(keyName));
            }

            listenMacroBind = false;
        }
        return super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    @Override
    public boolean mouseScrolled(double p_94686_, double p_94687_, double p_94688_) {
        if (this.commandSuggestions.mouseScrolled(p_94688_)) {
            return true;
        }
        return super.mouseScrolled(p_94686_, p_94687_, p_94688_);
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

        macroActionButton.setMessage(new TranslatableComponent(actionsType[actionTypeSelectId]));
        macroTypeButton.setMessage(new TranslatableComponent(macrosType[macroTypeSelectId]));


        if (MacroUtil.isCombinationAssigned(macroData)){
            macroKeyButton.setMessage(new TextComponent(getKeyName()).withStyle(ChatFormatting.RED));
        } else {
            macroKeyButton.setMessage(new TextComponent(getKeyName()));
        }

        if(macroTypeSelectId == 0) {
            timeBox.visible = false;
            macroKeyButton.y = guiTop / 2 + 40;
        } else {
            timeBox.visible = true;
            macroKeyButton.y = guiTop / 2 + 80;
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
