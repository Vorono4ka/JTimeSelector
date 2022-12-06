package jtimeselector;

public class IntervalCheck {
    public static boolean collision(int left1, int right1, int left2, int right2) {
        return left2 < right1 && right2 > left1;
    }

    /**
     * @param left
     * @param right
     * @param point
     * @return true if point lies in unbounded interval (left, right)
     */
    public static boolean pointIn(int left, int right, int point) {
        return point > left && point < right;
    }
}
