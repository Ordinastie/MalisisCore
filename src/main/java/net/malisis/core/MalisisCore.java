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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.configuration.ConfigurationGui;
import net.malisis.core.configuration.Settings;
import net.malisis.core.network.MalisisNetwork;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.registry.Registries;
import net.malisis.core.util.Utils;
import net.malisis.core.util.syncer.Syncer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;
import net.minecraftforge.fml.common.discovery.asm.ModAnnotation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The Class MalisisCore.
 */
@Mod(modid = MalisisCore.modid, name = MalisisCore.modname, version = MalisisCore.version, acceptedMinecraftVersions = "[1.12,1.13)")
public class MalisisCore implements IMalisisMod
{
	/** Mod ID. */
	public static final String modid = "malisiscore";
	/** Mod name. */
	public static final String modname = "Malisis Core";
	/** Current version. */
	public static final String version = "${version}";
	/** Url for the mod. */
	public static final String url = "";
	/** Reference to the mod instance */
	public static MalisisCore instance;
	/** Logger for the mod. */
	public static Logger log = LogManager.getLogger(modid);
	/** Network for the mod */
	public static MalisisNetwork network;

	/** Configs for MalisisCore. **/
	private MalisisCoreSettings settings;

	/** List of {@link IMalisisMod} registered. */
	private HashMap<String, IMalisisMod> registeredMods = new HashMap<>();

	/** Whether the mod is currently running in obfuscated environment or not. */
	public static boolean isObfEnv = false;

	/**
	 * Instantiates MalisisCore.
	 */
	public MalisisCore()
	{
		instance = this;
		network = new MalisisNetwork(this);
		isObfEnv = !(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
		registerMod(this);
	}

	//#region IMalisisMod
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
		return settings;
	}

	//#end IMalisisMod

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

	/**
	 * Checks the mod is loading on a physical client..
	 *
	 * @return true, if is client
	 */
	public static boolean isClient()
	{
		return FMLCommonHandler.instance().getSide().isClient();
	}

	/**
	 * Automatically loads (and eventually instanciate) classes with the {@link AutoLoad} annotation.
	 *
	 * @param asmDataTable the asm data table
	 */
	private void autoLoadClasses(ASMDataTable asmDataTable)
	{
		Set<ASMData> classes = ImmutableSortedSet.copyOf(	Ordering.natural().onResultOf(ASMData::getClassName),
															asmDataTable.getAll(AutoLoad.class.getName()));

		Set<ASMData> clientClasses = asmDataTable.getAll(SideOnly.class.getName());

		for (ASMData data : classes)
		{
			try
			{
				//don't load client classes on server.
				if (isClient() || !isClientClass(data.getClassName(), clientClasses))
				{
					Class<?> clazz = Class.forName(data.getClassName());
					AutoLoad anno = clazz.getAnnotation(AutoLoad.class);
					if (anno.value())
						clazz.newInstance();
				}
			}
			catch (Exception e)
			{
				MalisisCore.log.error("Could not autoload {}.", data.getClassName(), e);
			}
		}
	}

	/**
	 * Checks if is the specified class has a @SideOnly(Side.CLIENT) annotation.
	 *
	 * @param className the class name
	 * @param clientClasses the client classes
	 * @return true, if is client class
	 */
	private boolean isClientClass(String className, Set<ASMData> clientClasses)
	{
		for (ASMData data : clientClasses)
		{
			if (data.getClassName().equals(className) && data.getObjectName().equals(data.getClassName())
					&& ((ModAnnotation.EnumHolder) data.getAnnotationInfo().get("value")).getValue().equals("CLIENT"))
				return true;
		}
		return false;
	}

	/**
	 * Pre-initialization event
	 *
	 * @param event the event
	 */
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		autoLoadClasses(event.getAsmData());

		//register this to the EVENT_BUS for onGuiClose()
		MinecraftForge.EVENT_BUS.register(this);

		//TODO: migrate to @AutoLoad ?
		MalisisNetwork.createMessages(event.getAsmData());
		Syncer.get().discover(event.getAsmData());

		settings = new MalisisCoreSettings(event.getSuggestedConfigurationFile());

		Registries.processFMLStateEvent(event);
	}

	/**
	 * Initialization event
	 *
	 * @param event the event
	 */
	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if (isClient())
		{
			ClientCommandHandler.instance.registerCommand(new MalisisCommand());
		}

		Registries.processFMLStateEvent(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		Registries.processFMLStateEvent(event);
	}

	@EventHandler
	public void postInit(FMLLoadCompleteEvent event)
	{
		Registries.processFMLStateEvent(event);
	}

	/**
	 * Gui close event.<br>
	 * Used to cancel the closing of the {@link MalisisGui} when opened from command line.
	 *
	 * @param event the event
	 */
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onGuiClose(GuiOpenEvent event)
	{
		if (!MalisisGui.cancelClose || event.getGui() != null)
			return;

		MalisisGui.cancelClose = false;
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

		new ConfigurationGui(mod, settings).display(true);

		return true;
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
		String txt = text != null ? text.toString() : "null";
		if (text instanceof Object[])
			txt = Arrays.deepToString((Object[]) text);

		//on server simply log messages
		if (!MalisisCore.isClient())
		{
			log.info(String.format(txt, data));
			return;
		}

		TextComponentString msg = new TextComponentString(I18n.format(txt, data));
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			MinecraftServer server = FMLCommonHandler.instance().getSidedDelegate().getServer();

			if (server != null)
				server.getPlayerList().sendMessage(msg);
		}
		else
		{
			EntityPlayer player = Utils.getClientPlayer();
			if (player == null)
				return;
			Style cs = new Style();
			cs.setItalic(true);
			cs.setColor(TextFormatting.GRAY);
			msg.setStyle(cs);

			player.sendMessage(msg);
		}
	}
}
