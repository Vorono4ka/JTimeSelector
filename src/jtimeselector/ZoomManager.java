package jtimeselector;

import jtimeselector.layers.TimelineManager;

public class ZoomManager {
    private static final double RATIO = .8;
    private static final double RATIO_INVERTED = 1 / RATIO;

    private long minTime;
    private long maxTime;
    private long currentMin;
    private long currentMax;
    private boolean noZoom;

    public ZoomManager() {
        setDefaultZoom();
    }

    public final void updateMinAndMaxTime(TimelineManager timelineManager) {
        this.minTime = 0;
        this.maxTime = timelineManager.getMaxTime();
        if (noZoom) {
            setDefaultZoom();
        } else {
            trimZoom();
        }
    }

    private void setDefaultZoom() {
        noZoom = true;
        currentMin = minTime;
        currentMax = maxTime;
    }

    public long getCurrentMinTime() {
        return currentMin;
    }

    public long getCurrentMaxTime() {
        return currentMax;
    }

    public void zoomIn(int times, long center) {
        noZoom = false;
        for (int i = 0; i < times; i++) {
            zoomIn(center);
        }
    }

    public void zoomOut(int times, long center) {
        for (int i = 0; i < times; i++) {
            zoomOut(center);
        }

        if (currentMax > maxTime || currentMin < minTime) {
            setDefaultZoom();
        }
    }

    private void zoomIn(long center) {
        currentMin = (long)(center - (center - currentMin) * RATIO);
        currentMax = (long)(center + (currentMax - center) * RATIO);
    }

    private void zoomOut(long center) {
        currentMin = (long)(center - (center - currentMin) * RATIO_INVERTED);
        currentMax = (long)(center + (currentMax - center) * RATIO_INVERTED);
    }

    /**
     * @param timeOffset positive or negative value that will be added to the current interval
     */
    public void moveVisibleArea(long timeOffset) {
        if (currentMin + timeOffset < minTime) timeOffset = minTime - currentMin;
        if (currentMax + timeOffset > maxTime) timeOffset = maxTime - currentMax;
        currentMin += timeOffset;
        currentMax += timeOffset;
    }

    public boolean timeValueInCurrentRange(long time) {
        return time >= currentMin && time <= currentMax;
    }

    private void trimZoom() {
        currentMax = Math.min(currentMax, maxTime);
        currentMin = Math.max(currentMin, minTime);
        if (currentMax <= currentMin) {
            setDefaultZoom();
        }
    }
}
