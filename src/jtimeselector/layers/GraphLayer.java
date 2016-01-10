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
    private final double[][] valuesTable;

    public GraphLayer(String name, double[][]valuesTable) {
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
    void draw(Graphics2D g, double timeFrom, double timeTo, int headerSize, int graphicsWidth, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    double getMaxTimeValue() {
        double[] timeValues=valuesTable[0];
        return timeValues[timeValues.length-1] ;
    }

    @Override
    double getMinTimeValue() {
        return valuesTable[0][0];
    }


    @Override
    void drawTimeSelectionEffect(Graphics2D g, double time, TimelineManager t, ZoomManager z, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    void drawIntervalSelectionEffect(Graphics2D g, double from, double to, TimelineManager t, ZoomManager z, int y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   
    
}
