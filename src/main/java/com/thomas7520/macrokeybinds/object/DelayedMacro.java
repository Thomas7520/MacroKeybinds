package com.thomas7520.macrokeybinds.object;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

import java.util.UUID;

public class DelayedMacro implements IMacro {

    private final UUID uuid;
    private final String name;
    private final String actionText;
    private int key;
    private final KeyAction action;
    private final long delayedTime;
    private final String keyName;
    private boolean enable;
    private MacroType macroType = MacroType.DELAYED;

    private transient long startTime;
    private transient boolean start;
    private long createdTime;
    private MacroModifier modifier;

    public DelayedMacro(UUID uuid, String name, String actionText, int key, KeyAction action, long delayedTime, String keyName, boolean enable, long createdTime, MacroModifier modifier) {
        this.uuid = uuid;
        this.name = name;
        this.actionText = actionText;
        this.key = key;
        this.action = action;
        this.delayedTime = delayedTime;
        this.keyName = keyName;
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
    public int getKey() {
        return key;
    }

    @Override
    public String getKeyName() {
        return keyName;
    }

    public long getDelayedTime() {
        return delayedTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }


    public boolean isEnable() {
        return enable;
    }

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
    public KeyAction getAction() {
        return action;
    }

    @Override
    public void doAction() {
        if(startTime + getDelayedTime() > System.currentTimeMillis()) return;

        setStart(false);

        switch (action) {
            case COMMAND :
                Minecraft.getInstance().player.chat(getActionText().startsWith("/") ? getActionText() : "/" + getActionText());
                break;
            case MESSAGE :
                Minecraft.getInstance().player.chat(getActionText());
                break;
            case FILL_CHAT :
                Minecraft.getInstance().setScreen(new ChatScreen(getActionText()));
                break;

        }
    }


}