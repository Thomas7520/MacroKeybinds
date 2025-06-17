package com.thomas7520.macrokeybinds.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.thomas7520.macrokeybinds.object.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class MacroFlow {


    public static IMacro getMacroFromFile(File file) throws IOException {

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileInputStream fileInputStream = new FileInputStream(file);

        Reader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);


        JsonObject object = new GsonBuilder().create().fromJson(new BufferedReader(new FileReader(file)), JsonObject.class);

        IMacro macro;
        switch (MacroType.valueOf(object.get("macroType").getAsString())) {

            case SIMPLE -> macro = gson.fromJson(reader, SimpleMacro.class);
            case TOGGLE -> macro = gson.fromJson(reader, ToggleMacro.class);
            case REPEAT -> macro = gson.fromJson(reader, RepeatMacro.class);
            case DELAYED -> macro = gson.fromJson(reader, DelayedMacro.class);


            default -> throw new IllegalStateException("Unexpected value: " + MacroType.valueOf(object.get("type").getAsString()));
        }


        reader.close();
        return macro;
    }

    public static void writeMacro(IMacro macro, String path) {
        try {
            final Gson gson = new GsonBuilder().setPrettyPrinting().create();

            File macroFile = new File(path + "/" + macro.getUUID().toString() +  ".json");
            macroFile.createNewFile();

            FileOutputStream fileOutputStream = new FileOutputStream(macroFile);
            Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);

            //Writer writer = Files.newBufferedWriter(macroFile.toPath());
            gson.toJson(macro, writer);


            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
