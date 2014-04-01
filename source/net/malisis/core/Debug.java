package net.malisis.core;

import java.util.Map.Entry;

import net.malisis.core.light.ColoredLight;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.util.Point;
import net.malisis.core.util.Raytrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.ChunkPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class Debug
{
	public ColoredLight light;

	public int[] debugBlock = new int[4];
	public Point rts;
	public Point rte;
	public boolean debug = false;
	public boolean first = true;

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)
			return;
		EntityPlayer p = event.entityPlayer;
		if (p.getCurrentEquippedItem() != null)
			return;
		if (first)
		{
			first = false;
			return;
		}

		first = true;

		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			if (rts == null && rte == null)
			{
				MalisisCore.Message("Start : " + event.x + ", " + event.y + ", " + event.z);
				rts = new Point(event.x + 0.5F, event.y + 0.5F, event.z + 0.5F);

			}
			else if (rte == null)
			{
				MalisisCore.Message("End : " + event.x + ", " + event.y + ", " + event.z);
				rte = new Point(event.x + 0.5F, event.y + 0.5F, event.z + 0.5F);
			}
			else
			{
				MalisisCore.Message("Start and end cleared");
				rts = rte = null;
			}
		}
		else
		{
			debug = !debug;
			if (debug)
			{
				MalisisCore.Message("Debug face : " + event.x + ", " + event.y + ", " + event.z + " => " + event.face);
				debugBlock = new int[] { event.x, event.y, event.z, event.face };
			}
			else
			{
				MalisisCore.Message("Stop Debug face");
			}

		}

	}

	@SubscribeEvent
	public void onPostRender(RenderWorldLastEvent event)
	{
		if (debug == false && rts == null && rte == null)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
		GL11.glLineWidth(2.0F);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(false);

		renderLightRays(event);

		renderRayTraceBlocks(event);

		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);

	}

	public void renderLightRays(RenderWorldLastEvent event)
	{
		EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
		double pPos[] = { p.lastTickPosX + (p.posX - p.lastTickPosX) * (double) event.partialTicks,
				p.lastTickPosY + (p.posY - p.lastTickPosY) * (double) event.partialTicks,
				p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * (double) event.partialTicks };

		ColoredLight[] lights = ColoredLight.getLights();

		if (debug && lights.length != 0)
		{

			Face f = FacePreset.fromDirection(ForgeDirection.getOrientation(debugBlock[3]));
			for (ColoredLight l : lights)
			{
				if (l.emitting)
				{
					for (Vertex v : f.getVertexes())
					{
						// if (v.name().equals(Vertex.TopNorthEast.name()))
						{
							float[] bPos = { debugBlock[0] + v.getX(), debugBlock[1] + v.getY(), debugBlock[2] + v.getZ() };

							Point p1 = new Point(l.x, l.y, l.z);
							Point p2 = new Point(bPos[0], bPos[1], bPos[2]);

							Raytrace rt = new Raytrace(p1, p2);
							MovingObjectPosition mop = rt.trace();
							drawLine(pPos, p1, p2, (mop == null || mop.typeOfHit == MovingObjectType.MISS) ? 0x00FF00 : 0xFF0000);
						}
					}
				}
			}
		}

	}

	public void renderRayTraceBlocks(RenderWorldLastEvent event)
	{
		if (rts == null || rte == null)
			return;

		EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
		double pPos[] = { p.lastTickPosX + (p.posX - p.lastTickPosX) * (double) event.partialTicks,
				p.lastTickPosY + (p.posY - p.lastTickPosY) * (double) event.partialTicks,
				p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * (double) event.partialTicks };

		Raytrace rt = new Raytrace(rts, rte, Raytrace.Options.PASS_THROUGH | Raytrace.Options.LOG_BLOCK_PASSED);
		MovingObjectPosition mop = rt.trace();
		if (mop == null)
			drawLine(pPos, rts, rte, 0x00FF00);
		else
		{
			drawLine(pPos, rts, new Point(mop.hitVec), 0x00FF00);
			drawLine(pPos, new Point(mop.hitVec), rte, 0xFF0000);
		}

		if (rt.blockPassed != null)
			for (Entry<ChunkPosition, MovingObjectPosition> entry : rt.blockPassed.entrySet())
			{
				ChunkPosition cp = entry.getKey();
				mop = entry.getValue();
				if (cp != null)
					drawSelectionBox(pPos, cp, mop == null ? 0x000000 : 0x33FF33);
			}
	}

	public void drawSelectionBox(double[] p, ChunkPosition b, int color)
	{
		AxisAlignedBB aabb = AxisAlignedBB.getAABBPool().getAABB(b.chunkPosX, b.chunkPosY, b.chunkPosZ, b.chunkPosX + 1, b.chunkPosY + 1,
				b.chunkPosZ + 1);
		aabb = aabb.getOffsetBoundingBox(-p[0], -p[1], -p[2]);
		RenderGlobal.drawOutlinedBoundingBox(aabb, color);
	}

	public void drawLine(double[] pPos, Point start, Point end, int color)
	{
		Tessellator t = Tessellator.instance;
		t.startDrawing(1);
		t.setColorOpaque((color >> 16) & 255, (color >> 8) & 255, color & 255);
		t.addVertex(start.x - pPos[0], start.y - pPos[1], start.z - pPos[2]);
		t.addVertex(end.x - pPos[0], end.y - pPos[1], end.z - pPos[2]);
		t.draw();
	}

	public void drawLine(double[] pPos, float[] bPos, ColoredLight l)
	{
		Tessellator t = Tessellator.instance;

		t.addVertex(l.x - pPos[0], l.y - pPos[1], l.z - pPos[2]);
		t.addVertex(bPos[0] - pPos[0], bPos[1] - pPos[1], bPos[2] - pPos[2]);
	}

}
