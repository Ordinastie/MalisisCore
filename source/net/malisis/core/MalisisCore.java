package net.malisis.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import net.malisis.core.minty.Minty;
import net.malisis.core.test.Test;
import net.minecraft.block.Block;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;

public class MalisisCore extends DummyModContainer
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = "1.7.2-0.7";
	public static final String url = "http://github.com/Ordinastie/MalisisCore";
	public static File coremodLocation;

	public static MalisisCore instance;
	public static Debug debug;
	public static Test test;
	public static Minty minty;

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
		test = new Test();
		minty = new Minty();
		// debug = new Debug();
		//System.err.println("MalisisCore enabled!");
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public static void preInit(FMLPreInitializationEvent event)
	{
		test.preInit();
		minty.preInit();
		if (event.getSide() == Side.CLIENT)
		{
			// FMLCommonHandler.instance().bus().register(new VanillaBlockRenderer());
			if (debug != null)
				MinecraftForge.EVENT_BUS.register(debug);
		}
	}

	@Subscribe
	public void serverStart(FMLServerStartingEvent event)
	{
		MinecraftServer server = MinecraftServer.getServer();
		((ServerCommandManager) server.getCommandManager()).registerCommand(new MalisisCommand());
	}

	public static void replaceVanillaBlock(int id, String name, Block block, Block vanilla)
	{
		try
		{
			ItemBlock ib = (ItemBlock) Item.getItemFromBlock(vanilla);
			
			//add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Block.blockRegistry, id, name, block);

			//modify reference in Blocks class
			Field f = ReflectionHelper.findField(Blocks.class, name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, block);
			
			if(ib != null)
			{
				f = ReflectionHelper.findField(ItemBlock.class, "field_150939_a");
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(ib, block);
			}

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
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
	
	
	@Override
	public File getSource()
	{
		return coremodLocation;
	}

	@Override
	public Class<?> getCustomResourcePackClass()
	{
		return coremodLocation.isDirectory() ? FMLFolderResourcePack.class : FMLFileResourcePack.class;
	}
}
