/*
 */
package jtimeselector;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;

/**
 *
 * @author Tomas Prochazka 9.1.2016
 */
public class IntervalSelectionManager {

    private long t1;
    private long t2;
    private boolean hasSelection = false;
    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;
    int str1x1, str1x2, str2x1, str2x2;

    public IntervalSelectionManager(TimelineManager timelineManager, ZoomManager zoomManager) {
        this.timelineManager = timelineManager;
        this.zoomManager = zoomManager;
    }

    public void clearSelection() {
        if (!hasSelection) {
            return;
        }
        hasSelection = false;
    }

    public void setSelection(int x1, int x2) {
        setSelection(timelineManager.getTimeForX(x1), timelineManager.getTimeForX(x2));
    }

    public void setSelection(long t1, long t2) {
        hasSelection = true;
        this.t1 = t1;
        this.t2 = t2;
    }

    public long getT1() {
        return t1;
    }

    public long getT2() {
        return t2;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public void drawIntervalSelection(Graphics2D g) {
        if (!hasSelection) {
            return;
        }
        drawLeft = zoomManager.timeValueInCurrentRange(t1);
        drawRight = zoomManager.timeValueInCurrentRange(t2);
        if (!drawLeft && !drawRight) {
            return;
        }
        int x1 = timelineManager.getXForTime(t1) + timelineManager.getLegendWidth();
        int x2 = timelineManager.getXForTime(t2) + timelineManager.getLegendWidth();
        int textY = timelineManager.getTimeLabelsBaselineY();
        int topY = JTimeSelector.TOP_PADDING;
        g.setColor(TimeSelectionManager.SELECTION_COLOR);
        if (drawLeft) {
            g.drawLine(x1, topY, x1, textY);
            drawLeftLabel(g, textY, x1, x2, drawRight);
        }
        if (drawRight) {
            g.drawLine(x2, topY, x2, textY);
            drawRightLabel(g, textY, x1, x2, drawLeft);
        }
    }
    private boolean drawRight;
    private boolean drawLeft;

    private void drawLeftLabel(Graphics2D g, int textY, int x1, int x2, boolean rightDrawn) {
        FontMetrics fm = g.getFontMetrics();
        String s = timelineManager.getConverter().timeToString(t1);
        int width = fm.stringWidth(s);
        str1x1 = x1 + Layer.PADDING; // |Label  | Label
        str1x2 = str1x1 + width;
        if ((rightDrawn && str1x2 > x2) || str2x2 > timelineManager.getCurrentWidth() - Layer.PADDING) {
            str1x2 = x1 - Layer.PADDING;
            str1x1 = str1x2 - width;
        }
        g.drawString(s, str1x1, textY);
    }

    /**
     * Call after left label is drawn (uses its position)! (providing leftDrawn
     * == true)
     *
     * @param g
     * @param textY
     * @param x1
     * @param x2
     * @param leftDrawn
     */
    private void drawRightLabel(Graphics2D g, int textY, int x1, int x2, boolean leftDrawn) {
        FontMetrics fm = g.getFontMetrics();
        String s = timelineManager.getConverter().timeToString((long)t2);
        int width = fm.stringWidth(s);
        str2x1 = x2 + Layer.PADDING;
        str2x2 = str2x1 + width;
        if (str2x2 > timelineManager.getCurrentWidth() - Layer.PADDING) {
            int altright = x2 - Layer.PADDING;
            int altleft = altright - width;
            if (leftDrawn && altleft > str1x2) {
                str2x1 = altleft; // |Label   Label|     or    Label|   Label|
                str2x2 = altright;
            }
        }
        g.drawString(s, str2x1, textY);
    }


    public boolean labelsCollision(int a, int b) {
        return hasSelection
                && (IntervalCheck.collision(str1x1, str1x2, a, b) && drawLeft
                || IntervalCheck.collision(str2x1, str2x2, a, b) && drawRight);
    }

    /**
     * Ensures that the selected time interval does not lie out of bounds. If it
     * does, the selected time is set to the minimal value.
     *
     * @return false if change occurred
     */
    public boolean checkBounds() {
        if (!hasSelection) {
            return true;
        }
        if (timelineManager.isEmpty()) {
            clearSelection();
            return false;
        }
        final long minTime = timelineManager.getMinTime();
        final long maxTime = timelineManager.getMaxTime();
        boolean change= false;
        if (t1 < minTime) {
            t1 = minTime;
            change = true;
        }
        if (t2 > maxTime) {
            t2 = maxTime;
            change=true;
        }
        return !change;
        
    }

}
