package jtimeselector.interfaces;

import jtimeselector.TimeSelectionType;

public interface TimeSelector {
    /**
     * Adds a layer with a small circle for each time value in the list.
     * @param name name of the layer
     * @param timeValues time instants for which a small circle will be drawn on the timeline
     */
    void addTimeValuesLayer(String name, long[] timeValues);

    /**
     * Removes the layer with the given name from the list of displayed layers. Does not automatically refresh/redraw the panel!
     * @param name 
     */
    void removeGraphLayer(String name);

    /**
     * Clears the list of displayed layers. Does not automatically redraw the component.
     */
    void removeAllGraphLayers();
    
    /**
     *  Requires repainting of the component. (Use this if the list of layers has been changed.)
     */
    void requireRepaint();

    /**
     * Using this method the selected time can be set from outside.
     *
     * @param time selected time point in the timeline
     * @param layerIndex selected layer index
     */
    void selectTime(long time, int layerIndex);

    /**
     * Using this method the selected time interval can be set from outside.
     *
     * @param from time from selected
     * @param to time to selected
     * @param top y coordinate of the selection top
     * @param bottom y coordinate of the selection bottom
     */
    void selectTimeInterval(long from, long to, int top, int bottom);

    TimeSelectionType getTimeSelectionType();

    /**
     * Registers the given object as a new listener.  The listener will be notified of any change in the selected time.
     * @param selectionListener listener
     */
    void addTimeSelectionChangedListener(TimeSelectionListener selectionListener);

    /**
     * Removes the given object from the list of listeners.
     * If the object hasn't been registered, nothing happens.
     * @param selectionListener listener
     */
    void removeTimeSelectionChangedListener(TimeSelectionListener selectionListener);
}
