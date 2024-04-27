package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.MacroModifier;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLPaths;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class MacroUtil {

    private static final HashMap<UUID, IMacro> keybinds = new HashMap<>();
    private static final HashMap<UUID, IMacro> serverKeybinds = new HashMap<>();
    private static String serverIP;
    public static KeyMapping guiBinding;



    public static void initServerMacros(String ip) {
        MacroUtil.serverIP = ip;
        try {
            File directory = new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()) + "/servers-macros/" + serverIP + "/");

            if(directory.mkdirs() || directory.listFiles() == null) return;

            for (File file : directory.listFiles()) {
                IMacro macro = MacroFlow.getMacroFromFile(file);
                if(macro == null) {
                    MacroMod.LOGGER.error(String.format("Macro from %s is null !", file.getAbsolutePath()));
                    continue;
                }
                MacroUtil.getServerKeybinds().put(macro.getUUID(), macro);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<UUID, IMacro> getGlobalKeybindsMap() {
        return keybinds;
    }

    public static HashMap<UUID, IMacro> getServerKeybinds() {
        return serverKeybinds;
    }

    public static String getServerIP() {
        return serverIP;
    }

    public static void setServerIP(String ip) {
        serverIP = ip;
    }
    public static boolean isNumeric(final CharSequence cs) {
        if (cs.length() == 0 || cs.toString().equalsIgnoreCase("\u0008")) {
            return true;
        }
        final int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return new BigInteger(cs.toString()).compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0;
    }



    public static boolean isCombinationAssigned(IMacro macro) {
        boolean serverKeyAssigned = false;
        boolean globalKeyAssigned = false;
        for (IMacro value : getServerKeybinds().values()) {
            if(macro.getUUID() != value.getUUID() && value.getKey() == macro.getKey() && value.getModifier() == macro.getModifier() ) {
                serverKeyAssigned = true;
                break;
            }
        }

        for (IMacro value : getGlobalKeybindsMap().values()) {
            if(macro.getUUID() != value.getUUID() && value.getKey() == macro.getKey() && value.getModifier() == macro.getModifier()) {
                globalKeyAssigned = true;
                break;
            }
        }


        return serverKeyAssigned || globalKeyAssigned;
    }

    public static boolean isCombinationAssigned(int key, MacroModifier modifier) {
        boolean serverKeyAssigned = false;
        boolean globalKeyAssigned = false;
        for (IMacro value : getServerKeybinds().values()) {
            if(value.getKey() == key && value.getModifier() == modifier) {
                serverKeyAssigned = true;
                break;
            }
        }

        for (IMacro value : getGlobalKeybindsMap().values()) {
            if(value.getKey() == key && value.getModifier() == modifier) {
                globalKeyAssigned = true;
                break;
            }
        }

        return serverKeyAssigned || globalKeyAssigned;
    }
}
