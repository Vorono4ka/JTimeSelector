package jtimeselector;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

import com.vorono4ka.MathHelper;
import jtimeselector.layers.Layer;
import jtimeselector.layers.TimelineManager;

public class IntervalSelectionManager {
    private final TimelineManager timelineManager;
    private final ZoomManager zoomManager;

    private boolean hasSelection = false;
    private long fromTime;
    private long toTime;
    private int fromLayer;
    private int toLayer;
    private int leftLabelXLeft, leftLabelXRight, rightLabelXLeft, rightLabelXRight;
    private boolean drawRight;
    private boolean drawLeft;

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

    public void setSelection(long left, long right, int top, int bottom) {
        top = MathHelper.clamp(top, 0, this.timelineManager.getLayersBottomY() - 1);
        bottom = MathHelper.clamp(bottom, top, this.timelineManager.getLayersBottomY() - 1);

        int fromLayer = Math.max(this.timelineManager.getLayerIndex(top), 0);
        int toLayer = this.timelineManager.getLayerIndex(bottom);

        long closestFromTime = this.timelineManager.getClosestTime(left, fromLayer, toLayer);
        long closestToTime = this.timelineManager.getClosestTime(right, fromLayer, toLayer);

        if (Math.abs(closestFromTime - left) < TimeSelectionManager.SELECTION_THRESHOLD) {
            left = closestFromTime;
        }

        if (Math.abs(closestToTime - right) < TimeSelectionManager.SELECTION_THRESHOLD) {
            right = closestToTime;
        }

        this.hasSelection = true;
        this.fromTime = left;
        this.toTime = right;
        this.fromLayer = fromLayer;
        this.toLayer = toLayer;
    }

    public boolean hasSelection() {
        return hasSelection;
    }

    public long getFromTime() {
        return fromTime;
    }

    public long getToTime() {
        return toTime;
    }

    public int getFromLayer() {
        return fromLayer;
    }

    public int getToLayer() {
        return toLayer;
    }

    public void drawIntervalSelection(Graphics2D graphics) {
        if (!hasSelection) {
            return;
        }

        drawLeft = zoomManager.timeValueInCurrentRange(fromTime);
        drawRight = zoomManager.timeValueInCurrentRange(toTime);
        if (!drawLeft && !drawRight) {
            return;
        }

        int x1 = timelineManager.getXForTime(fromTime) + timelineManager.getLegendWidth();
        int x2 = timelineManager.getXForTime(toTime) + timelineManager.getLegendWidth();
        int textY = timelineManager.getTimeLabelsBaselineY();
        int topY = JTimeSelector.TOP_PADDING;
        graphics.setColor(TimeSelectionManager.SELECTION_COLOR);
        if (drawLeft) {
            graphics.drawLine(x1, topY, x1, textY);
            drawLeftLabel(graphics, textY, x1, x2, drawRight);
        }
        if (drawRight) {
            graphics.drawLine(x2, topY, x2, textY);
            drawRightLabel(graphics, textY, x2, drawLeft);
        }
    }

    private void drawLeftLabel(Graphics2D g, int textY, int x1, int x2, boolean rightDrawn) {
        FontMetrics fm = g.getFontMetrics();
        String s = timelineManager.getConverter().timeToString(fromTime);
        int width = fm.stringWidth(s);
        leftLabelXLeft = x1 + Layer.PADDING; // |Label  | Label
        leftLabelXRight = leftLabelXLeft + width;
        if ((rightDrawn && leftLabelXRight > x2) || rightLabelXRight > timelineManager.getCurrentWidth() - Layer.PADDING) {
            leftLabelXRight = x1 - Layer.PADDING;
            leftLabelXLeft = leftLabelXRight - width;
        }
        g.drawString(s, leftLabelXLeft, textY);
    }

    /**
     * Call after left label is drawn (uses its position)! (providing leftDrawn
     * == true)
     *
     * @param g
     * @param textY
     * @param x2
     * @param leftDrawn
     */
    private void drawRightLabel(Graphics2D g, int textY, int x2, boolean leftDrawn) {
        FontMetrics fontMetrics = g.getFontMetrics();
        String string = timelineManager.getConverter().timeToString(toTime);
        int width = fontMetrics.stringWidth(string);

        rightLabelXLeft = x2 + Layer.PADDING;
        rightLabelXRight = rightLabelXLeft + width;
        if (rightLabelXRight > timelineManager.getCurrentWidth() - Layer.PADDING) {
            int altRight = x2 - Layer.PADDING;
            int altLeft = altRight - width;
            if (leftDrawn && altLeft > leftLabelXRight) {
                rightLabelXLeft = altLeft; // |Label   Label|     or    Label|   Label|
                rightLabelXRight = altRight;
            }
        }
        g.drawString(string, rightLabelXLeft, textY);
    }


    public boolean labelsCollision(int a, int b) {
        return hasSelection && (
            IntervalCheck.collision(leftLabelXLeft, leftLabelXRight, a, b) && drawLeft ||
            IntervalCheck.collision(rightLabelXLeft, rightLabelXRight, a, b) && drawRight
        );
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
        boolean change = false;
        if (fromTime < minTime) {
            fromTime = minTime;
            change = true;
        }
        if (toTime > maxTime) {
            toTime = maxTime;
            change = true;
        }
        return !change;
    }
}
