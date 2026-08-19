package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.object.IMacro;
import com.thomas7520.macrokeybinds.object.MacroModifier;
import com.thomas7520.macrokeybinds.platform.Services;
import net.minecraft.client.KeyMapping;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class MacroUtil {

    private static final HashMap<UUID, IMacro> keybinds = new HashMap<>();
    private static final HashMap<UUID, IMacro> serverKeybinds = new HashMap<>();
    private static String serverIP = "";
    public static KeyMapping guiBinding;


    public static void initMacroDirectories() throws IOException {
        Path configDirectory = Services.PLATFORM.getConfigDirectory();
        Path macroKeybindsDirectory = configDirectory.resolve(MacroMod.MODID);

        Files.createDirectories(macroKeybindsDirectory);
        migrateLegacyMacroDirectory(configDirectory.resolve("global-macros"), getGlobalMacroDirectory());
        migrateLegacyMacroDirectory(configDirectory.resolve("servers-macros"), getServerMacrosDirectory());
        Files.createDirectories(getGlobalMacroDirectory());
        Files.createDirectories(getServerMacrosDirectory());
    }


    public static void initServerMacros(String ip) {
        MacroUtil.serverIP = ip;
        Path serverMacrosDirectory = getServerMacrosDirectory();
        Path directory = getServerMacroDirectory();

        try {
            Files.createDirectories(serverMacrosDirectory);
            migrateLegacyServerDirectory(serverMacrosDirectory, directory, ip);
            Files.createDirectories(directory);
        } catch(IOException e) {
            MacroMod.LOGGER.error("Failed to create server macros directory {}", directory.toAbsolutePath(), e);
            return;
        }

        File[] files = directory.toFile().listFiles(file -> file.isFile() && file.getName().endsWith(".json"));
        if(files == null) {
            MacroMod.LOGGER.error("Failed to list macros in {}", directory.toAbsolutePath());
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

    public static Path getGlobalMacroDirectory() {
        return Services.PLATFORM.getConfigDirectory().resolve(MacroMod.MODID).resolve("global-macros");
    }

    private static Path getServerMacrosDirectory() {
        return Services.PLATFORM.getConfigDirectory().resolve(MacroMod.MODID).resolve("servers-macros");
    }

    public static Path getServerMacroDirectory() {
        String normalizedIP = serverIP.strip().toLowerCase(Locale.ROOT);
        String readableIP = normalizedIP.replaceAll("[^a-z0-9._-]", "_");

        if(readableIP.length() > 80) {
            readableIP = readableIP.substring(0, 80);
        }
        if(readableIP.isBlank()) {
            readableIP = "server";
        }

        String addressID = UUID.nameUUIDFromBytes(normalizedIP.getBytes(StandardCharsets.UTF_8)).toString();
        return getServerMacrosDirectory().resolve(readableIP + "-" + addressID);
    }

    private static void migrateLegacyMacroDirectory(Path legacyDirectory, Path directory) throws IOException {
        if(!Files.isDirectory(legacyDirectory) || Files.exists(directory)) {
            return;
        }

        Files.move(legacyDirectory, directory);
        MacroMod.LOGGER.info("Migrated macro directory from {} to {}", legacyDirectory, directory);
    }

    private static void migrateLegacyServerDirectory(Path serverMacrosDirectory, Path directory, String ip) throws IOException {
        Path legacyDirectory;

        try {
            legacyDirectory = serverMacrosDirectory.resolve(ip).normalize();
        } catch(InvalidPathException ignored) {
            return;
        }

        if(!serverMacrosDirectory.equals(legacyDirectory.getParent()) || !Files.isDirectory(legacyDirectory) || Files.exists(directory)) {
            return;
        }

        Files.move(legacyDirectory, directory);
        MacroMod.LOGGER.info("Migrated server macros directory from {} to {}", legacyDirectory.getFileName(), directory.getFileName());
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
