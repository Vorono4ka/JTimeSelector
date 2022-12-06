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
 * It is possible to set how the time should be displayed to the user, an instance of {@link TimeToStringConverter} can be set either by constructor.
 */
public class JTimeSelector extends JPanel implements TimeSelector {
    public static final Color BACKGROUND_COLOR = Color.white;

    private final TimelineManager timelineManager;
    private final VisibleAreaManager visibleAreaManager;
    private final List<TimeSelectionListener> listeners = new ArrayList<>();
    private final RectangleSelectionGuides rectangleGuides = new RectangleSelectionGuides();

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
        visibleAreaManager = new VisibleAreaManager();
        timelineManager = new TimelineManager(visibleAreaManager, converter);
        setFont(getFont().deriveFont(15f));
        addMouseWheelListener(this::mouseWheelMoved);

        final MouseInteraction mouseInteraction = new MouseInteraction(
                timelineManager,
                visibleAreaManager,
                this,
                rectangleGuides
        );

        addMouseListener(mouseInteraction);
        addMouseMotionListener(mouseInteraction);
    }

    @Override
    protected void paintComponent(Graphics g) {  // TODO: draw major and minor ticks
        Dimension size = this.getSize();
        if (size.width != oldWidth || size.height != oldHeight) {
            image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            requireRepaint = true;
        }

        if (requireRepaint) {
            repaintImage();
            requireRepaint = false;
        }

        Graphics2D graphics = (Graphics2D) g;
        graphics.drawImage(image, null, 0, 0);
        graphics.setFont(getFont());

        if (rectangleGuides.isVisible()) {
            rectangleGuides.draw(graphics);
        } else if (timelineManager.hasSelection()) {
            timelineManager.drawSelectionEffects(graphics);
        }

        if (!timelineManager.isEmpty()) {
            timelineManager.drawTimeLabels(
                graphics,
                visibleAreaManager.getCurrentMinTime(),
                visibleAreaManager.getCurrentMaxTime()
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
            timelineManager.drawLayers(graphics, oldWidth, oldHeight);
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
        timelineManager.addLayer(new TimeEntryLayer(timelineManager, visibleAreaManager, name, timeValues));
        visibleAreaManager.updateMinAndMaxTime(timelineManager);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeGraphLayer(String name) {
        timelineManager.removeLayer(name);
        visibleAreaManager.updateMinAndMaxTime(timelineManager);
        timeSelectionChanged();
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeAllGraphLayers() {
        timelineManager.removeAllLayers();
        timelineManager.clearSelection();
        timeSelectionChanged();
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTime(long time, int layerIndex) {
        timelineManager.setSelection(time, layerIndex);
        timeSelectionChanged();
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void selectTimeInterval(long left, long right, int top, int bottom) {
        timelineManager.setSelection(left, right, top, bottom);
        timeSelectionChanged();
        repaint();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TimeSelectionType getTimeSelectionType() {
        return timelineManager.getSelectionType();
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

    private void mouseWheelMoved(MouseWheelEvent event) {
        int modifiersEx = event.getModifiersEx();

        final double preciseWheelRotation = event.getPreciseWheelRotation();
        // Limit the rotation to compensate the different behaviour of mouse and trackpad
        final int rotation = MathHelper.clamp((int) Math.round(preciseWheelRotation * 4), -3, 3);

        int ctrlDownMask = InputEvent.CTRL_DOWN_MASK;
        int shiftDownMask = InputEvent.SHIFT_DOWN_MASK;
        if ((modifiersEx & ctrlDownMask) == ctrlDownMask) {
            long time = timelineManager.getTimeForX(event.getX());
            final long currentMinTime = visibleAreaManager.getCurrentMinTime();
            final long currentMaxTime = visibleAreaManager.getCurrentMaxTime();
            if (time < currentMinTime || time > currentMaxTime) {
                return;
            }

            if (rotation < 0) {
                visibleAreaManager.zoomIn(-rotation, time);
            } else {
                visibleAreaManager.zoomOut(rotation, time);
            }
        } else if ((modifiersEx & shiftDownMask) == shiftDownMask) {
            visibleAreaManager.moveVisibleArea(rotation);
        } else {
            return;
        }

        requireRepaint = true;
        repaint();
    }

    public void setCursorPosition(long time) {
        this.timelineManager.setCursorPosition(time);
    }
}
