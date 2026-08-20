package com.thomas7520.macrokeybinds.gui.wheel;

import com.thomas7520.macrokeybinds.object.wheel.WheelSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WheelWidget extends AbstractContainerWidget {

    public static final int SLOTS_MAX = 6;
    public static final int SLOT_SIZE = 360 / SLOTS_MAX;
    public static final int THICKNESS_LINE = 2;

    private static final int SLOT_COLOR = 0xB0202020;
    private static final int HOVERED_SLOT_COLOR = 0xD0606060;
    private static final int LINE_COLOR = 0xFFD0D0D0;

    private static final double FULL_CIRCLE = 360d;
    private static final int RENDER_SCALE = 3;
    private static final double PIXEL_CENTER = 0.5d;

    private final int radius;

    private final List<Slot> slots = new ArrayList<>();
    private final Consumer<WheelSlot> onSlotClicked;

    private Slot hoveredSlot;

    public WheelWidget(int x, int y, int radius, List<WheelSlot> slots, Consumer<WheelSlot> onSlotClicked) {
        super(x, y, radius*2, radius*2, Component.empty());

        this.onSlotClicked = onSlotClicked;
        this.radius = radius;

        for(int i = 0; i < SLOTS_MAX; i++) {
            if (i >= slots.size()) break;

            double startAngle = 240d + i*SLOT_SIZE;
            double endAngle = 240d + (i+1)*SLOT_SIZE;

            this.slots.add(new Slot(slots.get(i), radius, startAngle, endAngle));
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return slots;
    }

    @Override
    protected int contentHeight() {
        return height;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Render at a higher resolution then we scale it down to reduce visible pixel steps.
        updateHoveredSlot(mouseX, mouseY);

        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        int renderRadius = radius * RENDER_SCALE;

        graphics.pose().pushMatrix();
        graphics.pose().translate(centerX, centerY);
        graphics.pose().scale(1f / RENDER_SCALE);

        fillArc(graphics, 0d, renderRadius, 0d, FULL_CIRCLE, SLOT_COLOR);

        if(hoveredSlot != null) {
            fillArc(graphics, 0d, renderRadius,
                    hoveredSlot.startAngle, hoveredSlot.endAngle, HOVERED_SLOT_COLOR);
        }

        int renderThickness = THICKNESS_LINE * RENDER_SCALE;

        for(Slot slot : slots) {
            drawSeparator(graphics, slot.startAngle, renderRadius, renderThickness);
        }

        fillArc(graphics, renderRadius - renderThickness, renderRadius,
                0d, FULL_CIRCLE, LINE_COLOR);
        graphics.pose().popMatrix();
    }


    private void fillArc(GuiGraphicsExtractor graphics, double innerRadius,
                         double outerRadius, double startAngle, double endAngle, int color) {
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

    private void drawSeparator(GuiGraphicsExtractor graphics, double angle, int radius, int thickness) {
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
        updateHoveredSlot(x,y);
    }

    private void updateHoveredSlot(double mouseX, double mouseY) {
        hoveredSlot = null;

        for (Slot slot : slots) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                hoveredSlot = slot;
                break;
            }
        }
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
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if(event.button() == 0) {
                WheelWidget.this.onSlotClicked.accept(wheelSlot);
            }
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }

        public WheelSlot getWheelSlot() {
            return wheelSlot;
        }
    }
}
