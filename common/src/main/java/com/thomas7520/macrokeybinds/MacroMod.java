package com.thomas7520.macrokeybinds;

import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class MacroMod {
    public static final String MODID = "macrokeybinds";

    public static final Logger LOGGER = LogManager.getLogger();

    public static void setup() {
        try {
            MacroUtil.initMacroDirectories();
        } catch(IOException e) {
            LOGGER.error("Failed to initialize macro directories", e);
            return;
        }

        File directory = MacroUtil.getGlobalMacroDirectory().toFile();

        if(directory.mkdirs()) return;

        File[] files = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
        if(files == null) {
            LOGGER.error("Failed to list macros in {}", directory.getAbsolutePath());
            return;
        }

        for(File file : files) {
            try {
                IMacro macro = MacroFlow.getMacroFromFile(file);
                if(macro == null) {
                    LOGGER.error("Macro from {} is null", file.getAbsolutePath());
                    continue;
                }

                MacroUtil.getGlobalKeybindsMap().put(macro.getUUID(), macro);
            } catch(IOException | RuntimeException e) {
                LOGGER.error("Failed to load macro from {}", file.getAbsolutePath(), e);
            }
        }

        LOGGER.info(MacroUtil.getGlobalKeybindsMap().size() + " macros loaded");
    }
}
