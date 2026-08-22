package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.object.macro.KeyAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

public final class MacroActionExecutor {

    private MacroActionExecutor() {
    }

    public static void execute(KeyAction action, String text) {
        Minecraft client = Minecraft.getInstance();

        switch(action) {
            case COMMAND -> client.player.connection.sendCommand(text.startsWith("/") ? text.substring(1) : text);
            case MESSAGE -> {
                if(text.startsWith("/")) {
                    client.player.connection.sendCommand(text.substring(1));
                } else {
                    client.player.connection.sendChat(text);
                }
            }
            case FILL_CHAT -> client.gui.setScreen(new ChatScreen(text, false));
            case LOCAL_MESSAGE -> client.gui.hud.getChat().addClientSystemMessage(Component.literal(text));
        }
    }
}