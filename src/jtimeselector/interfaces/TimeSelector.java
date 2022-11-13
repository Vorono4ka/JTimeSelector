/*
 */

package jtimeselector.interfaces;

import jtimeselector.LongRange;
import jtimeselector.TimeSelectionType;

/**
 *
 * @author Tomas Prochazka
 * 5.12.2015
 */
public interface TimeSelector {
    /**
     * Adds a layer with a small circle for each time value in the list.
     * @param name name of the layer
     * @param timeValues time instants for which a small circle will be drawn on the timeline
     */
    void addTimeValuesLayer(String name, long[] timeValues);
    /**
     * Removes the layer with the given name from the list of displayed layers.
     * Does not automatically refresh/redraw the panel!
     * @param name 
     */
    void removeGraphLayer(String name);
    /**
     * Clears the list of displayed layers.
     * Does not automatically redraw the component.
     */
    void removeAllGraphLayers();
    
    /**
     *  Requires repainting of the component.
     * (Use this if the list of layers has been changed.)
     */
    void requireRepaint();
    /**
     * Gets currently selected time.
     * @return Null if no time is selected.
     */
    Long getSelectedTime();
    /**
     * Using this method the selected time can be set from outside.
     * @param d 
     */
    void selectTime(long d);

    /**
     * Using this method the selected time interval can be set from outside. 
     * @param from
     * @param to 
     */
    void selectTimeInterval(long from, long to);
    /**
     * Gets currently selected time interval.
     * @return Null if no time interval is selected (e.g. TimeSelectionType.SingleValue is selected)
     */
    LongRange getSelectedTimeInterval();
    /**
     * Sets the function which is used to convert double time values
     * to strings that are displayed on the panel.
     * @param converter new converter function
     */
    void setTimeToStringConverter(TimeToStringConverter converter);
    TimeSelectionType getTimeSelectionType();
    /**
     * Registers the given object as a new listener.  The listener will be notified of any change in the selected time.
     * @param l listener
     */
    void addTimeSelectionChangedListener(TimeSelectionListener l);
    /**
     * Removes the given object from the list of listeners.
     * If the object hasn't been registered, nothing happens.
     * @param l listener
     */
    void removeTimeSelectionChangedListener(TimeSelectionListener l);
}
