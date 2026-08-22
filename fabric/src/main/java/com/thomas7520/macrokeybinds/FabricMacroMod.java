package com.thomas7520.macrokeybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.event.MacroEvent;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class FabricMacroMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MacroMod.setup();

        String category = "key.category.macrokeybinds.main";

        MacroUtil.guiBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macrokeybinds.openoptions.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                category
        ));

        MacroUtil.wheelBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.macrokeybinds.openwheel.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                category
        ));


        MacroEvent event = new MacroEvent();
        event.onInputEvent();
        event.onServerConnect();
        event.onServerDisconnect();
        event.onTick();
    }
}
