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

public class MalisisCore extends DummyModContainer implements IMalisisMod
{
	public static final String modid = "malisiscore";
	public static final String modname = "Malisis Core";
	public static final String version = MalisisCore.class.getPackage().getImplementationVersion();
	public static final String url = "";
	public static File coremodLocation;

	public static MalisisCore instance;
	public static Logger log;

	private HashMap<String, IMalisisMod> registeredMods = new HashMap<>();

	public static boolean isObfEnv = false;

	private HashMap<Block, Block> originals = new HashMap<>();
	private boolean keepConfigurationGuiOpen;

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

	public static void registerMod(IMalisisMod mod)
	{
		instance.registeredMods.put(mod.getModId(), mod);
	}

	public static IMalisisMod getMod(String id)
	{
		return instance.registeredMods.get(id);
	}

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

	@Subscribe
	public static void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(instance);
		log = event.getModLog();

		GameRegistry.registerTileEntity(MultiBlockTileEntity.class, "MalisisCoreMultiBlockTileEntity");
	}

	@Subscribe
	public static void init(FMLInitializationEvent event)
	{
		NetworkHandler.init(modid);
		ClientCommandHandler.instance.registerCommand(new MalisisCommand());
	}

	@SideOnly(Side.CLIENT)
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

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiClose(GuiOpenEvent event)
	{
		if (!keepConfigurationGuiOpen || event.gui != null)
			return;

		keepConfigurationGuiOpen = false;
		event.setCanceled(true);
	}

	@SideOnly(Side.CLIENT)
	public static boolean openConfigurationGui(IMalisisMod mod, boolean keepOpen)
	{
		Settings settings = mod.getSettings();
		if (settings == null)
			return false;

		instance.keepConfigurationGuiOpen = keepOpen;
		(new ConfigurationGui(settings)).display();

		return true;
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
