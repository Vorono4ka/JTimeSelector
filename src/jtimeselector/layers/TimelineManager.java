package jtimeselector.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.*;

import com.vorono4ka.BinarySearcher;
import jtimeselector.*;
import jtimeselector.interfaces.TimeToStringConverter;

/**
 * Keeps a list of all layers, draws them on the {@link JTimeSelector} component.
 */
public class TimelineManager {
    public static final Color TIME_LABEL_COLOR = Color.black;
    public static final int TOP_PADDING = 10;

    private final List<Layer> layers = new ArrayList<>();
    private final VisibleAreaManager visibleAreaManager;
    private final TimeSelectionManager timeSelection;
    private final IntervalSelectionManager intervalSelection;
    private final TimeToStringConverter converter;

    private int timelineWidth;
    private int legendWidth;
    private int width;
    private int height;
    private int layersBottomY;
    private int fontHeight;

    // Cursor
    private boolean drawCursor;
    private long cursorTime;
    private int cursorLabelXLeft, cursorLabelXRight;


    public TimelineManager(VisibleAreaManager visibleAreaManager, TimeToStringConverter converter) {
        this.visibleAreaManager = visibleAreaManager;
        this.converter = converter;

        this.timeSelection = new TimeSelectionManager(this);
        this.intervalSelection = new IntervalSelectionManager(this);
    }

    /**
     * Add a new layer containing either only points in time or graph to the
     * collection of the displayed layers
     *
     * @param layer layer object
     */
    public void addLayer(Layer layer) {
        layers.add(layer);
    }

    /**
     * Removes the layer with the given name from the collection of layers that
     * are drawn on the component.
     *
     * @param layerName name of the layer to be removed
     */
    public void removeLayer(String layerName) {
        if (layerName == null) {
            return;
        }

        Layer searchingLayer = getLayerByName(layerName);
        if (searchingLayer != null) {
            layers.remove(searchingLayer);
        }
    }

    /**
     * Clears the collection of layers that are drawn.
     */
    public void removeAllLayers() {
        layers.clear();
    }

    /**
     * Gets the minimum from all time values that need to fit on the timeline.
     *
     * @return Time value represented as a double.
     */
    public long getMinTime() {
        OptionalLong min = layers.stream().mapToLong(Layer::getMinTimeValue).min();
        if (min.isEmpty()) {
            throw new IllegalStateException("Layers list is empty.");
        }
        return min.getAsLong();
    }

    /**
     * Gets the maximum from all time values that need to fit on the timeline.
     *
     * @return Time value represented as a double.
     */
    public long getMaxTime() {
        OptionalLong max = layers.stream().mapToLong(Layer::getMaxTimeValue).max();
        if (max.isEmpty()) {
            throw new IllegalStateException("Layers list is empty.");
        }
        return max.getAsLong();
    }

    public long getClosestTime(long time, int fromLayer, int toLayer) {
        long[] closestTimes = new long[toLayer - fromLayer + 1];

        int i = 0;
        for (int layerIndex = fromLayer; layerIndex <= toLayer; layerIndex++) {
            long closestTime = getClosestTime(time, layerIndex);
            if (closestTime == -1) {
                closestTime = Integer.MAX_VALUE;
            }

            closestTimes[i++] = closestTime;
        }

        Arrays.sort(closestTimes);
        final int indexOfClosest = BinarySearcher.indexOfClosest(closestTimes, time);
        if (indexOfClosest == -1) {
            return -1;
        }

        return closestTimes[indexOfClosest];
    }

    public long getClosestTime(long time, int layerIndex) {
        TimeEntryLayer timeEntryLayer = (TimeEntryLayer) this.layers.get(layerIndex);

        final int indexOfClosest = BinarySearcher.indexOfClosest(timeEntryLayer.timeValues.getArray(), time);
        if (indexOfClosest == -1) {
            return -1;
        }

        return timeEntryLayer.timeValues.get(indexOfClosest);
    }

