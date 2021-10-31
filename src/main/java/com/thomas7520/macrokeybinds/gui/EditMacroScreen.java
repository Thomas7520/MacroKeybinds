package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.UUID;

public class EditMacroScreen extends Screen {


    private final Screen lastScreen;
    private String[] macrosType = {"text.type.simple", "text.type.toggle", "text.type.repeat", "text.type.delayed"};
    private String[] actionsType = {"text.action.message", "text.action.command"};

    private EditBox nameBox;
    private Button macroActionSelect;
    private EditBox macroActionBox;

    private Button macroTypeSelect;
    private EditBox timeBox;
    private Button macroKeySelect;

    private boolean listenMacroBind;
    private int keySelect = -1;
    private byte macroTypeSelectId;
    private byte actionTypeSelectId;
    private String keyName;

    private int guiLeft;
    private int guiTop;
    private IMacro macroData;


    public EditMacroScreen(Screen lastScreen, IMacro macro) {
        super(new TranslatableComponent(macro == null ? "text.createglobalmacros.title" : "text.editglobalmacro.title"));

        this.lastScreen = lastScreen;
        this.macroData = macro;
    }


    public void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;

        /*
        Box nom
        Bouton type macro ( entre Simple, Toggle, Repeat, Delayed ) dans une array pour switch entre eux
        Si simple rien, si toggle/repeat/delayed, un délais à définir ( 0 = instant ) en milisecondes
        Bouton type action ( Entre message & command pour le moment )
        Box avec l'action à entrer ( on négligera le premier slash si il le met ou non, tout sera géré
         */

        this.addRenderableWidget(nameBox = new EditBox(font, guiLeft - 200, guiTop / 2, 150, 20, Component.nullToEmpty(null)));

