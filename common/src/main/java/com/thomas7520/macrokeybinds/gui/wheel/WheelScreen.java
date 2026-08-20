package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.object.wheel.Wheel;
import com.thomas7520.macrokeybinds.object.wheel.WheelMode;
import com.thomas7520.macrokeybinds.object.wheel.WheelSlot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class WheelScreen extends Screen {

    private Wheel wheel;

    private WheelSlot slotHovered;

    public WheelScreen(Wheel wheel) {
        super(Component.translatable("wheel"));

        this.wheel = wheel;
    }


    @Override
    protected void init() {
        addRenderableWidget(new WheelWidget(this.width / 2 - 100, this.height / 2 - 100, 100, wheel.getMacros(), wheelSlot -> {

        }));
        super.init();
    }

    @Override
    public void onClose() {
        if(this.wheel.getMode() == WheelMode.HOLD && slotHovered != null) {
            // check if a slot was hovered and if macro exist
        }
        super.onClose();
    }
}