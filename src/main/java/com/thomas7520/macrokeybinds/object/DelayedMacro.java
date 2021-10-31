package com.thomas7520.macrokeybinds.object;

import com.google.gson.annotations.Expose;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class DelayedMacro implements IMacro {

    private UUID uuid;
    private String name;
    private String path;
    private String actionText;
    private int key;
    private KeyAction action;
    private long delayedTime;
    private String keyName;

    @Expose(deserialize = false)
    private long startTime;
    @Expose(deserialize = false)
    private boolean start;

    public DelayedMacro(UUID uuid, String name, String path, String actionText, int key, KeyAction action, long delayedTime, String keyName) {
        this.uuid = uuid;
        this.name = name;
        this.path = path;
        this.actionText = actionText;
        this.key = key;
        this.action = action;
        this.delayedTime = delayedTime;
        this.keyName = keyName;
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
    public String getPath() {
        return path;
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

    @Override
    public KeyAction getAction() {
        return action;
    }

    @Override
    public void doAction() {
        if(startTime + getDelayedTime() > System.currentTimeMillis()) return;

        setStart(false);

        switch (action) {
            case COMMAND -> Minecraft.getInstance().player.chat(getActionText().startsWith("/") ? getActionText() : "/" + getActionText());
            case MESSAGE -> Minecraft.getInstance().player.chat(getActionText());
        }
    }


}