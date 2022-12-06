package jtimeselector;

public class IntervalCheck {
    public static boolean collision(int left1, int right1, int left2, int right2) {
        return pointIn(left1, right1, left2) || pointIn(left1, right1, right2) || pointIn(left2, right2, left1) || pointIn(left2, right2, right1);
    }

    /**
     * @param left min value
     * @param right max value
     * @param point point
     * @return true if point lies in unbounded interval (left, right)
     */
    public static boolean pointIn(int left, int right, int point) {
        return point >= left && point <= right;
    }
}
