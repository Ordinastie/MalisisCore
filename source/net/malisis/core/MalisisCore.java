package net.malisis.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.demo.minty.Minty;
import net.malisis.core.demo.stargate.Stargate;
import net.malisis.core.demo.test.Test;
import net.malisis.core.packet.NetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class MalisisCore extends DummyModContainer
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = "1.7.2-0.7.3";
	public static final String url = "";
	public static File coremodLocation;

	public static MalisisCore instance;
	public static Logger log;
	public static Configuration config;

	// demos
	private static boolean demosEnabled = false;
	public static Test test;
	public static Minty minty;
	public static Stargate stargate;

	public static boolean isObfEnv = false;

	private HashMap<Block, Block> originals = new HashMap<>();

	public MalisisCore()
	{
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = modid;
		meta.name = modname;
		meta.version = version;
		meta.authorList = Arrays.asList("Ordinastie", "PaleoCrafter");
		meta.url = "http://github.com/Ordinastie/MalisisCore";
		meta.logoFile = "malisiscore.png";
		meta.description = "API rendering and ASM transformations.";

		instance = this;
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
		MinecraftForge.EVENT_BUS.register(instance);

		log = event.getModLog();

		config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		demosEnabled = config.get(Configuration.CATEGORY_GENERAL, "demosEnabled", false).getBoolean(false);
		config.save();

		// demosEnabled &= FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT;
		// demosEnabled = false;
		if (demosEnabled)
		{
			test = new Test();
			minty = new Minty();
			stargate = new Stargate();

			test.preInit();
			minty.preInit();
			stargate.preInit();
		}

	}

	@Subscribe
	public static void init(FMLInitializationEvent event)
	{
		NetworkHandler.init(modid);

		if (demosEnabled)
		{
			test.init();
			minty.init();
			// stargate.init();
		}
	}

	@Subscribe
	public void serverStart(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new MalisisCommand());
	}

	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		if (event.map.getTextureType() == 1)
			return;

		for (Entry<Block, Block> entry : originals.entrySet())
		{
			Block block = entry.getValue();
			block.registerBlockIcons(event.map);
		}
	}

	public static boolean toggleDemos()
	{
		demosEnabled = !demosEnabled;
		config.get(Configuration.CATEGORY_GENERAL, "demosEnabled", false).set(demosEnabled);
		config.save();
		return demosEnabled;
	}

	public static void replaceVanillaBlock(int id, String name, String srgFieldName, Block block, Block vanilla)
	{
		try
		{
			ItemBlock ib = (ItemBlock) Item.getItemFromBlock(vanilla);

			// add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Block.blockRegistry, id, "minecraft:" + name, block);

			// modify reference in Blocks class
			Field f = ReflectionHelper.findField(Blocks.class, isObfEnv ? srgFieldName : name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, block);

			if (ib != null)
			{
				f = ReflectionHelper.findField(ItemBlock.class, "field_150939_a");
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(ib, block);
			}

			instance.originals.put(block, vanilla);

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public static Block orignalBlock(Block block)
	{
		return instance.originals.get(block);
	}

	public static void message(Object text)
	{
		if (text != null)
		{
			ChatComponentText msg = new ChatComponentText(text.toString());
			MinecraftServer server = MinecraftServer.getServer();
			if (server != null)
				server.getConfigurationManager().sendChatMsg(msg);
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
