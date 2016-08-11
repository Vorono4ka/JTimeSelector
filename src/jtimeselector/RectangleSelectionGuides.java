/*
 */
package jtimeselector;

import java.awt.Graphics2D;
import static jtimeselector.JTimeSelector.RECT_COLOR_TRANSP;
import static jtimeselector.JTimeSelector.TOP_PADDING;
import jtimeselector.layers.Layer;

/**
 * A class responsible for drawing the two straight lines that mark
 * the selected interval.
 * @author Tomas Prochazka 9.1.2016
 */
public class RectangleSelectionGuides {

    private boolean rectSel = false;
    private int rectSelX1;
    private int rectSelX2;

    public boolean visible() {
        return rectSel;
    }

    public void setVisible(boolean rectSel) {
        this.rectSel = rectSel;
    }

    public int getRectSelX1() {
        return rectSelX1;
    }

    public void setRectSelX1(int rectSelX1) {
        this.rectSelX1 = rectSelX1;
    }

    public int getRectSelX2() {
        return rectSelX2;
    }

    public void setRectSelX2(int rectSelX2) {
        this.rectSelX2 = rectSelX2;
    }

    public void drawRectangleSelectionGuides(Graphics2D gr, int layersBottomY, int timelineStartX, int width) {
        //Rectangle selection:
        gr.setColor(RECT_COLOR_TRANSP);
        final int rectHeight = layersBottomY - TOP_PADDING;
        final int lineBottomY = layersBottomY + gr.getFontMetrics().getHeight();
        final int maxX = width-Layer.PADDING;
        rectSelX1 = clamp(rectSelX1,timelineStartX, maxX);
        rectSelX2 = clamp(rectSelX2,timelineStartX,maxX);
        
        gr.fillRect(rectSelX1, TOP_PADDING, rectSelX2 - rectSelX1, rectHeight);
        gr.setColor(TimeSelectionManager.SELECTION_COLOR);
        gr.drawLine(rectSelX1, TOP_PADDING, rectSelX1, lineBottomY);
        gr.drawLine(rectSelX2, TOP_PADDING, rectSelX2, lineBottomY);
    }
    public int clamp(int x, int from, int to) {
        if (x<=from) {
            return from;
        }
        if (x>=to) {
            return to;
        }
        return x;
    }

}
