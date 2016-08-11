/*
 */
package jtimeselector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;
import jtimeselector.layers.TimeEntryLayer;

/**
 * A swing component that displays a timeline with possibility of user
 * interaction: selection of a time / interval, zoom in / zoom out.
 * <p>
 * Layers with values can be added using the method {@link #addTimeValuesLayer(java.lang.String, long[])
 * }.
 * <p>
 * It is possible to attach listeners that will be notified of changes in
 * selection (method {@link #addTimeSelectionChangedListener(jtimeselector.TimeSelectionListener)
 * }).
 * <p>
 * A time or a time interval can also be set from outside using {@link #selectTime(long)
 * } or {@link #selectTimeInterval(long, long) }.
 * <p>
 * It is possible to set how the time should be displayed to the user, an
 * instance of {@link TimeToStringConverter} can be set either by constructor or
 * explicitly by the setter method {@link #setTimeToStringConverter(jtimeselector.TimeToStringConverter)
 * }
 *
 * @author Tomas Prochazka 5.12.2015
 */
public class JTimeSelector extends JPanel implements TimeSelector {

    private static final long serialVersionUID = 1L;

    private BufferedImage image;
    private int oldWidth = 0;
    private int oldHeight = 0;
    private final TimelineManager layerManager;
    private final ZoomManager zoomManager = new ZoomManager();
    private final TimeSelectionManager timeSelection;
    private boolean requireRepaint = false;
    private Color backgroundColor;
    public static final Color RECT_COLOR_TRANSP = new Color(217, 118, 12, 70);
    private List<TimeSelectionListener> listeners = new ArrayList<>();
    public static final int TOP_PADDING = 10;
    private RectangleSelectionGuides rectangleGuides = new RectangleSelectionGuides();

