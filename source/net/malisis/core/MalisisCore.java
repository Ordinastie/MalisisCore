package net.malisis.core;

import java.util.Arrays;

import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.eventbus.EventBus;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;

public class MalisisCore extends DummyModContainer
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = "1.7.2-0.7";
	public static final String url = "http://github.com/Ordinastie/MalisisCore";

	public static MalisisCore instance;
	public static Debug debug;
	
	public static boolean isObfEnv = false;

	public MalisisCore()
	{
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = modname;
		meta.version = version;
		meta.authorList = Arrays.asList("Ordinastie");
		meta.url = "";
		meta.description = "Add hooks";

		instance = this;
		// debug = new Debug();
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		if (event.getSide() == Side.CLIENT)
		{
		//	FMLCommonHandler.instance().bus().register(new VanillaBlockRenderer());
			if (debug != null)
				MinecraftForge.EVENT_BUS.register(debug);
		}
	}

	@EventHandler
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		((ServerCommandManager) server.getCommandManager()).registerCommand(new MalisisCommand());
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
