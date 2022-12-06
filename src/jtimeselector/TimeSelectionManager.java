package jtimeselector;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;

public class TimeSelectionManager {
    public static final Color SELECTION_COLOR = new Color(0x4b6eaf);
    public static final int SELECTION_THRESHOLD = 150;

    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;
    private long selectedTime;
    private int selectedLayer;
    private boolean hasSelection = false;

    /**
     * The position on the component on which the string time value has been drawn last time.
     * If we want to draw more time values on the component, we should first check that the strings do not overlap.
     */
    private int stringX1;
    private int stringX2;

    public TimeSelectionManager(TimelineManager timelineManager, ZoomManager zoomManager, long selectedTime) {
        this.timelineManager = timelineManager;
        this.zoomManager = zoomManager;
        this.selectedTime = selectedTime;
    }

    public TimeSelectionManager(TimelineManager timelineManager, ZoomManager zoomManager) {
        this(timelineManager, zoomManager, 0);
    }

    public void drawSelectedTime(Graphics2D graphics) {
        if (!hasSelection) {
            return;
        }
        if (!zoomManager.timeValueInCurrentRange(selectedTime)) {
            return;
        }
        int x = timelineManager.getLegendWidth() + timelineManager.getXForTime(selectedTime);
        graphics.setColor(SELECTION_COLOR);
        FontMetrics fontMetrics = graphics.getFontMetrics();
        String timeStr = timelineManager.getConverter().timeToString(selectedTime);
        int textWidth = fontMetrics.stringWidth(timeStr);
        if (x+textWidth+2*Layer.PADDING > timelineManager.getCurrentWidth()) {
            //draw the text on the left from the vertical line
            stringX1 = x-Layer.PADDING-textWidth;
        } else {
            stringX1 = x+Layer.PADDING;
        }
        final int bottomY = timelineManager.getTimeLabelsBaselineY();
        stringX2 = stringX1+textWidth;
        graphics.drawString(timeStr, stringX1, bottomY);
        graphics.drawLine(x, JTimeSelector.TOP_PADDING, x, bottomY);
    }

    /**
     * Sets the selected time to the given value.
     *
     * @param time time point in timeline
     * @param layerIndex selected layer index
     */
    public void selectTime(long time, int layerIndex) {
        long closestTime = timelineManager.getClosestTime(time, layerIndex);

        if (Math.abs(closestTime - time) >= TimeSelectionManager.SELECTION_THRESHOLD) {
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
        if (hasSelection) {
            hasSelection = false;
            selectedTime = 0;
            selectedLayer = -1;
        }
    }

    /**
     * Checks whether the given interval overlaps the interval
     * on which the string representation of selected time has been drawn.
     * @param minValue min value
     * @param maxValue max value
     * @return true if intervals overlap
     */
    public boolean labelCollision(int minValue, int maxValue) {
        return hasSelection && IntervalCheck.collision(stringX1, stringX2, minValue, maxValue);
    }
}
