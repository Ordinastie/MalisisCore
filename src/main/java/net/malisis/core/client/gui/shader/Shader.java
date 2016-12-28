/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
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

package net.malisis.core.client.gui.shader;

import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.google.common.collect.Maps;

import net.malisis.core.MalisisCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class Shader
{
	private int program = 0;
	private int lastProgram = 0;
	private ResourceLocation resourceLocation;

	private final HashMap<String, Integer> params = Maps.newHashMap();

	public Shader(ResourceLocation res)
	{
		this.resourceLocation = res;
		load();
	}

	private void load()
	{
		String code = loadShaderCode();
		createProgram(code);
	}

	private String loadShaderCode()
	{
		try
		{
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			return IOUtils.toString(res.getInputStream(), "UTF-8");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private void createProgram(String code)
	{
		try
		{
			program = GL20.glCreateProgram();
			GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			if (program == 0)
				return;
			GL20.glShaderSource(program, code);
			GL20.glCompileShader(program);
		}
		catch (Exception e)
		{
			MalisisCore.log.error("Failed to create shader.", e);
		}
	}

	public boolean start()
	{
		GL20.glLinkProgram(program);
		GL20.glValidateProgram(program);
		boolean active = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_TRUE;
		if (active)
		{
			lastProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
			GL20.glUseProgram(program);
		}
		return active;
	}

	public void stop()
	{
		GL20.glUseProgram(lastProgram);
	}

	public void setUniform1i(String param, int value)
	{
		GL20.glUniform1i(getUniformIndex(param), value);
	}

	public void setUniform2f(String param, float v1, float v2)
	{
		GL20.glUniform2f(getUniformIndex(param), v1, v2);
	}

	private int getUniformIndex(String param)
	{
		if (!params.containsKey(param))
			params.put(param, GL20.glGetUniformLocation(program, param));

		return params.get(param);
	}
}
