package com.thomas7520.macrokeybinds.object.wheel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Wheel {

    public static final int MAX_MACROS = 6;

    private WheelMode mode;
    private List<WheelSlot> macros;

    public Wheel() {
        this.mode = WheelMode.CLICK;
        this.macros = new ArrayList<>(Collections.nCopies(MAX_MACROS, null));
    }

    public Wheel(WheelMode mode, List<WheelSlot> macros) {
        this.mode = mode;
        this.macros = macros;
    }

    public WheelMode getMode() {
        return mode;
    }

    public void setMode(WheelMode mode) {
        this.mode = mode;
    }

    public List<WheelSlot> getMacros() {
        return macros;
    }

    public void setMacros(List<WheelSlot> macros) {
        this.macros = macros;
    }
}
