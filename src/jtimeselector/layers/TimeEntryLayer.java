/*
 */
package jtimeselector.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import jtimeselector.TimeSelectionManager;
import jtimeselector.TimeSearch;
import jtimeselector.ZoomManager;

/**
 * Responsible for drawing a single timeline layer.
 * Generates a series of small circles, a single circle 
 * for each time value.
 * 
 * @author Tomas Prochazka 6.12.2015
 */
public class TimeEntryLayer extends Layer {

    public static final int HEIGHT = 30;
    private final long[] timeValues;

    public TimeEntryLayer(String name, long[] timeValues) {
        super(name);
        this.timeValues = timeValues;
    }



    @Override
    int getHeight() {
        return HEIGHT;
    }

    @Override
    void draw(Graphics2D g, long timeFrom, long timeTo, int headerSize, int graphicsWidth, int y) {
        y = y + TimeEntryLayer.HEIGHT / 2;
        g.setColor(Color.black);
        g.drawString(getName(), Layer.PADDING, y + g.getFontMetrics().getHeight() / 2);
        if (timeValues.length == 0) return;
        int x0 = headerSize + 2 * Layer.PADDING + Layer.POINT_RADIUS;
        int timelineWidth = graphicsWidth - x0 - Layer.POINT_RADIUS - Layer.PADDING;// |(padding)NAME(padding)(point radius space) (POINTS) (point radius space) (padding)|
        long timeInterval = timeTo - timeFrom;
        if (timelineWidth <= 0) {
            return;
        }
        y = y - Layer.POINT_RADIUS;
        x0 = x0 - Layer.POINT_RADIUS;
        g.setColor(Color.black);
        for (int i = TimeSearch.firstGreaterThanOrEqual(timeValues, timeFrom); i <= TimeSearch.lastLessThanOrEqual(timeValues, timeTo); i++) {
            long time = timeValues[i];
            double positionPercent = ((double)time - timeFrom) / timeInterval;
            int position = (int) Math.round(positionPercent * timelineWidth);
            g.fillOval(x0 + position, y, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
        }

    }

    @Override
    void drawTimeSelectionEffect(Graphics2D g, long time, TimelineManager t, ZoomManager z, int y) {
         if (timeValues.length == 0) return;
        long timeFrom = z.getCurrentMinTime();
        long timeTo = z.getCurrentMaxTime();
        y = y + HEIGHT / 2 - Layer.POINT_RADIUS;
        final int indexOfClosest = TimeSearch.indexOfClosest(timeValues, time);
        long closestTime = timeValues[indexOfClosest];
        if (closestTime < timeFrom || closestTime > timeTo) {
            return;
        }
        int x = t.getXForTime(closestTime);
        int x0 = t.getHeaderWidth() - Layer.POINT_RADIUS;
        g.setColor(TimeSelectionManager.SELECTION_COLOR);
        g.fillOval(x0 + x-1, y-1, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D g, long from, long to, TimelineManager t, ZoomManager z, int y) {
         if (timeValues.length == 0) return;
        long minDisplayed = z.getCurrentMinTime();
        long maxDisplayed = z.getCurrentMaxTime();
        to = Math.min(to, maxDisplayed);
        from = Math.max(from, minDisplayed);
        int lower = TimeSearch.firstGreaterThanOrEqual(timeValues, from);
        int upper = TimeSearch.lastLessThanOrEqual(timeValues, to);
        int x0 = t.getHeaderWidth()-Layer.POINT_RADIUS;
        g.setColor(TimeSelectionManager.SELECTION_COLOR);
        int diameter = Layer.POINT_RADIUS*2;
        y = y + HEIGHT / 2 - Layer.POINT_RADIUS;
        for (int i = lower; i <= upper; i++) {
            long time = timeValues[i];
            int x = t.getXForTime(time);
            g.fillOval(x0+x-1, y-1, diameter, diameter);
        }
    }

    @Override
    long getMaxTimeValue() {
        if (timeValues.length==0) return Long.MIN_VALUE;
        return timeValues[timeValues.length - 1];
    }

    @Override
    long getMinTimeValue() {
        if (timeValues.length==0) return Long.MAX_VALUE;
        return timeValues[0];
    }

}
