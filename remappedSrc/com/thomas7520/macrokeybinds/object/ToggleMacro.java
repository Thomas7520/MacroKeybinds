package com.thomas7520.macrokeybinds.object;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.UUID;

public class ToggleMacro implements IMacro {

    private final UUID uuid;
    private final String name;
    private final String actionText;
    private int key;
    private final String keyName;
    private final KeyAction action;
    private final long cooldownTime;
    private boolean enable;
    private final MacroType macroType = MacroType.TOGGLE;

    private transient boolean isToggled;
    private transient long lastActionTime;
    private long createdTime;
    private MacroModifier modifier;

    public ToggleMacro(UUID uuid, String name, String actionText, int key, String keyName, KeyAction action, long cooldownTime, boolean enable, long createdTime, MacroModifier modifier) {
        this.uuid = uuid;
        this.name = name;
        this.actionText = actionText;
        this.key = key;
        this.keyName = keyName;
        this.action = action;
        this.cooldownTime = cooldownTime;
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

    public MacroType getType() {
        return macroType;
    }

    @Override
    public void doAction() {
        if(lastActionTime + cooldownTime > System.currentTimeMillis()) return;

        lastActionTime = System.currentTimeMillis();

        MinecraftClient client = MinecraftClient.getInstance();
        switch (action) {

            case COMMAND -> client.player.networkHandler.sendChatCommand((getActionText().startsWith("/") ? getActionText().substring(1) : getActionText()));
            case MESSAGE -> client.player.networkHandler.sendChatMessage(getActionText());
            case FILL_CHAT -> MinecraftClient.getInstance().setScreen(new ChatScreen(getActionText()));
        }
    }

    public long getCooldownTime() {
        return cooldownTime;
    }

    public boolean isToggled() {
        return isToggled;
    }

    public void setToggled(boolean toggled) {
        isToggled = toggled;
    }


}