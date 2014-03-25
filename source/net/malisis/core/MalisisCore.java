package net.malisis.core;

import net.malisis.core.renderer.VanillaBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = MalisisCore.modid, name = MalisisCore.modname, version = MalisisCore.version)
public class MalisisCore
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = "1.7.2-0.6";

	public static MalisisCore instance;
	public ColoredLight light;


	public MalisisCore()
	{
		instance = this;

	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		if (event.getSide() == Side.CLIENT)
		{
			FMLCommonHandler.instance().bus().register(new VanillaBlockRenderer());
		//	MinecraftForge.EVENT_BUS.register(MalisisCore.instance);
		}
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		((ServerCommandManager) server.getCommandManager()).registerCommand(new MalisisCommand());
	}

	/**
	 * Draws a line from the lights to selected block (for debugging purpose) 
	 * @param event
	 */
	@SubscribeEvent
	public void onDrawBlockHighlight(DrawBlockHighlightEvent event)
	{
		if (event.target.typeOfHit == MovingObjectType.BLOCK)
		{
			MovingObjectPosition mop = event.target;
			WorldClient world = Minecraft.getMinecraft().theWorld;
			Block b = world.getBlock(mop.blockX, mop.blockY, mop.blockZ);

			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
			GL11.glLineWidth(2.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glDepthMask(false);
			float f1 = 0.002F;

			double d0 = event.player.lastTickPosX + (event.player.posX - event.player.lastTickPosX) * (double) event.partialTicks;
			double d1 = event.player.lastTickPosY + (event.player.posY - event.player.lastTickPosY) * (double) event.partialTicks;
			double d2 = event.player.lastTickPosZ + (event.player.posZ - event.player.lastTickPosZ) * (double) event.partialTicks;

			ColoredLight[] lights = ColoredLight.getLights();
			Tessellator t = Tessellator.instance;

			t.startDrawing(1);
			for (ColoredLight l : lights)
			{
				t.setColorOpaque(l.red(), l.green(), l.blue());
				t.addVertex(mop.hitVec.xCoord - d0, mop.hitVec.yCoord - d1, mop.hitVec.zCoord - d2);
				t.addVertex(l.x - d0, l.y - d1, l.z - d2);
			}
			t.draw();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glDisable(GL11.GL_BLEND);
		}
	}

	public static void Message(Object text)
	{
		if (text != null)
		{
			ChatComponentText msg = new ChatComponentText(text.toString());
			MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msg);
		}
	}
}
