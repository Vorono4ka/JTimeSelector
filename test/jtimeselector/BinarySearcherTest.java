package jtimeselector;

import com.vorono4ka.BinarySearcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinarySearcherTest {
    public static final long[] SAME_VALUES_ARRAY = {2L, 2L, 2L, 2L, 2L, 3L, 3L, 3L, 3L, 3L, 3L, 4L, 4L, 6L};
    public static final long[] EMPTY_ARRAY = new long[0];
    public static final long[] DIFFERENT_VALUES_ARRAY = {2L, 3L, 4L, 5L, 6L};

    /**
     * Test of firstGreaterThanOrEqual method, of class TimeSearch.
     */
    @Test
    public void testFirstGreaterThanOrEqual() {
        assertEquals(0, BinarySearcher.firstGreaterThanOrEqual(DIFFERENT_VALUES_ARRAY, 0L));
        assertEquals(4, BinarySearcher.firstGreaterThanOrEqual(DIFFERENT_VALUES_ARRAY, 6L));
        assertEquals(DIFFERENT_VALUES_ARRAY.length - 1, BinarySearcher.firstGreaterThanOrEqual(DIFFERENT_VALUES_ARRAY, 7L));
        assertEquals(2, BinarySearcher.firstGreaterThanOrEqual(DIFFERENT_VALUES_ARRAY, 4L));
        assertEquals(0, BinarySearcher.firstGreaterThanOrEqual(DIFFERENT_VALUES_ARRAY, -4L));

        assertEquals(-1, BinarySearcher.firstGreaterThanOrEqual(EMPTY_ARRAY, 5L));
        assertEquals(-1, BinarySearcher.firstGreaterThanOrEqual(EMPTY_ARRAY, -4L));

        assertEquals(0, BinarySearcher.firstGreaterThanOrEqual(SAME_VALUES_ARRAY, 2L));
        assertEquals(5, BinarySearcher.firstGreaterThanOrEqual(SAME_VALUES_ARRAY, 3L));
        assertEquals(11, BinarySearcher.firstGreaterThanOrEqual(SAME_VALUES_ARRAY, 4L));
        assertEquals(SAME_VALUES_ARRAY.length - 1, BinarySearcher.firstGreaterThanOrEqual(SAME_VALUES_ARRAY, 10000L));
    }

    /**
     * Test of lastLessThanOrEqual method, of class TimeSearch.
     */
    @Test
    public void testLastLessThanOrEqual() {
        assertEquals(-1, BinarySearcher.lastLessThanOrEqual(DIFFERENT_VALUES_ARRAY, 0L));
        assertEquals(4, BinarySearcher.lastLessThanOrEqual(DIFFERENT_VALUES_ARRAY, 6L));
        assertEquals(4, BinarySearcher.lastLessThanOrEqual(DIFFERENT_VALUES_ARRAY, 7L));
        assertEquals(2, BinarySearcher.lastLessThanOrEqual(DIFFERENT_VALUES_ARRAY, 4L));

        assertEquals(-1, BinarySearcher.lastLessThanOrEqual(EMPTY_ARRAY, 4L));
        assertEquals(-1, BinarySearcher.lastLessThanOrEqual(EMPTY_ARRAY, -4L));

        assertEquals(4, BinarySearcher.lastLessThanOrEqual(SAME_VALUES_ARRAY, 2L));
        assertEquals(10, BinarySearcher.lastLessThanOrEqual(SAME_VALUES_ARRAY, 3L));
        assertEquals(12, BinarySearcher.lastLessThanOrEqual(SAME_VALUES_ARRAY, 4L));
        assertEquals(SAME_VALUES_ARRAY.length - 1, BinarySearcher.lastLessThanOrEqual(SAME_VALUES_ARRAY, 10000L));
        assertEquals(-1, BinarySearcher.lastLessThanOrEqual(SAME_VALUES_ARRAY, -5L));
    }

    /**
     * Test of indexOfClosest method, of class TimeSearch.
     */
    @Test
    public void testIndexOfClosest() {
        assertEquals(0, BinarySearcher.indexOfClosest(DIFFERENT_VALUES_ARRAY, 0L));
        assertEquals(4, BinarySearcher.indexOfClosest(DIFFERENT_VALUES_ARRAY, 6L));
        assertEquals(DIFFERENT_VALUES_ARRAY.length - 1, BinarySearcher.indexOfClosest(DIFFERENT_VALUES_ARRAY, 7L));
        assertEquals(2, BinarySearcher.indexOfClosest(DIFFERENT_VALUES_ARRAY, 4L));

        assertEquals(-1, BinarySearcher.indexOfClosest(EMPTY_ARRAY, 4L));
        assertEquals(-1, BinarySearcher.indexOfClosest(EMPTY_ARRAY, -4L));

        assertEquals(0, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, -3L));
        assertEquals(11, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, 4L));
        assertEquals(13, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, 6L));
        assertEquals(SAME_VALUES_ARRAY.length - 1, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, 10000L));
        assertEquals(0, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, -10000L));
        assertEquals(0, BinarySearcher.indexOfClosest(SAME_VALUES_ARRAY, 2L));
    }

}
