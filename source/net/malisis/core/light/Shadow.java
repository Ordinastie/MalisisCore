package net.malisis.core.light;

import java.util.Arrays;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.util.Point;
import net.malisis.core.util.Raytrace;
import net.malisis.core.util.Vector;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.common.util.ForgeDirection;

public class Shadow
{
	/**
	 * Block position
	 */
	public ChunkPosition block;
	/**
	 * Face to light
	 */
	public Face face;
	/**
	 * Colored light applying color
	 */
	public ColoredLight[] light;
	/**
	 * Side of the vertex
	 */
	public ForgeDirection side;
	/**
	 * Source of the ray
	 */
	public Vec3 source;
	/**
	 * Destination of the ray
	 */
	public Vec3 dest;
	/**
	 * Center vertex
	 */
	public Vertex[] center = new Vertex[4];
	/**
	 * Number of vertexes affected by a light
	 */
	public int vertexCount = 0;

	public Shadow(ChunkPosition block, Face face)
	{
		this.block = block;
		this.face = face;
		this.side = face.getParameters().direction;
		
	
		getFaceLights();
		
		if(vertexCount > 1)
			getCenterVertexes();
	
	}

	/**
	 * Get the lights lighting a face and apply corresponding color to it
	 */
	public void getFaceLights()
	{
		vertexCount = 0;
		for (Vertex v : face.getVertexes())
		{
			int[] lights = getVertexLights(v);
			if (lights.length > 0)
			{
				int light = computeVertexLight(lights);
				v.setBrightness(15 << 4);
				v.setAlpha((int) (ColoredLight.alpha(light) * 0.8F));
				v.setColor(light & 0xFFFFFF);
				vertexCount++;
			}
			else
				v.setAlpha(0);
		}		
	}

	/**
	 * Get the lights lighting a vertex
	 * 
	 * @param vertex
	 * @return color of the combined lights
	 */
	public int[] getVertexLights(Vertex vertex)
	{
		ColoredLight[] lights = ColoredLight.getLights();
		int[] tmp = new int[lights.length];
		int count = 0;
		for (ColoredLight l : lights)
		{
			if (l.emitting)
			{
				// ray trace from light to vertex (vertex position is relative
				// to the block, so we need to add the block position)
				Raytrace rt = new Raytrace(new Point(l.x, l.y, l.z), vertex.toPoint()
						.add(block.chunkPosX, block.chunkPosY, block.chunkPosZ));
				double distance = rt.distance();
				// check if the vertex is too far away and if the face is facing
				// toward the light
				if (distance < l.strength && rt.direction().dot(new Vector(side.offsetX, side.offsetY, side.offsetZ)) < 0)
				{
					MovingObjectPosition mop = rt.trace();
					// only light the vertex if ray trace do not hit anything
					if (mop == null || mop.typeOfHit == MovingObjectType.MISS)
					{
						int alpha = (int) (255 - Math.pow((distance / l.strength), 2) * 255);
						tmp[count++] = l.color | alpha << 24;
					}
				}
			}
		}

		return Arrays.copyOf(tmp, count);
	}

	public int computeVertexLight(int[] lights)
	{
		int color = lights[0];
		for (int i = 1; i < lights.length; i++)
		{
			color = ColoredLight.mergeAdd(color, lights[i]);
		}

		return color;
	}
	
	public void getCenterVertexes()
	{
		float x = 0.5F, y = 0.5F, z = 0.5F;
		if(side == ForgeDirection.UP)
			y = 1.001F;
		else if (side == ForgeDirection.DOWN)
			y = -0.001F;
		else if(side == ForgeDirection.EAST)
			x = 1.001F;
		else if (side == ForgeDirection.WEST)
			x = -0.001F;
		else if(side == ForgeDirection.NORTH)
			z = -0.001F;
		else if (side == ForgeDirection.SOUTH)
			z = 1.001F;
		
		float minU = 1, minV = 1, maxU = 0, maxV = 0;
		for(Vertex v : face.getVertexes())
		{
			minU = Math.min(minU, v.getU());
			maxU = Math.max(maxU, v.getU());
			minV = Math.min(minV, v.getV());
			maxV = Math.max(maxV, v.getV());
		}
		
		for(int i = 0; i < 4; i ++)
		{
			center[i] = new Vertex(x, y, z);
			center[i].setUV((minU + maxU) / 2, (minV + maxV) / 2);
			center[i].setBrightness(14 << 4);
		}
		
	
	}
	
	public Face splitFace()
	{
		if(vertexCount <= 1)
			return null;
		
		Vertex vertexes[] = new Vertex[16];
		Vertex faceVertexes[] = face.getVertexes();
		
		for(int i = 0; i < faceVertexes.length; i++)
		{
			Vertex v1 = faceVertexes[i];
			Vertex v2 = faceVertexes[i + 1 == 4 ? 0 : i + 1];
			vertexes[4 * i + 0] = v1;
			vertexes[4 * i + 1] = v2;
						
			int a = v1.getAlpha() + v2.getAlpha() / 2;
			int centerLight[] = { (v1.getAlpha() << 24) + v1.getColor(), (v1.getAlpha() << 24) + v2.getColor()};
			
			center[i].setAlpha(a);
			center[i].setColor(computeVertexLight(centerLight));
			
			vertexes[4 * i + 2] = center[i];
			vertexes[4 * i + 3] = center[i];
		}
		
		//vertexes = Arrays.copyOf(vertexes, 4);
		
		return new Face(vertexes, face.getParameters());
	}

}
