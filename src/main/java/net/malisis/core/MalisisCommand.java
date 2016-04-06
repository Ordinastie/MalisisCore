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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.malisis.core.configuration.Settings;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Commands handler for {@link MalisisCore} mod.
 *
 * @author Ordinastie
 *
 */
public class MalisisCommand extends CommandBase
{
	/** List of parameters available for this {@link MalisisCommand}. */
	Set<String> parameters = new HashSet<>();

	/**
	 * Instantiates the command
	 */
	public MalisisCommand()
	{
		parameters.add("config");
		parameters.add("version");
	}

	/**
	 * Gets the command name.
	 *
	 * @return the command name
	 */
	@Override
	public String getCommandName()
	{
		return "malisis";
	}

	/**
	 * Gets the command usage.
	 *
	 * @param sender the sender
	 * @return the command usage
	 */
	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "malisiscore.commands.usage";
	}

	/**
	 * Processes the command.
	 *
	 * @param sender the sender
	 * @param params the params
	 */
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] params) throws CommandException
	{
		if (params.length == 0)
			throw new WrongUsageException("malisiscore.commands.usage");

		if (!parameters.contains(params[0]))
			throw new WrongUsageException("malisiscore.commands.usage");

		switch (params[0])
		{
			case "config":
				configCommand(sender, params);
				break;

			case "version":
				IMalisisMod mod = null;
				if (params.length == 1)
					mod = MalisisCore.instance;
				else
				{
					mod = MalisisCore.getMod(params[1]);
					if (mod == null)
						MalisisCore.message("malisiscore.commands.modnotfound", params[1]);
				}
				if (mod != null)
					MalisisCore.message("malisiscore.commands.modversion", mod.getName(), mod.getVersion());
				break;
			default:
				MalisisCore.message("Not yet implemented");
				break;
		}

	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender)
	{
		return true;
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] params, BlockPos pos)
	{
		if (params.length == 1)
			return getListOfStringsMatchingLastWord(params, parameters);
		else if (params.length == 2)
			return getListOfStringsMatchingLastWord(params, MalisisCore.listModId());
		else
			return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i)
	{
		return false;
	}

	/**
	 * Handles the config command.<br>
	 * Opens the configuration GUI for the {@link IMalisisMod} with the id specified as parameter, if the mod as {@link Settings} available.
	 *
	 * @param sender the sender
	 * @param params the params
	 */
	public void configCommand(ICommandSender sender, String[] params)
	{
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
		{
			MalisisCore.log.warn("Can't open configuration GUI on a dedicated server.");
			return;
		}

		IMalisisMod mod = null;
		if (params.length == 1)
			mod = MalisisCore.instance;
		else
		{
			mod = MalisisCore.getMod(params[1]);
			if (mod == null)
				MalisisCore.message("malisiscore.commands.modnotfound", params[1]);
		}
		if (mod != null)
		{
			if (!MalisisCore.openConfigurationGui(mod))
				MalisisCore.message("malisiscore.commands.noconfiguration", mod.getName());
		}
	}
}
