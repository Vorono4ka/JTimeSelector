package jtimeselector.layers;

import java.awt.Graphics2D;
import jtimeselector.VisibleAreaManager;

/**
 * Represents a one layer / line on the graphics representation of the component.
 */
public abstract class Layer {
    public static final int PADDING = 5;
    public static final int POINT_RADIUS = 3;
    public static final int BRG_RECT_WIDTH = 6;

    protected final TimelineManager timelineManager;
    protected final VisibleAreaManager visibleAreaManager;
    private final String name;

    public Layer(TimelineManager timelineManager, VisibleAreaManager visibleAreaManager, String name) {
        this.timelineManager = timelineManager;
        this.visibleAreaManager = visibleAreaManager;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Draws the layer
     *
     * @param graphics      Graphics object for drawing
     * @param headerSize    width of the header column (column with layer names)
     * @param graphicsWidth width of the image on which the graphics is drawn.
     * @param y             y coordinate of the location where the layer should be drawn.
     */
    abstract void draw(Graphics2D graphics, int headerSize, int graphicsWidth, int y);

    abstract int getHeight();
    
    abstract long getMaxTimeValue();
    abstract long getMinTimeValue();
    abstract void drawTimeSelectionEffect(Graphics2D graphics, long time, int y);
    abstract void drawIntervalSelectionEffect(Graphics2D graphics, long from, long to, int y);
}
