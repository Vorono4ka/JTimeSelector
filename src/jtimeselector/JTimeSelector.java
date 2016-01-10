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
import jtimeselector.layers.GraphLayer;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;
import jtimeselector.layers.TimeEntryLayer;

/**
 *
 * @author Tomas Prochazka 5.12.2015
 */
public class JTimeSelector extends JPanel implements TimeSelector {

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

    public static void createGUI() {
        JFrame f = new JFrame("Time Selector Test");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());
        final JTimeSelector jTimeSelector = new JTimeSelector();
        f.add(jTimeSelector);
        jTimeSelector.addTimeValuesLayer("TestLayer", new double[]{1.2, 2, 3, 4, 5, 6});
        jTimeSelector.addTimeValuesLayer("TestLayer 2", new double[]{2, 3, 4, 5, 6, 8, 10, 15, 16});
        jTimeSelector.addTimeValuesLayer("Another multi-information layer", new double[]{1, 1.2, 1.5, 1.7, 2, 2.5, 4, 4.5, 6, 7});
        jTimeSelector.addTimeValuesLayer("Empty layer", new double[]{});
        jTimeSelector.addTimeValuesLayer("Empty layer 2", new double[]{});
        jTimeSelector.addTimeValuesLayer("Empty layer 3", new double[]{});
        f.setSize(800, 400);
        f.setVisible(true);
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
//            jTimeSelector.addTimeValuesLayer("TestLayer 2nd phase", new double[]{2, 3, 4, 5, 6, 8, 10, 15, 16});
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("NewLayer", new double[]{1.2, 2, 3, 4, 5, 6});
//
//        }).start();
        jTimeSelector.addTimeSelectionChangedListener((x)-> {
            String type=""; 
            switch(x.getTimeSelectionType()){
                case SingleValue:
                    type="single value";
                    break;
                case Interval:
                    type="interval";
                    break;
                case None:
                    type="none";
                    break;
            }
            System.out.println("Time selection chaned to " + type);
        });
    }
    private final IntervalSelectionManager intervalSelection;

    /**
     * Creates a new component that displays a list of values in time. Double
     * time values will be converted to string using the function {@link Double#toString()
     * }.
     */
    public JTimeSelector() {
        this(Double::toString);
    }

    /**
     * Creates a new component that displays a lists of values in time. The time
     * values are represented as doubles. By providing a converter
     * {@code converter} you can affect how the double values will be drawn on
     * the component. If no converter is specified, the function {@link Double#toString()
     * } is used.
     *
     * @param converter a function converting double time values to string
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
            double time = layerManager.getTimeForX(e.getX());
            final double currentMinTime = zoomManager.getCurrentMinTime();
            final double currentMaxTime = zoomManager.getCurrentMaxTime();
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
                layerManager.drawTimeSelectionEffects(gr, TOP_PADDING, timeSelection.getSelectedTime());
            }
            if (intervalSelection.isSelection()) {
                intervalSelection.drawIntervalSelection(gr);
                layerManager.drawIntervalSelectionEffects(gr,TOP_PADDING, intervalSelection.getT1(), intervalSelection.getT2());
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

    @Override
    public void addTimeValuesLayer(String name, double[] timeValues) {
        layerManager.addLayer(new TimeEntryLayer(name, timeValues));
        zoomManager.updateMinAndMaxTime(layerManager);
    }

    @Override
    public void addGraphLayer(String name, double[][] values) {
        layerManager.addLayer(new GraphLayer(name, values));
        zoomManager.updateMinAndMaxTime(layerManager);
    }

    @Override
    public void removeGraphLayer(String name) {
        layerManager.removeLayer(name);
        zoomManager.updateMinAndMaxTime(layerManager);
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
    }

    @Override
    public void removeAllGraphLayers() {
        layerManager.removeAllLayers();
        timeSelection.checkBounds();
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
    }

    @Override
    public Double getSelectedTime() {
        if (timeSelection.isSelection() == false) {
            return null;
        }
        return timeSelection.getSelectedTime();
    }

    @Override
    public void selectTime(double d) {
        timeSelection.selectTime(d);
        intervalSelection.clearSelection();
        repaint();
        timeSelectionChanged();
    }

    @Override
    public void selectTimeInterval(double from, double to) {
        intervalSelection.setSelection(from, to);
        timeSelection.clearSelection();
        timeSelectionChanged();
        repaint();
    }

    @Override
    public DoubleRange getSelectedTimeInterval() {
        if (intervalSelection.isSelection()) {
            return new DoubleRange(intervalSelection.getT1(), intervalSelection.getT2());
        } else {
            return null;
        }
    }

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
