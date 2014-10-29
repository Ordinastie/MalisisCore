/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
