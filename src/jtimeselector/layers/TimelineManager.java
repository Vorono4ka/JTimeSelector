/*
 */
package jtimeselector.layers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import jtimeselector.IntervalSelectionManager;
import jtimeselector.TimeSelectionManager;
import jtimeselector.TimeToStringConverter;
import jtimeselector.ZoomManager;

/**
 * Keeps a list of all layers, draws them on the JTimeSelector component.
 *
 * @author Tomas Prochazka 6.12.2015
 */
public class TimelineManager {

    List<Layer> layers = new ArrayList<>();
    int currentHeaderWidth;
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
    public double getMinTime() {
        OptionalDouble min = layers.stream().mapToDouble(l -> l.getMinTimeValue()).min();
        if (!min.isPresent()) {
            throw new IllegalStateException("Layers list is empty.");
        }
        return min.getAsDouble();
    }

    /**
     * Gets the maximum from all time values that need to fit on the timeline.
     *
     * @return Time value represented as a double.
     */
    public double getMaxTime() {
        OptionalDouble max = layers.stream().mapToDouble(l -> l.getMaxTimeValue()).max();
        if (!max.isPresent()) {
            throw new IllegalStateException("Layers list is empty.");
        }
        return max.getAsDouble();
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
        if (!max.isPresent()) {
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
        double timeFrom = zoomManager.getCurrentMinTime();
        double timeTo = zoomManager.getCurrentMaxTime();
        int x = getRequiredHeaderWidth(g);
        currentHeaderWidth = x + 2 * Layer.PADDING + Layer.POINT_RADIUS;
        timelineWidth = currentWidth - currentHeaderWidth - Layer.PADDING - Layer.POINT_RADIUS;
        for (Layer layer : layers) {
            layer.draw(g, timeFrom, timeTo, x, imageWidth, y);
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
    public void drawTimeLabels(Graphics2D g, int y, double timeFrom, double timeTo, TimeSelectionManager s, IntervalSelectionManager is) {
       int xMin,xMax;
        final FontMetrics fontMetrics = g.getFontMetrics();
        int baseline = getTimeLabelsBaselineY();
        g.setColor(Color.black);
        final String timeFromString = converter.timeToString(timeFrom);
        int stringFromWidth = fontMetrics.stringWidth(timeFromString);
        xMin = currentHeaderWidth;
        xMax = currentHeaderWidth + stringFromWidth;
        if (!s.labelCollision(xMin, xMax) && !is.labelsCollision(xMin, xMax)) {
            g.drawString(timeFromString, currentHeaderWidth, baseline);
        }
        final String timeToString = converter.timeToString(timeTo);
        int stringToWidth = fontMetrics.stringWidth(timeToString);
        xMin = currentHeaderWidth + timelineWidth - stringToWidth;
        xMax = xMin + stringToWidth;
        if (!s.labelCollision(xMin, xMax) && !is.labelsCollision(xMin, xMax)) {
            g.drawString(timeToString, xMin, baseline);
        }
    }
/**
     * Lets each layer respond to the fact that time is selected. 
 * @param g
 * @param y
 * @param time 
 */
    public void drawTimeSelectionEffects(Graphics2D g, int y, double time) {
        for (Layer layer : layers) {
            layer.drawTimeSelectionEffect(g, time, this, zoomManager, y);
            y = y + layer.getHeight();
        }
    }
    /**
     * Lets each layer respond to the fact that part of it is selected. 
     * (For example highlight the selected part.)
     * @param g
     * @param y
     * @param from
     * @param to 
     */
    public void drawIntervalSelectionEffects(Graphics2D g, int y, double from, double to) {
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

    public double getTimeForX(int x) {
        double timeFrom = zoomManager.getCurrentMinTime();
        double timeTo = zoomManager.getCurrentMaxTime();
        double timeInterval = timeTo - timeFrom;
        x = x - currentHeaderWidth;
        return timeFrom + timeInterval * x / timelineWidth;
    }

    public double getTimeDistance(int interval) {
        double timeFrom = zoomManager.getCurrentMinTime();
        double timeTo = zoomManager.getCurrentMaxTime();

        double timeInterval = timeTo - timeFrom;
        return timeInterval * interval / timelineWidth;

    }

    public int getXForTime(double time) {
        double timeFrom = zoomManager.getCurrentMinTime();
        double timeTo = zoomManager.getCurrentMaxTime();
        double timelinePercent = (time - timeFrom) / (timeTo - timeFrom);
        return (int) Math.round(timelinePercent * timelineWidth);
    }

    public int getHeaderWidth() {
        return currentHeaderWidth;
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
