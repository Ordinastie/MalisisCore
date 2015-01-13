/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import net.malisis.core.configuration.ConfigurationGui;
import net.malisis.core.configuration.Settings;
import net.malisis.core.packet.NetworkHandler;
import net.malisis.core.tileentity.MultiBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.client.FMLFileResourcePack;
import cpw.mods.fml.client.FMLFolderResourcePack;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

// TODO: Auto-generated Javadoc
/**
 * The Class MalisisCore.
 */
public class MalisisCore extends DummyModContainer implements IMalisisMod
{
	/** Mod ID. */
	public static final String modid = "malisiscore";

	/** Mod name. */
	public static final String modname = "Malisis Core";

	/** Current version. */
	public static final String version = MalisisCore.class.getPackage().getImplementationVersion() != null ? MalisisCore.class.getPackage()
			.getImplementationVersion() : "UNKNOWN";

	/** Url for the mod. */
	public static final String url = "";

	/** Path for the mod. */
	public static File coremodLocation;

	/** Reference to the mod instance */
	public static MalisisCore instance;

	/** Logger for the mod. */
	public static Logger log;

	/** List of {@link IMalisisMod} registered. */
	private HashMap<String, IMalisisMod> registeredMods = new HashMap<>();

	/** Whether the mod is currently running in obfuscated environment or not. */
	public static boolean isObfEnv = false;

	/** List of original {@link Block} being replaced. The key is the replacement, the value is the Vanilla {@code Block}. */
	private HashMap<Block, Block> originalBlocks = new HashMap<>();

	/** List of original {@link Item} being replaced. The key is the replacement, the value is the Vanilla {@code Item}. */
	private HashMap<Item, Item> originalItems = new HashMap<>();

	/** Whether the configuration Gui should be kept opened */
	private boolean keepConfigurationGuiOpen;

	/**
	 * Instantiates MalisisCore.
	 */
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
	public String getModId()
	{
		return modid;
	}

	@Override
	public String getName()
	{
		return modname;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public Settings getSettings()
	{
		return null;
	}

	/**
	 * Registers a {@link IMalisisMod} mod.
	 *
	 * @param mod the mod to register
	 */
	public static void registerMod(IMalisisMod mod)
	{
		instance.registeredMods.put(mod.getModId(), mod);
	}

	/**
	 * Gets the a registered {@link IMalisisMod} by his id.
	 *
	 * @param id the id of the mod
	 * @return the mod registered, null if no mod with the specified id is found
	 */
	public static IMalisisMod getMod(String id)
	{
		return instance.registeredMods.get(id);
	}

	/**
	 * Gets a list of registered {@link IMalisisMod} ids.
	 *
	 * @return set of ids.
	 */
	public static Set<String> listModId()
	{
		return instance.registeredMods.keySet();
	}

	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}

	/**
	 * Pre-initialization event
	 *
	 * @param event the event
	 */
	@Subscribe
	public static void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(instance);
		log = event.getModLog();

