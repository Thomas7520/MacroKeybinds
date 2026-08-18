package com.thomas7520.macrokeybinds;

import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.platform.Services;
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
            File directory = Services.PLATFORM.getConfigDirectory().resolve("global-macros").toFile();

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
}
