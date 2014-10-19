/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.gl11.curves;

import net.malisis.core.gl11.AntiAliassing;
import net.malisis.core.gl11.InitializeForCurves;
import net.malisis.core.util.MathF.ConstraintValue;
import net.malisis.core.util.MathF.ConvertColor;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.util.vector.Vector4f;

import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;

public class Draw2D
{
    private static float SPLeft2D,SPTop2D;
    private static Vector4f SPColor2D;

    public static class Line
    {
        public static void setCoordinates(float left, float top)
        {
            SPLeft2D = left;
            SPTop2D = top;
        }

        public static void setColor(Vector4f color)
        {
            color = ConvertColor.calculate(color);
            SPColor2D = ConstraintValue.calculate(color, new Vector4f(0, 0, 0, 0), new Vector4f(1, 1, 1, 1));
        }

        public static void setColor(float red, float green, float blue, float alpha)
        {
            Vector4f temp = ConvertColor.calculate(red,green,blue,alpha);
            SPColor2D = ConstraintValue.calculate(temp, new Vector4f(0,0,0,0), new Vector4f(1,1,1,1));
        }

        public static void draw (Vector2f pointOne, Vector2f pointTwo, boolean antiAliased, int lineWidth)
        {
            if (antiAliased)
                AntiAliassing.enable(lineWidth);
            glPushMatrix();
            InitializeForCurves.disable(true,true,true);
            glColor4f(SPColor2D.x, SPColor2D.y,SPColor2D.z,SPColor2D.w);
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawing(GL_LINE_STRIP);
            tessellator.addVertex(SPLeft2D + pointOne.x + 0.5f, SPTop2D + pointOne.y + 0.5f, 0);
            tessellator.addVertex( SPLeft2D + pointTwo.x + 0.5f, SPTop2D + pointTwo.y  + 0.5f, 0);
            tessellator.draw();
            InitializeForCurves.enable(true,true,true);
            glPopMatrix();

            if (antiAliased)
                AntiAliassing.disable(lineWidth);
        }

        public static void draw (float pointOneX, float pointOneY, float pointTwoX, float pointTwoY, boolean antiAliased, int lineWidth)
        {
            draw(new Vector2f(pointOneX, pointOneY), new Vector2f(pointTwoX, pointTwoY), antiAliased, lineWidth);
        }

        public static void draw (Vector2f[] p, boolean antiAliased, int lineWidth)
        {

            for (int i = 0; i < p.length - 1; i++)
                draw(p[i], p[i + 1], antiAliased, lineWidth);
        }

        public static void draw (float[] pointsX, float[] pointsY, boolean antiAliased, int lineWidth)
        {
            if (pointsX.length != pointsY.length)
                return;
            Vector2f[] tmp = new Vector2f[pointsX.length];
            for (int i = 0; i < pointsX.length; i++)
                tmp[i] = new Vector2f(pointsX[i], pointsY[i]);
            draw(tmp, antiAliased, lineWidth);
        }

        public static void draw (Vector2f pointOne, Vector2f pointTwo, int lineWidth)
        {
            draw(pointOne,pointTwo,false,lineWidth);
        }

        public static void draw (float pointOneX, float pointOneY, float pointTwoX, float pointTwoY,  int lineWidth)
        {
            draw(new Vector2f(pointOneX, pointOneY), new Vector2f(pointTwoX, pointTwoY), true, lineWidth);
        }

        public static void draw (Vector2f[] p, int lineWidth)
        {

            for (int i = 0; i < p.length - 1; i++)
                draw(p[i], p[i + 1], false, lineWidth);
        }

        public static void draw (float[] pointsX, float[] pointsY, int lineWidth)
        {
            if (pointsX.length != pointsY.length)
                return;
            Vector2f[] tmp = new Vector2f[pointsX.length];
            for (int i = 0; i < pointsX.length; i++)
                tmp[i] = new Vector2f(pointsX[i], pointsY[i]);
            draw(tmp, false, lineWidth);
        }

        public static void draw (Vector2f pointOne, Vector2f pointTwo, boolean antiAliased)
        {
            draw(pointOne,pointTwo,antiAliased,1);
        }

        public static void draw (float pointOneX, float pointOneY, float pointTwoX, float pointTwoY, boolean antiAliased)
        {
            draw(new Vector2f(pointOneX, pointOneY), new Vector2f(pointTwoX, pointTwoY), antiAliased, 1);
        }

        public static void draw (Vector2f[] p, boolean antiAliased)
        {

            for (int i = 0; i < p.length - 1; i++)
                draw(p[i], p[i + 1], antiAliased, 1);
        }

        public static void draw (float[] pointsX, float[] pointsY, boolean antiAliased)
        {
            if (pointsX.length != pointsY.length)
                return;
            Vector2f[] tmp = new Vector2f[pointsX.length];
            for (int i = 0; i < pointsX.length; i++)
                tmp[i] = new Vector2f(pointsX[i], pointsY[i]);
            draw(tmp, antiAliased, 1);
        }
    }
}
