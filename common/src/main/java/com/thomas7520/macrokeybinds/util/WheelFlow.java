package com.thomas7520.macrokeybinds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.thomas7520.macrokeybinds.MacroMod;
import com.thomas7520.macrokeybinds.object.wheel.Wheel;
import com.thomas7520.macrokeybinds.platform.Services;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class WheelFlow {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private WheelFlow() {}

    public static Wheel getWheel() throws IOException {
        Path wheelFile = getWheelFile();

        if(Files.notExists(wheelFile)) {
            writeWheel(new Wheel());
        }

        try(Reader reader = Files.newBufferedReader(wheelFile, StandardCharsets.UTF_8)) {
            Wheel wheel = GSON.fromJson(reader, Wheel.class);
            if(wheel == null) throw new JsonParseException("Wheel file is empty");
            return wheel;
        }
    }

    public static void writeWheel(Wheel wheel) throws IOException {
        Path wheelFile = getWheelFile();
        Files.createDirectories(wheelFile.getParent());

        try(Writer writer = Files.newBufferedWriter(wheelFile, StandardCharsets.UTF_8)) {
            GSON.toJson(wheel, writer);
        }
    }

    private static Path getWheelFile() {
        return Services.PLATFORM.getConfigDirectory()
                .resolve(MacroMod.MODID)
                .resolve("wheel.json");
    }
}
