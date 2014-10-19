/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.util.MathF;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class ConstraintValue
{
    //region Double
    public static double calculate(double value, double min, double max)
    {
        double temp = value;
        if (temp < min) temp = min;
        if (temp > max) temp = max;
        return temp;
    }

    public static double[] calculate(double[] value, double[] min, double[] max)
    {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max[i]);
        return value;
    }

    public static double[] calculate(double[] value, double min, double max)
    {
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max);
        return value;
    }

    public static double[] calculate(double[] value, double[] min, double max) {
        if (value.length != min.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max);
        return value;
    }

    public static double[] calculate(double[] value, double min, double[] max) {
        if (value.length != max.length)
            return null;
        for (int i = 0; i < max.length; i++)
            value[i] = calculate(value[i], min, max[i]);
        return value;
    }
    //endregion

    //region Int
    public static int calculate(int value, int min, int max) {
        int temp = value;
        if (temp < min) temp = min;
        if (temp > max) temp = max;
        return temp;
    }

    public static int[] calculate(int[] value, int[] min, int[] max) {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max[i]);
        return value;
    }

    public static int[] calculate(int[] value, int min, int max) {
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max);
        return value;
    }

    public static int[] calculate(int[] value, int[] min, int max) {
        if (value.length != min.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max);
        return value;
    }

    public static int[] calculate(int[] value, int min, int[] max) {
        if (value.length != max.length)
            return null;
        for (int i = 0; i < max.length; i++)
            value[i] = calculate(value[i], min, max[i]);
        return value;
    }
    //endregion

    //region Float
    public static float calculate(float value, float min, float max) {
        float temp = value;
        if (temp < min) temp = min;
        if (temp > max) temp = max;
        return temp;
    }

    public static float[] calculate(float[] value, float[] min, float[] max) {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max[i]);
        return value;
    }

    public static float[] calculate(float[] value, float min, float max) {
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max);
        return value;
    }

    public static float[] calculate(float[] value, float[] min, float max) {
        if (value.length != min.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max);
        return value;
    }

    public static float[] calculate(float[] value, float min, float[] max) {
        if (value.length != max.length)
            return null;
        for (int i = 0; i < max.length; i++)
            value[i] = calculate(value[i], min, max[i]);
        return value;
    }
    //endregion

    //region Vector2f
    public static Vector2f calculate(Vector2f value, Vector2f min, Vector2f max) {
        Vector2f temp = value;
        temp.x = calculate(temp.x, min.x, max.x);
        temp.y = calculate(temp.y, min.x, max.y);
        return temp;
    }

    public static Vector2f[] calculate(Vector2f[] value, Vector2f[] min, Vector2f[] max)
    {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max[i]);
        return value;
    }

    public static Vector2f[] calculate(Vector2f[] value, Vector2f min, Vector2f[] max)
    {
        if (value.length != max.length)
            return null;
        for (int i = 0; i < max.length; i++)
            value[i] = calculate(value[i], min, max[i]);
        return value;
    }

    public static Vector2f[] calculate(Vector2f[] value, Vector2f[] min, Vector2f max)
    {
        if (value.length != min.length)
            return null;
        for (int i = 0; i < min.length; i++)
            value[i] = calculate(value[i], min[i], max);
        return value;
    }

    public static Vector2f[] calculate(Vector2f[] value, Vector2f min, Vector2f max)
    {
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max);
        return value;
    }
    //endregion

    //region Vector3f
    public static Vector3f calculate(Vector3f value, Vector3f min, Vector3f max) {
        Vector3f temp = value;
        temp.x = calculate(temp.x, min.x, max.x);
        temp.y = calculate(temp.y, min.y, max.y);
        temp.z = calculate(temp.z, min.z, max.z);
        return temp;
    }

    public static Vector3f[] calculate(Vector3f[] value, Vector3f[] min, Vector3f[] max) {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min[i], max[i]);
        return value;
    }

    public static Vector3f[] calculate(Vector3f[] value, Vector3f min, Vector3f[] max) {
        if (value.length != max.length)
            return null;
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max[i]);
        return value;
    }

    public static Vector3f[] calculate(Vector3f[] value, Vector3f[] min, Vector3f max) {
        if (value.length != min.length)
            return null;
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min[i], max);
        return value;
    }

    public static Vector3f[] calculate(Vector3f[] value, Vector3f min, Vector3f max) {
        for (int i = 0; i < value.length; i++)
            value[i] = calculate(value[i], min, max);
        return value;
    }
    //endregion

    //region Vector4f
    public static Vector4f calculate(Vector4f value, Vector4f min, Vector4f max) {
        Vector4f temp = value;
        temp.x = calculate(temp.x, min.x, max.x);
        temp.y = calculate(temp.y, min.y, max.y);
        temp.z = calculate(temp.z, min.z, max.z);
        temp.w = calculate(temp.w, min.w, max.w);
        return temp;
    }

    public static Vector4f[] calculate(Vector4f[] value, Vector4f[] min, Vector4f[] max)
    {
        if (value.length != min.length ||
                value.length != max.length ||
                min.length != max.length)
            return null;
        for (int i=0; i < value.length; i++)
            value[i]= calculate(value[i], min[i], max[i]);
        return value;
    }

    public static Vector4f[] calculate(Vector4f[] value, Vector4f min, Vector4f[] max)
    {
        if (value.length != max.length)
            return null;
        for (int i=0; i < value.length; i++)
            value[i]= calculate(value[i], min, max[i]);
        return value;
    }

    public static Vector4f[] calculate(Vector4f[] value, Vector4f[] min, Vector4f max)
    {
        if (value.length != min.length)
            return null;
        for (int i=0; i < value.length; i++)
            value[i]= calculate(value[i], min[i], max);
        return value;
    }

    public static Vector4f[] calculate(Vector4f[] value, Vector4f min, Vector4f max) {
        for (int i=0; i < value.length; i++)
            value[i]= calculate(value[i], min, max);
        return value;
    }
    //endregion
}
