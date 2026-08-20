package com.thomas7520.macrokeybinds.object.wheel;

import net.minecraft.resources.Identifier;

import java.util.UUID;

public class WheelSlot {

    private UUID macroId;
    private Identifier iconId;

    public WheelSlot(UUID macroId, Identifier iconId) {
        this.macroId = macroId;
        this.iconId = iconId;
    }

    public UUID getMacroId() {
        return macroId;
    }

    public void setMacroId(UUID macroId) {
        this.macroId = macroId;
    }

    public Identifier getIconId() {
        return iconId;
    }

    public void setIconId(Identifier iconId) {
        this.iconId = iconId;
    }
}
