package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.object.macro.AlternateMacro;
import com.thomas7520.macrokeybinds.object.macro.CountedRepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.DelayedMacro;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.RepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.ToggleMacro;

public class MacroSessionManager {


    public static void connectToServer(String serverAddress) {
        if(serverAddress == null || serverAddress.isBlank()) return;
        MacroUtil.initServerMacros(serverAddress);
    }

    public static void disconnectFromServer() {
        resetRuntimeState();
        MacroUtil.getServerKeybinds().clear();
        MacroUtil.setServerIP("");
    }

    private static void resetRuntimeState() {
        for(IMacro macro : MacroUtil.getAllMacros()) {
            if(macro instanceof ToggleMacro toggleMacro) toggleMacro.setToggled(false);
            if(macro instanceof RepeatMacro repeatMacro) repeatMacro.setRepeat(false);
            if(macro instanceof DelayedMacro delayedMacro) delayedMacro.setStart(false);
            if(macro instanceof CountedRepeatMacro countedRepeatMacro) countedRepeatMacro.cancel();
            if(macro instanceof AlternateMacro alternateMacro) alternateMacro.reset();
        }
    }
}