    /**
     * Calculates the space needed for names of the layers. (=the width of the
     * longest name) The value is dependent on the font size therefore the
     * function needs the graphics on which the text will be drawn.
     *
     * @param graphics The graphics on which the text will be drawn
     * @return required header width
     */
    public int getRequiredHeaderWidth(Graphics2D graphics) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        OptionalInt max = layers.stream().mapToInt(l -> fontMetrics.stringWidth(l.getName())).max();
        if (max.isEmpty()) {
            throw new IllegalStateException("List of layers is empty");
        }
        return max.getAsInt();
    }

    /**
     * @param graphics graphics for drawing the layers
     * @param imageWidth width of the image on which the graphics draws.
     * @param imageHeight height of the image on which the graphics draws.
     */
    public void drawLayers(Graphics2D graphics, int imageWidth, int imageHeight) {
        width = imageWidth;
        height = imageHeight;

        int x = getRequiredHeaderWidth(graphics);
        int y = TimelineManager.TOP_PADDING;

        legendWidth = x + 2 * Layer.PADDING + Layer.POINT_RADIUS;

        timelineWidth = width - legendWidth - Layer.PADDING - Layer.POINT_RADIUS;
        for (Layer layer : layers) {
            layer.draw(graphics, x, imageWidth, y);
            y = y + layer.getHeight();
            if (y > imageHeight) {
                break;
            }
            graphics.setColor(Color.gray);
            graphics.setStroke(new BasicStroke(.1f));
            graphics.drawLine(x, y, imageWidth - Layer.PADDING, y);
        }
        layersBottomY = y;
    }
    public int getTimeLabelsBaselineY() {
        return layersBottomY+fontHeight;
    }

    public void drawTimeLabels(Graphics2D graphics, long timeFrom, long timeTo) {
        final int baseline = getTimeLabelsBaselineY();
        graphics.setColor(TIME_LABEL_COLOR);

        final FontMetrics fontMetrics = graphics.getFontMetrics();

        final String timeFromString = converter.timeToString(timeFrom);
        final int stringFromWidth = fontMetrics.stringWidth(timeFromString);

        final String timeToString = converter.timeToString(timeTo);
        final int timeToStringWidth = fontMetrics.stringWidth(timeToString);

        int left, right;

        left = legendWidth;
        right = legendWidth + stringFromWidth;
        if (!this.isLabelsCollision(left, right)) {
            graphics.drawString(timeFromString, left, baseline);
        }

        left = legendWidth + timelineWidth - timeToStringWidth;
        right = left + timeToStringWidth;
        if (!this.isLabelsCollision(left, right)) {
            graphics.drawString(timeToString, left, baseline);
        }
    }

    /**
     * Lets each layer respond to the fact that time is selected.
     * @param graphics Graphics2D object
     * @param time selected time point
     * @param layerIndex index of selected layer
     */
    public void drawTimeSelectionEffects(Graphics2D graphics, long time, int layerIndex) {
        Layer layer = layers.get(layerIndex);
        layer.drawTimeSelectionEffect(graphics, time, TimelineManager.TOP_PADDING + layer.getHeight() * layerIndex);
    }

    /**
     * Lets each layer respond to the fact that part of it is selected. (For example highlight the selected part.)
     * @param graphics Graphics2D object
     */
    public void drawIntervalSelectionEffects(Graphics2D graphics) {
        long fromX = intervalSelection.getFromTime();
        long toX = intervalSelection.getToTime();
        int fromLayer = intervalSelection.getFromLayer();
        int toLayer = intervalSelection.getToLayer();

        assert fromLayer >= 0 : "fromLayer less than 0";
        assert toLayer < layers.size() : "toLayer more than layers count";

        int y = TimelineManager.TOP_PADDING + TimeEntryLayer.HEIGHT * fromLayer;
        for (int i = fromLayer; i <= toLayer; i++) {
            Layer layer = layers.get(i);
            layer.drawIntervalSelectionEffect(graphics, fromX, toX, y);
            y += layer.getHeight();
        }
    }

    /**
     * @return true if there are no layers to display
     */
    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public int getLayerIndex(int y) {
        if (hasLayerOnPosition(y)) {
            return (y - TimelineManager.TOP_PADDING) / TimeEntryLayer.HEIGHT;
        }

        return -1;
    }

    public int getLayersBottomY() {
        return layersBottomY;
    }

    public long getTimeForX(int x) {
        long timeFrom = visibleAreaManager.getCurrentMinTime();
        long timeTo = visibleAreaManager.getCurrentMaxTime();
        long timeInterval = timeTo - timeFrom;
        x = x - legendWidth;
        return Math.round(timeFrom + timeInterval * x / (double)timelineWidth);
    }

    public long getTimeDistance(int interval) {
        long timeFrom = visibleAreaManager.getCurrentMinTime();
        long timeTo = visibleAreaManager.getCurrentMaxTime();

        long timeInterval = timeTo - timeFrom;
        return (timeInterval * interval) / timelineWidth;
    }

    public int getXForTime(long time) {
        long timeFrom = visibleAreaManager.getCurrentMinTime();
        long timeTo = visibleAreaManager.getCurrentMaxTime();
        double timelinePercent = ((double)(time - timeFrom)) / (timeTo - timeFrom);
        return (int) Math.round(timelinePercent * timelineWidth);
    }

    public int getLegendWidth() {
        return legendWidth;
    }

    /**
     * @return width of the whole component
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return height of the whole component
     */
    public int getHeight() {
        return height;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
    }

    private boolean hasLayerOnPosition(int y) {
        return y >= TimelineManager.TOP_PADDING && y < this.getLayersBottomY();
    }

    private Layer getLayerByName(String layerName) {
        for (Layer layer : layers) {
            if (layer.getName().equals(layerName)) {
                return layer;
            }
        }

        return null;
    }

    public boolean hasSelection() {
        return this.timeSelection.hasSelection() || this.intervalSelection.hasSelection();
    }

    public void setSelection(long time, int layerIndex) {
        timeSelection.setSelection(time, layerIndex);
        intervalSelection.clearSelection();
    }

    public void setSelection(long left, long right, int top, int bottom) {
        intervalSelection.setSelection(left, right, top, bottom);
        timeSelection.clearSelection();
    }

    /**
     * Checks whether the given interval overlaps the interval on which the string representation of selected time has been drawn.
     *
     * @param left min value
     * @param right max value
     * @return true if intervals overlap
     */
    private boolean isLabelsCollision(int left, int right) {
        return this.hasSelection() && IntervalCheck.collision(cursorLabelXLeft, cursorLabelXRight, left, right);
    }

    public void drawSelectionEffects(Graphics2D graphics) {
        if (timeSelection.hasSelection()) {
            this.drawTimeSelectionEffects(graphics, timeSelection.getSelectedTime(), timeSelection.getSelectedLayer());
        }
        if (intervalSelection.hasSelection()) {
            this.drawIntervalSelectionEffects(graphics);
        }

        if (drawCursor && visibleAreaManager.timeValueInCurrentRange(cursorTime)) {
            int cursorX = this.legendWidth + this.getXForTime(cursorTime);
            drawCursor(graphics, cursorX);
        }
    }

    public void clearSelection() {
        this.intervalSelection.clearSelection();
        this.timeSelection.clearSelection();
        this.drawCursor = false;
    }

    public TimeSelectionType getSelectionType() {
        if (this.timeSelection.hasSelection()) {
            return TimeSelectionType.SingleValue;
        }
        if (this.intervalSelection.hasSelection()) {
            return TimeSelectionType.Interval;
        }
        return TimeSelectionType.None;
    }

    /**
     * Draws cursor from top to bottom of the timeline.
     *
     * @param graphics {@link Graphics2D} object
     * @param x position of cursor
     */
    private void drawCursor(Graphics2D graphics, int x) {
        graphics.setColor(TimeSelectionManager.CURSOR_COLOR);

        @SuppressWarnings("UnnecessaryLocalVariable")
        final int cursorTop = TimelineManager.TOP_PADDING;
        final int cursorBottom = this.getTimeLabelsBaselineY();

        graphics.drawLine(x, cursorTop, x, cursorBottom);
        drawLabelOnRightSide(graphics, x, cursorBottom);
    }

    /**
     * Draws label on right side of cursor
     *
     * @param graphics {@link Graphics2D} object
     * @param cursorX x coordinate of the cursor
     * @param textY y coordinate of drawing text
     */
    private void drawLabelOnRightSide(Graphics2D graphics, int cursorX, int textY) {
        FontMetrics fontMetrics = graphics.getFontMetrics();
        String string = this.converter.timeToString(this.cursorTime);
        int width = fontMetrics.stringWidth(string);

        cursorLabelXLeft = cursorX + Layer.PADDING;
        cursorLabelXRight = cursorLabelXLeft + width;

        graphics.drawString(string, cursorLabelXLeft, textY);
    }

    public void setCursorPosition(long time) {
        this.cursorTime = time;
        this.drawCursor = true;
    }
}
