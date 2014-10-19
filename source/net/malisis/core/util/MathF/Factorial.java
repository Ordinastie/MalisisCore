/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.util.MathF;

public class Factorial {
    public static int calculate ( int n )
    {
        if (n==0) n = 1;
        int t = 1;
        for (int i = 1; i < n + 1; i++)
            t = t * i;
        return t;
    }

    public float calculate(float n)
    {
        if (n==0) n = 1;
        int t = 1;
        for (int i = 1; i < n; i++)
            t = t * i;
        return t;
    }

    public double calculate(double n)
    {
        if (n==0) n = 1;
        int t = 1;
        for (int i = 1; i < n + 1; i++)
            t = t * i;
        return t;
    }
}
