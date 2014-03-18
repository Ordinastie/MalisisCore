package net.malisis.core;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkMod;


@Mod(modid = MalisisCore.modid, name = MalisisCore.modname, version = MalisisCore.version)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MalisisCore
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = "0.01";

	public static MalisisCore instance;

	public MalisisCore()
	{
		instance = this;
	}

	public static void Message(Object text)
	{
		ChatMessageComponent msg = new ChatMessageComponent().addText(text.toString());
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msg);
	}
	
	public static String Block(World world, int x, int y, int z)
	{
		int id = world.getBlockId(x, y, z);
		Block b = Block.blocksList[id];
		return b.getUnlocalizedName() + " (" + id + "/" + b.blockID + ")";
	}

}
