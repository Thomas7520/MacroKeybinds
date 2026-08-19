package com.thomas7520.macrokeybinds.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.gui.other.EditMacroFormList;
import com.thomas7520.macrokeybinds.object.*;
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
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class EditMacroScreen extends Screen {

    private static final int FORM_WIDTH = 310;
    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int COMPACT_FIELD_WIDTH = 70;
    private static final int WIDGET_HEIGHT = 20;
    private static final int TITLE_COLOR = 0xFFFFFFFF;

    private final Screen lastScreen;
    private final String[] macrosType = {"text.type.simple", "text.type.toggle", "text.type.repeat", "text.type.delayed", "text.type.countedrepeat", "text.type.alternate"};
    private final String[] macrosTypeTooltip = {"text.tooltip.macrotype.simple", "text.tooltip.macrotype.toggle", "text.tooltip.macrotype.repeat", "text.tooltip.macrotype.delayed", "text.tooltip.macrotype.countedrepeat", "text.tooltip.macrotype.alternate"};
    private final String[] actionsType = {"text.action.message", "text.action.command", "text.action.fillchat", "text.action.localmessage"};
    private final String[] actionsTypeTooltip = {"text.tooltip.actiontype.message", "text.tooltip.actiontype.command", "text.tooltip.actiontype.fillchat", "text.tooltip.actiontype.localmessage"};

    private EditBox nameBox;
    private Button macroActionButton;
    private EditBox macroActionBox;
    private Button secondMacroActionButton;
    private EditBox secondMacroActionBox;

    private Button macroTypeButton;
    private EditBox timeBox;
    private EditBox countBox;
    private Button macroKeyButton;

    private Button confirmButton;

    private boolean listenMacroBind;
    private int keySelect = -1;
    private byte macroTypeSelectId;
    private byte actionTypeSelectId;
    private byte secondActionTypeSelectId;
    private String keyName;

    private int formLeft;
    private final IMacro macroData;
    private final boolean serverMacro;
    private MacroCMDSuggestor commandSuggestions;
    private MacroCMDSuggestor secondCommandSuggestions;
    private EditMacroFormList formList;

    private MacroModifier macroModifierSelect = MacroModifier.NONE;
    private InputConstants.Key inputSelected;

    public EditMacroScreen(Screen lastScreen, IMacro macro, boolean serverMacro) {
        super(Component.translatable((macro == null) ? (serverMacro ? "text.createservermacros.title" : "text.createglobalmacros.title") : (serverMacro ? "text.editservermacro.title" : "text.editglobalmacro.title")));

        this.lastScreen = lastScreen;
        this.macroData = macro;
        this.serverMacro = serverMacro;
    }


    public void init() {
        this.formLeft = (this.width - FORM_WIDTH) / 2;
        nameBox = new EditBox(font, 0, 0, FORM_WIDTH, WIDGET_HEIGHT, Component.empty());

        macroActionButton = createButton(Component.translatable(actionsType[0]), 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, button -> cycleActionType(false));
        macroActionButton.setTooltip(Tooltip.create(Component.translatable(actionsTypeTooltip[0])));
        macroActionBox = new EditBox(font, 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, Component.empty());

        this.commandSuggestions = new MacroCMDSuggestor(this.minecraft, this, this.macroActionBox, this.font, false, false, 10, false, -805306368);
        this.commandSuggestions.refresh();
        macroActionBox.setResponder(this::onEdited);
        macroActionBox.setMaxLength(256);

        secondMacroActionButton = createButton(Component.translatable(actionsType[0]), 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, button -> cycleActionType(true));
        secondMacroActionButton.setTooltip(Tooltip.create(Component.translatable(actionsTypeTooltip[0])));
        secondMacroActionBox = new EditBox(font, 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, Component.empty());
        this.secondCommandSuggestions = new MacroCMDSuggestor(this.minecraft, this, this.secondMacroActionBox, this.font, false, false, 10, false, -805306368);
        this.secondCommandSuggestions.refresh();
        secondMacroActionBox.setResponder(this::onSecondEdited);
        secondMacroActionBox.setMaxLength(256);


        macroTypeButton = createButton(Component.translatable(macrosType[0]), 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, onPress -> {
                    if(macroTypeSelectId == macrosType.length - 1) {
                        macroTypeSelectId = 0;
                    } else {
                        macroTypeSelectId++;
                    }

                    macroTypeButton.setMessage(Component.translatable(macrosType[macroTypeSelectId]));
                    updateMacroTypeTooltip();
                    updateTimingFields();
                    rebuildFormRows();
                });
        macroTypeButton.setTooltip(Tooltip.create(Component.translatable(macrosTypeTooltip[0])));

        timeBox = new EditBox(font, 0, 0, COLUMN_WIDTH, WIDGET_HEIGHT, Component.empty());
        timeBox.setResponder(value -> {
            if (!value.isEmpty() && !MacroUtil.isNumeric(value)) {
                timeBox.setValue(value.replaceAll("[^0-9]", ""));
            }
        });

        countBox = new EditBox(font, 0, 0, COMPACT_FIELD_WIDTH, WIDGET_HEIGHT, Component.empty());
        countBox.setMaxLength(9);
        countBox.setResponder(value -> {
            if (!value.isEmpty() && !MacroUtil.isNumeric(value)) {
                countBox.setValue(value.replaceAll("[^0-9]", ""));
            }
        });


        macroKeyButton = createButton(Component.translatable("text.key"), 0, 0, FORM_WIDTH, WIDGET_HEIGHT, onPress -> {
                    if(listenMacroBind) return;
                    listenMacroBind = true;

                    macroKeyButton.setMessage((Component.literal("> ")).append(macroKeyButton.getMessage().copy().withStyle(ChatFormatting.YELLOW)).append(" <").withStyle(ChatFormatting.YELLOW));

                });
        macroKeyButton.setTooltip(Tooltip.create((macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect)) ?
                        Component.translatable("text.tooltip.editmacro.keyalreadyassigned").withStyle(ChatFormatting.RED)
                        : Component.translatable("text.tooltip.keybind")));

        formList = addRenderableWidget(new EditMacroFormList(this, minecraft));
        addRenderableWidget(createButton(Component.translatable("text.globalmacros.back"), formLeft + COLUMN_WIDTH + COLUMN_GAP, this.height - 28, COLUMN_WIDTH, WIDGET_HEIGHT, p_93751_ -> minecraft.setScreenAndShow(this.lastScreen)));


        addRenderableWidget(confirmButton = createButton(Component.translatable(macroData == null ? "text.createmacro" : "text.editmacro"), formLeft, this.height - 28, COLUMN_WIDTH, WIDGET_HEIGHT, p_93751_ -> {
                    UUID macroUUID = macroData == null ? UUID.randomUUID() : macroData.getUUID();
                    boolean macroEnabled = macroData == null || macroData.isEnable();
                    IMacro macro = switch (macroTypeSelectId) {
                        case 0 ->
                                new SimpleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 1 ->
                                new ToggleMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 2 ->
                                new RepeatMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 3 ->
                                new DelayedMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), keyName, macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 4 ->
                                new CountedRepeatMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getValue()), Integer.parseInt(countBox.getValue()), macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 5 ->
                                new AlternateMacro(macroUUID, nameBox.getValue(), macroActionBox.getValue(), secondMacroActionBox.getValue(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], KeyAction.values()[secondActionTypeSelectId], macroEnabled, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        default -> throw new IllegalStateException("Unexpected value: " + macroTypeSelectId);
                    };
                    if(serverMacro) {
                        MacroUtil.getServerKeybinds().put(macroUUID, macro);
                    } else {
                        MacroUtil.getGlobalKeybindsMap().put(macroUUID, macro);
                    }
                    String directory = serverMacro
                            ? MacroUtil.getServerMacroDirectory().toString()
                            : MacroUtil.getGlobalMacroDirectory().toString();
                    MacroFlow.writeMacro(macro, directory);

                    minecraft.setScreenAndShow(this.lastScreen);
                }));

        if(macroData != null) {
            initDataMacro();
        } else {
            updateTimingFields();
            rebuildFormRows();
        }



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

    private void onSecondEdited(String value) {
        this.secondCommandSuggestions.setWindowActive(!value.isEmpty() && secondActionTypeSelectId == 1);
        if(secondActionTypeSelectId != 1) return;
        this.secondCommandSuggestions.refresh();
    }

    private void cycleActionType(boolean secondAction) {
        if(secondAction) {
            secondActionTypeSelectId = (byte) ((secondActionTypeSelectId + 1) % actionsType.length);
            secondMacroActionButton.setMessage(Component.translatable(actionsType[secondActionTypeSelectId]));
            updateActionTypeTooltip(true);
            onSecondEdited(secondMacroActionBox.getValue());
        } else {
            actionTypeSelectId = (byte) ((actionTypeSelectId + 1) % actionsType.length);
            macroActionButton.setMessage(Component.translatable(actionsType[actionTypeSelectId]));
            updateActionTypeTooltip(false);
            onEdited(macroActionBox.getValue());
        }
    }



    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        context.text(font, this.title, this.width / 2 - font.width(title) / 2, 15, TITLE_COLOR, false);

        boolean showingCommandSuggestions = actionTypeSelectId == KeyAction.COMMAND.ordinal()
                && macroActionBox.isFocused()
                && !macroActionBox.getValue().isEmpty()
                && macroActionBox.getValue().startsWith("/");

        boolean showingSecondCommandSuggestions = macroTypeSelectId == 5
                && secondActionTypeSelectId == KeyAction.COMMAND.ordinal()
                && secondMacroActionBox.isFocused()
                && !secondMacroActionBox.getValue().isEmpty()
                && secondMacroActionBox.getValue().startsWith("/");

        confirmButton.active = !nameBox.getValue().isEmpty() && !macroActionBox.getValue().isEmpty()
                && (macroTypeSelectId != 5 || !secondMacroActionBox.getValue().isEmpty())
                && (macroTypeSelectId == 0 || macroTypeSelectId == 5 || !timeBox.getValue().isEmpty())
                && (macroTypeSelectId != 4 || isValidRepeatCount())
                && keySelect != -1;

        if(!confirmButton.active && confirmButton.isHovered()) {
            context.setTooltipForNextFrame(font, font.split(Component.translatable("text.tooltip.editmacro.forgotvalue").withStyle(ChatFormatting.RED), 150), mouseX, mouseY);
        }

        if(showingCommandSuggestions) {
            this.commandSuggestions.render(context, mouseX, mouseY);
        } else if(showingSecondCommandSuggestions) {
            this.secondCommandSuggestions.render(context, mouseX, mouseY);
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
        if (macroTypeSelectId == 5 && this.secondCommandSuggestions.mouseClicked((int)mouseX, (int)mouseY, button)) {
            return true;
        }

        if (this.listenMacroBind) {
            InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
            keySelect = key.getValue();
            keyName = key.getDisplayName().getString();
            listenMacroBind = false;

            macroModifierSelect = MacroModifier.NONE;

            if(hasConflictKey()) {
                macroKeyButton.setMessage(Component.literal(keyName).withStyle(ChatFormatting.RED));
            } else {
                macroKeyButton.setMessage(Component.literal(keyName));
            }
            return true;
        } else {
            boolean handled = super.mouseClicked(click, doubled);
            if (!handled) {
                this.setFocused(null);
            }
            return handled;
        }

    }



    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.commandSuggestions.keyPressed(input)) {
            return true;
        }
        if (macroTypeSelectId == 5 && this.secondCommandSuggestions.keyPressed(input)) {
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
        if (macroTypeSelectId == 5 && this.secondCommandSuggestions.mouseScrolled(verticalAmount)) {
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

        if(macroData instanceof CountedRepeatMacro countedRepeatMacro) {
            timeBox.setValue(String.valueOf(countedRepeatMacro.getIntervalTime()));
            countBox.setValue(String.valueOf(countedRepeatMacro.getRepeatCount()));
            macroTypeSelectId = 4;
        }

        if(macroData instanceof AlternateMacro alternateMacro) {
            secondActionTypeSelectId = (byte) alternateMacro.getSecondAction().ordinal();
            secondMacroActionBox.setValue(alternateMacro.getSecondActionText());
            macroTypeSelectId = 5;
        }

        macroActionButton.setMessage(Component.translatable(actionsType[actionTypeSelectId]));
        secondMacroActionButton.setMessage(Component.translatable(actionsType[secondActionTypeSelectId]));
        updateActionTypeTooltip(false);
        updateActionTypeTooltip(true);
        macroTypeButton.setMessage(Component.translatable(macrosType[macroTypeSelectId]));
        updateMacroTypeTooltip();

        if (MacroUtil.isCombinationAssigned(macroData)){
            macroKeyButton.setMessage(Component.literal(getKeyName()).withStyle(ChatFormatting.RED));
        } else {
            macroKeyButton.setMessage(Component.literal(getKeyName()));
        }

        updateTimingFields();
        rebuildFormRows();
    }

    private void updateTimingFields() {
        boolean countedRepeat = macroTypeSelectId == 4;
        boolean alternate = macroTypeSelectId == 5;
        timeBox.visible = macroTypeSelectId != 0 && !alternate;
        timeBox.setWidth(countedRepeat ? COMPACT_FIELD_WIDTH : COLUMN_WIDTH);
        countBox.visible = countedRepeat;
        secondMacroActionButton.visible = alternate;
        secondMacroActionBox.visible = alternate;
    }

    private void updateMacroTypeTooltip() {
        macroTypeButton.setTooltip(Tooltip.create(Component.translatable(macrosTypeTooltip[macroTypeSelectId])));
    }

    private void updateActionTypeTooltip(boolean secondAction) {
        byte actionType = secondAction ? secondActionTypeSelectId : actionTypeSelectId;
        Button actionButton = secondAction ? secondMacroActionButton : macroActionButton;
        actionButton.setTooltip(Tooltip.create(Component.translatable(actionsTypeTooltip[actionType])));
    }

    private void rebuildFormRows() {
        double scrollAmount = formList.scrollAmount();
        boolean alternate = macroTypeSelectId == 5;

        formList.clearRows();
        formList.addRow(new EditMacroFormList.Field(Component.translatable("text.editmacro.field.name"), nameBox, 0));
        formList.addRow(
                new EditMacroFormList.Field(Component.translatable(alternate ? "text.editmacro.field.actiontype.first" : "text.editmacro.field.actiontype"), macroActionButton, 0),
                new EditMacroFormList.Field(Component.translatable(alternate ? "text.editmacro.field.action.first" : "text.editmacro.field.action"), macroActionBox, COLUMN_WIDTH + COLUMN_GAP)
        );

        if(alternate) {
            formList.addRow(
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.actiontype.second"), secondMacroActionButton, 0),
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.action.second"), secondMacroActionBox, COLUMN_WIDTH + COLUMN_GAP)
            );
        }

        if(macroTypeSelectId == 4) {
            formList.addRow(
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.macrotype"), macroTypeButton, 0),
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.interval"), timeBox, COLUMN_WIDTH + COLUMN_GAP),
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.count"), countBox, COLUMN_WIDTH + COLUMN_GAP + COMPACT_FIELD_WIDTH + COLUMN_GAP)
            );
        } else if(timeBox.visible) {
            formList.addRow(
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.macrotype"), macroTypeButton, 0),
                    new EditMacroFormList.Field(Component.translatable("text.editmacro.field.timing"), timeBox, COLUMN_WIDTH + COLUMN_GAP)
            );
        } else {
            formList.addRow(new EditMacroFormList.Field(Component.translatable("text.editmacro.field.macrotype"), macroTypeButton, 0));
        }

        formList.addRow(new EditMacroFormList.Field(Component.translatable("text.editmacro.field.keybind"), macroKeyButton, 0));
        formList.setScrollAmount(scrollAmount);
    }

    private boolean isValidRepeatCount() {
        if(countBox.getValue().isEmpty()) return false;

        try {
            return Integer.parseInt(countBox.getValue()) > 0;
        } catch (NumberFormatException ignored) {
            return false;
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
        return macroData != null
                ? MacroUtil.isCombinationAssigned(macroData, keySelect, macroModifierSelect)
                : MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect);
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
