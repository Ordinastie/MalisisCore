/**
 * Created by Alireza Khodakarami on 10/19/2014.
 */

package net.malisis.core.gl11;

import static org.lwjgl.opengl.GL11.*;

public class InitializeForCurves {

    public static void disable (boolean texturing, boolean lighting, boolean depth)
    {
        if (texturing)
            glDisable(GL_TEXTURE_2D);
        if (lighting)
            glDisable(GL_LIGHTING);
        if (depth)
            glDisable(GL_DEPTH_TEST);
        glDepthMask(depth);
        glColor4f(1,1,1,1);
    }

    public static void enable (boolean texturing, boolean lighting, boolean depth)
    {
        if (texturing)
            glEnable(GL_TEXTURE_2D);
        if (lighting)
            glEnable(GL_LIGHTING);
        if (depth)
            glEnable(GL_DEPTH_TEST);
        glDepthMask(depth);
        glColor4f(1,1,1,1);
    }
}
