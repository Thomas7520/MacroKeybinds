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
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLScancode;

import java.util.HashSet;
import java.util.Set;

public class MacroInputHandler {

    private static final int KEYBOARD_KEY_OFFSET = 1000;
    private static final int MOUSE_KEY_OFFSET = 2000;

    public static void checkOpenGui() {
        if(MacroUtil.guiBinding != null && MacroUtil.guiBinding.consumeClick()) {
            Minecraft.getInstance().gui.setScreen(new MainMacroScreen());
        }

        if(MacroUtil.wheelBinding != null && MacroUtil.wheelBinding.consumeClick()) {
            Minecraft.getInstance().gui.setScreen(new WheelScreen(MacroUtil.getWheel()));
        }
    }

    public static boolean canReceiveInput() {
        Minecraft client = Minecraft.getInstance();
        return client.level != null && client.gui.screen() == null;
    }

    public static boolean isWheelBindingDown() {
        if(MacroUtil.wheelBinding == null || MacroUtil.wheelBinding.isUnbound()) return false;

        InputConstants.Key key = InputConstants.getKey(MacroUtil.wheelBinding.saveString());

        if(key.getType() == InputConstants.Type.MOUSE) {
            return isMouseButtonDown(key.getValue());
        }

        if(key.getType() == InputConstants.Type.KEYBOARD) {
            return InputConstants.isKeyDown(key.getValue());
        }
        return false;
    }

