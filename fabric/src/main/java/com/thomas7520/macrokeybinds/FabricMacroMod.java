package com.thomas7520.macrokeybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.event.MacroEvent;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class FabricMacroMod implements ModInitializer {

    @Override
    public void onInitialize() {
        MacroMod.setup();
        MacroUtil.guiBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.macrokeybinds.openoptions.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MacroMod.MODID, "key.categories.macrokeybinds"))
        ));

        MacroEvent event = new MacroEvent();
        event.onInputEvent();
        event.onServerConnect();
        event.onServerDisconnect();
        event.onTick();
    }
}
