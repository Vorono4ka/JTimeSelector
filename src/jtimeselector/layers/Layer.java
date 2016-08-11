/*
 */

package jtimeselector.layers;

import java.awt.Graphics2D;
import jtimeselector.ZoomManager;

/**
 * Represents a one layer / line on the graphics representation of the component.
 * @author Tomas Prochazka
 * 6.12.2015
 */
public abstract class Layer {
    private final String name;
    public static final int PADDING=5;
    public static final int POINT_RADIUS=5;
    public Layer(String name) {
        this.name = name;
    }
    
    abstract int getHeight();

    public String getName() {
        return name;
    }
    /**
     * Draws the layer
     * @param g Graphics object for drawing
      * @param timeFrom lower bound of a range of time values 
     * @param timeTo upper bound of a range of time values
     * @param headerSize width of the header column (column with layer names)
     * @param graphicsWidth width of the image on which the graphics is drawn.
     * @param y y coordinate of the location where the layer should be drawn.
     */
    abstract void draw(Graphics2D g, long timeFrom, long timeTo,int headerSize, int graphicsWidth,int y);
    
    abstract long getMaxTimeValue();
    abstract long getMinTimeValue();
    abstract void drawTimeSelectionEffect(Graphics2D g, long time, TimelineManager t, ZoomManager z, int y);
    abstract void drawIntervalSelectionEffect(Graphics2D g, long from, long to, TimelineManager t, ZoomManager  z,int y);
}
