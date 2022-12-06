package jtimeselector;

public class IntervalCheck {
    public static boolean collision(int a,int b,int x,int y) {
        return x < b && y > a;
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
