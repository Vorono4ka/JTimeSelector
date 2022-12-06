package jtimeselector.layers;

import java.awt.Color;
import java.awt.Graphics2D;

import com.vorono4ka.LongList;
import com.vorono4ka.BinarySearcher;
import jtimeselector.TimeSelectionManager;
import jtimeselector.VisibleAreaManager;

/**
 * Responsible for drawing a single timeline layer.
 */
public class TimeEntryLayer extends Layer {
    public static final int HEIGHT = 30;
    public static final Color LEGEND_TEXT_COLOR = Color.black;
    public static final Color BGR_DEFAULT_COLOR = Color.black;
    public static final Color BGR_SELECTED_COLOR = Color.gray;

    public final LongList timeValues;

    public TimeEntryLayer(TimelineManager timelineManager, VisibleAreaManager visibleAreaManager, String name, long[] array) {
        this(timelineManager, visibleAreaManager, name, new LongList(array));
    }

    public TimeEntryLayer(TimelineManager timelineManager, VisibleAreaManager visibleAreaManager, String name, LongList timeValues) {
        super(timelineManager, visibleAreaManager, name);

        this.timeValues = timeValues;
    }

    @Override
    int getHeight() {
        return HEIGHT;
    }

    @Override
    public void draw(Graphics2D graphics, int headerSize, int graphicsWidth, int y) {  // TODO: draw selected and unselected entries in current method
        drawLegend(graphics, y);

        if (timeValues.size() == 0) return;

        final int x = headerSize + 2 * Layer.PADDING;
        final int pointY = y + TimeEntryLayer.HEIGHT / 2 - Layer.POINT_RADIUS;

        graphics.setColor(BGR_DEFAULT_COLOR);
        for (int i = BinarySearcher.firstGreaterThanOrEqual(timeValues.getArray(), this.visibleAreaManager.getCurrentMinTime()); i <= BinarySearcher.lastLessThanOrEqual(timeValues.getArray(), visibleAreaManager.getCurrentMaxTime()); i++) {
            int position = timelineManager.getXForTime(timeValues.get(i));

            int pointX = x + position;
            int bgrX = pointX - BRG_RECT_WIDTH / 2;
            int bgrY = y + 3;

            graphics.setColor(Color.darkGray);
            graphics.fillRoundRect(bgrX, bgrY, Layer.BRG_RECT_WIDTH * 2, HEIGHT - 6, 5, 5);
            graphics.setColor(Color.white);
            graphics.fillOval(pointX, pointY, Layer.POINT_RADIUS * 2, Layer.POINT_RADIUS * 2);
        }
    }

    private void drawLegend(Graphics2D graphics, int y) {
        graphics.setColor(LEGEND_TEXT_COLOR);
        graphics.drawString(getName(), Layer.PADDING, y + TimeEntryLayer.HEIGHT / 2 + graphics.getFontMetrics().getHeight() / 2);
    }

    @Override
    void drawTimeSelectionEffect(Graphics2D graphics, long time, int y) {
        if (timeValues.size() == 0) return;

        int circleX = timelineManager.getLegendWidth() + timelineManager.getXForTime(time) - Layer.POINT_RADIUS;
        int circleY = y + HEIGHT / 2 - Layer.POINT_RADIUS;

        int diameter = Layer.POINT_RADIUS * 2;

        graphics.setColor(TimeSelectionManager.SELECTION_COLOR);
        graphics.fillOval(circleX, circleY, diameter, diameter);
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D graphics, long from, long to, int y) {
        if (timeValues.size() == 0) return;

        for (long time : timeValues.toArray()) {
            if (time < from || time > to) continue;

            drawTimeSelectionEffect(graphics, time, y);
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
