package com.thomas7520.macrokeybinds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thomas7520.macrokeybinds.object.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class MacroFlow {

    private static final String CURRENT_VERSION = "1.4.0";

    public static IMacro getMacroFromFile(File file) throws IOException {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject object;

        try (Reader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            object = gson.fromJson(reader, JsonObject.class);
        }

        boolean migrated = migrateLegacyMacroTo1_4_0(object);

        IMacro macro;
        switch (MacroType.valueOf(object.get("macroType").getAsString())) {

            case SIMPLE -> macro = gson.fromJson(object, SimpleMacro.class);
            case TOGGLE -> macro = gson.fromJson(object, ToggleMacro.class);
            case REPEAT -> macro = gson.fromJson(object, RepeatMacro.class);
            case DELAYED -> macro = gson.fromJson(object, DelayedMacro.class);


            default -> throw new IllegalStateException("Unexpected value: " + MacroType.valueOf(object.get("macroType").getAsString()));
        }

        if(migrated) {
            writeMacroFile(macro, file.toPath());
        }

        return macro;
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
