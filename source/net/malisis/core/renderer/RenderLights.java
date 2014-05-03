package net.malisis.core.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.light.ColoredLight;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.world.ChunkPosition;

import org.lwjgl.opengl.GL11;

public class RenderLights extends BaseRenderer
{
	private static RenderLights instance = new RenderLights();
	private int glList = -1;
	
	private static int X = 0;
	private static int Y = 1;
	private static int Z = 2;
	
	public double[] playerPosition;
	
	private boolean needsUpdate = true;
	
	private RenderLights()
	{
		glList = GL11.glGenLists(1);
	}
	
	public int render()
	{
		if(!needsUpdate)
			return glList;
		
		world = Minecraft.getMinecraft().theWorld;
		
		ColoredLight[] lights = ColoredLight.getLights();
		HashMap<ChunkPosition, ArrayList<ColoredLight>> affectedBlocks = getAffectedBlocks(lights);
		
		renderLights(affectedBlocks);
	
		needsUpdate = false;
		
		return glList;
	}

	private void setPlayerPosition(double x, double y, double z)
	{
		playerPosition = new double[] { x, y, z };
	}
	
	public HashMap<ChunkPosition, ArrayList<ColoredLight>> getAffectedBlocks(ColoredLight[] lights)
	{
		HashMap<ChunkPosition, ArrayList<ColoredLight>> blocks = new HashMap<ChunkPosition, ArrayList<ColoredLight>>();
		ArrayList<ColoredLight> list;
		ChunkPosition cp;
		for (ColoredLight l : lights)
		{
			for (int i = (int) l.x - l.strength; i < (int) l.x + l.strength + 1; i++)
			{
				for (int j = (int) l.y - l.strength; j < (int) l.y + l.strength + 1; j++)
				{
					for (int k = (int) l.z - l.strength; k < (int) l.z + l.strength + 1; k++)
					{
						if(!world.getBlock(i, j, k).isAir(world, i, j, k))
						{
							cp = new ChunkPosition(i, j, k);
							list = blocks.get(cp);
							if (list == null)
								list = new ArrayList<ColoredLight>();
							list.add(l);
							blocks.put(cp, list);
						}
					}
				}
			}
		}
		return blocks;
	}

	private void initGL()
	{
	//	GL11.glDepthMask(false);
		//GL11.glDisable(GL11.GL_DEPTH_TEST);
	//	GL11.glEnable(GL11.GL_BLEND);
	//	OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				
	//	GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glNewList(glList, GL11.GL_COMPILE);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
	//	GL11.glLineWidth(2.0F);
	//	GL11.glPushMatrix();
	//	GL11.glTranslated(-playerPosition[X], -playerPosition[Y], -playerPosition[Z]);
	}
	
	private void endGL()
	{
	//	GL11.glDepthMask(true);
	//	GL11.glEnable(GL11.GL_DEPTH_TEST);
	//	GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glPopMatrix();
		GL11.glEndList();		
	}
	
	public void renderLights(HashMap<ChunkPosition, ArrayList<ColoredLight>> affectedBlocks)
	{
		initGL();
		t.startDrawingQuads();
		
		
		RenderParameters rp = RenderParameters.setDefault();
			
		for (Entry<ChunkPosition, ArrayList<ColoredLight>> entry : affectedBlocks.entrySet())
		{
			ChunkPosition cp= entry.getKey();
			int color = entry.getValue().remove(0).color;		
			for(ColoredLight l : entry.getValue())
				color = ColoredLight.mergeAdd(color, l.color);
			
			block = world.getBlock(cp.chunkPosX, cp.chunkPosY, cp.chunkPosZ);
			set(cp.chunkPosX, cp.chunkPosY, cp.chunkPosZ);
			
			prepare(TYPE_WORLD, MODE_LINES);
			rp.colorMultiplier = color;
			drawShape(ShapePreset.Cube(), rp);
			clean();
			
		}
		t.draw();
		endGL();
	}


	public static RenderLights instance()
	{
		if(instance == null)
			instance = new RenderLights();
		return instance;
	}
	
	public static void render(RenderGlobal renderer, RenderList[] renderlist, double x, double y, double z)
	{
		instance.setPlayerPosition(x, y, z);
		renderlist[3].addGLRenderList(instance.render());		
	}
}
