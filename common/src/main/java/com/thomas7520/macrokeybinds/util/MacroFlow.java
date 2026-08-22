package com.thomas7520.macrokeybinds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.thomas7520.macrokeybinds.object.macro.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MacroFlow {

    private static final String CURRENT_VERSION = "2.0.0";

    public static IMacro getMacroFromFile(File file) throws IOException {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject object;

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            object = gson.fromJson(reader, JsonObject.class);
        }

        if(object == null) {
            throw new JsonParseException("Macro file is empty");
        }

        boolean migrated = migrateLegacyMacroTo1_4_0(object);

        IMacro macro;
        switch (MacroType.valueOf(object.get("macroType").getAsString())) {

            case SIMPLE -> macro = gson.fromJson(object, SimpleMacro.class);
            case TOGGLE -> macro = gson.fromJson(object, ToggleMacro.class);
            case REPEAT -> macro = gson.fromJson(object, RepeatMacro.class);
            case DELAYED -> macro = gson.fromJson(object, DelayedMacro.class);
            case COUNTED_REPEAT -> macro = gson.fromJson(object, CountedRepeatMacro.class);
            case ALTERNATE -> macro = gson.fromJson(object, AlternateMacro.class);


            default -> throw new IllegalStateException("Unexpected value: " + MacroType.valueOf(object.get("macroType").getAsString()));
        }

        validateMacro(macro);

        if(migrated) {
            writeMacroFile(macro, file.toPath());
        }

        return macro;
    }

    private static void validateMacro(IMacro macro) {
        if(macro == null || macro.getUUID() == null || macro.getType() == null || macro.getName() == null
                || macro.getActionText() == null || macro.getAction() == null || macro.getKeyName() == null
                || macro.getKey() < 0) {
            throw new JsonParseException("Macro is missing required data");
        }
    }

    private static boolean migrateLegacyMacroTo1_4_0(JsonObject object) {
        if(object.has("version")) return false;

        String macroType = object.get("macroType").getAsString();
        switch (macroType) {
            case "TOGGLE" -> object.addProperty("macroType", "REPEAT");
            case "REPEAT" -> object.addProperty("macroType", "TOGGLE");
        }

        object.addProperty("version", CURRENT_VERSION);
        return true;
    }

    public static void writeMacro(IMacro macro, String path) {
        try {
            Path macroFile = new File(path, macro.getUUID().toString() + ".json").toPath();
            writeMacroFile(macro, macroFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeMacroFile(IMacro macro, Path macroFile) throws IOException {
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject object = gson.toJsonTree(macro).getAsJsonObject();
        object.addProperty("version", CURRENT_VERSION);

        try (Writer writer = Files.newBufferedWriter(macroFile, StandardCharsets.UTF_8)) {
            gson.toJson(object, writer);
        }
    }

}
