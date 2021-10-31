package com.thomas7520.macrokeybinds.object;

import com.google.gson.annotations.Expose;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class RepeatMacro implements IMacro {

    private UUID uuid;
    private String name;
    private String path;
    private String actionText;
    private int key;
    private String keyName;
    private KeyAction action;
    private long cooldownTime;

    @Expose(deserialize = false)
    private boolean doRepeat;
    @Expose(deserialize = false)
    private long lastActionTime;


    public RepeatMacro(UUID uuid, String name, String path, String actionText, int key, String keyName, KeyAction action, long cooldownTime) {
        this.uuid = uuid;
        this.name = name;
        this.path = path;
        this.actionText = actionText;
        this.key = key;
        this.keyName = keyName;
        this.action = action;
        this.cooldownTime = cooldownTime;
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

    @Override
    public KeyAction getAction() {
        return action;
    }

    @Override
    public void doAction() {
        if(lastActionTime + cooldownTime > System.currentTimeMillis()) return;

        lastActionTime = System.currentTimeMillis();

        switch (action) {
            case COMMAND -> Minecraft.getInstance().player.chat(getActionText().startsWith("/") ? getActionText().substring(1) : getActionText());
            case MESSAGE -> Minecraft.getInstance().player.chat(getActionText());
        }
    }

    public long getCooldownTime() {
        return cooldownTime;
    }

    public boolean isRepeat() {
        return doRepeat;
    }

    public void setRepeat(boolean repeat) {
        this.doRepeat = repeat;
    }


}
