package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.util.MacroExecutor;
import com.thomas7520.macrokeybinds.util.MacroInputHandler;
import com.thomas7520.macrokeybinds.util.MacroSessionManager;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = MacroMod.MODID)
public class MacroEvent {

    @SubscribeEvent
    public static void onKeyInputEvent(InputEvent.Key event) {
        MacroInputHandler.handleKeyAction(event.getAction(), event.getKey(), event.getModifiers());
    }

    @SubscribeEvent
    public static void onMouseInputEvent(InputEvent.MouseButton.Post event) {
        MacroInputHandler.handleMouseAction(event.getAction(), event.getButton());
    }

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        MacroExecutor.tick();
    }

    @SubscribeEvent
    public static void onServerConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft client = Minecraft.getInstance();
        if(client.getCurrentServer() == null || client.getCurrentServer().isLan()) return;

        MacroSessionManager.connectToServer(client.getCurrentServer().ip);
    }

    @SubscribeEvent
    public static void onServerDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        MacroSessionManager.disconnectFromServer();
    }
}
