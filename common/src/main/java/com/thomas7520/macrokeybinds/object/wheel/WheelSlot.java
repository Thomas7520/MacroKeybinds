package com.thomas7520.macrokeybinds.object.wheel;

import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class WheelSlot {

    private UUID macroId;
    private ResourceLocation iconId;

    public WheelSlot(UUID macroId, ResourceLocation iconId) {
        this.macroId = macroId;
        this.iconId = iconId;
    }

    public UUID getMacroId() {
        return macroId;
    }

    public void setMacroId(UUID macroId) {
        this.macroId = macroId;
    }

    public ResourceLocation getIconId() {
        return iconId;
    }

    public void setIconId(ResourceLocation iconId) {
        this.iconId = iconId;
    }
}
