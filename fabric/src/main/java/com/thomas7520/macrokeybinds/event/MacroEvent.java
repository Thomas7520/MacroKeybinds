package com.thomas7520.macrokeybinds.event;

import com.thomas7520.macrokeybinds.util.MacroExecutor;
import com.thomas7520.macrokeybinds.util.MacroInputHandler;
import com.thomas7520.macrokeybinds.util.MacroSessionManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.HashSet;
import java.util.Set;

public class MacroEvent {

    private final Set<Integer> pressedKeys = new HashSet<>();

    public void onInputEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> MacroInputHandler.checkInputs(pressedKeys));
    }

    public void onTick() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> MacroExecutor.tick());
    }

    public void onServerConnect() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPacketListener connection = client.getConnection();
            if(connection == null || connection.getServerData() == null || connection.getServerData().isLan()) return;

            MacroSessionManager.connectToServer(connection.getServerData().ip);
        });
    }

    public void onServerDisconnect() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MacroSessionManager.disconnectFromServer());
    }
}
