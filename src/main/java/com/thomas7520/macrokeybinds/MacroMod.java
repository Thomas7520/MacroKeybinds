package com.thomas7520.macrokeybinds;


import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

@Mod(MacroMod.MODID)
public class MacroMod {
    public static final String MODID = "macrokeybinds";

    public static final Logger LOGGER = LogManager.getLogger();

    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(MODID, MODID));


    public MacroMod(IEventBus modEventBus) {
        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerKeybindingEvent);
    }

    private void setup(final FMLClientSetupEvent event) {
        try {
            File directory = new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + "/global-macros");

            if (directory.mkdirs() || directory.listFiles() == null) return;

            for (File file : directory.listFiles()) {
                IMacro macro = MacroFlow.getMacroFromFile(file);
                if (macro == null) {
                    LOGGER.error(String.format("Macro from %s is null !", file.getAbsolutePath()));
                    continue;
                }
                MacroUtil.getGlobalKeybindsMap().put(macro.getUUID(), macro);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER.info(MacroUtil.getGlobalKeybindsMap().size() + " macros loaded");
    }

    private void registerKeybindingEvent(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);

        MacroUtil.guiBinding = new KeyMapping("key.macrokeybinds.openoptions.desc" , GLFW.GLFW_KEY_N, CATEGORY);

        event.register(MacroUtil.guiBinding);
    }
}
