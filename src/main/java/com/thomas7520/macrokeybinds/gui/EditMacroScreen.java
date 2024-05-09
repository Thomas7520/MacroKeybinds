package com.thomas7520.macrokeybinds.gui;

import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroCMDSuggestor;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.FocusedTooltipPositioner;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.client.gui.tooltip.WidgetTooltipPositioner;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.UUID;

public class EditMacroScreen extends Screen {

    private final Screen lastScreen;
    private final String[] macrosType = {"text.type.simple", "text.type.toggle", "text.type.repeat", "text.type.delayed"};
    private final String[] actionsType = {"text.action.message", "text.action.command", "text.action.fillchat"};

    private TextFieldWidget nameBox;
    private ButtonWidget macroActionButton;
    private TextFieldWidget macroActionBox;

    private ButtonWidget macroTypeButton;
    private TextFieldWidget timeBox;
    private ButtonWidget macroKeyButton;

    private ButtonWidget confirmButton;

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
    private InputUtil.Key inputSelected;

    public EditMacroScreen(Screen lastScreen, IMacro macro, boolean serverMacro) {
        super(Text.translatable((macro == null) ? (serverMacro ? "text.createservermacros.title" : "text.createglobalmacros.title") : (serverMacro ? "text.createservermacros.title" : "text.editglobalmacro.title")));

        this.lastScreen = lastScreen;
        this.macroData = macro;
        this.serverMacro = serverMacro;
    }


    public void init() {
        this.guiLeft = (this.width) / 2;
        this.guiTop = (this.height) / 2;


        addDrawableChild(nameBox = new TextFieldWidget(textRenderer, guiLeft - 200, guiTop / 2, 150, 20, Text.empty()));

        addDrawableChild(macroActionButton = createButton(Text.translatable(actionsType[0])
                        , guiLeft - 200, guiTop / 2 + 40, 150, 20
                , button -> {
                            if(actionTypeSelectId == 2) {
                                macroActionBox.setMessage(macroActionBox.getMessage().copy().formatted(Formatting.WHITE));
                                actionTypeSelectId = 0;
                            } else {
                                actionTypeSelectId++;
                            }

                            macroActionButton.setMessage(Text.translatable(actionsType[actionTypeSelectId]));

                }))
                .setTooltip(Tooltip.of(Text.translatable("text.tooltip.actiontype")));


        addDrawableChild(macroActionBox = new TextFieldWidget(textRenderer, guiLeft - 200, guiTop / 2 + 80, 150, 20, Text.empty()));


        this.commandSuggestions = new MacroCMDSuggestor(this.client, this, this.macroActionBox, this.textRenderer, false, false, 10, 10, true, -805306368);
        this.commandSuggestions.refresh();
        macroActionBox.setChangedListener(this::onEdited);
        macroActionBox.setMaxLength(256);


        addDrawableChild(macroTypeButton = createButton(Text.translatable(macrosType[0]), guiLeft + 55, guiTop / 2, 150, 20, onPress -> {
                    if(macroTypeSelectId == 3) {
                        macroTypeSelectId = 0;
                    } else {
                        macroTypeSelectId++;
                    }

                    macroTypeButton.setMessage(Text.translatable(macrosType[macroTypeSelectId]));

                    if(macroTypeSelectId == 0) {
                        timeBox.visible = false;
                        macroKeyButton.setY(guiTop / 2 + 40);
                    } else {
                        timeBox.visible = true;
                        macroKeyButton.setY(guiTop / 2 + 80);
                    }
                }))
                .setTooltip(Tooltip.of(Text.translatable("text.tooltip.macrotype")));



        addDrawableChild(timeBox = new TextFieldWidget(textRenderer, guiLeft + 55, guiTop / 2 + 40, 150, 20, Text.empty()));
        timeBox.setTextPredicate(MacroUtil::isNumeric);


        addDrawableChild(macroKeyButton = createButton(Text.translatable("text.key"), guiLeft + 55, guiTop / 2 + 80, 150, 20, onPress -> {
                    if(listenMacroBind) return;
                    listenMacroBind = true;

                    macroKeyButton.setMessage((Text.literal("> ")).append(macroKeyButton.getMessage().copy().formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));

                }))
                .setTooltip(Tooltip.of((macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect)) ?
                        Text.translatable("text.tooltip.editmacro.keyalreadyassigned").formatted(Formatting.RED)
                        : Text.translatable("text.tooltip.keybind")));



        addDrawableChild(createButton(Text.translatable("text.globalmacros.back"), this.width / 2 - 155 + 160, this.height - 38, 150, 20, p_93751_ -> client.setScreen(this.lastScreen)));


