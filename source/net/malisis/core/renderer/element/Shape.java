package net.malisis.core.renderer.element;

public class Shape
{

	private Face[] faces;
	private RenderParameters params;

	public Shape(Face[] faces, RenderParameters params)
	{
		// we need a copy of the faces else the modification for one shape would
		// impact the others ones
		this.faces = new Face[faces.length];
		for (int i = 0; i < faces.length; i++)
			this.faces[i] = new Face(faces[i]);
		if (params == null)
			params = RenderParameters.Default();
		this.params = new RenderParameters(params);
	}

	public Shape(Face[] faces)
	{
		this(faces, null);
	}

	public Shape(Shape s)
	{
		this(s.faces, s.params);
	}

	public Shape(Shape s, RenderParameters params)
	{
		this(s.faces, params);
	}

	public Face[] getFaces()
	{
		return faces;
	}

	public RenderParameters getParameters()
	{
		return params;
	}

	/**
	 * Set parameters for a face. The face is determined by
	 * <b>face</b>.<i>name()</i> in order to avoid having to keep a reference to
	 * the actual shape face. If <b>merge</b> is true, the parameters will be
	 * merge with the shape face parameters instead of completely overriding
	 * them
	 * 
	 * @param face
	 * @param params
	 * @param merge
	 * @return
	 */
	public Shape setParameters(Face face, RenderParameters params, boolean merge)
	{
		for (Face f : faces)
		{
			if (f.name() == face.name())
			{
				if (merge)
					params = RenderParameters.merge(f.getParameters(), params);
				f.setParameters(params);
			}
		}
		return this;
	}

}
