/*
 */

package jtimeselector;

/**
 *
 * @author Tomas Prochazka
 * 9.1.2016
 */
public class IntervalCheck {
    public static boolean collision(int a,int b,int x,int y) {
        return x<b && y>a;
    }
    /**
     * Returns true if point lies in unbounded interval (a,b)
     * @param a
     * @param b
     * @param point
     * @return 
     */
    public static boolean pointIn(int a, int b, int point) {
        return point>a && point<b;
    }
}