        addDrawableChild(confirmButton = createButton(Text.translatable(macroData == null ? "text.createmacro" : "text.editmacro"), this.width / 2 - 155, this.height - 38, 150, 20, p_93751_ -> {
                    UUID macroUUID = macroData == null ? UUID.randomUUID() : macroData.getUUID();
                    IMacro macro = switch (macroTypeSelectId) {
                        case 0 ->
                                new SimpleMacro(macroUUID, nameBox.getText(), macroActionBox.getText(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 1 ->
                                new ToggleMacro(macroUUID, nameBox.getText(), macroActionBox.getText(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getText()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 2 ->
                                new RepeatMacro(macroUUID, nameBox.getText(), macroActionBox.getText(), keySelect, keyName, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getText()), true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        case 3 ->
                                new DelayedMacro(macroUUID, nameBox.getText(), macroActionBox.getText(), keySelect, KeyAction.values()[actionTypeSelectId], Long.parseLong(timeBox.getText()), keyName, true, macroData == null ? System.currentTimeMillis() : macroData.getCreatedTime(), macroModifierSelect);
                        default -> throw new IllegalStateException("Unexpected value: " + macroTypeSelectId);
                    };
                    if(serverMacro) {
                        MacroUtil.getServerKeybinds().put(macroUUID, macro);
                    } else {
                        MacroUtil.getGlobalKeybindsMap().put(macroUUID, macro);
                    }
                    String directory = serverMacro ? "/servers-macros/" + MacroUtil.getServerIP() + "/" : "/global-macros/";
                    MacroFlow.writeMacro(macro, FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + directory);

                    client.setScreen(this.lastScreen);
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
        String string = this.macroActionBox.getText();

        this.commandSuggestions.setWindowActive(!string.isEmpty() && actionTypeSelectId == 1);
        if(actionTypeSelectId != 1) return;
        this.commandSuggestions.refresh();
    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(textRenderer, this.title, this.width / 2 - textRenderer.getWidth(title) / 2, 16, 16777215, false);


        if(macroTypeSelectId != 0) {
            MutableText timeBoxText = Text.translatable("text.tooltip.timebox");
            MutableText timeBoxSubText = Text.translatable("text.tooltip.timeboxsub");

            context.drawText(textRenderer, timeBoxText, guiLeft + 47 + 80 - textRenderer.getWidth(timeBoxText) / 2, guiTop / 2 + 21, Color.WHITE.getRGB(), false);
            context.drawText(textRenderer, timeBoxSubText, guiLeft + 90 + 40 - textRenderer.getWidth(timeBoxSubText) / 2, guiTop / 2 + 31, Color.WHITE.getRGB(), false);
        }


        confirmButton.active = !nameBox.getText().isEmpty() && !macroActionBox.getText().isEmpty()
                && (macroTypeSelectId == 0 || !timeBox.getText().isEmpty()) && keySelect != -1;

        if(!confirmButton.active && confirmButton.isHovered()) {
            context.drawTooltip(textRenderer, textRenderer.wrapLines(Text.translatable("text.tooltip.editmacro.forgotvalue").formatted(Formatting.RED), 150), createPositioner(true,false, confirmButton), mouseX, mouseY);
        }

        MutableText actionBox = Text.translatable("text.tooltip.actionbox");
        MutableText nameBoxTitle = Text.translatable("text.tooltip.namebox");

        if(actionTypeSelectId == 1 && macroActionBox.isFocused() && !macroActionBox.getText().isEmpty() && macroActionBox.getText().startsWith("/")) {
            nameBox.visible = false;
            macroActionButton.visible = false;
            context.drawText(textRenderer, actionBox, guiLeft - 45, guiTop / 2 + 85, Color.WHITE.getRGB(), false);

            this.commandSuggestions.render(context, mouseX, mouseY);
        } else {

            context.drawText(textRenderer, nameBoxTitle, guiLeft - 127 - textRenderer.getWidth(nameBoxTitle) / 2, guiTop / 2 - 15, Color.WHITE.getRGB(), false);
            context.drawText(textRenderer, actionBox, guiLeft - 127 - textRenderer.getWidth(actionBox) / 2, guiTop / 2 + 65, Color.WHITE.getRGB(), false);
            this.nameBox.visible = true;
            macroActionButton.visible = true;
        }

    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
            InputUtil.Key key = InputUtil.Type.MOUSE.createFromCode(button);
            keySelect = key.getCode();
            keyName = key.getLocalizedText().getString();
            listenMacroBind = false;

            if(macroData != null) {
                macroData.setKey(keySelect);
                macroData.setModifier(MacroModifier.NONE);
            }

            macroModifierSelect = MacroModifier.NONE;

            if(hasConflictKey()) {
                macroKeyButton.setMessage(Text.literal(keyName).formatted(Formatting.RED));
            } else {
                macroKeyButton.setMessage(Text.literal(keyName));
            }
            return true;
        } else {
            return super.mouseClicked(mouseX, mouseY, button);
        }

    }

    @Override
    public boolean keyPressed(int p_97526_, int p_97527_, int p_97528_) {
        if (this.commandSuggestions.keyPressed(p_97526_, p_97527_, p_97528_)) {
            return true;
        }
        if (this.listenMacroBind) {
            if (p_97526_ == GLFW.GLFW_KEY_ESCAPE) {
                if (hasConflictKey()) {
                    macroKeyButton.setMessage(Text.literal(getKeyName()).formatted(Formatting.RED));
                } else {
                    macroKeyButton.setMessage(Text.literal(getKeyName()));
                }
                listenMacroBind = false;
            } else {
                InputUtil.Key key = InputUtil.fromKeyCode(p_97526_, p_97527_);

                inputSelected = key;

                keySelect = key.getCode();
                keyName = key.getLocalizedText().getString();

                macroModifierSelect = getPressedModifierKeyCode();


                if (!isKeyCodeModifier(inputSelected.getCode())) {
                    listenMacroBind = false;

                    if (macroData != null) {
                        macroData.setModifier(macroModifierSelect);
                        macroData.setKey(keySelect);
                    }


                    if (hasConflictKey()) {
                        macroKeyButton.setMessage(Text.literal(getKeyName()).formatted(Formatting.RED));
                    } else {
                        macroKeyButton.setMessage(Text.literal(getKeyName()));
                    }

                } else {
                    macroKeyButton.setMessage(Text.literal("> ").append(Text.literal(keyName).formatted(Formatting.YELLOW)).append(" <").formatted(Formatting.YELLOW));
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
                macroKeyButton.setMessage(Text.literal(keyName).formatted(Formatting.RED));
            } else {
                macroKeyButton.setMessage(Text.literal(keyName));
            }

            listenMacroBind = false;
        }
        return super.keyReleased(p_94715_, p_94716_, p_94717_);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.commandSuggestions.mouseScrolled(verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void resize(MinecraftClient p_96575_, int p_96576_, int p_96577_) {
        MinecraftClient.getInstance().setScreen(new EditMacroScreen(lastScreen, macroData, serverMacro));
        super.resize(p_96575_, p_96576_, p_96577_);
    }

    public void initDataMacro() {
        nameBox.setText(macroData.getName());
        actionTypeSelectId = (byte) macroData.getAction().ordinal();
        macroActionBox.setText(macroData.getActionText());
        keySelect = macroData.getKey();
        keyName = macroData.getKeyName();
        macroTypeSelectId = 0;
        macroModifierSelect = macroData.getModifier();

        if(macroData instanceof ToggleMacro) {
            timeBox.setText(String.valueOf(((ToggleMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 1;
        }

        if(macroData instanceof RepeatMacro) {
            timeBox.setText(String.valueOf(((RepeatMacro) macroData).getCooldownTime()));
            macroTypeSelectId = 2;
        }

        if(macroData instanceof DelayedMacro) {
            timeBox.setText(String.valueOf(((DelayedMacro) macroData).getDelayedTime()));
            macroTypeSelectId = 3;
        }

        macroActionButton.setMessage(Text.translatable(actionsType[actionTypeSelectId]));
        macroTypeButton.setMessage(Text.translatable(macrosType[macroTypeSelectId]));

        if (MacroUtil.isCombinationAssigned(macroData)){
            macroKeyButton.setMessage(Text.literal(getKeyName()).formatted(Formatting.RED));
        } else {
            macroKeyButton.setMessage(Text.literal(getKeyName()));
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
    public void close() {
        client.setScreen(lastScreen);
    }

    private String getKeyName() {
        return macroModifierSelect != MacroModifier.NONE ? macroModifierSelect.name() + " + " + keyName : keyName;
    }
    private boolean hasConflictKey() {
        return macroData != null && MacroUtil.isCombinationAssigned(macroData) || macroData == null && MacroUtil.isCombinationAssigned(keySelect, macroModifierSelect);
    }

    private ButtonWidget createButton(Text text, int x, int y, int width, int height, ButtonWidget.PressAction pressSupplier) {
        return ButtonWidget.builder(text, pressSupplier)
                .dimensions(x,y,width,height)
                .build();
    }

    protected TooltipPositioner createPositioner(boolean hovered, boolean focused, ClickableWidget focus) {
        if (!hovered && focused && MinecraftClient.getInstance().getNavigationType().isKeyboard()) {
            return new FocusedTooltipPositioner(focus.getNavigationFocus());
        }
        return new WidgetTooltipPositioner(focus.getNavigationFocus());
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
