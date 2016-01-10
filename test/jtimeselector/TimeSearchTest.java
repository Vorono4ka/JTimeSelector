/*
 */
package jtimeselector;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Proch
 */
public class TimeSearchTest {

    public TimeSearchTest() {
    }

    /**
     * Test of firstGreaterThanOrEqual method, of class TimeSearch.
     */
    @Test
    public void testFirstGreaterThanOrEqual() {
        System.out.println("firstGreaterThanOrEqual");
        double[] array = {2, 3, 4, 5, 6};
        double value = 0.0;
        int expResult = 0;
        int result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 6.0;
        expResult = 4;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 7.0;
        expResult = array.length;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = 2;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.5;
        expResult = 3;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        array = new double[0];
        value = 4.5;
        expResult = 0;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = -4.5;
        expResult = 0;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        array = new double[]{2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4};
        value = 2;
        expResult = 0;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 3.0;
        expResult = 5;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = 11;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 10000;
        expResult = array.length;
        result = TimeSearch.firstGreaterThanOrEqual(array, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of lastLessThanOrEqual method, of class TimeSearch.
     */
    @Test
    public void testLastLessThanOrEqual() {
        System.out.println("lastLessThanOrEqual");
        double[] array = {2, 3, 4, 5, 6};
        double value = 0.0;
        int expResult = -1;
        int result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 6.0;
        expResult = 4;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 7.0;
        expResult = 4;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = 2;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.5;
        expResult = 2;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        array = new double[0];
        value = 4.5;
        expResult = -1;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = -4.5;
        expResult = -1;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        array = new double[]{2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4};
        value = 2;
        expResult = 4;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 3.0;
        expResult = 10;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = array.length - 1;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);

        value = 10000;
        expResult = array.length - 1;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);
        value = -5;
        expResult = -1;
        result = TimeSearch.lastLessThanOrEqual(array, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of indexOfClosest method, of class TimeSearch.
     */
    @Test
    public void testIndexOfClosest() {
        System.out.println("indexOfClosest");
        double[] array = {2, 3, 4, 5, 6};
        double value = 0.0;
        int expResult = 0;
        int result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 6.0;
        expResult = 4;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 7.0;
        expResult = array.length - 1;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = 2;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 4.5;
        expResult = 2;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        array = new double[0];
        value = 4.5;
        expResult = -1;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = -4.5;
        expResult = -1;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        array = new double[]{2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 4, 4};
        value = 2.5;
        expResult = 0;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 3.3;
        expResult = 5;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 4.0;
        expResult = 11;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = 10000;
        expResult = array.length - 1;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);

        value = -10000;
        expResult = 0;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);
        value = 2;
        expResult = 0;
        result = TimeSearch.indexOfClosest(array, value);
        assertEquals(expResult, result);
    }

}
