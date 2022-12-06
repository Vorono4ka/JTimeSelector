package jtimeselector;

import jtimeselector.layers.TimelineManager;

public class VisibleAreaManager {
    private static final double SCROLL_RATIO = 200;
    private static final double ZOOM_RATIO = .8;
    private static final double ZOOM_RATIO_INVERTED = 1 / ZOOM_RATIO;

    private long minTime;
    private long maxTime;
    private long currentMin;
    private long currentMax;
    private boolean noZoom;

    public VisibleAreaManager() {
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
        currentMin = (long)(center - (center - currentMin) * ZOOM_RATIO);
        currentMax = (long)(center + (currentMax - center) * ZOOM_RATIO);
    }

    private void zoomOut(long center) {
        currentMin = (long)(center - (center - currentMin) * ZOOM_RATIO_INVERTED);
        currentMax = (long)(center + (currentMax - center) * ZOOM_RATIO_INVERTED);
    }

    /**
     * @param scrollRotation positive or negative value that will be added to the current interval
     */
    public void moveVisibleArea(long scrollRotation) {
        scrollRotation *= SCROLL_RATIO;

        if (currentMin + scrollRotation < minTime) scrollRotation = minTime - currentMin;
        if (currentMax + scrollRotation > maxTime) scrollRotation = maxTime - currentMax;
        currentMin += scrollRotation;
        currentMax += scrollRotation;
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
