package net.malisis.core.renderer.element;

import java.util.HashMap;

import net.minecraft.util.AxisAlignedBB;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

public class Shape
{
	protected Face[] faces;
	protected Matrix4f selfRotationMatrix;
	protected Matrix4f transformMatrix;

	public Shape(Face[] faces)
	{
		// we need a copy of the faces else the modification for one shape would
		// impact the others ones
		this.faces = new Face[faces.length];
		for (int i = 0; i < faces.length; i++)
			this.faces[i] = new Face(faces[i]);
	}

	public Shape(Shape s)
	{
		this(s.faces);
		copyMatrix(s);
	}

	public Face[] getFaces()
	{
		return faces;
	}

	
	private Matrix4f matrix()
	{
		if(transformMatrix == null)
		{
			transformMatrix = new Matrix4f();
			transformMatrix.translate(new Vector3f(0.5F, 0.5F, 0.5F));
		}
		return transformMatrix;
	}
	private Matrix4f rotationMatrix()
	{
		if(selfRotationMatrix == null)
		{
			selfRotationMatrix = new Matrix4f();
			selfRotationMatrix.translate(new Vector3f(0.5F, 0.5F, 0.5F));
		}
		return selfRotationMatrix;
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
	
	
	public Shape setBounds(float x, float y, float z, float X, float Y, float Z)
	{
		for(Face f : faces)
		{
			for(Vertex v : f.getVertexes())
			{
				String name = v.name();
				if(name.contains("West"))
					v.setX(x);
				if(name.contains("East"))
					v.setX(X);
				if(name.contains("Bottom"))
					v.setY(y);
				if(name.contains("Top"))
					v.setY(Y);
				if(name.contains("North"))
					v.setZ(z);
				if(name.contains("South"))
					v.setZ(Z);				
			}
		}
		return this;
	}
	
	
	public Shape limit(AxisAlignedBB aabb)
	{
		return limit(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
	}
	
	public Shape limit(double x, double y, double z, double X, double Y, double Z)
	{
		for(Face f : faces)
		{
			for(Vertex v : f.getVertexes())
			{
				v.setX(Vertex.clamp(v.getX(), x, X));
				v.setY(Vertex.clamp(v.getY(), y, Y));
				v.setZ(Vertex.clamp(v.getZ(), z, Z));
			}
		}
		return this;
	}
		
	public Shape translate(float x, float y, float z)
	{
		matrix().translate(new Vector3f(x, y, z));
		return this;
	}

	public Shape scale(float f)
	{
		return scale(f, f, f);
	}
	public Shape scale(float x, float y, float z)
	{
		matrix().scale(new Vector3f(x, y, z));
		return this;
	}

	
	public Shape rotate(float angle, float x, float y, float z)
	{
		return rotate(angle, x, y, z, 0, 0, 0);
	}
	
	public Shape rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		translate(offsetX, offsetY, offsetZ);
		matrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
		translate(offsetX, -offsetY, -offsetZ);
		return this;
	}
	

	public Shape rotateAroundX(float angle)
	{
		return rotate(angle, 1, 0, 0, 0, 0, 0);
	}
	public Shape rotateAroundX(float angle, float y, float z)
	{
		return rotate(angle, 1, 0, 0, 0, y, z);
	}
	public Shape rotateAroundY(float angle)
	{
		return rotate(angle, 0, 1, 0, 0, 0, 0);
	}
	public Shape rotateAroundY(float angle, float x, float z)
	{
		return rotate(angle, 0, 1, 0, x, 0, z);
	}
	public Shape rotateAroundZ(float angle)
	{
		return rotate(angle, 0, 0, 1, 0, 0, 0);
	}
	public Shape rotateAroundZ(float angle, float x, float y)
	{
		return rotate(angle, 0, 0, 1, x, y, 0);
	}
	
	
	
	public Shape pivot(float angle, float x, float y, float z)
	{
		rotationMatrix().rotate((float) Math.toRadians(angle), new Vector3f(x, y, z));
		return this;
	}
	public Shape pivotX(float angle)
	{
		return pivot(angle, 1, 0, 0);
	}
	public Shape pivotY(float angle)
	{
		return pivot(angle, 0, 1, 0);
	}
	public Shape pivotZ(float angle)
	{
		return pivot(angle, 0, 0, 1);
	}
	
	public Shape copyMatrix(Shape shape)
	{
		if(shape.transformMatrix != null)
			this.transformMatrix = new Matrix4f(shape.transformMatrix);
		return this;
	}

	
	public Shape applyMatrix()
	{
		if(transformMatrix == null && selfRotationMatrix == null)
			return this;
		
		if(transformMatrix != null)
			transformMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));
		if(selfRotationMatrix != null)
			selfRotationMatrix.translate(new Vector3f(-0.5F, -0.5F, -0.5F));
		
		for(Face f : faces)
		{
			for(Vertex v : f.getVertexes())
			{
				if(selfRotationMatrix != null)
					v.applyMatrix(selfRotationMatrix);
				if(transformMatrix != null)
					v.applyMatrix(transformMatrix);					
			}
		}
		
		transformMatrix = null;
		selfRotationMatrix = null;
		return this;
	}
	
	public Shape shrink(Face face, float factor)
	{
		for(Face f : faces)
			if(f.name() == face.name())
				face = f;
		
		HashMap<String, Vertex> vertexNames = new HashMap<String, Vertex>();
		double x = 0, y = 0, z = 0;
		for(Vertex v : face.getVertexes())
		{
			vertexNames.put(v.name(), v);
			x += v.getX() / 4;
			y += v.getY() / 4;
			z += v.getZ() / 4;
		}
		
		face.scale(factor, x, y, z);
		
		for(Face f : faces)
		{
			for(Vertex v : f.getVertexes())
			{
				Vertex tmpV = vertexNames.get(v.name());
				if(tmpV != null)
					v.set(tmpV.getX(), tmpV.getY(), tmpV.getZ());
			}
		}
		
		return this;
	}

}
