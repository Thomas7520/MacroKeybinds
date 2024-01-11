package com.thomas7520.macrokeybinds;

import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.util.MacroFlow;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

@Mod("macrokeybinds")
public class MacroMod
{
    public static final String MODID = "macrokeybinds";

    public static final Logger LOGGER = LogManager.getLogger();


    public MacroMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLClientSetupEvent event)
    {
        MacroUtil.registerBindings();

        try {
            File directory = new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + "/global-macros");

            if(directory.mkdirs() || directory.listFiles() == null) return;

            for (File file : directory.listFiles()) {
                IMacro macro = MacroFlow.getMacroFromFile(file);
                if(macro == null) {
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
