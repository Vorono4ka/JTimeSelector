package jtimeselector.layers;

import java.awt.Color;
import java.awt.Graphics2D;

import com.vorono4ka.LongList;
import com.vorono4ka.interfaces.BinarySearcher;
import jtimeselector.TimeSelectionManager;
import jtimeselector.ZoomManager;

/**
 * Responsible for drawing a single timeline layer.
 */
public class TimeEntryLayer extends Layer {
    public static final int HEIGHT = 30;
    public static final Color LEGEND_TEXT_COLOR = Color.black;
    public final LongList timeValues;

    public TimeEntryLayer(String name, long[] array) {
        this(name, new LongList(array));
    }

    public TimeEntryLayer(String name, LongList timeValues) {
        super(name);

        this.timeValues = timeValues;
    }

    @Override
    int getHeight() {
        return HEIGHT;
    }

    @Override
    public void draw(Graphics2D graphics, TimelineManager timelineManager, ZoomManager zoomManager, int headerSize, int graphicsWidth, int y) {
        drawLegend(graphics, y);
        if (timeValues.size() == 0) return;

        int x0 = headerSize + 2 * Layer.PADDING;
        int timelineWidth = graphicsWidth - x0 - Layer.PADDING;// |(padding)NAME(padding)(point radius space) (POINTS) (point radius space) (padding)|
        if (timelineWidth <= 0) {
            return;
        }

        graphics.setColor(Color.black);
        for (int i = BinarySearcher.firstGreaterThanOrEqual(timeValues.getArray(), zoomManager.getCurrentMinTime()); i <= BinarySearcher.lastLessThanOrEqual(timeValues.getArray(), zoomManager.getCurrentMaxTime()); i++) {
            int position = timelineManager.getXForTime(timeValues.get(i));

            int x = x0 + position;
            int bgrX = x - BRG_RECT_WIDTH / 2;
            int bgrY = y + 3;
            int circleY = y + TimeEntryLayer.HEIGHT / 2 - Layer.POINT_RADIUS;

            graphics.setColor(Color.darkGray);
            graphics.fillRoundRect(bgrX, bgrY, Layer.BRG_RECT_WIDTH * 2, HEIGHT - 6, 5, 5);
            graphics.setColor(Color.white);
            graphics.fillOval(x, circleY, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
        }
    }

    private void drawLegend(Graphics2D graphics, int y) {
        graphics.setColor(LEGEND_TEXT_COLOR);
        graphics.drawString(getName(), Layer.PADDING, y + TimeEntryLayer.HEIGHT / 2 + graphics.getFontMetrics().getHeight() / 2);
    }

    @Override
    void drawTimeSelectionEffect(Graphics2D graphics, long time, TimelineManager timelineManager, ZoomManager zoomManager, int y) {
        if (timeValues.size() == 0) return;

        int x0 = timelineManager.getLegendWidth() - Layer.POINT_RADIUS;
        int x = x0 + timelineManager.getXForTime(time);
        int circleY = y + HEIGHT / 2 - Layer.POINT_RADIUS;

        graphics.setColor(TimeSelectionManager.SELECTION_COLOR);
        graphics.fillOval(x, circleY, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D graphics, long from, long to, TimelineManager timelineManager, ZoomManager zoomManager, int y) {
        if (timeValues.size() == 0) return;

        graphics.setColor(TimeSelectionManager.SELECTION_COLOR);
        int diameter = Layer.POINT_RADIUS * 2;
        y = y + HEIGHT / 2 - Layer.POINT_RADIUS;
        for (long time : timeValues.toArray()) {
            if (time < from || time > to) continue;

            int x = timelineManager.getLegendWidth() + timelineManager.getXForTime(time) - Layer.POINT_RADIUS;
            graphics.fillOval(x-1, y-1, diameter, diameter);
        }
    }

    @Override
    long getMaxTimeValue() {
        if (timeValues.size() == 0) return Long.MIN_VALUE;
        return timeValues.get(timeValues.size() - 1);
    }

    @Override
    long getMinTimeValue() {
        if (timeValues.size() == 0) return Long.MAX_VALUE;
        return timeValues.get(0);
    }

}
