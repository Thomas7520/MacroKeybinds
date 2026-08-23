package com.thomas7520.macrokeybinds.object.wheel;

import net.minecraft.resources.Identifier;

import java.util.UUID;

public class WheelSlot {

    private UUID macroId;
    private String iconId;

    public WheelSlot(UUID macroId, Identifier iconId) {
        this.macroId = macroId;
        setIconId(iconId);
    }

    public UUID getMacroId() {
        return macroId;
    }

    public void setMacroId(UUID macroId) {
        this.macroId = macroId;
    }

    public Identifier getIconId() {
        return iconId == null ? null : Identifier.tryParse(iconId);
    }

    public void setIconId(Identifier iconId) {
        this.iconId = iconId == null ? null : iconId.toString();
    }
}
