package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.gui.MainMacroScreen;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MacroEvent {

    private final List<Integer> keysPressed = new ArrayList<>();

    public void onKeyInputEvent() {

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if(MacroUtil.guiBinding.isPressed()) {
                MinecraftClient.getInstance().setScreen(new MainMacroScreen());
            }

            if(MinecraftClient.getInstance().world == null || MinecraftClient.getInstance().currentScreen != null) return;

            Collection<IMacro> macros = new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values());
            macros.addAll(MacroUtil.getServerKeybinds().values());

            List<Integer> macroKeys = macros.stream().filter(IMacro::isEnable).map(IMacro::getKey).toList();

            for (Integer key : macroKeys) {
                MacroModifier modifier = switch (getAnyModifierKeyPressed()) {
                    case 340, 344 -> MacroModifier.SHIFT;
                    case 342, 346 -> MacroModifier.ALT;
                    case 341, 345 -> MacroModifier.CONTROL;
                    default -> MacroModifier.NONE;
                };

                int state;

                if(key == 0 || key <= 7) {
                    state = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), key);
                    modifier = MacroModifier.NONE;
                } else {
                    state = GLFW.glfwGetKey(client.getWindow().getHandle(), key);
                }

                boolean isPress = state == GLFW.GLFW_PRESS;
                boolean isRelease = state == GLFW.GLFW_RELEASE;

                if(isRelease) {
                    keysPressed.remove(key);
                }

                if(isPress) {
                    if(keysPressed.contains(key)) return;

                    keysPressed.add(key);
                }


                onInputEvent(isPress, isRelease, key, modifier);
            }
        });



    }


    public void onMouseInputEvent() {
    }

    public void onTick() {

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {

            if(MinecraftClient.getInstance().world == null) return;

            Collection<IMacro> macros = new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values());
            macros.addAll(MacroUtil.getServerKeybinds().values());

            for (IMacro bind : macros) {

                if(bind instanceof SimpleMacro && ((SimpleMacro) bind).isStart()) {
                    bind.doAction();
                }
                if(bind instanceof RepeatMacro && ((RepeatMacro) bind).isRepeat()) {
                    bind.doAction();
                }

                if(bind instanceof ToggleMacro && ((ToggleMacro) bind).isToggled()) {
                    if(!bind.isEnable()) {
                        ((ToggleMacro) bind).setToggled(false);
                        return;
                    }

                    bind.doAction();
                }

                if(bind instanceof DelayedMacro) {
                    DelayedMacro keybind = (DelayedMacro) bind;

                    if(!keybind.isEnable()) {
                        keybind.setStart(false);
                        return;
                    }

                    if(!keybind.isStart()) continue;

                    if(keybind.getStartTime() + keybind.getDelayedTime() < System.currentTimeMillis()) {
                        keybind.setStart(true);
                        keybind.doAction();
                    }
                }


            }
        });
    }

    public void onServerConnect() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();

            if(networkHandler == null || networkHandler.getServerInfo() == null) return;

            if(networkHandler.getServerInfo().isLocal()) return;

            MacroUtil.initServerMacros(networkHandler.getServerInfo().address);
        });


    }

    public void onServerDisconnect() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {

            Collection<IMacro> macros = new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values());
            macros.addAll(MacroUtil.getServerKeybinds().values());

            for (IMacro bind : macros) {

                if (bind instanceof ToggleMacro keybind) {
                    keybind.setToggled(false);
                }

                if (bind instanceof RepeatMacro keybind) {
                    keybind.setRepeat(false);
                }

                if (bind instanceof DelayedMacro keybind) {
                    keybind.setStart(false);
                }
            }

            MacroUtil.getServerKeybinds().clear();
            MacroUtil.setServerIP("");
        });
    }

    private void onInputEvent(boolean isPress, boolean isRelease, int key, MacroModifier modifier) {
        Collection<IMacro> macros = new ArrayList<>(MacroUtil.getGlobalKeybindsMap().values());
        macros.addAll(MacroUtil.getServerKeybinds().values());

        for (IMacro bind : macros) {
            if(!bind.isEnable()) continue;

            if(key != bind.getKey()) continue;

            boolean modifierPressed = bind.getModifier() == MacroModifier.NONE || bind.getModifier() == modifier;

            if (bind instanceof SimpleMacro) {
                if(isPress && modifierPressed) {
                    ((SimpleMacro) bind).setStartTime(System.currentTimeMillis());
                    ((SimpleMacro) bind).setStart(true);
                }
            }

            if(bind instanceof RepeatMacro) {
                if(isPress && modifierPressed) {
                    ((RepeatMacro) bind).setRepeat(!((RepeatMacro) bind).isRepeat());
                }
            }

            if(bind instanceof ToggleMacro) {
                if(isPress && modifierPressed) {
                    ((ToggleMacro) bind).setToggled(true);
                } else if(isRelease || !modifierPressed) {
                    ((ToggleMacro) bind).setToggled(false);
                }
            }

            if(bind instanceof DelayedMacro) {
                if(isPress && modifierPressed && !((DelayedMacro) bind).isStart()) {
                    ((DelayedMacro) bind).setStartTime(System.currentTimeMillis());
                    ((DelayedMacro) bind).setStart(true);
                }
            }
        }
    }

    private int getAnyModifierKeyPressed() {
        int[] modifierKeys = {GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT, GLFW.GLFW_KEY_LEFT_ALT, GLFW.GLFW_KEY_RIGHT_ALT, GLFW.GLFW_KEY_LEFT_CONTROL, GLFW.GLFW_KEY_RIGHT_CONTROL};

        for (int key : modifierKeys) {
            if (GLFW.glfwGetKey(MinecraftClient.getInstance().getWindow().getHandle(), key) == GLFW.GLFW_PRESS) {
                return key;
            }
        }
        return -1;
    }

}