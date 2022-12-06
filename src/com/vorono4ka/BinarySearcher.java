package com.vorono4ka;

import java.util.Arrays;

public class BinarySearcher {
    /**
     * @param array array sorted in the ascending order
     * @param value searching value
     * @return index of last element which is less than or equal to value if element exists, otherwise -1
     * @see #firstGreaterThanOrEqual(long[], long, int, int)
     */
    public static int firstGreaterThanOrEqual(long[] array, long value) {
        return firstGreaterThanOrEqual(array, value, 0, array.length);
    }

    /**
     * Returns the index of the first element in the array, which is greater
     * than or equal to 'value'
     *
     * @param array array sorted in the ascending order
     * @param value searching value
     * @return index of first element that is greater than or equal to value if element exists, otherwise array length
     */
    public static int firstGreaterThanOrEqual(long[] array, long value, int fromIndex, int toIndex) {
        int foundIndex = Arrays.binarySearch(array, value);
        if (foundIndex < 0) {
            int insertionPoint = -(foundIndex + 1);
            if (insertionPoint == array.length) {
                return array.length - 1;
            }

            return insertionPoint;
        } else {
            return getLeftmost(array, value, foundIndex, fromIndex, toIndex);
        }
    }

    /**
     * @param array array sorted in the ascending order
     * @param value searching value
     * @return index of last element which is less than or equal to value if element exists, otherwise -1
     * @see #lastLessThanOrEqual(long[], long, int, int)
     */
    public static int lastLessThanOrEqual(long[] array, long value) {
        return lastLessThanOrEqual(array, value, 0, array.length);
    }

    /**
     * Gets the index of the last element in the array which is less than or
     * equal to the given number.
     *
     * @param array array sorted in the ascending order
     * @param value searching value
     * @return index of last element which is less than or equal to value if element exists, otherwise -1
     */
    public static int lastLessThanOrEqual(long[] array, long value, int fromIndex, int toIndex) {
        int foundIndex = Arrays.binarySearch(array, fromIndex, toIndex, value);
        if (foundIndex < 0) {
            return -foundIndex - 2;
        } else {
            return getRightmost(array, value, foundIndex, fromIndex, toIndex);
        }
    }

    public static int indexOfClosest(long[] array, long value) {
        return indexOfClosest(array, value, 0, array.length);
    }

    /**
     * Gets the index of such element in the array, that distance of argument
     * 'value' from this element is smallest. If the closest value is not
     * determined unambiguously, index of the leftmost them is returned.
     *
     * @param array array sorted in the ascending order
     * @param value searching value
     * @return index of the closest element if array isn't empty, otherwise -1
     */
    public static int indexOfClosest(long[] array, long value, int fromIndex, int toIndex) {
        if (array.length == 0) return -1;
        int foundIndex = Arrays.binarySearch(array, value);
        if (foundIndex >= 0) { // array contains the given value, find the leftmost occurrence
            return getLeftmost(array, value, foundIndex, fromIndex, toIndex);
        }

        int insertionPoint = -(foundIndex + 1);
        if (insertionPoint <= 0) {
            return insertionPoint;
        }

        if (insertionPoint == array.length) {
            return insertionPoint - 1;
        }

        double d1 = Math.abs(array[insertionPoint - 1] - value);
        double d2 = Math.abs(array[insertionPoint] - value);

        if (d1 <= d2) {
            foundIndex = insertionPoint - 1;
        } else {
            foundIndex = insertionPoint;
        }

        return getLeftmost(array, array[foundIndex], foundIndex, fromIndex, toIndex);
    }

    /**
     * @param array array sorted in the ascending order
     * @param value searching value
     * @param index index found with {@link Arrays#binarySearch(long[], long)}
     * @return leftmost index of value
     */
    public static int getLeftmost(long[] array, long value, int index, int fromIndex, int toIndex) {
        while (index - 1 >= 0) {
            if (index < fromIndex || index > toIndex) {
                break;
            }

            if (array[index - 1] == value) {
                index--;
            } else {
                break;
            }
        }
        return index;
    }

    /**
     * @param array array sorted in the ascending order
     * @param value searching value
     * @param index index found with {@link Arrays#binarySearch(long[], long)}
     * @return rightmost index of value
     */
    public static int getRightmost(long[] array, long value, int index, int fromIndex, int toIndex) {
        while (index + 1 < array.length) {
            if (index < fromIndex || index > toIndex) {
                break;
            }

            if (array[index + 1] == value) {
                index++;
            } else {
                break;
            }
        }
        return index;
    }
}
