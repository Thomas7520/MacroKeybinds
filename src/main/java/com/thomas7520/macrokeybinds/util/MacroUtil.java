package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.object.*;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.UUID;

public class MacroUtil {

    private static HashMap<IMacro, UUID> keybinds = new HashMap<>();
    private static HashMap<IMacro, UUID> serverKeybinds = new HashMap<>();


    public static HashMap<IMacro, UUID> getGlobalKeybindsMap() {
        return keybinds;
    }

    public static HashMap<IMacro, UUID> getServerKeybinds() {
        return serverKeybinds;
    }

    //    public Optional<SimpleKeybind> getKeybindByName(String name) {
//        return keybindsMap.keySet().stream().filter(simpleKeybind -> simpleKeybind.getName().equalsIgnoreCase(name)).findAny();
//    }

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
}
