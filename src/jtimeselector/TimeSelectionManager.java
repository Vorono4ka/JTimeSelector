/*
 */
package jtimeselector;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimeEntryLayer;
import jtimeselector.layers.TimelineManager;

/**
 *
 * @author Tomas Prochazka 8.12.2015
 */
public class TimeSelectionManager {
    public static final Color SELECTION_COLOR = new Color(0x4b6eaf);
    private long selectedTime;
    private int selectedLayer;
    private boolean hasSelection = false;
    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;

    /**
     * The position on the component on which
     * the string time value has been drawn last time.
     * If we want to draw more time values on the component,
     * we should first check that the strings do not overlap.
     */
    private int stringX1;
    private int stringX2;

    public void selectTime(long time, int layerIndex) {
        long closestTime = timelineManager.getClosestTime(time, layerIndex);

        if (Math.abs(closestTime - time) >= 150) {
            hasSelection = false;
            return;
        }

        hasSelection = true;
        this.selectedTime = closestTime;
        this.selectedLayer = layerIndex;
    }

    public long getSelectedTime() {
        return selectedTime;
    }

    public int getSelectedLayer() {
        return selectedLayer;
    }

    public TimeSelectionManager(long selectedTime, TimelineManager timelineManager, ZoomManager zoomManager) {
        this.selectedTime = selectedTime;
        this.timelineManager = timelineManager;
        this.zoomManager = zoomManager;
    }

    /**
     * @param timelineManager
     * @param zoomManager
     */
    public TimeSelectionManager(TimelineManager timelineManager, ZoomManager zoomManager) {
        this(0, timelineManager, zoomManager);
    }

    public void drawSelectedTime(Graphics2D g) {
        if (!hasSelection) {
            return;
        }
        if (!zoomManager.timeValueInCurrentRange(selectedTime)) {
            return;
        }
        int x = timelineManager.getLegendWidth() + timelineManager.getXForTime(selectedTime);
        g.setColor(SELECTION_COLOR);
        FontMetrics fontMetrics = g.getFontMetrics();
        String timeStr = timelineManager.getConverter().timeToString((long)selectedTime);
        int textWidth = fontMetrics.stringWidth(timeStr);
        if (x+textWidth+2*Layer.PADDING> timelineManager.getCurrentWidth()) {
            //draw the text on the left from the vertical line
            stringX1=x-Layer.PADDING-textWidth;
        } else {
            stringX1 = x+Layer.PADDING;
        }
        final int bottomY = timelineManager.getTimeLabelsBaselineY();
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
        long time = timelineManager.getTimeForX(x);
        selectTime(time, 1);
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    /**
     * Ensures that the selected time does not lie out of bounds. If it does,
     * the selected time is set to the minimal value.
     * 
     * @return false if change occurred
     */
    public boolean checkBounds() {
        if (!hasSelection) return true;
        if (timelineManager.isEmpty()) {
            clearSelection();
            return false;
        }
        if (selectedTime < timelineManager.getMinTime() || selectedTime > timelineManager.getMaxTime()) {
            selectTime(timelineManager.getMinTime(), 0);
            return false;
        }
        return true;
    }

    /**
     * Selection will not be displayed anymore.
     */
    public void clearSelection() {
        if (hasSelection ==false) return;
        hasSelection = false;
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
        return hasSelection
                && IntervalCheck.collision(stringX1, stringX2, a, b);
    }

}
