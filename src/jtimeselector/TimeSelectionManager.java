/*
 */
package jtimeselector;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;

/**
 *
 * @author Tomas Prochazka 8.12.2015
 */
public class TimeSelectionManager {

    public static final Color SELECTION_COLOR = new Color(0xD9760C);
    private long selectedTime;
    private boolean selection = false;
    private final TimelineManager l;
    private final ZoomManager z;
    /**
     * The position on the component on which
     * the string time value has been drawn last time.
     * If we want to draw more time values on the component,
     * we should first check that the strings do not overlap.
     */
    private int stringX1;
    private int stringX2;

    public void selectTime(long time) {
        selection = true;
        this.selectedTime = time;

    }
    public double getSelectedTime() {
        return selectedTime;
    }

    public TimeSelectionManager(long selectedTime, TimelineManager l, ZoomManager z) {
        this.selectedTime = selectedTime;
        this.l = l;
        this.z = z;
    }

    /**
     *
     * @param l
     * @param z
     */
    public TimeSelectionManager(TimelineManager l, ZoomManager z) {
        this(0, l, z);
    }

    public void drawSelectedTime(Graphics2D g) {
        if (!selection) {
            return;
        }
        if (!z.timeValueInCurrentRange((long)selectedTime)) {
            return;
        }
        int x = l.getHeaderWidth() + l.getXForTime(selectedTime);
        g.setColor(SELECTION_COLOR);
        FontMetrics fontMetrics = g.getFontMetrics();
        String timeStr = l.getConverter().timeToString((long)selectedTime);
        int textWidth = fontMetrics.stringWidth(timeStr);
        if (x+textWidth+2*Layer.PADDING>l.getCurrentWidth()) {
            //draw the text on the left from the vertical line
            stringX1=x-Layer.PADDING-textWidth;
        } else {
            stringX1 = x+Layer.PADDING;
        }
        final int bottomY = l.getTimeLabelsBaselineY();
        stringX2 = stringX1+textWidth;
        g.drawString(timeStr, stringX1, bottomY);
        g.drawLine(x, JTimeSelector.TOP_PADDING, x, bottomY);
    }

    /**
     * Sets the selected time to the value which corresponds to the time value
     * of the point on the timeline.
     *
     * @param x x-coordinate of whole JTimeSelector component
     */
    public void selectTime(int x) {
        long time = l.getTimeForX(x);
        selectTime(time);
    }

    public boolean isSelection() {
        return selection;
    }

    /**
     * Ensures that the selected time does not lie out of bounds. If it does,
     * the selected time is set to the minimal value.
     * 
     * @return false if change occurred
     */
    public boolean checkBounds() {
        if (! selection) return true;
        if (l.isEmpty()) {
            clearSelection();
            return false;
        }
        if (selectedTime < l.getMinTime() || selectedTime > l.getMaxTime()) {
            selectTime(l.getMinTime());
            return false;
        }
        return true;
    }

    /**
     * Selection will not be displayed anymore.
     */
    public void clearSelection() {
        if (selection==false) return;
        selection = false;
        selectedTime = 0;
    }
    /**
     * Checks whether the given interval overlaps the interval
     * on which the string representation of selected time has been drawn.
     * @param a min value
     * @param b max value
     * @return true if intervals overlap
     */
    public boolean labelCollision(int a, int b) {
        return selection 
                && IntervalCheck.collision(stringX1, stringX2, a, b);
    }

}
