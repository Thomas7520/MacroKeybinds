package com.thomas7520.macrokeybinds.util;

import com.google.gson.JsonObject;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.MacroModifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores the optional enable/disable binding for each macro.
 *
 * The binding is serialized into the macro's existing JSON file by MacroFlow,
 * keeping existing macro classes and old configuration files backwards compatible.
 */
public final class MacroToggleBindings {

    private static final String KEY_PROPERTY = "toggleKey";
    private static final String KEY_NAME_PROPERTY = "toggleKeyName";
    private static final String MODIFIER_PROPERTY = "toggleModifier";

    private static final Map<UUID, Binding> BINDINGS = new HashMap<>();

    private MacroToggleBindings() {
    }

    public record Binding(int key, String keyName, MacroModifier modifier) {
        public Binding {
            if(keyName == null) keyName = "";
            if(modifier == null) modifier = MacroModifier.NONE;
        }
    }

    public static Binding get(UUID macroId) {
        return BINDINGS.get(macroId);
    }

    public static void set(UUID macroId, int key, String keyName, MacroModifier modifier) {
        if(key < 0) {
            clear(macroId);
            return;
        }

        BINDINGS.put(macroId, new Binding(key, keyName, modifier));
    }

    public static void clear(UUID macroId) {
        BINDINGS.remove(macroId);
    }

    public static boolean matches(IMacro macro, int key, MacroModifier modifier) {
        Binding binding = get(macro.getUUID());
        return binding != null && binding.key() == key && binding.modifier() == modifier;
    }

    /**
     * Checks both normal macro bindings and optional enable/disable bindings.
     */
    public static boolean isCombinationAssigned(UUID excludedMacroId, int key, MacroModifier modifier) {
        for(IMacro macro : MacroUtil.getAllMacros()) {
            if(excludedMacroId != null && excludedMacroId.equals(macro.getUUID())) continue;

            if(macro.getKey() == key && macro.getModifier() == modifier) return true;

            Binding binding = get(macro.getUUID());
            if(binding != null && binding.key() == key && binding.modifier() == modifier) return true;
        }

        return false;
    }

    public static void readFromJson(IMacro macro, JsonObject object) {
        if(macro == null || object == null || !object.has(KEY_PROPERTY)) {
            if(macro != null) clear(macro.getUUID());
            return;
        }

        try {
            int key = object.get(KEY_PROPERTY).getAsInt();
            if(key < 0) {
                clear(macro.getUUID());
                return;
            }

            String keyName = object.has(KEY_NAME_PROPERTY)
                    ? object.get(KEY_NAME_PROPERTY).getAsString()
                    : Integer.toString(key);
            MacroModifier modifier = object.has(MODIFIER_PROPERTY)
                    ? MacroModifier.valueOf(object.get(MODIFIER_PROPERTY).getAsString())
                    : MacroModifier.NONE;

            set(macro.getUUID(), key, keyName, modifier);
        } catch(RuntimeException ignored) {
            // The toggle binding is optional. A malformed optional binding should
            // not prevent the rest of an otherwise valid macro from loading.
            clear(macro.getUUID());
        }
    }

    public static void writeToJson(IMacro macro, JsonObject object) {
        Binding binding = get(macro.getUUID());
        if(binding == null) return;

        object.addProperty(KEY_PROPERTY, binding.key());
        object.addProperty(KEY_NAME_PROPERTY, binding.keyName());
        object.addProperty(MODIFIER_PROPERTY, binding.modifier().name());
    }
}
