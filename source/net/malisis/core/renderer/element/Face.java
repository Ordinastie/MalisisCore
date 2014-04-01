package net.malisis.core.renderer.element;

import java.util.HashMap;

import net.minecraft.util.IIcon;

public class Face
{
	private Vertex[] vertexes;
	private RenderParameters params = new RenderParameters();

	public Face(Vertex[] vertexes, RenderParameters params)
	{
		// we need a copy of the vertexes else the modification for one face
		// would impact the others ones
		this.vertexes = new Vertex[vertexes.length];
		for (int i = 0; i < vertexes.length; i++)
			this.vertexes[i] = new Vertex(vertexes[i]);
		if (params == null)
			params = RenderParameters.Default();
		this.params = new RenderParameters(params);
	}

	public Face(Vertex[] vertexes)
	{
		this(vertexes, null);
	}

	public Face(Face face)
	{
		this(face, face.params);
	}

	public Face(Face face, RenderParameters params)
	{
		this(face.vertexes, params);
	}

	public Vertex[] getVertexes()
	{
		return vertexes;
	}

	public Face setParameters(RenderParameters params)
	{
		this.params = params;
		return this;
	}

	public RenderParameters getParameters()
	{
		return params;
	}

	public Face setColor(int color)
	{
		for (Vertex v : vertexes)
			v.setColor(color);
		return this;
	}

	public Face setAlpha(int alpha)
	{
		for (Vertex v : vertexes)
			v.setAlpha(alpha);
		return this;
	}

	public Face setBrightness(int brightness)
	{
		for (Vertex v : vertexes)
			v.setBrightness(brightness);
		return this;
	}

	public Face setTexture(IIcon icon)
	{
		return setTexture(icon, params.uvFactor, params.flipU, params.flipV);
	}

	public Face setTexture(IIcon icon, float[][] uvFactor)
	{
		return setTexture(icon, uvFactor, params.flipU, params.flipV);
	}

	public Face setTexture(IIcon icon, float[][] uvFactor, boolean flippedU, boolean flippedV)
	{
		if (uvFactor == null)
			return this;

		float u = icon.getMinU();
		float v = icon.getMinV();
		float U = icon.getMaxU();
		float V = icon.getMaxV();

		for (int i = 0; i < vertexes.length; i++)
		{
			vertexes[i].setUV(interpolate(u, U, uvFactor[i][0], flippedU), interpolate(v, V, uvFactor[i][1], flippedV));
		}

		return this;
	}

	private float interpolate(float min, float max, float factor, boolean flipped)
	{
		if (flipped)
		{
			factor = 1 - factor;
			float t = max;
			max = min;
			min = t;
		}
		return min + (max - min) * factor;
	}

	public void scale(float f)
	{
		for (Vertex v : vertexes)
			v.factor(f);
	}

	public String name()
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		String[] dirs = new String[] { "North", "South", "East", "West", "Top", "Bottom" };
		for (String dir : dirs)
		{
			map.put(dir, 0);
			for (Vertex v : vertexes)
			{
				if (v.name().contains(dir))
					map.put(dir, map.get(dir) + 1);
			}
			if (map.get(dir) == 4)
				return dir;
		}
		return "";
	}

	public String toString()
	{
		String s = name() + "[";
		for (Vertex v : vertexes)
			s += v.name() + ", ";
		return s + "]";
	}

}
