/*
 */
package jtimeselector;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;

/**
 * Reacts on the user interaction.
 * Notifies  {@link TimelineManager}, {@link ZoomManager}, {@link RectangleSelectionGuides}
 * and {@link IntervalSelectionManager} about the changes that were caused
 * by the user clicking / dragging / scrolling with the mouse.
 * @author Tomas Prochazka 8.1.2016
 */
public class MouseInteraction extends MouseAdapter {

    private int startX;
    private boolean rectSelStarted = false;
    TimelineManager layerManager;
    ZoomManager zoomManager;
    JTimeSelector component;
    TimeSelectionManager selectionManager;
    RectangleSelectionGuides rectangleGuides;
    private final IntervalSelectionManager intervalSelection;

    public MouseInteraction(TimelineManager layerManager, ZoomManager zoomManager,
            JTimeSelector component, TimeSelectionManager selectionManager,
            RectangleSelectionGuides rectangleGuides,
            IntervalSelectionManager intervalSelection) {
        this.layerManager = layerManager;
        this.zoomManager = zoomManager;
        this.component = component;
        this.selectionManager = selectionManager;
        this.rectangleGuides = rectangleGuides;
        this.intervalSelection = intervalSelection;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (layerManager.isEmpty()) {
            return;
        }
        startX = e.getX();
        if (!e.isControlDown()) {
            rectSelStarted = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (layerManager.isEmpty()) {
            rectSelStarted = false;
            rectangleGuides.setVisible(false);
            return;
        }
        if (!e.isControlDown()) {
            rangeSelectionDrag(e);
            return;
        }
        rectSelStarted = false;
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
        long time = layerManager.getTimeDistance(abs);
        zoomManager.moveVisibleArea(-time * sgn);
        startX = e.getX();
        component.requireRepaint();
    }

    private void rangeSelectionDrag(MouseEvent e) {
        rectangleGuides.setVisible(true);
        final int x = e.getX();
        if (startX > x) {
            rectangleGuides.setRectSelX1(x);
            rectangleGuides.setRectSelX2(startX);
        } else {
            rectangleGuides.setRectSelX1(startX);
            rectangleGuides.setRectSelX2(x);
        }
        component.repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (layerManager.isEmpty()) {
            return;
        }
        int x = e.getX();
        if (x > layerManager.getHeaderWidth() || x < Layer.PADDING) {
            selectionManager.selectTime(x);
            intervalSelection.clearSelection();
            component.repaint();
            component.timeSelectionChanged();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        final int x = e.getX();
        if (!e.isControlDown() && rectSelStarted && (startX != x)) {
            int a, b;
            if (startX <= x) {
                a = startX;
                b = x;
            } else {
                a = x;
                b = startX;
            }
            long t1 = (long) layerManager.getTimeForX(a);
            long t2 = (long) layerManager.getTimeForX(b);
            final long minTime = zoomManager.getCurrentMinTime();
            if (t1 < minTime) {
                t1 = minTime;
            }
            final long maxTime = zoomManager.getCurrentMaxTime();
            if (t2 > maxTime) {
                t2 = maxTime;
            }
            intervalSelection.setSelection(t1, t2);
            selectionManager.clearSelection();
            component.timeSelectionChanged();
        }
        rectangleGuides.setVisible(false);
        rectSelStarted = false;
        component.repaint();

    }

}
