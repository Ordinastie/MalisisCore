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

package net.malisis.core.renderer.model.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class ObjFileImporter
{
	private MalisisModel model;

	protected String fileName;
	protected Pattern linePattern = Pattern.compile("^(?<type>.*?) (?<data>.*)$");
	protected Pattern facePattern = Pattern.compile("(?<v>\\d+)(/(?<t>\\d+)?(/(?<n>\\d+))?)?");
	protected Matcher matcher;
	protected String currentLine;
	protected int lineNumber;
	protected String currentShape = "Default";

	protected List<Vertex> vertexes = new ArrayList<>();
	protected List<UV> uvs = new ArrayList<>();
	protected List<Face> faces = new ArrayList<>();

	public ObjFileImporter(ResourceLocation resource)
	{
		this.fileName = resource.toString();
		this.model = new MalisisModel();

		IResource res;
		try
		{
			res = Minecraft.getMinecraft().getResourceManager().getResource(resource);
			loadObjModel(res.getInputStream());
		}
		catch (IOException e)
		{
			MalisisCore.log.error("[ObjFileImporter] An error happened while reading the file : {}", e);
		}

	}

	public MalisisModel getModel()
	{
		return model;
	}

	private void loadObjModel(InputStream inputStream) throws IOException
	{
		BufferedReader reader = null;

		try
		{

			reader = new BufferedReader(new InputStreamReader(inputStream));

			while ((currentLine = reader.readLine()) != null)
			{
				lineNumber++;
				currentLine = currentLine.replaceAll("\\s+", " ").trim();
				matcher = linePattern.matcher(currentLine);

				if (matcher.matches())
				{
					String type = matcher.group("type");
					String data = matcher.group("data");

					switch (type)
					{
						case "v":
							addVertex(data);
							break;
						case "vn":
							//addVertex(data);
							break;
						case "vt":
							addUV(data);
							break;
						case "f":
							addFace(data);
							break;
						case "g":
						case "o":
							addShape(data);
							break;

						default:
							MalisisCore.log.debug("[ObjFileImporter] Skipped type {} at line {} : {}", type, lineNumber, currentLine);
							break;
					}
				}
				else
				{
					MalisisCore.log.debug("[ObjFileImporter] Skipped non-matching line {} : {}", lineNumber, currentLine);
				}
			}

			addShape("");
		}
		catch (Exception e)
		{
			MalisisCore.log.error("[ObjFileImporter] An error happened while reading the file : {}", e);
		}
		finally
		{
			try
			{
				reader.close();
				inputStream.close();
			}
			catch (IOException e)
			{
				// hush
			}
		}
	}

	private void addVertex(String data)
	{
		String coords[] = data.split("\\s+");
		float x = 0;
		float y = 0;
		float z = 0;
		if (coords.length != 3)
		{
			MalisisCore.log.error("[ObjFileImporter] Wrong coordinates number {} at line {} : {}", coords.length, lineNumber, currentLine);
		}
		else
		{
			x = Float.parseFloat(coords[0]);
			y = Float.parseFloat(coords[1]);
			z = Float.parseFloat(coords[2]);
		}

		vertexes.add(new Vertex(x, y, z));
	}

	private void addUV(String data)
	{
		String coords[] = data.split("\\s+");
		float u = 0;
		float v = 0;
		if (coords.length != 2)
		{
			MalisisCore.log.error("[ObjFileImporter] Wrong coordinates number {} at line {} : {}", coords.length, lineNumber, currentLine);
		}
		else
		{
			u = Float.parseFloat(coords[0]);
			v = 1 - Float.parseFloat(coords[1]);
		}

		uvs.add(new UV(u, v));
	}

	private void addFace(String data)
	{
		matcher = facePattern.matcher(data);

		List<Vertex> faceVertex = new ArrayList<>();
		int v = 0, t = 0;
		String strV, strT;
		Vertex vertex;
		Vertex vertexCopy;
		UV uv = null;
		while (matcher.find())
		{
			strV = matcher.group("v");
			strT = matcher.group("t");

			v = Integer.parseInt(strV);
			vertex = vertexes.get(v > 0 ? v - 1 : vertexes.size() - v - 1);

			if (vertex != null)
			{
				vertexCopy = new Vertex(vertex);
				if (strT != null)
				{
					t = Integer.parseInt(strT);
					uv = uvs.get(t > 0 ? t - 1 : uvs.size() - t - 1);
					if (uv != null)
						vertexCopy.setUV(uv.u, uv.v);
				}
				faceVertex.add(vertexCopy);
			}
			else
			{
				MalisisCore.log.error("[ObjFileImporter] Wrong vertex reference {} for face at line {} :\n{}", v, lineNumber, currentLine);
			}
		}

		faces.add(new Face(faceVertex));
	}

	private void addShape(String data)
	{
		if (faces.size() != 0)
		{
			model.addShape(currentShape, new Shape(faces));
			faces.clear();
		}

		if (data != "")
			currentShape = data.substring(0, data.indexOf('_'));
	}

	private class UV
	{
		float u;
		float v;

		public UV(float u, float v)
		{
			this.u = u;
			this.v = v;
		}
	}
}
