package com.thomas7520.macrokeybinds.util;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.gui.MainMacroScreen;
import com.thomas7520.macrokeybinds.gui.wheel.WheelScreen;
import com.thomas7520.macrokeybinds.object.macro.AlternateMacro;
import com.thomas7520.macrokeybinds.object.macro.CountedRepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.DelayedMacro;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.MacroModifier;
import com.thomas7520.macrokeybinds.object.macro.RepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.SimpleMacro;
import com.thomas7520.macrokeybinds.object.macro.ToggleMacro;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class MacroInputHandler {

    public static void checkOpenGui() {
        if(MacroUtil.guiBinding != null && MacroUtil.guiBinding.consumeClick()) {
            Minecraft.getInstance().setScreen(new MainMacroScreen());
        }

        if(MacroUtil.wheelBinding != null && MacroUtil.wheelBinding.consumeClick()) {
            Minecraft.getInstance().setScreen(new WheelScreen(MacroUtil.getWheel()));
        }
    }

    public static boolean canReceiveInput() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.screen == null;
    }

    public static boolean isWheelBindingDown() {
        if(MacroUtil.wheelBinding == null || MacroUtil.wheelBinding.isUnbound()) return false;

        Minecraft client = Minecraft.getInstance();
        InputConstants.Key key = InputConstants.getKey(MacroUtil.wheelBinding.saveString());
        long window = client.getWindow().handle();

        if(key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }

        if(key.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(client.getWindow(), key.getValue());
        }

        for(int keyCode = GLFW.GLFW_KEY_SPACE; keyCode <= GLFW.GLFW_KEY_LAST; keyCode++) {
            if(GLFW.glfwGetKeyScancode(keyCode) == key.getValue()
                    && GLFW.glfwGetKey(window, keyCode) == GLFW.GLFW_PRESS) {
                return true;
            }
        }

        return false;
    }

    public static void checkInputs(Set<Integer> pressedKeys) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        Minecraft client = Minecraft.getInstance();
        long window = client.getWindow().handle();

        for(int key : getEnabledMacroKeys()) {
            MacroModifier modifier = getPressedModifier(window);
            int state;

            if(key >= 0 && key <= 7) {
                state = GLFW.glfwGetMouseButton(window, key);
                modifier = MacroModifier.NONE;
            } else {
                state = GLFW.glfwGetKey(window, key);
            }

            boolean isPress = state == GLFW.GLFW_PRESS;
            boolean isRelease = state == GLFW.GLFW_RELEASE;

            if(isRelease) pressedKeys.remove(key);
            if(isPress && !pressedKeys.add(key)) continue;

            handleInput(isPress, isRelease, key, modifier);
        }
    }

    public static void handleKeyAction(int action, int key, int modifiers) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        handleInput(
                action == GLFW.GLFW_PRESS,
                action == GLFW.GLFW_RELEASE,
                key,
                modifierFromMask(modifiers)
        );
    }

    public static void handleMouseAction(int action, int button) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        // No modifier in mouse input
        handleInput(action == GLFW.GLFW_PRESS, action == GLFW.GLFW_RELEASE, button, MacroModifier.NONE);
    }

    private static void handleInput(boolean isPress, boolean isRelease, int key, MacroModifier modifier) {
        for(IMacro macro : MacroUtil.getAllMacros()) {
            if(!macro.isEnable() || key != macro.getKey()) continue;

            boolean modifierPressed = macro.getModifier() == modifier;

            switch (macro) {
                case SimpleMacro simpleMacro when isPress && modifierPressed -> {
                    simpleMacro.setStartTime(System.currentTimeMillis());
                    simpleMacro.setStart(true);
                }
                case AlternateMacro alternateMacro when isPress && modifierPressed -> alternateMacro.start();
                case RepeatMacro repeatMacro -> {
                    if (isPress && modifierPressed) {
                        repeatMacro.setRepeat(true);
                    } else if (isRelease || !modifierPressed) {
                        repeatMacro.setRepeat(false);
                    }
                }
                case ToggleMacro toggleMacro when isPress && modifierPressed -> toggleMacro.setToggled(!toggleMacro.isToggled());
                case DelayedMacro delayedMacro when isPress && modifierPressed && !delayedMacro.isStart() -> {
                    delayedMacro.setStartTime(System.currentTimeMillis());
                    delayedMacro.setStart(true);
                }
                case CountedRepeatMacro countedRepeatMacro when isPress && modifierPressed -> countedRepeatMacro.start();

                default -> {
                    // The macro type is known, but this input event does not trigger it
                    // (for example, a SimpleMacro when the key is released).
                }
            }
        }
    }

    private static Set<Integer> getEnabledMacroKeys() {
        Set<Integer> keys = new HashSet<>();
        for(IMacro macro : MacroUtil.getAllMacros()) {
            if(macro.isEnable()) keys.add(macro.getKey());
        }
        return keys;
    }

    private static MacroModifier modifierFromMask(int modifiers) {
        if((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) return MacroModifier.SHIFT;
        if((modifiers & GLFW.GLFW_MOD_ALT) != 0) return MacroModifier.ALT;
        if((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) return MacroModifier.CONTROL;
        return MacroModifier.NONE;
    }

    private static MacroModifier getPressedModifier(long window) {
        if(isPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT)) return MacroModifier.SHIFT;
        if(isPressed(window, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT)) return MacroModifier.ALT;
        if(isPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL)) return MacroModifier.CONTROL;
        return MacroModifier.NONE;
    }

    private static boolean isPressed(long window, int leftKey, int rightKey) {
        return GLFW.glfwGetKey(window, leftKey) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, rightKey) == GLFW.GLFW_PRESS;
    }
}
