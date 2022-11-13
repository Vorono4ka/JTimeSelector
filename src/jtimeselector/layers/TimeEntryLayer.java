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
    public static final Color LEGEND_TEXT_COLOR = Color.black;
    public final long[] timeValues;

    public TimeEntryLayer(String name, long[] timeValues) {
        super(name);
        this.timeValues = timeValues;
    }

    @Override
    int getHeight() {
        return HEIGHT;
    }

    @Override
    void draw(Graphics2D g, TimelineManager timelineManager, ZoomManager zoomManager, int headerSize, int graphicsWidth, int y) {
        g.setColor(LEGEND_TEXT_COLOR);
        g.drawString(getName(), Layer.PADDING, y + TimeEntryLayer.HEIGHT / 2 + g.getFontMetrics().getHeight() / 2);
        if (timeValues.length == 0) return;

        int x0 = headerSize + 2 * Layer.PADDING;
        int timelineWidth = graphicsWidth - x0 - Layer.PADDING;// |(padding)NAME(padding)(point radius space) (POINTS) (point radius space) (padding)|
        if (timelineWidth <= 0) {
            return;
        }

        g.setColor(Color.black);
        for (int i = TimeSearch.firstGreaterThanOrEqual(timeValues, zoomManager.getCurrentMinTime()); i <= TimeSearch.lastLessThanOrEqual(timeValues, zoomManager.getCurrentMaxTime()); i++) {
            int position = timelineManager.getXForTime(timeValues[i]);

            int x = x0 + position;
            int bgrX = x - BRG_RECT_WIDTH / 2;
            int bgrY = y + 3;
            int circleY = y + TimeEntryLayer.HEIGHT / 2 - Layer.POINT_RADIUS;

            g.setColor(Color.darkGray);
            g.fillRoundRect(bgrX, bgrY, Layer.BRG_RECT_WIDTH * 2, HEIGHT - 6, 5, 5);
            g.setColor(Color.white);
            g.fillOval(x, circleY, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
        }
    }

    @Override
    void drawTimeSelectionEffect(Graphics2D g, long time, TimelineManager timelineManager, ZoomManager zoomManager, int y) {
        if (timeValues.length == 0) return;

        int x0 = timelineManager.getLegendWidth() - Layer.POINT_RADIUS;
        int x = x0 + timelineManager.getXForTime(time);
        int circleY = y + HEIGHT / 2 - Layer.POINT_RADIUS;

        g.setColor(TimeSelectionManager.SELECTION_COLOR);
        g.fillOval(x, circleY, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D g, long from, long to, TimelineManager timelineManager, ZoomManager zoomManager, int y) {
         if (timeValues.length == 0) return;
        long minDisplayed = zoomManager.getCurrentMinTime();
        long maxDisplayed = zoomManager.getCurrentMaxTime();
        to = Math.min(to, maxDisplayed);
        from = Math.max(from, minDisplayed);
        int lower = TimeSearch.firstGreaterThanOrEqual(timeValues, from);
        int upper = TimeSearch.lastLessThanOrEqual(timeValues, to);
        int x0 = timelineManager.getLegendWidth()-Layer.POINT_RADIUS;
        g.setColor(TimeSelectionManager.SELECTION_COLOR);
        int diameter = Layer.POINT_RADIUS*2;
        y = y + HEIGHT / 2 - Layer.POINT_RADIUS;
        for (int i = lower; i <= upper; i++) {
            long time = timeValues[i];
            int x = timelineManager.getXForTime(time);
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
