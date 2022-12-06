package jtimeselector;

import com.vorono4ka.MathHelper;
import com.vorono4ka.interfaces.SelectionManager;
import jtimeselector.layers.TimelineManager;

public class IntervalSelectionManager implements SelectionManager {
    private final TimelineManager timelineManager;

    private boolean hasSelection;
    private long fromTime;
    private long toTime;
    private int fromLayer;
    private int toLayer;

    public IntervalSelectionManager(TimelineManager timelineManager) {
        this.timelineManager = timelineManager;
        hasSelection = false;
    }

    public void setSelection(long left, long right, int top, int bottom) {
        top = MathHelper.clamp(top, 0, this.timelineManager.getLayersBottomY() - 1);
        bottom = MathHelper.clamp(bottom, top, this.timelineManager.getLayersBottomY() - 1);

        int fromLayer = Math.max(this.timelineManager.getLayerIndex(top), 0);
        int toLayer = this.timelineManager.getLayerIndex(bottom);

        long closestFromTime = this.timelineManager.getClosestTime(left, fromLayer, toLayer);
        long closestToTime = this.timelineManager.getClosestTime(right, fromLayer, toLayer);

        if (Math.abs(closestFromTime - left) < TimeSelectionManager.SELECTION_THRESHOLD) {
            left = closestFromTime;
        }

        if (Math.abs(closestToTime - right) < TimeSelectionManager.SELECTION_THRESHOLD) {
            right = closestToTime;
        }

        this.hasSelection = true;
        this.fromTime = left;
        this.toTime = right;
        this.fromLayer = fromLayer;
        this.toLayer = toLayer;
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

    public long getFromTime() {
        return fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public int getFromLayer() {
        return fromLayer;
    }

    public int getToLayer() {
        return toLayer;
    }

    /**
     * Ensures that the selected time interval does not lie out of bounds. If it
     * does, the selected time is set to the minimal value.
     *
     * @return false if change occurred
     */
    public boolean checkBounds() {
        if (!hasSelection) {
            return true;
        }
        if (timelineManager.isEmpty()) {
            clearSelection();
            return false;
        }
        final long minTime = timelineManager.getMinTime();
        final long maxTime = timelineManager.getMaxTime();
        boolean change = false;
        if (fromTime < minTime) {
            fromTime = minTime;
            change = true;
        }
        if (toTime > maxTime) {
            toTime = maxTime;
            change = true;
        }
        return !change;
    }
}
