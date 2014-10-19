/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.util.MathF;

public class BinominalCoefficient {
    public static double calculate ( int n, int i )
    {
        return ( Factorial.calculate( n ) / ( Factorial.calculate( i ) * Factorial.calculate( n - i )));
    }
}
