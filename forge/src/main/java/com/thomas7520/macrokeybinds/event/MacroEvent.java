package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.util.MacroExecutor;
import com.thomas7520.macrokeybinds.util.MacroInputHandler;
import com.thomas7520.macrokeybinds.util.MacroSessionManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT, modid = MacroMod.MODID)
public class MacroEvent {

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        MacroInputHandler.handleKeyAction(event.getAction(), event.getKey(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onMouseInputEvent(InputEvent.MouseButton event) {
        MacroInputHandler.handleMouseAction(event.getAction(), event.getButton());
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            MacroExecutor.tick();
        }
    }

    @SubscribeEvent
    public static void onServerConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft client = Minecraft.getInstance();
        if (client.getCurrentServer() == null || client.getCurrentServer().isLan()) {
            return;
        }

        MacroSessionManager.connectToServer(client.getCurrentServer().ip);
    }

    @SubscribeEvent
    public static void onServerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        MacroSessionManager.disconnectFromServer();
    }
}