    /**
     * Test.
     *
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createGUI();
        });
    }

    /**
     * test method only
     */
    public static void createGUI() {
        JFrame f = new JFrame("Time Selector Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        final JTimeSelector jTimeSelector = new JTimeSelector((x) -> {
            return Long.toString(x / 1000);
        });
        f.add(jTimeSelector);
        jTimeSelector.addTimeValuesLayer("Test Layer", new long[]{1000, 2000, 3000, 4000, 5000, 6000});
        jTimeSelector.addTimeValuesLayer("Test Layer 2", new long[]{2_000, 3_000, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000, 16_000});
        jTimeSelector.addTimeValuesLayer("Third Test Layer", new long[]{-6_000, -1_000, 0_000, 1_000, 2_000, 3_000, 4_000, 5_000, 6_000, 7_000});
        jTimeSelector.addTimeValuesLayer("Empty layer", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 2", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 3", new long[]{});
        f.setSize(800, 400);
        f.setVisible(true);
        // test of the correct behaviour if layers are added/removed:
//        new Thread(() -> {
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.removeAllGraphLayers();
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("TestLayer 2nd phase", new long[]{2, 3, 4, 5, 6, 8, 10, 15, 16});
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("NewLayer", new long[]{1.2, 2, 3, 4, 5, 6});
//
//        }).start();
        jTimeSelector.addTimeSelectionChangedListener((x) -> {
            String type = "";
            switch (x.getTimeSelectionType()) {
                case SingleValue:
                    type = "single value";
                    break;
                case Interval:
                    type = "interval";
                    break;
                case None:
                    type = "none";
                    break;
            }
            System.out.println("Time selection chaned to " + type);
        });
    }
    private final IntervalSelectionManager intervalSelection;

    /**
     * Creates a new component that displays a list of values in time. Long time
     * values will be converted to string using the function {@link Long#toString()
     * }.
     */
    public JTimeSelector() {
        this(Long::toString);
    }

    /**
     * Creates a new component that displays a lists of values in time. The time
     * values are represented as longs. By providing a converter
     * {@code converter} you can affect how the long values will be drawn on the
     * component. If no converter is specified, the function {@link Long#toString()
     * } is used.
     *
     * @param converter a function converting long time values to string
     */
    public JTimeSelector(TimeToStringConverter converter) {
        layerManager = new TimelineManager(zoomManager, converter);
        timeSelection = new TimeSelectionManager(layerManager, zoomManager);
        intervalSelection = new IntervalSelectionManager(layerManager, zoomManager);
        setFont(getFont().deriveFont(15f));
        addMouseWheelListener((MouseWheelEvent e) -> {
            final double preciseWheelRotation = e.getPreciseWheelRotation();
            int rotation = (int) Math.round(preciseWheelRotation * 4);
            //Limit the rotation to compensate the different behaviour 
            // of mouse and trackpad
            if (rotation < -3) {
                rotation = -3;
            }
            if (rotation > 3) {
                rotation = 3;
            }
            long time = layerManager.getTimeForX(e.getX());
            final long currentMinTime = zoomManager.getCurrentMinTime();
            final long currentMaxTime = zoomManager.getCurrentMaxTime();
            if (time < currentMinTime || time > currentMaxTime) {
                return;
            }
            if (rotation < 0) {
                zoomManager.zoomIn(-rotation, time);
            } else {
                zoomManager.zoomOut(rotation, time);
            }
            requireRepaint = true;
            repaint();

        });
        final MouseInteraction mouseInteraction = new MouseInteraction(layerManager,
                zoomManager, this, timeSelection,
                rectangleGuides, intervalSelection);
        addMouseListener(mouseInteraction);
        addMouseMotionListener(mouseInteraction);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Dimension size = getSize();
        if (size.width != oldWidth || size.height != oldHeight) {
            image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            repaintImage();
            requireRepaint = false;
        } else if (requireRepaint) {
            repaintImage();
            requireRepaint = false;
        }
        Graphics2D gr = (Graphics2D) g;
        gr.drawImage(image, null, 0, 0);
        gr.setFont(getFont());

        if (rectangleGuides.visible()) {
            rectangleGuides.drawRectangleSelectionGuides(gr, layerManager.getLayersBottomY(), layerManager.getHeaderWidth() - Layer.POINT_RADIUS, layerManager.getCurrentWidth());
        } else {
            timeSelection.drawSelectedTime(gr);
            if (timeSelection.isSelection()) {
                layerManager.drawTimeSelectionEffects(gr, TOP_PADDING, (long) timeSelection.getSelectedTime());
            }
            if (intervalSelection.isSelection()) {
                intervalSelection.drawIntervalSelection(gr);
                layerManager.drawIntervalSelectionEffects(gr, TOP_PADDING, (long) intervalSelection.getT1(), (long) intervalSelection.getT2());
            }
        }
        if (!layerManager.isEmpty()) {
            layerManager.drawTimeLabels(gr, layerManager.getLayersBottomY(),
                    zoomManager.getCurrentMinTime(), zoomManager.getCurrentMaxTime(),
                    timeSelection, intervalSelection);
        }
        requireRepaint = false;
    }

    protected void repaintImage() {

        Dimension size = getSize();
        oldWidth = size.width;
        oldHeight = size.height;
        Graphics2D g = image.createGraphics();
        g.setFont(getFont());
        layerManager.setFontHeight(g.getFontMetrics().getHeight());

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backgroundColor = Color.white;
        g.setColor(backgroundColor);
        g.fillRect(0, 0, oldWidth, oldHeight);
        if (!layerManager.isEmpty()) {
            layerManager.drawLayers(g, TOP_PADDING, oldWidth, oldHeight);
        }

    }

    @Override
    public void requireRepaint() {
        requireRepaint = true;
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void addTimeValuesLayer(String name, long[] timeValues) {
        layerManager.addLayer(new TimeEntryLayer(name, timeValues));
        zoomManager.updateMinAndMaxTime(layerManager);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeGraphLayer(String name) {
        layerManager.removeLayer(name);
        zoomManager.updateMinAndMaxTime(layerManager);
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeAllGraphLayers() {
        layerManager.removeAllLayers();
        timeSelection.checkBounds();
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Long getSelectedTime() {
        if (timeSelection.isSelection() == false) {
            return null;
        }
        return (long) timeSelection.getSelectedTime();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTime(long d) {
        timeSelection.selectTime(d);
        intervalSelection.clearSelection();
        repaint();
        timeSelectionChanged();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTimeInterval(long from, long to) {
        intervalSelection.setSelection(from, to);
        timeSelection.clearSelection();
        timeSelectionChanged();
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LongRange getSelectedTimeInterval() {
        if (intervalSelection.isSelection()) {
            return new LongRange((long) intervalSelection.getT1(), (long) intervalSelection.getT2());
        } else {
            return null;
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public TimeSelectionType getTimeSelectionType() {
        if (timeSelection.isSelection()) {
            return TimeSelectionType.SingleValue;
        }
        if (intervalSelection.isSelection()) {
            return TimeSelectionType.Interval;
        }
        return TimeSelectionType.None;
    }

    @Override
    public void addTimeSelectionChangedListener(TimeSelectionListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTimeSelectionChangedListener(TimeSelectionListener l) {
        listeners.remove(l);
    }

    protected void timeSelectionChanged() {
        List<TimeSelectionListener> copy = new ArrayList<>(listeners);
        copy.stream().forEach((l) -> {
            l.timeSelectionChanged(this);
        });
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * {@inheritDoc }
     * The component IS NOT automatically refreshed.
     *
     * @param converter new converter function
     */
    @Override
    public void setTimeToStringConverter(TimeToStringConverter converter) {
        layerManager.setConverter(converter);
    }

}
