package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.platform.Services;
import com.thomas7520.macrokeybinds.util.MacroCMDSuggestor;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
    private MacroCMDSuggestor commandSuggestions;

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


        addRenderableWidget(nameBox = new EditBox(font, guiLeft - 200, guiTop / 2, 150, 20, Component.empty()));

        addRenderableWidget(macroActionButton = createButton(Component.translatable(actionsType[0])
                        , guiLeft - 200, guiTop / 2 + 40, 150, 20
                , button -> {
                            if(actionTypeSelectId == 2) {
                                macroActionBox.setMessage(macroActionBox.getMessage().copy().withStyle(ChatFormatting.WHITE));
                                actionTypeSelectId = 0;
                            } else {
                                actionTypeSelectId++;
                            }

                            macroActionButton.setMessage(Component.translatable(actionsType[actionTypeSelectId]));

                }))
                .setTooltip(Tooltip.create(Component.translatable("text.tooltip.actiontype")));


        addRenderableWidget(macroActionBox = new EditBox(font, guiLeft - 200, guiTop / 2 + 80, 150, 20, Component.empty()));


        this.commandSuggestions = new MacroCMDSuggestor(this.minecraft, this, this.macroActionBox, this.font, false, false, 10, 10, true, -805306368);
        this.commandSuggestions.refresh();
        macroActionBox.setResponder(this::onEdited);
        macroActionBox.setMaxLength(256);


        addRenderableWidget(macroTypeButton = createButton(Component.translatable(macrosType[0]), guiLeft + 55, guiTop / 2, 150, 20, onPress -> {
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
                }))
                .setTooltip(Tooltip.create(Component.translatable("text.tooltip.macrotype")));



        addRenderableWidget(timeBox = new EditBox(font, guiLeft + 55, guiTop / 2 + 40, 150, 20, Component.empty()));
        timeBox.setResponder(value -> {
            if (!value.isEmpty() && !MacroUtil.isNumeric(value)) {
                timeBox.setValue(value.replaceAll("[^0-9]", ""));
            }
        });


        addRenderableWidget(macroKeyButton = createButton(Component.translatable("text.key"), guiLeft + 55, guiTop / 2 + 80, 150, 20, onPress -> {
                    if(listenMacroBind) return;
                    listenMacroBind = true;

                    macroKeyButton.setMessage((Component.literal("> ")).append(macroKeyButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));

                }))
                .setTooltip(Tooltip.create((macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect)) ?
                        Component.translatable("text.tooltip.editmacro.keyalreadyassigned").withStyle(ChatFormatting.RED)
                        : Component.translatable("text.tooltip.keybind")));



        addRenderableWidget(createButton(Component.translatable("text.globalmacros.back"), this.width / 2 - 155 + 160, this.height - 38, 150, 20, p_93751_ -> minecraft.setScreenAndShow(this.lastScreen)));


        addRenderableWidget(confirmButton = createButton(Component.translatable(macroData == null ? "text.createmacro" : "text.editmacro"), this.width / 2 - 155, this.height - 38, 150, 20, p_93751_ -> {
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
                    MacroFlow.writeMacro(macro, Services.PLATFORM.getConfigDirectory() + directory);

                    minecraft.setScreenAndShow(this.lastScreen);
                }));

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
        String string = this.macroActionBox.getValue();

        this.commandSuggestions.setWindowActive(!string.isEmpty() && actionTypeSelectId == 1);
        if(actionTypeSelectId != 1) return;
        this.commandSuggestions.refresh();
    }



    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.text(font, this.title, this.width / 2 - font.width(title) / 2, 16, 16777215, false);


        if(macroTypeSelectId != 0) {
            MutableComponent timeBoxText = Component.translatable("text.tooltip.timebox");
            MutableComponent timeBoxSubText = Component.translatable("text.tooltip.timeboxsub");

            context.text(font, timeBoxText, guiLeft + 47 + 80 - font.width(timeBoxText) / 2, guiTop / 2 + 21, Color.WHITE.getRGB(), false);
            context.text(font, timeBoxSubText, guiLeft + 90 + 40 - font.width(timeBoxSubText) / 2, guiTop / 2 + 31, Color.WHITE.getRGB(), false);
        }


        confirmButton.active = !nameBox.getValue().isEmpty() && !macroActionBox.getValue().isEmpty()
                && (macroTypeSelectId == 0 || !timeBox.getValue().isEmpty()) && keySelect != -1;

        if(!confirmButton.active && confirmButton.isHovered()) {
            context.setTooltipForNextFrame(font, font.split(Component.translatable("text.tooltip.editmacro.forgotvalue").withStyle(ChatFormatting.RED), 150), mouseX, mouseY);
        }

        MutableComponent actionBox = Component.translatable("text.tooltip.actionbox");
        MutableComponent nameBoxTitle = Component.translatable("text.tooltip.namebox");

        if(actionTypeSelectId == 1 && macroActionBox.isFocused() && !macroActionBox.getValue().isEmpty() && macroActionBox.getValue().startsWith("/")) {
            nameBox.visible = false;
            macroActionButton.visible = false;
            context.text(font, actionBox, guiLeft - 45, guiTop / 2 + 85, Color.WHITE.getRGB(), false);

            this.commandSuggestions.render(context, mouseX, mouseY);
        } else {

            context.text(font, nameBoxTitle, guiLeft - 127 - font.width(nameBoxTitle) / 2, guiTop / 2 - 15, Color.WHITE.getRGB(), false);
            context.text(font, actionBox, guiLeft - 127 - font.width(actionBox) / 2, guiTop / 2 + 65, Color.WHITE.getRGB(), false);
            this.nameBox.visible = true;
            macroActionButton.visible = true;
        }

    }



    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        if (this.commandSuggestions.mouseClicked((int)mouseX, (int)mouseY, button)) {
            return true;
        }

        if(!macroActionBox.isMouseOver(mouseX, mouseY) && macroActionBox.isFocused()) {
            macroActionBox.setFocused(false);
        }

        if(!nameBox.isMouseOver(mouseX, mouseY) && nameBox.isFocused()) {
            nameBox.setFocused(false);
        }

        if (this.listenMacroBind) {
            InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
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
            return super.mouseClicked(click, doubled);
        }

    }



    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.commandSuggestions.keyPressed(input)) {
            return true;
        }
        if (this.listenMacroBind) {
            if (input.isEscape()) {
                if (hasConflictKey()) {
                    macroKeyButton.setMessage(Component.literal(getKeyName()).withStyle(ChatFormatting.RED));
                } else {
                    macroKeyButton.setMessage(Component.literal(getKeyName()));
                }
                listenMacroBind = false;
            } else {
                InputConstants.Key key = InputConstants.getKey(input);

                inputSelected = key;

                keySelect = key.getValue();
                keyName = key.getDisplayName().getString();

                macroModifierSelect = getPressedModifierKeyCode();


                if (!isKeyCodeModifier(inputSelected.getValue())) {
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
            return super.keyPressed(input);
        }
    }



    @Override
    public boolean keyReleased(KeyEvent input) {
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
        return super.keyReleased(input);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.commandSuggestions.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }


    @Override
    public void resize(int width, int height) {
        Minecraft.getInstance().setScreenAndShow(new EditMacroScreen(lastScreen, macroData, serverMacro));
        super.resize(width, height);
    }

    public void initDataMacro() {
        nameBox.setValue(macroData.getName());
        actionTypeSelectId = (byte) macroData.getAction().ordinal();
        macroActionBox.setValue(macroData.getActionText());
        keySelect = macroData.getKey();
        keyName = macroData.getKeyName();
        macroTypeSelectId = 0;
        macroModifierSelect = macroData.getModifier();

        if(macroData instanceof ToggleMacro) {
            timeBox.setValue(String.valueOf(((ToggleMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 1;
        }

        if(macroData instanceof RepeatMacro) {
            timeBox.setValue(String.valueOf(((RepeatMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 2;
        }

        if(macroData instanceof DelayedMacro) {
            timeBox.setValue(String.valueOf(((DelayedMacro) macroData).getDelayedTime()));
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
        minecraft.setScreenAndShow(lastScreen);
    }

    private String getKeyName() {
        return macroModifierSelect != MacroModifier.NONE ? macroModifierSelect.name() + " + " + keyName : keyName;
    }
    private boolean hasConflictKey() {
        return macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect);
    }

    private Button createButton(Component text, int x, int y, int width, int height, Button.OnPress pressSupplier) {
        return Button.builder(text, pressSupplier)
                .bounds(x,y,width,height)
                .build();
    }

    protected ClientTooltipPositioner createPositioner(boolean hovered, boolean focused, AbstractWidget focus) {
        if (!hovered && focused && Minecraft.getInstance().getLastInputType().isKeyboard()) {
            return DefaultTooltipPositioner.INSTANCE;
        }
        return new BelowOrAboveWidgetTooltipPositioner(focus.getRectangle());
    }

    private boolean isKeyCodeModifier(int key) {
        int[] modifierKeys = {GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL};

        for (int modifierKey : modifierKeys) {
            if (key == modifierKey) {
                return true;
            }
        }
        return false;
    }

    public static MacroModifier getPressedModifierKeyCode() {
        long window = GLFW.glfwGetCurrentContext();

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
            return MacroModifier.SHIFT;
        } else if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS) {
            return MacroModifier.CONTROL;
        } else if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS) {
            return MacroModifier.ALT;
        }

        return MacroModifier.NONE;
    }
}
