package com.thomas7520.macrokeybinds;


import com.mojang.blaze3d.platform.InputConstants;
import com.thomas7520.macrokeybinds.event.MacroEvent;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.io.IOException;

public class MacroMod implements ModInitializer {
    public static final String MODID = "macrokeybinds";

    public static final Logger LOGGER = LogManager.getLogger();


    @Override
    public void onInitialize() {
        setup();
        registerKeybindingEvent();

        MacroEvent event = new MacroEvent();

        event.onInputEvent();
        event.onServerConnect();
        event.onServerDisconnect();
        event.onTick();

    }


    private void setup() {
        try {
            File directory = new File( FabricLoader.getInstance().getGameDir().resolve(FabricLoader.getInstance().getConfigDir()) + "/global-macros");

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

    private void registerKeybindingEvent() {
        MacroUtil.guiBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.macrokeybinds.openoptions.desc",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MODID, "key.categories.macrokeybinds"))
        ));

    }




}
