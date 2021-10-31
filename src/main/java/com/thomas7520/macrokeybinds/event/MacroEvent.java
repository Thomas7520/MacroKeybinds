package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.gui.MainMacroScreen;
import com.thomas7520.macrokeybinds.object.*;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = MacroMod.MODID)
public class MacroEvent {


    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.KeyInputEvent event) {
        if(Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) return;

        boolean isPress = event.getAction() == GLFW.GLFW_PRESS;
        boolean isRelease = event.getAction() == GLFW.GLFW_RELEASE;
        boolean isRepeat = event.getAction() == GLFW.GLFW_REPEAT;
        int key = event.getKey();

        onInputEvent(isPress, isRelease, isRepeat, key);

    }

    @SubscribeEvent
    public static void onMouseInputEvent(InputEvent.MouseInputEvent event) {
        if(Minecraft.getInstance().level == null || Minecraft.getInstance().screen != null) return;

        boolean isPress = event.getAction() == GLFW.GLFW_PRESS;
        boolean isRelease = event.getAction() == GLFW.GLFW_RELEASE;
        int key = event.getButton();

        onInputEvent(isPress, isRelease, isPress, key);
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        // TODO est-ce que c'est utile ??? if(event.phase != TickEvent.Phase.END) return;
        for (IMacro iKeybind : MacroUtil.getGlobalKeybindsMap().keySet()) {

            if(iKeybind instanceof ToggleMacro && ((ToggleMacro) iKeybind).isToggled()) {
                iKeybind.doAction();
            }

            if(iKeybind instanceof DelayedMacro) {
                DelayedMacro keybind = (DelayedMacro) iKeybind;
                if(!keybind.isStart()) continue;

                if(keybind.getStartTime() + keybind.getDelayedTime() < System.currentTimeMillis()) {
                    keybind.setStart(true);
                    keybind.doAction();
                }
            }


        }
    }

    private static void onInputEvent(boolean isPress, boolean isRelease, boolean isRepeat, int key) {
        if(isPress && key == GLFW.GLFW_KEY_B) {
            Minecraft.getInstance().setScreen(new MainMacroScreen());
        }

        for (IMacro bind : MacroUtil.getGlobalKeybindsMap().keySet()) {
            if(key != bind.getKey()) continue;

            if (bind instanceof SimpleMacro) {
                if(isPress && !((SimpleMacro) bind).isPressed()) {
                    bind.doAction();
                    ((SimpleMacro) bind).setPressed(true);
                } else if(isRelease && ((SimpleMacro) bind).isPressed()) {
                    ((SimpleMacro) bind).setPressed(false);
                }
            }

            if(bind instanceof RepeatMacro) {
                if(isPress || isRepeat) {
                    ((RepeatMacro) bind).setRepeat(true);
                } else if(isRelease) {
                    ((RepeatMacro) bind).setRepeat(false);
                }
            }

            if(bind instanceof ToggleMacro) {
                if(isPress) {
                    ((ToggleMacro) bind).setToggled(!((ToggleMacro) bind).isToggled());
                }
            }

            if(bind instanceof DelayedMacro) {
                if(isPress && !((DelayedMacro) bind).isStart()) {
                    ((DelayedMacro) bind).setStartTime(System.currentTimeMillis());
                    ((DelayedMacro) bind).setStart(true);
                }
            }
        }
    }
}
