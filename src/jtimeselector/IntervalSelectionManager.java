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

    private double t1;
    private double t2;
    private boolean selection = false;
    private final TimelineManager tm;
    private final ZoomManager z;
    int str1x1, str1x2, str2x1, str2x2;

    public IntervalSelectionManager(TimelineManager tm, ZoomManager z) {
        this.tm = tm;
        this.z = z;
    }

 

    public void clearSelection() {
        if (selection == false) {
            return;
        }
        selection = false;
    }

    public void setSelection(int x1, int x2) {
        setSelection(tm.getTimeForX(x1), tm.getTimeForX(x2));
    }

    public void setSelection(double t1, double t2) {
        selection = true;
        this.t1 = t1;
        this.t2 = t2;
    }

    public double getT1() {
        return t1;
    }

    public double getT2() {
        return t2;
    }

    public boolean isSelection() {
        return selection;
    }

    public void drawIntervalSelection(Graphics2D g) {
        if (!selection) {
            return;
        }
        drawLeft = z.timeValueInCurrentRange(t1);
        drawRight = z.timeValueInCurrentRange(t2);
        if (!drawLeft && !drawRight) {
            return;
        }
        int x1 = tm.getXForTime(t1) + tm.getHeaderWidth();
        int x2 = tm.getXForTime(t2) + tm.getHeaderWidth();
        int textY = tm.getTimeLabelsBaselineY();
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
        String s = tm.getConverter().timeToString(t1);
        int width = fm.stringWidth(s);
        str1x1 = x1 + Layer.PADDING; // |Label  | Label
        str1x2 = str1x1 + width;
        if ((rightDrawn && str1x2 > x2) || str2x2 > tm.getCurrentWidth() - Layer.PADDING) {
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
        String s = tm.getConverter().timeToString(t2);
        int width = fm.stringWidth(s);
        str2x1 = x2 + Layer.PADDING;
        str2x2 = str2x1 + width;
        if (str2x2 > tm.getCurrentWidth() - Layer.PADDING) {
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
        return selection
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
        if (!selection) {
            return true;
        }
        if (tm.isEmpty()) {
            clearSelection();
            return false;
        }
        final double minTime = tm.getMinTime();
        final double maxTime = tm.getMaxTime();
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