		GameRegistry.registerTileEntity(MultiBlockTileEntity.class, "MalisisCoreMultiBlockTileEntity");
	}

	/**
	 * Initialization event
	 *
	 * @param event the event
	 */
	@Subscribe
	public static void init(FMLInitializationEvent event)
	{
		NetworkHandler.init(modid);
		ClientCommandHandler.instance.registerCommand(new MalisisCommand());
	}

	/**
	 * Texture stitch event.<br>
	 * Used to register the icons of the replaced vanilla blocks since they're not in the registry anymore and won't be called to register
	 * their icons.
	 *
	 * @param event the event
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		if (event.map.getTextureType() == 1)
			return;

		for (Entry<Block, Block> entry : originalBlocks.entrySet())
		{
			Block block = entry.getValue();
			block.registerBlockIcons(event.map);
		}
	}

	/**
	 * Gui close event.<br>
	 * Used to cancel the closing of the configuration GUI when opened from command line.
	 *
	 * @param event the event
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiClose(GuiOpenEvent event)
	{
		if (!keepConfigurationGuiOpen || event.gui != null)
			return;

		keepConfigurationGuiOpen = false;
		event.setCanceled(true);
	}

	/**
	 * Open the configuration GUI for the {@link IMalisisMod}.
	 *
	 * @param mod the mod to open the GUI for
	 * @return true, if a the mod had {@link Settings} and the GUI was opened, false otherwise
	 */
	@SideOnly(Side.CLIENT)
	public static boolean openConfigurationGui(IMalisisMod mod)
	{
		Settings settings = mod.getSettings();
		if (settings == null)
			return false;

		instance.keepConfigurationGuiOpen = true;
		(new ConfigurationGui(settings)).display();

		return true;
	}

	/**
	 * Replaces vanilla block with another one.<br>
	 * Changes the registry by removing the vanilla block and adding the replacement.
	 *
	 * @param id the id
	 * @param name the name
	 * @param srgFieldName the srg field name
	 * @param replacement the block
	 * @param vanilla the vanilla
	 */
	public static void replaceVanillaBlock(int id, String name, String srgFieldName, Block replacement, Block vanilla)
	{
		try
		{
			ItemBlock ib = (ItemBlock) Item.getItemFromBlock(vanilla);

			// add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Block.blockRegistry, id, "minecraft:" + name, replacement);

			// modify reference in Blocks class
			Field f = ReflectionHelper.findField(Blocks.class, isObfEnv ? srgFieldName : name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, replacement);

			if (ib != null)
			{
				f = ReflectionHelper.findField(ItemBlock.class, "field_150939_a");
				modifiers = Field.class.getDeclaredField("modifiers");
				modifiers.setAccessible(true);
				modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
				f.set(ib, replacement);
			}

			instance.originalBlocks.put(replacement, vanilla);

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	public static void replaceVanillaItem(int id, String name, String srgFieldName, Item replacement, Item vanilla)
	{
		try
		{
			// add block to registry
			Class[] types = { Integer.TYPE, String.class, Object.class };
			Method method = ReflectionHelper.findMethod(FMLControlledNamespacedRegistry.class, (FMLControlledNamespacedRegistry) null,
					new String[] { "addObjectRaw" }, types);
			method.invoke(Item.itemRegistry, id, "minecraft:" + name, replacement);

			// modify reference in Item class
			Field f = ReflectionHelper.findField(Items.class, isObfEnv ? srgFieldName : name);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			f.set(null, replacement);

			instance.originalItems.put(replacement, vanilla);

		}
		catch (ReflectiveOperationException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Gets the original/vanilla block for the specified one.
	 *
	 * @param block the block
	 * @return the block
	 */
	public static Block orignalBlock(Block block)
	{
		return instance.originalBlocks.get(block);
	}

	/**
	 * Gets the orginal/vanilla item for the specified one.
	 *
	 * @param item the item
	 * @return the item
	 */
	public static Item originalItem(Item item)
	{
		return instance.originalItems.get(item);
	}

	/**
	 * Displays a text in the chat.
	 *
	 * @param text the text
	 */
	public static void message(Object text)
	{
		message(text, (Object) null);
	}

	/**
	 * Displays a text in the chat.<br>
	 * Client side calls will display italic and grey text.<br>
	 * Server side calls will display white text. The text will be sent to all clients connected.
	 *
	 * @param text the text
	 * @param data the data
	 */
	public static void message(Object text, Object... data)
	{
		if (text == null)
			return;

		String txt = text.toString();
		if (text instanceof Object[])
			txt = Arrays.deepToString((Object[]) text);
		ChatComponentText msg = new ChatComponentText(StatCollector.translateToLocalFormatted(txt, data));
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			MinecraftServer server = MinecraftServer.getServer();

			if (server != null)
				server.getConfigurationManager().sendChatMsg(msg);
		}
		else
		{
			ChatStyle cs = new ChatStyle();
			cs.setItalic(true);
			cs.setColor(EnumChatFormatting.GRAY);
			msg.setChatStyle(cs);
			Minecraft.getMinecraft().thePlayer.addChatMessage(msg);
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