        this.addRenderableWidget(macroActionSelect = new Button(guiLeft - 200, guiTop / 2 + 40, 150, 20, new TranslatableComponent(actionsType[0]), p_93751_ -> {
            if(actionTypeSelectId == 1) {
                actionTypeSelectId = 0;
            } else {
                actionTypeSelectId++;
            }

            macroActionSelect.setMessage(new TranslatableComponent(actionsType[actionTypeSelectId]));

        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.actiontype"), 150), p_93755_, p_93756_)));

        this.addRenderableWidget(macroActionBox = new EditBox(font, guiLeft - 200, guiTop / 2 + 80, 150, 20, Component.nullToEmpty(null)));


        this.addRenderableWidget(macroTypeSelect = new Button(guiLeft + 55, guiTop / 2, 150, 20, new TranslatableComponent(macrosType[0]), (p_96099_)
                -> {
            if(macroTypeSelectId == 3) {
                macroTypeSelectId = 0;
            } else {
                macroTypeSelectId++;
            }

            macroTypeSelect.setMessage(new TranslatableComponent(macrosType[macroTypeSelectId]));

            if(macroTypeSelectId == 0) {
                timeBox.visible = false;
                macroKeySelect.y = guiTop / 2 + 40;
            } else {
                timeBox.visible = true;
                macroKeySelect.y = guiTop / 2 + 80;
            }
        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.macrotype"), 150), p_93755_, p_93756_)));




        this.addRenderableWidget(timeBox = new EditBox(font, guiLeft + 55, guiTop / 2 + 40, 150, 20, Component.nullToEmpty(null)));
        timeBox.setFilter(MacroUtil::isNumeric);


        this.addRenderableWidget(macroKeySelect = new Button(guiLeft + 55, guiTop / 2 + 80, 150, 20, new TranslatableComponent("text.key"), p_93751_ -> {
            if(listenMacroBind) return;
            listenMacroBind = true;

            macroKeySelect.setMessage((new TextComponent("> ")).append(macroKeySelect.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));
        }, (p_93753_, p_93754_, p_93755_, p_93756_) -> renderTooltip(p_93754_, font.split(new TranslatableComponent("text.tooltip.keybind"), 150), p_93755_, p_93756_)));



        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 38, 150, 20, new TranslatableComponent("text.globalmacros.back"), (p_96099_)
                -> this.minecraft.setScreen(this.lastScreen)));

        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 38, 150, 20, new TranslatableComponent(macroData == null ? "text.createmacro" : "text.editmacro"), (p_96099_)
                -> {
            UUID macroUUID = UUID.randomUUID();
            IMacro macro;
            switch (macroTypeSelectId) {
                case 0 -> macro = new SimpleMacro(macroUUID, nameBox.getValue(), "", macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId]);
                case 1 -> macro = new ToggleMacro(macroUUID, nameBox.getValue(), "", macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()));
                case 2 -> macro = new RepeatMacro(macroUUID, nameBox.getValue(), "", macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()));
                case 3 -> macro = new DelayedMacro(macroUUID, nameBox.getValue(), "", macroActionBox.getValue(), keySelect, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), keyName);
                default -> throw new IllegalStateException("Unexpected value: " + macroTypeSelectId);
            }
            MacroUtil.getGlobalKeybindsMap().put(macro, macroUUID);
            this.minecraft.setScreen(this.lastScreen);
        }));

        timeBox.visible = false;
        macroKeySelect.y = guiTop / 2 + 40;
        if(macroData != null) initDataMacro();
        super.init();
    }


    @Override
    public void tick() {
        nameBox.tick();
        timeBox.tick();
        super.tick();
    }

    public void render(PoseStack p_96089_, int p_96090_, int p_96091_, float p_96092_) {
        renderDirtBackground(0);
        drawCenteredString(p_96089_, this.font, this.title, this.width / 2, 16, 16777215);

        drawString(p_96089_, font, new TranslatableComponent("text.tooltip.namebox"), guiLeft - 140, guiTop / 2 - 15, Color.WHITE.getRGB());
        drawString(p_96089_, font, new TranslatableComponent("text.tooltip.actionbox"), guiLeft - 142, guiTop / 2 + 65, Color.WHITE.getRGB());

        if(macroTypeSelectId != 0) {
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.timebox"), guiLeft + 47, guiTop / 2 + 21, Color.WHITE.getRGB());
            drawString(p_96089_, font, new TranslatableComponent("text.tooltip.timeboxsub"), guiLeft + 90, guiTop / 2 + 31, Color.WHITE.getRGB());
        }
        nameBox.render(p_96089_, p_96090_, p_96090_, p_96090_);
        timeBox.render(p_96089_, p_96090_, p_96090_, p_96090_);


        super.render(p_96089_, p_96090_, p_96091_, p_96092_);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }


    public boolean mouseClicked(double p_97522_, double p_97523_, int p_97524_) {
        if (this.listenMacroBind) {
            InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(p_97524_);
            keySelect = key.getValue();
            keyName = key.getDisplayName().getString();
            listenMacroBind = false;
            this.macroKeySelect.setMessage(new TextComponent(key.getDisplayName().getString()));
            return true;
        } else {
            return super.mouseClicked(p_97522_, p_97523_, p_97524_);
        }
    }

    public boolean keyPressed(int p_97526_, int p_97527_, int p_97528_) {
        if (this.listenMacroBind) {
            if (p_97526_ == GLFW.GLFW_KEY_ESCAPE) {
                return super.keyPressed(p_97526_, p_97527_, p_97528_);
            } else {
                InputConstants.Key key = InputConstants.getKey(p_97526_, p_97527_);
                keySelect = key.getValue();
                keyName = key.getDisplayName().getString();
                this.macroKeySelect.setMessage(new TextComponent(key.getDisplayName().getString()));
                listenMacroBind = false;
            }
            return true;
        } else {
            return super.keyPressed(p_97526_, p_97527_, p_97528_);
        }
    }

    @Override
    public void resize(Minecraft p_96575_, int p_96576_, int p_96577_) {
        Minecraft.getInstance().setScreen(new EditMacroScreen(lastScreen, macroData));
        super.resize(p_96575_, p_96576_, p_96577_);
    }

    public void initDataMacro() {
        nameBox.insertText(macroData.getName());
        actionTypeSelectId = (byte) macroData.getAction().ordinal();
        macroActionBox.insertText(macroData.getActionText());
        macroTypeSelectId = 0;


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

        macroActionSelect.setMessage(new TranslatableComponent(actionsType[actionTypeSelectId]));
        macroTypeSelect.setMessage(new TranslatableComponent(macrosType[macroTypeSelectId]));
        macroKeySelect.setMessage(new TextComponent(macroData.getKeyName()));

        if(macroTypeSelectId == 0) {
            timeBox.visible = false;
            macroKeySelect.y = guiTop / 2 + 40;
        } else {
            timeBox.visible = true;
            macroKeySelect.y = guiTop / 2 + 80;
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(lastScreen);
        super.onClose();
    }
}
