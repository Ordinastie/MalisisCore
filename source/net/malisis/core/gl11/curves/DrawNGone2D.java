/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.gl11.curves;

import net.malisis.core.util.MathF.ConstraintValue;
import net.malisis.core.util.MathF.ConvertColor;
import org.lwjgl.util.vector.Vector4f;

import org.lwjgl.util.vector.Vector2f;

public class DrawNGone2D {
    private static float NGTop2D;
    private static float NGLeft2D;
    private static Vector4f NGColor2D = new Vector4f(1,1,1,1);

    public static void setCoordinates(float left, float top)
    {
        NGLeft2D = left;
        NGTop2D = top;
    }

    public static void setColor(Vector4f color)
    {
        color = ConvertColor.calculate(color);
        NGColor2D = ConstraintValue.calculate(color, new Vector4f(0, 0, 0, 0), new Vector4f(1, 1, 1, 1));
    }

    public static void setColor(float red, float green, float blue, float alpha)
    {
        Vector4f temp = ConvertColor.calculate(red,green,blue,alpha);
        NGColor2D = ConstraintValue.calculate(temp, new Vector4f(0,0,0,0), new Vector4f(1,1,1,1));
    }

    public static void draw (Vector2f center, float radius, int sides, float angle, float rotation, boolean antialissed, int lineWidth)
    {
        if (sides < 3)
            sides = 3;
        for (int i = 0; i < sides; i++) {
            double angleOne = Math.toRadians((angle / sides * i) + rotation);
            double angleTwo = Math.toRadians((angle / sides * (i + 1)) + rotation);
            double x1 = Math.cos(angleOne) * radius;
            double x2 = Math.cos(angleTwo) * radius;
            double y1 = Math.sin(angleOne) * radius;
            double y2 = Math.sin(angleTwo) * radius;
            Vector2f pointOne = new Vector2f((float) x1 + center.x, (float) y1 + center.y);
            Vector2f pointTwo = new Vector2f((float) x2 + center.x, (float) y2 + center.y);
            Draw2D.Line.setCoordinates(NGLeft2D,NGTop2D);
            Draw2D.Line.setColor(NGColor2D);
            Draw2D.Line.draw(pointOne,pointTwo,antialissed,lineWidth);
        }
    }

    public static void draw (Vector2f center, float radius, int sides, float angle, float rotation, int lineWidth)
    {
        draw(center,radius,sides,angle,rotation,false, lineWidth);
    }

    public static void draw (Vector2f center, float radius, int sides, float angle, float rotation,boolean antialiased)
    {
        draw(center,radius,sides,angle,rotation, antialiased, 1);
    }

    public static void draw (Vector2f center, float radius, int sides, float angle, float rotation)
    {
        draw(center,radius,sides,angle,rotation,false, 1);
    }

    public static void draw(Vector2f center, float radius, int sides, boolean antialised, int lineWidth)
    {
        draw(center, radius, sides, 360, 0, antialised, lineWidth);
    }

    public static void draw(Vector2f center, float radius, int sides, boolean antialised)
    {
        draw ( center, radius, sides, 360, 0, antialised, 1);
    }

    public static void draw(Vector2f center, float radius, int sides, int lineWidth)
    {
        draw ( center, radius, sides, 360, 0, false, lineWidth);
    }

    public static void draw(Vector2f center, float radius, int sides)
    {
        draw ( center, radius, sides, 360, 0, false, 1);
    }

    public static void draw(Vector2f center, float radius, int sides, float angle, boolean isRotation, boolean antialised, int lineWidth)
    {
        if (!isRotation)
            draw(center, radius, sides, angle, 0, antialised, lineWidth);
        else
            draw(center, radius, sides, 360, angle, antialised, lineWidth);
    }

    public static void draw(Vector2f center, float radius, int sides, float angle, boolean isRotation, boolean antialised)
    {
        if (!isRotation)
            draw(center, radius, sides, angle, 0, antialised, 1);
        else
            draw(center, radius, sides, 360, angle, antialised, 1);
    }

    public static void draw(Vector2f center, float radius, int sides, float angle, boolean isRotation, int lineWidth)
    {
        if (!isRotation)
            draw(center, radius, sides, angle, 0, false, lineWidth);
        else
            draw(center, radius, sides, 360, angle, false, lineWidth);
    }

    public static void draw(Vector2f center, float radius, int sides, float angle, boolean isRotation)
    {
        if (!isRotation)
            draw(center, radius, sides, angle, 0, false, 1);
        else
            draw(center, radius, sides, 360, angle, false, 1);
    }
}
