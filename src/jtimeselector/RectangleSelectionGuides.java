package jtimeselector;

import com.vorono4ka.MathHelper;
import jtimeselector.layers.Layer;

import java.awt.*;

/**
 * A class responsible for drawing the two straight lines that mark
 * the selected interval.
 */
public class RectangleSelectionGuides {
    public static final Color COLOR = new Color(0xB3555555, true);

    private boolean selected = false;
    private int left;
    private int top;
    private int right;
    private int bottom;

    public void drawRectangleSelectionGuides(Graphics2D graphics, int timelineStartX, int currentWidth) {
        graphics.setColor(COLOR);
        final int maxX = currentWidth-Layer.PADDING;
        left = MathHelper.clamp(left, timelineStartX, maxX);
        right = MathHelper.clamp(right, timelineStartX, maxX);

        final int width = right - left;
        final int height = bottom - top;

        graphics.fillRect(left, top, width, height);
        graphics.setColor(TimeSelectionManager.SELECTION_COLOR);
        graphics.drawRect(left, top, width, height);
    }

    public void setSelectionRectangle(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean isVisible() {
        return selected;
    }

    public void setVisible(boolean rectSel) {
        this.selected = rectSel;
    }
}
