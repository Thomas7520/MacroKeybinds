package com.thomas7520.macrokeybinds;

import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.gui.MainMacroScreen;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod(MacroMod.MODID)
public class ForgeMacroMod {
    private static final String CATEGORY = "key.category.macrokeybinds.main";

    public ForgeMacroMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerKeybindingEvent);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new MainMacroScreen(parent)
                )
        );
    }

    private void setup(final FMLClientSetupEvent event) {
        MacroMod.setup();
    }

    private void registerKeybindingEvent(RegisterKeyMappingsEvent event) {
        MacroUtil.guiBinding = new KeyMapping(
                "key.macrokeybinds.openoptions.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        );

        MacroUtil.wheelBinding = new KeyMapping(
                "key.macrokeybinds.openwheel.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        );

        event.register(MacroUtil.guiBinding);
        event.register(MacroUtil.wheelBinding);
    }
}
