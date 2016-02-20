/*
 */

package jtimeselector.layers;

import java.awt.Graphics2D;
import jtimeselector.ZoomManager;

/**
 *
 * @author Tomas Prochazka
 * 6.12.2015
 */


public class GraphLayer extends Layer {
    private final long[][] valuesTable;

    public GraphLayer(String name, long[][]valuesTable) {
        super(name);
        this.valuesTable= valuesTable;
    }
    
    public final int HEIGHT=60;
    @Override
    LayerType getLayerType() {
        return LayerType.Graph;
    }

    @Override
    int getHeight() {
        return 60;
    }

    @Override
    void draw(Graphics2D g, long timeFrom, long timeTo, int headerSize, int graphicsWidth, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    long getMaxTimeValue() {
        long[] timeValues=valuesTable[0];
        return timeValues[timeValues.length-1] ;
    }

    @Override
    long getMinTimeValue() {
        return valuesTable[0][0];
    }


    @Override
    void drawTimeSelectionEffect(Graphics2D g, long time, TimelineManager t, ZoomManager z, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D g, long from, long to, TimelineManager t, ZoomManager z, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
