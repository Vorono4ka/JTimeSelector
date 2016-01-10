/*
 */
package jtimeselector;

import jtimeselector.layers.TimelineManager;

/**
 *
 * @author Tomas Prochazka 7.12.2015
 */
public class ZoomManager {

    double minTime;
    double maxTime;
    double currentMin;
    double currentMax;
    boolean noZoom = true;
    private static final double COEF = .8;
    private static final double COEF_INV = 1.25;

    public ZoomManager(double minTime, double maxTime) {
        updateMinAndMaxTime(minTime, maxTime);
        setDefaultZoom();
    }

    ZoomManager() {

    }

    public final void updateMinAndMaxTime(double minTime1, double maxTime1) {
        this.minTime = minTime1;
        this.maxTime = maxTime1;
    }

    public final void updateMinAndMaxTime(TimelineManager m) {
        this.minTime = m.getMinTime();
        this.maxTime = m.getMaxTime();
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

    public double getCurrentMinTime() {
        return currentMin;
    }

    public double getCurrentMaxTime() {
        return currentMax;
    }

    public void zoomIn(int times, double center) {
        noZoom=false;
        for (int i = 0; i < times; i++) {
            zoomIn(center);
        }
    }

    public void zoomOut(int times, double center) {
        for (int i = 0; i < times; i++) {
            zoomOut(center);
        }
        if (currentMax>maxTime|| currentMin<minTime) {
            currentMax=maxTime;
            currentMin=minTime;
            noZoom=true;
        }
    }

    private void zoomIn(double center) {
        currentMin = center - (center - currentMin) * COEF;
        currentMax = center + (currentMax - center) * COEF;
        
    }

    private void zoomOut(double center) {
        currentMin = center - (center - currentMin) * COEF_INV;
        currentMax = center + (currentMax - center) * COEF_INV;
    }
    /**
     * p
     * @param timeOffset positive or negative value that will be added to the current interval
     */
    public void moveVisibleArea(double timeOffset) {
        if (currentMin+timeOffset<minTime) timeOffset=minTime-currentMin;
        if (currentMax+timeOffset>maxTime) timeOffset=maxTime-currentMax;
        currentMin+=timeOffset;
        currentMax+=timeOffset;
    }
    public boolean timeValueInCurrentRange(double time) {
        return time>=currentMin&&time<=currentMax;
    }

    private void trimZoom() {
        currentMax = Math.min(currentMax, maxTime);
        currentMin=Math.max(currentMin, minTime);
        if (currentMax<=currentMin) {
            setDefaultZoom();
        }
    }

}
