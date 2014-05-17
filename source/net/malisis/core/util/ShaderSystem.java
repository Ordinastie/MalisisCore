package net.malisis.core.util;

import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL20.*;

/**
 * ShaderSystem
 *
 * @author PaleoCrafter
 */
public class ShaderSystem
{

    private HashMap<String, Integer> varLocations;

    private int program, lastProgram = 0;

    private boolean initialized, active;

    public ShaderSystem()
    {
        varLocations = new HashMap<>();
        program = glCreateProgram();
    }

    public void init()
    {
        glLinkProgram(program);
        glValidateProgram(program);
        initialized = true;
        active = glGetProgrami(program, GL_LINK_STATUS) == GL_TRUE;
    }

    public void activate()
    {
        if (!initialized) init();
        if (active)
        {
            lastProgram = glGetInteger(GL_CURRENT_PROGRAM);
            glUseProgram(program);
        }
    }

    public void deactivate()
    {
        if (!initialized) init();
        if (active) glUseProgram(lastProgram);
    }

    public void addShader(String source, int type)
    {
        if (!initialized)
        {
            try
            {
                glAttachShader(program, createShader(source, type));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void setUniform1i(String var, int value)
    {
        if (active) glUniform1i(getUniformLocation(var), value);
    }

    public void setUniform2f(String var, float v1, float v2)
    {
        if (active) glUniform2f(getUniformLocation(var), v1, v2);
    }

    public int getUniformLocation(String var)
    {
        if (!varLocations.containsKey(var))
        {
            varLocations.put(var, glGetUniformLocation(program, var));
        }
        return varLocations.get(var);
    }

    private static int createShader(String source, int shaderType) throws Exception
    {
        int shader = 0;
        try
        {
            shader = glCreateShader(shaderType);

            if (shader == 0)
                return 0;

            glShaderSource(shader, source);
            glCompileShader(shader);

            return shader;
        }
        catch (Exception exc)
        {
            glDeleteShader(shader);
            throw exc;
        }
    }

}
