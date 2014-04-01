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

	public Shadow(ChunkPosition block, Face face)
	{
		this.block = block;
		this.face = face;
		this.side = face.getParameters().direction;

		getFaceLights();
	}

	/**
	 * Get the lights lighting a face and apply corresponding color to it
	 */
	public void getFaceLights()
	{
		for (Vertex v : face.getVertexes())
		{
			int[] lights = getVertexLights(v);
			if (lights.length > 0)
			{
				int light = computeVertexLight(v, lights);
				v.setBrightness(14 << 4);
				v.setAlpha((int) (ColoredLight.alpha(light) * 0.6F));
				v.setColor(light & 0xFFFFFF);
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

	public int computeVertexLight(Vertex vertex, int[] lights)
	{
		int color = lights[0];
		for (int i = 1; i < lights.length; i++)
		{
			color = ColoredLight.mergeAdd(color, lights[i]);
		}

		return color;
	}

}
