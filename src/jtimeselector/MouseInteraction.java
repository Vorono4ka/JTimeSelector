package jtimeselector;

import jtimeselector.layers.TimelineManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Reacts on the user interaction.
 * Notifies  {@link TimelineManager}, {@link ZoomManager}, {@link RectangleSelectionGuides}
 * and {@link IntervalSelectionManager} about the changes that were caused
 * by the user clicking / dragging / scrolling with the mouse.
 */
public class MouseInteraction extends MouseAdapter {
    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;
    private final JTimeSelector component;
    private final TimeSelectionManager selectionManager;
    private final RectangleSelectionGuides rectangleGuides;
    private final IntervalSelectionManager intervalSelection;

    private int startX;
    private int startY;
    private boolean rectSelectionStarted = false;

    public MouseInteraction(TimelineManager timelineManager, ZoomManager zoomManager,
                            JTimeSelector component, TimeSelectionManager selectionManager,
                            RectangleSelectionGuides rectangleGuides,
                            IntervalSelectionManager intervalSelection) {
        this.timelineManager = timelineManager;
        this.zoomManager = zoomManager;
        this.component = component;
        this.selectionManager = selectionManager;
        this.rectangleGuides = rectangleGuides;
        this.intervalSelection = intervalSelection;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (timelineManager.isEmpty()) {
            return;
        }
        startX = e.getX();
        startY = e.getY();
        if (!e.isControlDown()) {
            rectSelectionStarted = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (timelineManager.isEmpty()) {
            rectSelectionStarted = false;
            rectangleGuides.setVisible(false);
            return;
        }
        if (!e.isControlDown()) {
            rangeSelectionDrag(e);
            return;
        }
        rectSelectionStarted = false;
        rectangleGuides.setVisible(false);

        int dist = e.getX() - startX;
        int abs;
        int sgn;
        if (dist > 0) {
            sgn = 1;
            abs = dist;
        } else {
            sgn = -1;
            abs = -dist;
        }
        if (abs < 10) {
            return;
        }
        long time = timelineManager.getTimeDistance(abs);
        zoomManager.moveVisibleArea(-time * sgn);
        startX = e.getX();
        startY = e.getY();
        component.requireRepaint();
    }

    private void rangeSelectionDrag(MouseEvent e) {
        rectangleGuides.setVisible(true);

        int left, top, right, bottom;

        final int x = e.getX();
        if (startX > x) {
            left = x;
            right = startX;
        } else {
            left = startX;
            right = x;
        }

        final int y = e.getY();
        if (startY > y) {
            top = y;
            bottom = startY;
        } else {
            top = startY;
            bottom = y;
        }

        rectangleGuides.setSelectionRectangle(left, top, right, bottom);
        component.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (timelineManager.isEmpty()) {
            return;
        }

        final int x = e.getX();
        final int y = e.getY();
        if (x > timelineManager.getLegendWidth()) {
            intervalSelection.clearSelection();
            selectionManager.clearSelection();

            long timeForX = timelineManager.getTimeForX(x);
            int layerIndex = timelineManager.getLayerIndex(y);

            if (layerIndex == -1) {
                component.selectTimeInterval(timeForX, timeForX, JTimeSelector.TOP_PADDING, timelineManager.getLayersBottomY() - 1);
            } else {
                component.selectTime(timeForX, layerIndex);
            }

            component.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        final int x = event.getX();
        final int y = event.getY();
        if (!event.isControlDown() && rectSelectionStarted && (startX != x) && (startY != y)) {
            int left, right;
            if (startX <= x) {
                left = startX;
                right = x;
            } else {
                left = x;
                right = startX;
            }

            int top, bottom;
            if (startY <= y) {
                top = startY;
                bottom = y;
            } else {
                top = y;
                bottom = startY;
            }
            final long minTime = zoomManager.getCurrentMinTime();
            final long maxTime = zoomManager.getCurrentMaxTime();

            long timeLeft = Math.max(timelineManager.getTimeForX(left), minTime);
            long timeRight = Math.min(timelineManager.getTimeForX(right), maxTime);

            component.selectTimeInterval(timeLeft, timeRight, top, bottom);
        }
        rectangleGuides.setVisible(false);
        rectSelectionStarted = false;
        component.repaint();
    }
}
