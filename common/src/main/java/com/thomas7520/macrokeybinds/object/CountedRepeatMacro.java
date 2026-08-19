package com.thomas7520.macrokeybinds.object;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.UUID;

public class CountedRepeatMacro implements IMacro {

    private final UUID uuid;
    private final String name;
    private final String actionText;
    private int key;
    private final String keyName;
    private final KeyAction action;
    private final long intervalTime;
    private final int repeatCount;
    private boolean enable;
    private final MacroType macroType = MacroType.COUNTED_REPEAT;
    private final long createdTime;

    private transient boolean running;
    private transient int completedExecutions;
    private transient long lastActionTime;
    private MacroModifier modifier;

    public CountedRepeatMacro(UUID uuid, String name, String actionText, int key, String keyName, KeyAction action, long intervalTime, int repeatCount, boolean enable, long createdTime, MacroModifier modifier) {
        this.uuid = uuid;
        this.name = name;
        this.actionText = actionText;
        this.key = key;
        this.keyName = keyName;
        this.action = action;
        this.intervalTime = intervalTime;
        this.repeatCount = repeatCount;
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

    @Override
    public KeyAction getAction() {
        return action;
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
    public void setEnable(boolean enable) {
        this.enable = enable;
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

    @Override
    public void doAction() {
        if(!running) return;

        long currentTime = System.currentTimeMillis();
        if(completedExecutions > 0 && lastActionTime + intervalTime > currentTime) return;

        lastActionTime = currentTime;

        Minecraft client = Minecraft.getInstance();
        switch (action) {
            case COMMAND -> client.player.connection.sendCommand((getActionText().startsWith("/") ? getActionText().substring(1) : getActionText()));
            case MESSAGE -> {
                if(getActionText().startsWith("/")) {
                    client.player.connection.sendCommand(getActionText().substring(1));
                } else {
                    client.player.connection.sendChat(getActionText());
                }
            }
            case FILL_CHAT -> Minecraft.getInstance().setScreenAndShow(new ChatScreen(getActionText(), false));
        }

        completedExecutions++;
        if(completedExecutions >= repeatCount) {
            cancel();
        }
    }

    public void start() {
        if(running || repeatCount <= 0) return;

        completedExecutions = 0;
        lastActionTime = 0;
        running = true;
    }

    public void cancel() {
        running = false;
        completedExecutions = 0;
        lastActionTime = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public long getIntervalTime() {
        return intervalTime;
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
