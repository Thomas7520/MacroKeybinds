package com.thomas7520.macrokeybinds.object;

import java.util.UUID;

public interface IMacro {

    UUID getUUID();

    String getName();

    long getCreatedTime();

    MacroType getType();

    String getActionText();

    KeyAction getAction();

    void doAction();

    int getKey();

    String getKeyName();

    boolean isEnable();

    void setEnable(boolean state);

    void setKey(int key);

    MacroModifier getModifier();

    void setModifier(MacroModifier macroModifierSelect);
}
