package com.thomas7520.macrokeybinds.object.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class AlternateMacro implements IMacro {

    private final UUID uuid;
    private final String name;
    private final String actionText;
    private final String secondActionText;
    private int key;
    private final String keyName;
    private final KeyAction action;
    private final KeyAction secondAction;
    private boolean enable;
    private final MacroType macroType = MacroType.ALTERNATE;
    private final long createdTime;
    private MacroModifier modifier;

    private transient long startTime;
    private transient boolean start;
    private transient boolean secondActionNext;

    public AlternateMacro(UUID uuid, String name, String actionText, String secondActionText, int key, String keyName, KeyAction action, KeyAction secondAction, boolean enable, long createdTime, MacroModifier modifier) {
        this.uuid = uuid;
        this.name = name;
        this.actionText = actionText;
        this.secondActionText = secondActionText;
        this.key = key;
        this.keyName = keyName;
        this.action = action;
        this.secondAction = secondAction;
        this.enable = enable;
        this.createdTime = createdTime;
        this.modifier = modifier;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getCreatedTime() {
        return createdTime;
    }

    @Override
    public MacroType getType() {
        return macroType;
    }

    @Override
    public String getActionText() {
        return actionText;
    }

    public String getSecondActionText() {
        return secondActionText;
    }

    @Override
    public KeyAction getAction() {
        return action;
    }

    public KeyAction getSecondAction() {
        return secondAction;
    }

    @Override
    public void doAction() {
        if(!start || startTime + 50 > System.currentTimeMillis()) return;

        start = false;

        if(secondActionNext) {
            executeAction(secondAction, secondActionText);
        } else {
            executeAction(action, actionText);
        }

        secondActionNext = !secondActionNext;
    }

    private void executeAction(KeyAction actionToExecute, String text) {
        Minecraft client = Minecraft.getInstance();
        switch (actionToExecute) {
            case COMMAND -> client.player.connection.sendCommand(text.startsWith("/") ? text.substring(1) : text);
            case MESSAGE -> {
                if(text.startsWith("/")) {
                    client.player.connection.sendCommand(text.substring(1));
                } else {
                    client.player.connection.sendChat(text);
                }
            }
            case FILL_CHAT -> client.setScreenAndShow(new ChatScreen(text, false));
            case LOCAL_MESSAGE -> client.gui.hud.getChat().addClientSystemMessage(Component.literal(text));
        }
    }

    public void start() {
        startTime = System.currentTimeMillis();
        start = true;
    }

    public boolean isStart() {
        return start;
    }

    public boolean isSecondActionNext() {
        return secondActionNext;
    }

    public void reset() {
        startTime = 0;
        start = false;
        secondActionNext = false;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void setEnable(boolean state) {
        enable = state;
        if(!state) reset();
    }

    @Override
    public void setKey(int key) {
        this.key = key;
    }

    @Override
    public MacroModifier getModifier() {
        return modifier == null ? MacroModifier.NONE : modifier;
    }

    @Override
    public void setModifier(MacroModifier modifier) {
        this.modifier = modifier;
    }
}
