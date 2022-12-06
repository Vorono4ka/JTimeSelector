package jtimeselector;

import java.awt.Color;

import com.vorono4ka.interfaces.SelectionManager;
import jtimeselector.layers.TimelineManager;

public class TimeSelectionManager implements SelectionManager {
    public static final Color SELECTION_COLOR = new Color(0x4b6eaf);
    public static final Color CURSOR_COLOR = SELECTION_COLOR;
    public static final int SELECTION_THRESHOLD = 150;

    private final TimelineManager timelineManager;
    private long selectedTime;
    private int selectedLayer;
    private boolean hasSelection = false;

    /**
     * The position on the component on which the string time value has been drawn last time.
     * If we want to draw more time values on the component, we should first check that the strings do not overlap.
     */

    public TimeSelectionManager(TimelineManager timelineManager, long selectedTime) {
        this.timelineManager = timelineManager;
        this.selectedTime = selectedTime;
    }

    public TimeSelectionManager(TimelineManager timelineManager) {
        this(timelineManager, 0);
    }

    /**
     * Sets the selected time to the given value.
     *
     * @param time time point in timeline
     * @param layerIndex selected layer index
     */
    public void setSelection(long time, int layerIndex) {
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

    public void clearSelection() {
        if (!hasSelection) {
            return;
        }

        hasSelection = false;
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
            setSelection(timelineManager.getMinTime(), 0);
            return false;
        }
        return true;
    }
}
