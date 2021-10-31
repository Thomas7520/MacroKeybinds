package com.thomas7520.macrokeybinds.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.thomas7520.macrokeybinds.object.SimpleMacro;

public class MacroFlow {


    public SimpleMacro readKeybind() {
        return new Gson().fromJson((JsonElement) null, null);
    }

    public void writeKeybind(SimpleMacro simpleKeybind) {

    }

    public void saveAll() {

    }
}
