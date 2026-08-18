package com.thomas7520.macrokeybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@Mod(MacroMod.MODID)
public class NeoForgeMacroMod {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(MacroMod.MODID, "key.categories.macrokeybinds")
    );

    public NeoForgeMacroMod(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerKeybindingEvent);
    }

    private void setup(final FMLClientSetupEvent event) {
        MacroMod.setup();
    }

    private void registerKeybindingEvent(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        MacroUtil.guiBinding = new KeyMapping(
                "key.macrokeybinds.openoptions.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        );
        event.register(MacroUtil.guiBinding);
    }
}
