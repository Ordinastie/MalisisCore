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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.model.IModelLoader;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.Vector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

// TODO: Auto-generated Javadoc
/**
 * Model loader for OBJ files (Wavefront).
 *
 * @author Ordinastie
 */
public class ObjFileImporter implements IModelLoader
{
	/** Pattern for lines. */
	protected static Pattern linePattern = Pattern.compile("^(?<type>.*?) (?<data>.*)$");

	/** Pattern for face data. */
	protected static Pattern facePattern = Pattern.compile("(?<v>\\d+)(/(?<t>\\d+)?(/(?<n>\\d+))?)?");

	/** Matcher object. */
	protected Matcher matcher;

	/** Current line being parsed. */
	protected String currentLine;

	/** Current line number. */
	protected int lineNumber;

	/** Current shape name. */
	protected String currentShape = "Default";

	/** List of {@link Vertex vertexes} used for the {@link MalisisModel}. */
	protected List<Vertex> vertexes = new ArrayList<>();

	/** List of UV used for the {@link MalisisModel}. */
	protected List<UV> uvs = new ArrayList<>();

	/** List of vertex normals used for the {@link MalisisModel}. */
	protected List<Vector> normals = new ArrayList<>();

	/** List of {@link Face faces} used for the {@link MalisisModel}. */
	protected List<Face> faces = new ArrayList<>();

	/** Map of the {@link Shape shapes} used for the {@link MalisisModel}. */
	protected Map<String, Shape> shapes = new HashMap<>();

	/**
	 * Instantiates a new {@link ObjFileImporter} from a {@link ResourceLocation}.
	 *
	 * @param resourceLocation the resource location
	 */
	public ObjFileImporter(ResourceLocation resourceLocation)
	{
		load(resourceLocation);
	}

	/**
	 * Instantiates a new {@link ObjFileImporter} from {@link InputStream}.
	 *
	 * @param inputStream the input stream
	 */
	public ObjFileImporter(InputStream inputStream)
	{
		load(inputStream);
	}

	/**
	 * Gets the shapes.
	 *
	 * @return the shapes
	 */
	@Override
	public Map<String, Shape> getShapes()
	{
		return shapes;
	}

	/**
	 * Load the shapes from a {@link ResourceLocation}.
	 *
	 * @param resourceLocation the resource location
	 */
	public void load(ResourceLocation resourceLocation)
	{
		try
		{
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation);
			load(res.getInputStream());
		}
		catch (IOException e)
		{
			MalisisCore.log.error("[ObjFileImporter] An error happened while reading the file : {}", resourceLocation, e);
		}

	}

	/**
	 * Loads the shapes from {@link InputStream}.
	 *
	 * @param inputStream the input stream
	 */
	public void load(InputStream inputStream)
	{

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
		{
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
							addNormal(data);
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
						case "s": 
							// smoothing group, not supported by backing rendering implementation / ignore.
							break;
						case "#":
							// Skip Comments
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
	}

	/**
	 * Creates a new {@link Vertex} from data and adds it to {@link #vertexes}.
	 *
	 * @param data the data
	 */
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

	/**
	 * Creates a new {@link UV} from data and adds it to {@link #uvs}.
	 *
	 * @param data the data
	 */
	private void addUV(String data)
	{
		String coords[] = data.split("\\s+");
		float u = 0;
		float v = 0;
		if (coords.length != 2)
		{
			MalisisCore.log.error("[ObjFileImporter] Wrong UV coordinates number {} at line {} : {}", coords.length, lineNumber,
					currentLine);
		}
		else
		{
			u = Float.parseFloat(coords[0]);
			v = 1 - Float.parseFloat(coords[1]);
		}

		uvs.add(new UV(u, v));
	}

	/**
	 * Creates a new normal {@link Vector} from data and adds it to {@link #normals}.
	 *
	 * @param data the data
	 */
	private void addNormal(String data)
	{
		String coords[] = data.split("\\s+");
		float x = 0;
		float y = 0;
		float z = 0;
		if (coords.length != 3)
		{
			MalisisCore.log.error("[ObjFileImporter] Wrong Normal coordinates number {} at line {} : {}", coords.length, lineNumber,
					currentLine);
		}
		else
		{
			x = Float.parseFloat(coords[0]);
			y = Float.parseFloat(coords[1]);
			z = Float.parseFloat(coords[2]);
		}

		normals.add(new Vector(x, y, z));
	}

	/**
	 * Creates a new {@link Face} from data and adds it to {@link #faces}.<br>
	 * Tries to deduct parameters from the normals provided and disable brightness/AO calculations.
	 *
	 * @param data the data
	 */
	private void addFace(String data)
	{
		matcher = facePattern.matcher(data);

		List<Vertex> faceVertex = new ArrayList<>();
		List<Vector> faceNormals = new ArrayList<>();
		int v = 0, t = 0, n = 0;
		String strV, strT, strN;
		Vertex vertex, vertexCopy;
		UV uv = null;
		Vector normal;
		while (matcher.find())
		{
			normal = null;
			uv = null;

			strV = matcher.group("v");
			strT = matcher.group("t");
			strN = matcher.group("n");

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

				if (strN != null)
				{
					n = Integer.parseInt(strN);
					n = n > 0 ? n - 1 : normals.size() - n - 1;
					if (n >= 0 && n < normals.size())
						normal = normals.get(n);
					if (normal != null)
						faceNormals.add(new Vector(normal.x, normal.y, normal.z));
				}
			}
			else
			{
				MalisisCore.log.error("[ObjFileImporter] Wrong vertex reference {} for face at line {} :\n{}", v, lineNumber, currentLine);
			}
		}

		Face f = new Face(faceVertex);
		f.deductParameters(faceNormals.toArray(new Vector[0]));
		RenderParameters params = f.getParameters();
		if (params.direction.get() == ForgeDirection.NORTH || params.direction.get() == ForgeDirection.EAST)
			params.flipU.set(true);
		params.renderAllFaces.set(true);
		params.interpolateUV.set(false);
		params.calculateAOColor.set(false);
		params.useEnvironmentBrightness.set(false);

		faces.add(f);
	}

	/**
	 * Creates a {@link Shape} and adds it to {@link #shapes}.
	 *
	 * @param data the data
	 */
	private void addShape(String data)
	{
		if (faces.size() != 0)
		{
			Shape s = new Shape(faces);
			shapes.put(currentShape.toLowerCase(), s);
			faces.clear();
		}

		if (data != "" && data.indexOf('_') != -1)
			currentShape = data.substring(0, data.indexOf('_'));
	}

	/**
	 * UV holder class
	 */
	private class UV
	{
		/** U coordinate. */
		float u;
		/** V coordinate. */
		float v;

		/**
		 * Instantiates a new {@link UV}
		 *
		 * @param u the u
		 * @param v the v
		 */
		public UV(float u, float v)
		{
			this.u = u;
			this.v = v;
		}
	}

}
