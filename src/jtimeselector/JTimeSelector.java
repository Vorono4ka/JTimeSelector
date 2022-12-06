package jtimeselector;

import com.vorono4ka.MathHelper;
import jtimeselector.interfaces.TimeSelectionListener;
import jtimeselector.interfaces.TimeSelector;
import jtimeselector.interfaces.TimeToStringConverter;
import jtimeselector.layers.TimeEntryLayer;
import jtimeselector.layers.TimelineManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A swing component that displays a timeline with possibility of user interaction: selection of a time / interval, zoom in / zoom out.
 * <p>
 * Layers with values can be added using the method {@link #addTimeValuesLayer(java.lang.String, long[])}.
 * <p>
 * It is possible to attach listeners that will be notified of changes in selection (method {@link #addTimeSelectionChangedListener(TimeSelectionListener)}).
 * <p>
 * A time or a time interval can also be set from outside using {@link #selectTime(long, int)} or {@link #selectTimeInterval(long, long, int, int)}.
 * <p>
 * It is possible to set how the time should be displayed to the user, an instance of {@link TimeToStringConverter} can be set either by constructor or explicitly by the setter method {@link #setTimeToStringConverter(TimeToStringConverter)}
 */
public class JTimeSelector extends JPanel implements TimeSelector {
    public static final Color BACKGROUND_COLOR = Color.white;
    public static final int TOP_PADDING = 10;

    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;
    private final TimeSelectionManager timeSelection;
    private final List<TimeSelectionListener> listeners = new ArrayList<>();
    private final RectangleSelectionGuides rectangleGuides = new RectangleSelectionGuides();
    private final IntervalSelectionManager intervalSelection;

    private boolean requireRepaint = false;
    private BufferedImage image;
    private int oldWidth = 0;
    private int oldHeight = 0;

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
        zoomManager = new ZoomManager();
        timelineManager = new TimelineManager(zoomManager, converter);
        timeSelection = new TimeSelectionManager(timelineManager, zoomManager);
        intervalSelection = new IntervalSelectionManager(timelineManager, zoomManager);
        setFont(getFont().deriveFont(15f));
        addMouseWheelListener(this::mouseWheelMoved);

        final MouseInteraction mouseInteraction = new MouseInteraction(
                timelineManager,
                zoomManager,
                this,
                timeSelection,
                rectangleGuides,
                intervalSelection
        );

        addMouseListener(mouseInteraction);
        addMouseMotionListener(mouseInteraction);
    }

    @Override
    protected void paintComponent(Graphics graphics) {  // TODO: draw major and minor ticks
        Dimension size = this.getSize();
        if (size.width != oldWidth || size.height != oldHeight) {
            image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            requireRepaint = true;
        }

        if (requireRepaint) {
            repaintImage();
            requireRepaint = false;
        }

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.drawImage(image, null, 0, 0);
        graphics2D.setFont(getFont());

        if (rectangleGuides.isVisible()) {
            rectangleGuides.drawRectangleSelectionGuides(graphics2D, timelineManager.getLegendWidth(), timelineManager.getCurrentWidth());
        } else {
            timeSelection.drawSelectedTime(graphics2D);
            if (timeSelection.hasSelection()) {
                timelineManager.drawTimeSelectionEffects(graphics2D, TOP_PADDING, timeSelection.getSelectedTime(), timeSelection.getSelectedLayer());
            }
            if (intervalSelection.hasSelection()) {
                intervalSelection.drawIntervalSelection(graphics2D);
                timelineManager.drawIntervalSelectionEffects(
                        graphics2D,
                        intervalSelection.getFromTime(),
                        intervalSelection.getToTime(),
                        intervalSelection.getFromLayer(),
                        intervalSelection.getToLayer()
                );
            }
        }
        if (!timelineManager.isEmpty()) {
            timelineManager.drawTimeLabels(
                graphics2D,
                    zoomManager.getCurrentMinTime(),
                zoomManager.getCurrentMaxTime(),
                timeSelection,
                intervalSelection
            );
        }
        requireRepaint = false;
    }

    protected void repaintImage() {
        Dimension size = getSize();
        oldWidth = size.width;
        oldHeight = size.height;

        Graphics2D graphics = image.createGraphics();
        graphics.setFont(getFont());
        timelineManager.setFontHeight(graphics.getFontMetrics().getHeight());

        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, oldWidth, oldHeight);

        if (!timelineManager.isEmpty()) {
            timelineManager.drawLayers(graphics, TOP_PADDING, oldWidth, oldHeight);
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
    public void selectTime(long time, int layerIndex) {
        timeSelection.selectTime(time, layerIndex);
        intervalSelection.clearSelection();
        repaint();
        timeSelectionChanged();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTimeInterval(long left, long right, int top, int bottom) {
        intervalSelection.setSelection(left, right, top, bottom);
        timeSelection.clearSelection();
        timeSelectionChanged();
        repaint();
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
    public synchronized void addTimeSelectionChangedListener(TimeSelectionListener selectionListener) {
        listeners.add(selectionListener);
    }

    @Override
    public synchronized void removeTimeSelectionChangedListener(TimeSelectionListener selectionListener) {
        listeners.remove(selectionListener);
    }

    protected synchronized void timeSelectionChanged() {
        for (TimeSelectionListener listener : listeners) {
            listener.timeSelectionChanged(this);
        }
    }

    /**
     * {@inheritDoc}
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
