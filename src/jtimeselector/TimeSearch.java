/*
 */
package jtimeselector;

import java.util.Arrays;

/**
 *Contains static helper methods for quick finding of values in arrays.
 * @author Tomas Prochazka 6.12.2015
 */
public class TimeSearch {

    /**
     * Returns the index of the first element in the array, which is greater
     * than or equal to 'value'
     *
     * @param array array of long ints sorted in the ascending order
     * @param value
     * @return index of first element that is greater than or equal to the given
     * value or array length if no such element exists
     */
    public static int firstGreaterThanOrEqual(long[] array, long value) {
        int res = Arrays.binarySearch(array, value);
        if (res >= 0) {  //array contains the given value
            while (res - 1 >= 0) {
                if (array[res - 1] == value) {
                    res--;
                } else {
                    break;
                }
            }
            return res;
        }
        int insertionPoint = -(res + 1);  //insertion point according to the binarySearch docs
        return insertionPoint;
    }

    /**
     * Gets the index of the last element in the array which is less than or
     * equal to the given number.
     *
     * @param array array of long ints sorted in the ascending order
     * @param value
     * @return index of last element which is less than or equal to value or -1
     * if no such element exist
     */
    public static int lastLessThanOrEqual( long[] array, long value) {
        int res = Arrays.binarySearch(array, value);
        if (res > 0) {      //array contains the given value
            while (res + 1 < array.length) {
                if (array[res + 1] == value) {
                    res++;
                } else {
                    break;
                }
            }
            return res;
        }
        int insertionPoint = -(res + 1);  //insertion point according to the binarySearch docs
        return insertionPoint - 1;
    }

    /**
     * Gets the index of such element in the array, that distance of argument
     * 'value' from this element is smallest. If the closest value is not
     * determined unambiguously, index of the leftmost of them is returned.
     *
     * @param array array of longs sorted in the ascending order
     * @param value
     * @return index of the closest element or -1 in the given array was empty
     */
    public static int indexOfClosest(long[] array, long value) {
        if (array.length==0) return -1;
        int res = Arrays.binarySearch(array, value);
        if (res >= 0) { // array contains the given value, find the leftmost occurrence
            while (res - 1 >= 0) {
                if (array[res - 1] == value) {
                    res--;
                } else {
                    break;
                }
            }
             return res;
        }
        int insertionPoint = -(res + 1); // insertion point according to the binarySearch docs
        if (insertionPoint - 1 < 0) {
            return insertionPoint;
        }
        if (insertionPoint == array.length) {
            return insertionPoint - 1;
        }
        double d1 = Math.abs(array[insertionPoint - 1] - value);
        double d2 = Math.abs(array[insertionPoint] - value);

        if (d1 <= d2) {
            res= insertionPoint - 1;
        } else {
            res=  insertionPoint;
        }
        while (res-1>=0) { //get the leftmost occurrence
            if (array[res-1]==array[res]) {
                res--;
            } else {
                break;
            }
        }
        return res;
    }
}
