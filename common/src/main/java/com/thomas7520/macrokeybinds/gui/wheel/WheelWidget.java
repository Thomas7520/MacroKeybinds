package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.object.macro.AlternateMacro;
import com.thomas7520.macrokeybinds.object.macro.CountedRepeatMacro;
import com.thomas7520.macrokeybinds.object.macro.DelayedMacro;
import com.thomas7520.macrokeybinds.object.macro.IMacro;
import com.thomas7520.macrokeybinds.object.macro.ToggleMacro;
import com.thomas7520.macrokeybinds.object.wheel.Wheel;
import com.thomas7520.macrokeybinds.object.wheel.WheelSlot;
import com.thomas7520.macrokeybinds.util.MacroUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WheelWidget extends AbstractWidget implements ContainerEventHandler {

    public static final int SLOTS_MAX = Wheel.MAX_MACROS;
    public static final int SLOT_SIZE = 360 / SLOTS_MAX;
    public static final int THICKNESS_LINE = 2;

    private static final double FIRST_SLOT_START_ANGLE = 270d - SLOT_SIZE / 2d;

    private static final int SLOT_COLOR = 0xB0202020;
    private static final int HOVERED_SLOT_COLOR = 0xD0606060;
    private static final int DISABLED_OVERLAY_COLOR = 0x80902020;
    private static final int LINE_COLOR = 0xFFD0D0D0;

    private static final double FULL_CIRCLE = 360d;
    private static final int RENDER_SCALE = 3;
    private static final double PIXEL_CENTER = 0.5d;
    private static final int MAX_NAME_WIDTH = 72;
    private static final int NAME_PADDING = 3;

    private final int radius;
    private final boolean showIcons;
    private final boolean hoverEnabled;
    private final boolean clickEnabled;
    private final int hoverDeadZone;

    private final List<Slot> slots = new ArrayList<>();
    private final Consumer<WheelSlot> onSlotClicked;

    private Slot hoveredSlot;
    private GuiEventListener focused;
    private boolean dragging;

    public WheelWidget(int x, int y, int radius, List<WheelSlot> slots, boolean showIcons, boolean hoverEnabled,
                       boolean clickEnabled, int hoverDeadZone, Consumer<WheelSlot> onSlotClicked) {
        super(x, y, radius*2, radius*2, Component.empty());

        this.onSlotClicked = onSlotClicked;
        this.radius = radius;
        this.showIcons = showIcons;
        this.hoverEnabled = hoverEnabled;
        this.clickEnabled = clickEnabled;
        this.hoverDeadZone = hoverDeadZone;

        for(int i = 0; i < SLOTS_MAX; i++) {
            double startAngle = FIRST_SLOT_START_ANGLE + i*SLOT_SIZE;
            double endAngle = FIRST_SLOT_START_ANGLE + (i+1)*SLOT_SIZE;
            WheelSlot wheelSlot = i < slots.size() ? slots.get(i) : null;

            this.slots.add(new Slot(wheelSlot, radius, startAngle, endAngle));
        }
    }

    public static double getSlotMiddleAngle(int slotIndex) {
        return FIRST_SLOT_START_ANGLE + slotIndex*SLOT_SIZE + SLOT_SIZE / 2d;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return slots;
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public GuiEventListener getFocused() {
        return focused;
    }

    @Override
    public void setFocused(GuiEventListener focused) {
        if(this.focused != null) {
            this.focused.setFocused(false);
        }
        if(focused != null) {
            focused.setFocused(true);
        }
        this.focused = focused;
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent event) {
        return ContainerEventHandler.super.nextFocusPath(event);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return ContainerEventHandler.super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return ContainerEventHandler.super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isFocused() {
        return ContainerEventHandler.super.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        ContainerEventHandler.super.setFocused(focused);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Render at a higher resolution then we scale it down to reduce visible pixel steps.
        if(hoverEnabled) {
            updateHoveredSlot(mouseX, mouseY);
        } else {
            hoveredSlot = null;
        }

        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        int renderRadius = radius * RENDER_SCALE;

        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().scale(1f / RENDER_SCALE, 1f / RENDER_SCALE, 1.0F);

        fillArc(graphics, 0d, renderRadius, 0d, FULL_CIRCLE, SLOT_COLOR);

        if(hoveredSlot != null) {
            fillArc(graphics, 0d, renderRadius,
                    hoveredSlot.startAngle, hoveredSlot.endAngle, HOVERED_SLOT_COLOR);
        }

        int renderThickness = THICKNESS_LINE * RENDER_SCALE;

        for(Slot slot : slots) {
            drawSeparator(graphics, slot.startAngle, renderRadius, renderThickness);
        }

        fillArc(graphics, renderRadius - renderThickness, renderRadius, 0d, FULL_CIRCLE, LINE_COLOR);
        graphics.pose().popPose();

        renderSlotContents(graphics);
        renderDisabledOverlay(graphics, centerX, centerY, renderRadius, renderThickness);
    }

    private void renderDisabledOverlay(GuiGraphics graphics, int centerX, int centerY,
                                       int renderRadius, int renderThickness) {
        graphics.pose().pushPose();
        graphics.pose().translate(centerX, centerY, 0.0F);
        graphics.pose().scale(1f / RENDER_SCALE, 1f / RENDER_SCALE, 1.0F);

        for(Slot slot : slots) {
            if(isDisabled(slot.wheelSlot)) {
                fillArc(graphics, 0d, renderRadius,
                        slot.startAngle, slot.endAngle, DISABLED_OVERLAY_COLOR);
            }
        }

        for(Slot slot : slots) {
            drawSeparator(graphics, slot.startAngle, renderRadius, renderThickness);
        }

        fillArc(graphics, renderRadius - renderThickness, renderRadius, 0d, FULL_CIRCLE, LINE_COLOR);
        graphics.pose().popPose();
    }

    private void renderSlotContents(GuiGraphics graphics) {
        Font font = Minecraft.getInstance().font;
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;

        for(Slot slot : slots) {
            if(slot.wheelSlot == null || slot.wheelSlot.getMacroId() == null) {
                continue;
            }

            double middleAngle = Math.toRadians((slot.startAngle + slot.endAngle) / 2d);
            int contentX = centerX + (int) Math.round(Math.cos(middleAngle) * radius * 0.62d);
            int contentY = centerY + (int) Math.round(Math.sin(middleAngle) * radius * 0.62d);
            IMacro macro = findMacro(slot.wheelSlot);
            boolean unknownMacro = macro == null;
            boolean disabledMacro = macro != null && !macro.isEnable();
            String name = unknownMacro ? Component.translatable("text.wheel.unknown").getString() : macro.getName();
            ItemStack icon = findIcon(slot.wheelSlot);

            boolean iconRendered = showIcons && !icon.isEmpty();

            if(iconRendered) {
                graphics.renderItem(icon, contentX - 8, contentY - 15);
            }

            int nameY;
            if(disabledMacro) {
                nameY = iconRendered ? contentY + 2 : contentY - font.lineHeight;
            } else {
                nameY = iconRendered ? contentY + 5 : contentY - font.lineHeight / 2;
            }

            int nameColor = unknownMacro ? 0xFFFF5555 : 0xFFFFFFFF;
            int maxNameWidth = getMaxNameWidth(slot, contentX, nameY, font.lineHeight, centerX, centerY);

            graphics.drawCenteredString(font, trimName(font, name, maxNameWidth), contentX, nameY, nameColor);

            if(!unknownMacro) {
                renderMacroState(graphics, font, macro, contentX, nameY, iconRendered);
            }

            if(disabledMacro) {
                int disabledY = nameY + font.lineHeight + 1;
                int disabledWidth = getMaxNameWidth(slot, contentX, disabledY, font.lineHeight, centerX, centerY);
                String disabledText = Component.translatable("text.wheel.disabled").getString();
                graphics.drawCenteredString(font, trimName(font, disabledText, disabledWidth), contentX, disabledY, 0xFFFFAAAA);
            }
        }
    }

    private void renderMacroState(GuiGraphics graphics, Font font, IMacro macro,
                                  int contentX, int nameY, boolean iconRendered) {
        Component stateText = null;
        int stateColor = 0xFFFFFFFF;

        if(macro instanceof DelayedMacro delayedMacro && delayedMacro.isStart()
                || macro instanceof ToggleMacro toggleMacro && toggleMacro.isToggled()
                || macro instanceof CountedRepeatMacro countedRepeatMacro && countedRepeatMacro.isRunning()) {
            stateText = Component.translatable("text.running");
            stateColor = 0xFF00FF00;
        }

        if(macro instanceof AlternateMacro alternateMacro && alternateMacro.isSecondActionNext()) {
            stateText = Component.literal("B");
            stateColor = 0xFFFFAA00;
        }

        if(stateText == null) {
            return;
        }

        int stateX = iconRendered ? contentX + 10 : contentX - font.width(stateText) / 2;
        int stateY = iconRendered ? nameY - 23 : nameY - font.lineHeight - 1;
        graphics.drawString(font, stateText, stateX, stateY, stateColor, true);
    }

    private IMacro findMacro(WheelSlot wheelSlot) {
        if(wheelSlot == null || wheelSlot.getMacroId() == null) return null;

        return MacroUtil.getMacro(wheelSlot.getMacroId());
    }

    private boolean isDisabled(WheelSlot wheelSlot) {
        IMacro macro = findMacro(wheelSlot);
        return macro != null && !macro.isEnable();
    }

    private ItemStack findIcon(WheelSlot wheelSlot) {
        if(wheelSlot == null || wheelSlot.getIconId() == null) return ItemStack.EMPTY;

        return BuiltInRegistries.ITEM.getOptional(wheelSlot.getIconId())
                .map(Item::getDefaultInstance)
                .filter(stack -> !stack.isEmpty())
                .orElse(ItemStack.EMPTY);
    }

    private int getMaxNameWidth(Slot slot, int textCenterX, int textY, int textHeight,
                                int wheelCenterX, int wheelCenterY) {
        int maxHalfWidth = 0;

        for(int halfWidth = 1; halfWidth <= MAX_NAME_WIDTH / 2; halfWidth++) {
            int left = textCenterX - halfWidth - NAME_PADDING;
            int right = textCenterX + halfWidth + NAME_PADDING;
            int top = textY - 1;
            int bottom = textY + textHeight;

            if(!isInsideSlot(slot, left, top, wheelCenterX, wheelCenterY)
                    || !isInsideSlot(slot, right, top, wheelCenterX, wheelCenterY)
                    || !isInsideSlot(slot, left, bottom, wheelCenterX, wheelCenterY)
                    || !isInsideSlot(slot, right, bottom, wheelCenterX, wheelCenterY)) {
                break;
            }

            maxHalfWidth = halfWidth;
        }

        return maxHalfWidth * 2;
    }

    private boolean isInsideSlot(Slot slot, int x, int y, int wheelCenterX, int wheelCenterY) {
        double offsetX = x - wheelCenterX;
        double offsetY = y - wheelCenterY;

        return offsetX * offsetX + offsetY * offsetY <= (double) radius * radius
                && containsAngle(Math.toDegrees(Math.atan2(offsetY, offsetX)), slot.startAngle, slot.endAngle);
    }

    private Component trimName(Font font, String name, int maxWidth) {
        if(font.width(name) <= maxWidth) return Component.literal(name);

        String suffix = "...";
        if(maxWidth < font.width(suffix)) {
            suffix = ".";
        }

        if(maxWidth < font.width(suffix)) {
            return Component.empty();
        }

        String trimmedName = font.plainSubstrByWidth(name, maxWidth - font.width(suffix));
        return Component.literal(trimmedName + suffix);
    }


    private void fillArc(GuiGraphics graphics, double innerRadius, double outerRadius, double startAngle, double endAngle, int color) {
        int bound = (int) Math.ceil(outerRadius);
        double innerSquared = innerRadius * innerRadius;
        double outerSquared = outerRadius * outerRadius;
        boolean wholeCircle = endAngle - startAngle >= FULL_CIRCLE;

        for(int offsetY = -bound; offsetY < bound; offsetY++) {
            double pixelY = offsetY + PIXEL_CENTER;
            int runStart = 0;
            boolean drawingRun = false;

            for(int offsetX = -bound; offsetX < bound; offsetX++) {
                double pixelX = offsetX + PIXEL_CENTER;
                double distanceSquared = pixelX * pixelX + pixelY * pixelY;
                boolean inside = distanceSquared <= outerSquared
                        && distanceSquared > innerSquared
                        && (wholeCircle || containsAngle(Math.toDegrees(Math.atan2(pixelY, pixelX)),
                        startAngle, endAngle));

                if(inside && !drawingRun) {
                    runStart = offsetX;
                    drawingRun = true;
                } else if(!inside && drawingRun) {
                    graphics.fill(runStart, offsetY,
                            offsetX, offsetY + 1, color);
                    drawingRun = false;
                }
            }

            if(drawingRun) {
                graphics.fill(runStart, offsetY,
                        bound, offsetY + 1, color);
            }
        }
    }

    private void drawSeparator(GuiGraphics graphics, double angle, int radius, int thickness) {
        double radians = Math.toRadians(angle);
        int halfThickness = thickness / 2;
        int lineLength = radius - halfThickness;

        for(int distance = 0; distance <= lineLength; distance++) {
            int x = (int) Math.round(Math.cos(radians) * distance);
            int y = (int) Math.round(Math.sin(radians) * distance);

            graphics.fill(x - halfThickness, y - halfThickness,
                    x - halfThickness + thickness, y - halfThickness + thickness, LINE_COLOR);
        }
    }

    private static boolean containsAngle(double angle, double startAngle, double endAngle) {
        double normalizedAngle = normalizeAngle(angle);
        double normalizedStart = normalizeAngle(startAngle);
        double normalizedEnd = normalizeAngle(endAngle);

        if(normalizedStart <= normalizedEnd) {
            return normalizedAngle >= normalizedStart && normalizedAngle < normalizedEnd;
        }

        return normalizedAngle >= normalizedStart || normalizedAngle < normalizedEnd;
    }

    private static double normalizeAngle(double angle) {
        double normalized = angle % FULL_CIRCLE;
        return normalized < 0d ? normalized + FULL_CIRCLE : normalized;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {

    }

    @Override
    public void mouseMoved(double x, double y) {
        if(hoverEnabled) {
            updateHoveredSlot(x,y);
        } else {
            hoveredSlot = null;
        }
    }

    private void updateHoveredSlot(double mouseX, double mouseY) {
        hoveredSlot = null;

        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        double offsetX = mouseX - centerX;
        double offsetY = mouseY - centerY;

        // In HOLD mode, the center must stay empty to avoid selecting a slot as soon as the wheel opens.
        if(offsetX * offsetX + offsetY * offsetY <= (double) hoverDeadZone * hoverDeadZone) {
            return;
        }

        for (Slot slot : slots) {
            IMacro macro = findMacro(slot.getWheelSlot());

            if(slot.isMouseOver(mouseX, mouseY) && macro != null && macro.isEnable()) {
                hoveredSlot = slot;
                break;
            }
        }
    }

    public Slot getHoveredSlot() {
        return hoveredSlot;
    }

    public class Slot extends AbstractWidget {

        private final WheelSlot wheelSlot;

        private final int distance;
        private final double startAngle;
        private final double endAngle;

        public Slot(WheelSlot wheelSlot, int distance, double startAngle, double endAngle) {
            super(0, 0, 0, 0, Component.empty());
            this.wheelSlot = wheelSlot;
            this.distance = distance;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
        }

        @Override
        public boolean isMouseOver(double mouseX, double mouseY) {
            int centerX = WheelWidget.this.getX() + WheelWidget.this.getWidth() / 2;
            int centerY = WheelWidget.this.getY() + WheelWidget.this.getHeight() / 2;
            double offsetX = mouseX - centerX;
            double offsetY = mouseY - centerY;

            return offsetX * offsetX + offsetY * offsetY <= (double) distance * distance
                    && containsAngle(Math.toDegrees(Math.atan2(offsetY, offsetX)), startAngle, endAngle);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if(wheelSlot != null) {
                WheelWidget.this.onSlotClicked.accept(wheelSlot);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            IMacro macro = findMacro(wheelSlot);

            if(!clickEnabled || button != 0 || !isMouseOver(mouseX, mouseY)
                    || macro == null || !macro.isEnable()) {
                return false;
            }

            playDownSound(Minecraft.getInstance().getSoundManager());
            onClick(mouseX, mouseY);
            return true;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        public WheelSlot getWheelSlot() {
            return wheelSlot;
        }
    }
}
