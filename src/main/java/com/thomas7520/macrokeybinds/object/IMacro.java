package com.thomas7520.macrokeybinds.object;

import java.util.UUID;

public interface IMacro {

    UUID getUUID();

    String getName();

    String getPath();

    String getActionText();

    KeyAction getAction();

    void doAction();

    int getKey();

    String getKeyName();
}
