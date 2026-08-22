package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.wheel.Wheel;
import com.thomas7520.macrokeybinds.object.wheel.WheelMode;
import com.thomas7520.macrokeybinds.object.wheel.WheelSlot;
import com.thomas7520.macrokeybinds.util.MacroExecutor;
import com.thomas7520.macrokeybinds.util.MacroInputHandler;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WheelScreen extends Screen {

    // Add this to avoid missclick from player when it's HOLD mode
    private static final int HOLD_HOVER_DEAD_ZONE = 10;

    private final Wheel wheel;
    private WheelWidget wheelWidget;


    public WheelScreen(Wheel wheel) {
        super(Component.translatable("text.wheel.title"));

        this.wheel = wheel;
    }


    @Override
    protected void init() {
        addRenderableWidget(wheelWidget = new WheelWidget(this.width / 2 - 100, this.height / 2 - 100, 100,
                wheel.getMacros(), true, true, wheel.getMode() == WheelMode.CLICK,
                wheel.getMode() == WheelMode.HOLD ? HOLD_HOVER_DEAD_ZONE : 5, wheelSlot -> {
            this.onClose();
            MacroExecutor.trigger(MacroUtil.getMacro(wheelSlot.getMacroId()));
        }));
        super.init();
    }

    @Override
    public void tick() {
        super.tick();

        if(wheel.getMode() == WheelMode.HOLD && !MacroInputHandler.isWheelBindingDown()) {
            confirmHoveredSlot();
        }
    }

    private void confirmHoveredSlot() {
        WheelSlot hoveredSlot = wheelWidget.getHoveredSlot() == null ? null : wheelWidget.getHoveredSlot().getWheelSlot();
        IMacro macro = hoveredSlot == null ? null : MacroUtil.getMacro(hoveredSlot.getMacroId());

        super.onClose();

        if(macro != null) {
            wheelWidget.playDownSound(minecraft.getSoundManager());
            MacroExecutor.trigger(macro);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
