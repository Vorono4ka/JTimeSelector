/*
 */
package jtimeselector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import com.vorono4ka.MathHelper;
import jtimeselector.interfaces.TimeSelectionListener;
import jtimeselector.interfaces.TimeSelector;
import jtimeselector.interfaces.TimeToStringConverter;
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
 * selection (method {@link #addTimeSelectionChangedListener(TimeSelectionListener)
 * }).
 * <p>
 * A time or a time interval can also be set from outside using {@link #selectTime(long)
 * } or {@link #selectTimeInterval(long, long) }.
 * <p>
 * It is possible to set how the time should be displayed to the user, an
 * instance of {@link TimeToStringConverter} can be set either by constructor or
 * explicitly by the setter method {@link #setTimeToStringConverter(TimeToStringConverter)
 * }
 *
 * @author Tomas Prochazka 5.12.2015
 */
public class JTimeSelector extends JPanel implements TimeSelector {

    private static final long serialVersionUID = 1L;
    public static final Color BACKGROUND_COLOR = Color.white;

    private BufferedImage image;
    private int oldWidth = 0;
    private int oldHeight = 0;
    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager = new ZoomManager();
    private final TimeSelectionManager timeSelection;
    private boolean requireRepaint = false;
    private final List<TimeSelectionListener> listeners = new ArrayList<>();
    private final RectangleSelectionGuides rectangleGuides = new RectangleSelectionGuides();
    public static final int TOP_PADDING = 10;

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
        JFrame frame = new JFrame("Time Selector Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        final JTimeSelector jTimeSelector = new JTimeSelector((x) -> Float.toString(x / 1000f));
        frame.add(new JScrollPane(jTimeSelector));
        jTimeSelector.addTimeValuesLayer("Test Layer", new long[]{1000, 2000, 3000, 4000, 5000, 6000});
        jTimeSelector.addTimeValuesLayer("Test Layer 2", new long[]{2_000, 3_000, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000, 16_000});
        jTimeSelector.addTimeValuesLayer("Empty layer", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 2", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 3", new long[]{});
        frame.setSize(800, 400);
        frame.setVisible(true);
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
//            jTimeSelector.addTimeValuesLayer("TestLayer 2nd phase", new long[]{2_000L, 3_000L, 4_000L, 5_000L, 6_000L, 8_000L, 10_000L, 15_000L, 16_000L});
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("NewLayer", new long[]{1_200L, 2_000L, 3_000L, 4_000L, 5_000L, 6_000L});
//            jTimeSelector.requireRepaint();
//
//        }).start();
        jTimeSelector.addTimeSelectionChangedListener((x) -> {
            String type = switch (x.getTimeSelectionType()) {
                case SingleValue -> "single value";
                case Interval -> "interval";
                case None -> "none";
            };
            System.out.println("Time selection changed to " + type);
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
        timelineManager = new TimelineManager(zoomManager, converter);
        timeSelection = new TimeSelectionManager(timelineManager, zoomManager);
        intervalSelection = new IntervalSelectionManager(timelineManager, zoomManager);
        setFont(getFont().deriveFont(15f));
        addMouseWheelListener(this::mouseWheelMoved);
        final MouseInteraction mouseInteraction = new MouseInteraction(timelineManager,
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
            rectangleGuides.drawRectangleSelectionGuides(gr, timelineManager.getLayersBottomY(), timelineManager.getLegendWidth() - Layer.POINT_RADIUS, timelineManager.getCurrentWidth());
        } else {
            timeSelection.drawSelectedTime(gr);
            if (timeSelection.hasSelection()) {
                timelineManager.drawTimeSelectionEffects(gr, TOP_PADDING, timeSelection.getSelectedTime(), timeSelection.getSelectedLayer());
            }
            if (intervalSelection.hasSelection()) {
                intervalSelection.drawIntervalSelection(gr);
                timelineManager.drawIntervalSelectionEffects(gr, TOP_PADDING, intervalSelection.getT1(), intervalSelection.getT2());
            }
        }
        if (!timelineManager.isEmpty()) {
            timelineManager.drawTimeLabels(gr, timelineManager.getLayersBottomY(),
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
        timelineManager.setFontHeight(g.getFontMetrics().getHeight());

        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(BACKGROUND_COLOR);
        g.fillRect(0, 0, oldWidth, oldHeight);
        if (!timelineManager.isEmpty()) {
            timelineManager.drawLayers(g, TOP_PADDING, oldWidth, oldHeight);
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
        timelineManager.addLayer(new TimeEntryLayer(name, timeValues));
        zoomManager.updateMinAndMaxTime(timelineManager);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeGraphLayer(String name) {
        timelineManager.removeLayer(name);
        zoomManager.updateMinAndMaxTime(timelineManager);
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeAllGraphLayers() {
        timelineManager.removeAllLayers();
        if (!timeSelection.checkBounds()) {
            timeSelectionChanged();
        }
        if (intervalSelection.hasSelection()) {
            intervalSelection.clearSelection();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Long getSelectedTime() {
        if (!timeSelection.hasSelection()) {
            return null;
        }

        return timeSelection.getSelectedTime();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTime(long time) {
        timeSelection.selectTime(time, 0);
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
        if (intervalSelection.hasSelection()) {
            return new LongRange(intervalSelection.getT1(), intervalSelection.getT2());
        } else {
            return null;
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public TimeSelectionType getTimeSelectionType() {
        if (timeSelection.hasSelection()) {
            return TimeSelectionType.SingleValue;
        }
        if (intervalSelection.hasSelection()) {
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
        copy.forEach((l) -> l.timeSelectionChanged(this));
    }

    /**
     * {@inheritDoc }
     * The component IS NOT automatically refreshed.
     *
     * @param converter new converter function
     */
    @Override
    public void setTimeToStringConverter(TimeToStringConverter converter) {
        timelineManager.setConverter(converter);
    }

    private void mouseWheelMoved(MouseWheelEvent e) {
        int modifiersEx = e.getModifiersEx();

        int ctrlDownMask = InputEvent.CTRL_DOWN_MASK;
        if ((modifiersEx & ctrlDownMask) == ctrlDownMask) {
            final double preciseWheelRotation = e.getPreciseWheelRotation();
            // Limit the rotation to compensate the different behaviour of mouse and trackpad
            int rotation = MathHelper.clamp((int) Math.round(preciseWheelRotation * 4), -3, 3);
            long time = timelineManager.getTimeForX(e.getX());
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
        } else {
            return;
        }

        requireRepaint = true;
        repaint();
    }
}
