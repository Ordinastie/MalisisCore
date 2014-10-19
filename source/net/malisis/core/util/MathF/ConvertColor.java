/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.util.MathF;

import org.lwjgl.util.vector.Vector4f;

public class ConvertColor {
    public static Vector4f calculate(Vector4f color)
    {
        if (color.x > 1)
            color.x = color.x / 256;
        if (color.y > 1)
            color.y = color.y / 256;
        if (color.z > 1)
            color.z = color.z / 256;
        if (color.w > 1)
            color.w = color.w / 256;
        return color;
    }

    public static Vector4f calculate (float red, float green, float blue, float alpha)
    {
        Vector4f temp = calculate(new Vector4f(red,green,blue,alpha));
        return temp;
    }
}
