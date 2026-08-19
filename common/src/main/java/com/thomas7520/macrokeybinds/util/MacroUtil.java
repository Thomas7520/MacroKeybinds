package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.MacroModifier;
import com.thomas7520.macrokeybinds.platform.Services;
import net.minecraft.client.KeyMapping;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class MacroUtil {

    private static final HashMap<UUID, IMacro> keybinds = new HashMap<>();
    private static final HashMap<UUID, IMacro> serverKeybinds = new HashMap<>();
    private static String serverIP = "";
    public static KeyMapping guiBinding;



    public static void initServerMacros(String ip) {
        MacroUtil.serverIP = ip;
        File directory = Services.PLATFORM.getConfigDirectory().resolve("servers-macros").resolve(serverIP).toFile();

        if(directory.mkdirs()) return;

        File[] files = directory.listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
        if(files == null) {
            MacroMod.LOGGER.error("Failed to list macros in {}", directory.getAbsolutePath());
            return;
        }

        for(File file : files) {
            try {
                IMacro macro = MacroFlow.getMacroFromFile(file);
                if(macro == null) {
                    MacroMod.LOGGER.error("Macro from {} is null", file.getAbsolutePath());
                    continue;
                }

                MacroUtil.getServerKeybinds().put(macro.getUUID(), macro);
            } catch(IOException | RuntimeException e) {
                MacroMod.LOGGER.error("Failed to load macro from {}", file.getAbsolutePath(), e);
            }
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
        if (cs.length() == 0 || cs.toString().equalsIgnoreCase("")) {
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
        return isCombinationAssigned(macro, macro.getKey(), macro.getModifier());
    }

    public static boolean isCombinationAssigned(IMacro macro, int key, MacroModifier modifier) {
        boolean serverKeyAssigned = false;
        boolean globalKeyAssigned = false;
        for (IMacro value : getServerKeybinds().values()) {
            if(!macro.getUUID().equals(value.getUUID()) && value.getKey() == key && value.getModifier() == modifier) {
                serverKeyAssigned = true;
                break;
            }
        }

        for (IMacro value : getGlobalKeybindsMap().values()) {
            if(!macro.getUUID().equals(value.getUUID()) && value.getKey() == key && value.getModifier() == modifier) {
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
