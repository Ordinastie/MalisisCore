package net.malisis.core.renderer.element;

import java.util.HashMap;

import net.malisis.core.renderer.RenderParameters;
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

		this.params = params != null ? params : new RenderParameters();
	}

	public Face(Vertex[] vertexes)
	{
		this(vertexes, null);
	}

	public Face(Face face)
	{
		this(face, new RenderParameters(face.params));
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
		this.params = params != null ? params : new RenderParameters();
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
		return setTexture(icon, params.flipU.get(), params.flipV.get(), false);
	}

	public Face setStandardUV()
	{
		vertexes[0].setUV(0, 0);
		vertexes[1].setUV(0, 1);
		vertexes[2].setUV(1, 1);
		vertexes[3].setUV(1, 0);
		return this;
	}

	public Face setTexture(IIcon icon, boolean flippedU, boolean flippedV, boolean interpolate)
	{
		if (icon == null)
			return this;

		float u = icon.getMinU();
		float v = icon.getMinV();
		float U = icon.getMaxU();
		float V = icon.getMaxV();

		double factorU, factorV;

		for (Vertex vertex : vertexes)
		{
			factorU = interpolate ? getFactorU(vertex) : vertex.getU();
			factorV = interpolate ? getFactorV(vertex) : vertex.getV();
			vertex.setUV(interpolate(u, U, factorU, flippedU), interpolate(v, V, factorV, flippedV));

		}

		return this;
	}

	private double getFactorU(Vertex vertex)
	{
		if (params.textureSide.get() == null)
			return vertex.getU();

		switch (params.textureSide.get())
		{
			case EAST:
				return vertex.getZ();
			case WEST:
				return vertex.getZ();
			case NORTH:
				return vertex.getX();
			case SOUTH:
			case UP:
			case DOWN:
				return vertex.getX();
			default:
				return 0;
		}
	}

	private double getFactorV(Vertex vertex)
	{
		if (params.textureSide.get() == null)
			return vertex.getV();

		switch (params.textureSide.get())
		{
			case EAST:
			case WEST:
			case NORTH:
			case SOUTH:
				return 1 - vertex.getY();
			case UP:
			case DOWN:
				return vertex.getZ();
			default:
				return 0;
		}
	}

	private float interpolate(float min, float max, double factor, boolean flipped)
	{
		if (factor > 1)
			factor = 1;
		if (factor < 0)
			factor = 0;
		if (flipped)
		{
			factor = 1 - factor;
			float t = max;
			max = min;
			min = t;
		}
		return min + (max - min) * (float) factor;
	}

	public Face factor(float fx, float fy, float fz)
	{
		for (Vertex v : vertexes)
		{
			v.factorX(fx);
			v.factorY(fy);
			v.factorZ(fz);
		}
		return this;
	}

	public Face translate(double x, double y, double z)
	{
		for (Vertex v : vertexes)
			v.add(x, y, z);
		return this;
	}

	public void scale(float f)
	{
		scale(f, 0.5, 0.5, 0.5);
	}

	public void scale(float f, double x, double y, double z)
	{
		for (Vertex v : vertexes)
			v.scale(f, x, y, z);
	}

	public void rotateAroundX(double angle)
	{
		rotateAroundX(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundX(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundX(angle, centerX, centerY, centerZ);
	}

	public void rotateAroundY(double angle)
	{
		rotateAroundY(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundY(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundY(angle, centerX, centerY, centerZ);
	}

	public void rotateAroundZ(double angle)
	{
		rotateAroundZ(angle, 0.5, 0.5, 0.5);
	}

	public void rotateAroundZ(double angle, double centerX, double centerY, double centerZ)
	{
		for (Vertex v : vertexes)
			v.rotateAroundZ(angle, centerX, centerY, centerZ);
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

	@Override
	public String toString()
	{
		String s = name() + "[";
		for (Vertex v : vertexes)
			s += v.name() + ", ";
		return s + "]";
	}

}
