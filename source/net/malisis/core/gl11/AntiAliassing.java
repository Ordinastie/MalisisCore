/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.gl11;

import static org.lwjgl.opengl.GL11.*;

public class AntiAliassing
{

    public static void enable(int lineWidth)
    {
        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
    }

    public static void disable(int lineWidth)
    {
        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);
        glLineWidth(lineWidth);
    }
}