    public static void checkInputs(Set<Integer> pressedKeys) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        for(int key : getEnabledMacroKeys()) {
            MacroModifier modifier = isMouseKey(key) ? MacroModifier.NONE : getPressedModifier();
            boolean isPress = isStoredKeyDown(key);
            boolean isRelease = !isPress;

            if(isRelease) pressedKeys.remove(key);
            if(isPress && !pressedKeys.add(key)) continue;

            handleInput(isPress, isRelease, key, modifier);
        }
    }

    public static void handleKeyAction(int action, int key, int modifiers) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        handleInput(
                action == InputConstants.PRESS,
                action == InputConstants.RELEASE,
                encodeKeyboardKey(key),
                modifierFromMask(modifiers)
        );
    }

    public static void handleMouseAction(int action, int button) {
        checkOpenGui();
        if(!canReceiveInput()) return;

        // No modifier in mouse input
        handleInput(action == InputConstants.PRESS, action == InputConstants.RELEASE, encodeMouseButton(button), MacroModifier.NONE);
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
        if((modifiers & InputConstants.MOD_SHIFT) != 0) return MacroModifier.SHIFT;
        if((modifiers & InputConstants.MOD_ALT) != 0) return MacroModifier.ALT;
        if((modifiers & InputConstants.MOD_CONTROL) != 0) return MacroModifier.CONTROL;
        return MacroModifier.NONE;
    }

    private static MacroModifier getPressedModifier() {
        if(isPressed(InputConstants.KEY_LSHIFT, InputConstants.KEY_RSHIFT)) return MacroModifier.SHIFT;
        if(isPressed(InputConstants.KEY_LALT, InputConstants.KEY_RALT)) return MacroModifier.ALT;
        if(isPressed(InputConstants.KEY_LCONTROL, InputConstants.KEY_RCONTROL)) return MacroModifier.CONTROL;
        return MacroModifier.NONE;
    }

    private static boolean isPressed(int leftKey, int rightKey) {
        return InputConstants.isKeyDown(leftKey) || InputConstants.isKeyDown(rightKey);
    }

    private static boolean isStoredKeyDown(int key) {
        if(isKeyboardKey(key)) return InputConstants.isKeyDown(key - KEYBOARD_KEY_OFFSET);
        if(isMouseKey(key)) return isMouseButtonDown(key - MOUSE_KEY_OFFSET);
        return false;
    }

    private static boolean isMouseButtonDown(int button) {
        return (SDLMouse.SDL_GetMouseState(null, null) & (1 << (button - 1))) != 0;
    }

    private static boolean isKeyboardKey(int key) {
        return key >= KEYBOARD_KEY_OFFSET && key < MOUSE_KEY_OFFSET;
    }

    private static boolean isMouseKey(int key) {
        return key >= MOUSE_KEY_OFFSET;
    }

    public static int encodeKeyboardKey(int key) {
        return KEYBOARD_KEY_OFFSET + key;
    }

    public static int encodeMouseButton(int button) {
        return MOUSE_KEY_OFFSET + button;
    }

    public static int migrateLegacyKey(int key) {
        if(key >= KEYBOARD_KEY_OFFSET || key < 0) return key;
        if(key <= 7) return encodeMouseButton(key + 1);

        int migratedKey;
        if(key >= 65 && key <= 90) {
            migratedKey = InputConstants.KEY_A + key - 65;
        } else if(key >= 49 && key <= 57) {
            migratedKey = InputConstants.KEY_1 + key - 49;
        } else if(key >= 290 && key <= 301) {
            migratedKey = InputConstants.KEY_F1 + key - 290;
        } else if(key >= 302 && key <= 313) {
            migratedKey = InputConstants.KEY_F13 + key - 302;
        } else if(key >= 320 && key <= 329) {
            migratedKey = key == 320 ? InputConstants.KEY_NUMPAD0 : InputConstants.KEY_NUMPAD1 + key - 321;
        } else {
            migratedKey = switch(key) {
                case 32 -> InputConstants.KEY_SPACE;
                case 39 -> InputConstants.KEY_APOSTROPHE;
                case 44 -> InputConstants.KEY_COMMA;
                case 45 -> InputConstants.KEY_MINUS;
                case 46 -> InputConstants.KEY_PERIOD;
                case 47 -> InputConstants.KEY_SLASH;
                case 48 -> InputConstants.KEY_0;
                case 59 -> InputConstants.KEY_SEMICOLON;
                case 61 -> InputConstants.KEY_EQUALS;
                case 91 -> InputConstants.KEY_LBRACKET;
                case 92 -> InputConstants.KEY_BACKSLASH;
                case 93 -> InputConstants.KEY_RBRACKET;
                case 96 -> InputConstants.KEY_GRAVE;
                case 161 -> SDLScancode.SDL_SCANCODE_NONUSBACKSLASH;
                case 162 -> SDLScancode.SDL_SCANCODE_NONUSHASH;
                case 256 -> InputConstants.KEY_ESCAPE;
                case 257 -> InputConstants.KEY_RETURN;
                case 258 -> InputConstants.KEY_TAB;
                case 259 -> InputConstants.KEY_BACKSPACE;
                case 260 -> InputConstants.KEY_INSERT;
                case 261 -> InputConstants.KEY_DELETE;
                case 262 -> InputConstants.KEY_RIGHT;
                case 263 -> InputConstants.KEY_LEFT;
                case 264 -> InputConstants.KEY_DOWN;
                case 265 -> InputConstants.KEY_UP;
                case 266 -> InputConstants.KEY_PAGEUP;
                case 267 -> InputConstants.KEY_PAGEDOWN;
                case 268 -> InputConstants.KEY_HOME;
                case 269 -> InputConstants.KEY_END;
                case 280 -> InputConstants.KEY_CAPSLOCK;
                case 281 -> InputConstants.KEY_SCROLLLOCK;
                case 282 -> InputConstants.KEY_NUMLOCK;
                case 283 -> InputConstants.KEY_PRINTSCREEN;
                case 284 -> InputConstants.KEY_PAUSE;
                case 330 -> SDLScancode.SDL_SCANCODE_KP_PERIOD;
                case 331 -> SDLScancode.SDL_SCANCODE_KP_DIVIDE;
                case 332 -> InputConstants.KEY_MULTIPLY;
                case 333 -> SDLScancode.SDL_SCANCODE_KP_MINUS;
                case 334 -> InputConstants.KEY_ADD;
                case 335 -> InputConstants.KEY_NUMPADENTER;
                case 336 -> InputConstants.KEY_NUMPADEQUALS;
                case 340 -> InputConstants.KEY_LSHIFT;
                case 341 -> InputConstants.KEY_LCONTROL;
                case 342 -> InputConstants.KEY_LALT;
                case 343 -> InputConstants.KEY_LGUI;
                case 344 -> InputConstants.KEY_RSHIFT;
                case 345 -> InputConstants.KEY_RCONTROL;
                case 346 -> InputConstants.KEY_RALT;
                case 347 -> InputConstants.KEY_RGUI;
                case 348 -> SDLScancode.SDL_SCANCODE_MENU;
                default -> InputConstants.UNKNOWN.getValue();
            };
        }
        return encodeKeyboardKey(migratedKey);
    }
}
