package com.thomas7520.macrokeybinds.util;

import com.thomas7520.macrokeybinds.object.macro.AlternateMacro;
import com.thomas7520.macrokeybinds.object.macro.CountedRepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.DelayedMacro;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.RepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.SimpleMacro;
import com.thomas7520.macrokeybinds.object.macro.ToggleMacro;
import net.minecraft.client.Minecraft;

public class MacroExecutor {

    public static void trigger(IMacro macro) {
        if(macro == null || !macro.isEnable()) return;

        switch(macro.getType()) {
            case SIMPLE -> {
                SimpleMacro simpleMacro = (SimpleMacro) macro;
                simpleMacro.setStartTime(System.currentTimeMillis());
                simpleMacro.setStart(true);
            }
            case DELAYED -> {
                DelayedMacro delayedMacro = (DelayedMacro) macro;
                if(!delayedMacro.isStart()) {
                    delayedMacro.setStartTime(System.currentTimeMillis());
                    delayedMacro.setStart(true);
                }
            }
            case COUNTED_REPEAT -> ((CountedRepeatMacro) macro).start();
            case ALTERNATE -> ((AlternateMacro) macro).start();
            case TOGGLE -> ((ToggleMacro)macro).setToggled(!((ToggleMacro) macro).isToggled());
            default -> throw new IllegalStateException("Unexpected value: " + macro.getType());
        }
    }

    public static void tick() {
        if(Minecraft.getInstance().level == null) return;

        for(IMacro macro : MacroUtil.getAllMacros()) {
            if(macro instanceof SimpleMacro simpleMacro && simpleMacro.isStart()) {
                macro.doAction();
            }

            if(macro instanceof AlternateMacro alternateMacro && alternateMacro.isStart()) {
                macro.doAction();
            }

            if(macro instanceof RepeatMacro repeatMacro && repeatMacro.isRepeat()) {
                if(!macro.isEnable()) {
                    repeatMacro.setRepeat(false);
                } else {
                    macro.doAction();
                }
            }

            if(macro instanceof ToggleMacro toggleMacro && toggleMacro.isToggled()) {
                if(!macro.isEnable()) {
                    toggleMacro.setToggled(false);
                } else {
                    macro.doAction();
                }
            }

            if(macro instanceof CountedRepeatMacro countedRepeatMacro && countedRepeatMacro.isRunning()) {
                if(!macro.isEnable()) {
                    countedRepeatMacro.cancel();
                } else {
                    macro.doAction();
                }
            }

            if(macro instanceof DelayedMacro delayedMacro && delayedMacro.isStart()) {
                if(!macro.isEnable()) {
                    delayedMacro.setStart(false);
                } else if(delayedMacro.getStartTime() + delayedMacro.getDelayedTime() < System.currentTimeMillis()) {
                    delayedMacro.doAction();
                }
            }
        }
    }
}