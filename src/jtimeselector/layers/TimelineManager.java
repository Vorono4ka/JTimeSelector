/*
 */
package jtimeselector.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import jtimeselector.IntervalSelectionManager;
import jtimeselector.TimeSearch;
import jtimeselector.TimeSelectionManager;
import jtimeselector.interfaces.TimeToStringConverter;
import jtimeselector.ZoomManager;

/**
 * Keeps a list of all layers, draws them on the JTimeSelector component.
 *
 * @author Tomas Prochazka 6.12.2015
 */
public class TimelineManager {
    public static final Color TIME_LABEL_COLOR = Color.black;
    private final List<Layer> layers = new ArrayList<>();
    private int currentLegendWidth;
    private final ZoomManager zoomManager;
    private int currentWidth;
    private int currentHeight;
    private int timelineWidth;
    private int layersBottomY;
    private TimeToStringConverter converter;
    private int fontHeight;

    public int getLayersBottomY() {
        return layersBottomY;
    }

    public TimelineManager(ZoomManager zoomManager, TimeToStringConverter converter) {
        this.zoomManager = zoomManager;
        this.converter = converter;
    }

    /**
     * Add a new layer containing either only points in time or graph to the
     * collection of the displayed layers
     *
     * @param l
     */
    public void addLayer(Layer l) {
        layers.add(l);
    }

    public void setConverter(TimeToStringConverter converter) {
        this.converter = converter;
    }

    public TimeToStringConverter getConverter() {
        return converter;
    }

    /**
     * Removes the layer with the given name from the collection of layers that
     * are drawn on the component.
     *
     * @param layerName
     */
    public void removeLayer(String layerName) {
        if (layerName == null) {
            return;
        }
        Layer l = null;
        for (Layer layer : layers) {
            if (layer.getName().equals(layerName)) {
                l = layer;
                break;
            }
        }
        if (l != null) {
            layers.remove(l);
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

    public long getClosestTime(long time, int layerIndex) {
        long[] timeValues = ((TimeEntryLayer) this.layers.get(layerIndex)).timeValues;

        final int indexOfClosest = TimeSearch.indexOfClosest(timeValues, time);
        if (indexOfClosest == -1) {
            return -1;
        }

        return timeValues[indexOfClosest];
    }

    /**
     * Calculates the space needed for names of the layers. (=the width of the
     * longest name) The value is dependent on the font size therefore the
     * function needs the graphics on which the text will be drawn.
     *
     * @param g The graphics on which the text will be drawn
     * @return
     */
    public int getRequiredHeaderWidth(Graphics2D g) {
        FontMetrics fontMetrics = g.getFontMetrics();
        OptionalInt max = layers.stream().mapToInt(l -> fontMetrics.stringWidth(l.getName())).max();
        if (max.isEmpty()) {
            throw new IllegalStateException("List of layers is empty");
        }
        return max.getAsInt();
    }

    /**
     *
     * @param g Graphics for drawing the layers
     * @param y y coordinate of the top left corner
     * @param imageWidth Width of the image on which the graphics g draws.
     * @param imageHeight Height of the image on which the graphics g draws.
     */
    public void drawLayers(Graphics2D g, int y, int imageWidth, int imageHeight) {
        currentWidth = imageWidth;
        currentHeight = imageHeight;
        int x = getRequiredHeaderWidth(g);
        currentLegendWidth = x + 2 * Layer.PADDING + Layer.POINT_RADIUS;
        timelineWidth = currentWidth - currentLegendWidth - Layer.PADDING - Layer.POINT_RADIUS;
        for (Layer layer : layers) {
            layer.draw(g, this, zoomManager, x, imageWidth, y);
            y = y + layer.getHeight();
            if (y > imageHeight) {
                break;
            }
            g.setColor(Color.gray);
            g.setStroke(new BasicStroke(.1f));
            g.drawLine(x, y, imageWidth - Layer.PADDING, y);
        }
        layersBottomY = y;
    }
    public int getTimeLabelsBaselineY() {
        return layersBottomY+fontHeight;
    }

    public void drawTimeLabels(Graphics2D g, int y, long timeFrom, long timeTo, TimeSelectionManager timeSelectionManager, IntervalSelectionManager intervalSelectionManager) {
        final int baseline = getTimeLabelsBaselineY();
        g.setColor(TIME_LABEL_COLOR);

        final FontMetrics fontMetrics = g.getFontMetrics();

        final String timeFromString = converter.timeToString(timeFrom);
        final int stringFromWidth = fontMetrics.stringWidth(timeFromString);

        final String timeToString = converter.timeToString(timeTo);
        final int timeToStringWidth = fontMetrics.stringWidth(timeToString);

        int left, right;

        left = currentLegendWidth;
        right = currentLegendWidth + stringFromWidth;
        if (!timeSelectionManager.labelCollision(left, right) && !intervalSelectionManager.labelsCollision(left, right)) {
            g.drawString(timeFromString, left, baseline);
        }

        left = currentLegendWidth + timelineWidth - timeToStringWidth;
        right = left + timeToStringWidth;
        if (!timeSelectionManager.labelCollision(left, right) && !intervalSelectionManager.labelsCollision(left, right)) {
            g.drawString(timeToString, left, baseline);
        }
    }
/**
     * Lets each layer respond to the fact that time is selected. 
 * @param g
 * @param y
 * @param time 
 */
    public void drawTimeSelectionEffects(Graphics2D g, int y, long time, int layerIndex) {  // TODO: add selection of all layers on layerIndex = -1
        Layer layer = layers.get(layerIndex);
        layer.drawTimeSelectionEffect(g, time, this, zoomManager, y + layer.getHeight() * layerIndex);
    }

    /**
     * Lets each layer respond to the fact that part of it is selected. 
     * (For example highlight the selected part.)
     * @param g
     * @param y
     * @param from
     * @param to 
     */
    public void drawIntervalSelectionEffects(Graphics2D g, int y, long from, long to) {
        for (Layer layer : layers) {
            layer.drawIntervalSelectionEffect(g, from,to, this, zoomManager, y);
            y = y + layer.getHeight();
        }
    }

    /**
     *
     * @return true if there are no layers to display
     */
    public boolean isEmpty() {
        return layers.isEmpty();
    }

    public long getTimeForX(int x) {
        long timeFrom = zoomManager.getCurrentMinTime();
        long timeTo = zoomManager.getCurrentMaxTime();
        long timeInterval = timeTo - timeFrom;
        x = x - currentLegendWidth;
        return Math.round(timeFrom + timeInterval * x / (double)timelineWidth);
    }

    public long getTimeDistance(int interval) {
        long timeFrom = zoomManager.getCurrentMinTime();
        long timeTo = zoomManager.getCurrentMaxTime();

        long timeInterval = timeTo - timeFrom;
        return (timeInterval * interval) / timelineWidth;
    }

    public int getXForTime(long time) {
        long timeFrom = zoomManager.getCurrentMinTime();
        long timeTo = zoomManager.getCurrentMaxTime();
        double timelinePercent = ((double)(time - timeFrom)) / (timeTo - timeFrom);
        return (int) Math.round(timelinePercent * timelineWidth);
    }

    public int getLegendWidth() {
        return currentLegendWidth;
    }

    /**
     * Gets the current width of the whole component
     *
     * @return
     */
    public int getCurrentWidth() {
        return currentWidth;
    }

    /**
     * Gets the current height of the whole component
     *
     * @return
     */
    public int getCurrentHeight() {
        return currentHeight;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
    }
    
}
