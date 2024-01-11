package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.MainMacroScreen;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = MacroMod.MODID)
public class MacroEvent {



    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        if(MacroUtil.guiBinding.isDown()) {
            Minecraft.getInstance().setScreen(new MainMacroScreen());
        }

        if(Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) return;

        boolean isPress = event.getAction() == GLFW.GLFW_PRESS;
        boolean isRelease = event.getAction() == GLFW.GLFW_RELEASE;

        int key = event.getKey();

        MacroModifier modifier = switch (event.getModifiers()) {
            case 1 -> MacroModifier.SHIFT;
            case 4, 6 -> MacroModifier.ALT;
            case 2 -> MacroModifier.CONTROL;
            default -> MacroModifier.NONE;
        };

        onInputEvent(isPress, isRelease, key, modifier);
    }

    @SubscribeEvent
    public static void onMouseInputEvent(InputEvent.MouseButton event) {
        if(Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) return;

        boolean isPress = event.getAction() == GLFW.GLFW_PRESS;
        boolean isRelease = event.getAction() == GLFW.GLFW_RELEASE;
        int key = event.getButton();

        // No modifier in mouse input
        onInputEvent(isPress, isRelease, key, MacroModifier.NONE);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if(Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) return;

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
    }

    @SubscribeEvent
    public static void onServerConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        if(Minecraft.getInstance().getCurrentServer() == null) return;
        if(Minecraft.getInstance().getCurrentServer().isLan()) return;
        MacroUtil.initServerMacros(Minecraft.getInstance().getCurrentServer().ip);
    }

    @SubscribeEvent
    public static void onServerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        for (IMacro bind : MacroUtil.getGlobalKeybindsMap().values()) {

            if(bind instanceof ToggleMacro keybind) {
                keybind.setToggled(false);
            }

            if(bind instanceof RepeatMacro keybind) {
                keybind.setRepeat(false);
            }

            if(bind instanceof DelayedMacro keybind) {
                keybind.setStart(false);
            }
        }

        MacroUtil.getServerKeybinds().clear();
        MacroUtil.setServerIP("");
    }

    private static void onInputEvent(boolean isPress, boolean isRelease, int key, MacroModifier modifier) {
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
}
