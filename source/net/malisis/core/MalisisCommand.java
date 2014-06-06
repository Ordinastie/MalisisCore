package net.malisis.core;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class MalisisCommand extends CommandBase
{

	@Override
	public String getCommandName()
	{
		return "malisis";
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return null;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params)
	{
		if (params[0] != null)
		{
			switch (params[0])
			{
				case "demos":
					MalisisCore.message("Demos will be " +  (MalisisCore.toggleDemos() ? "activated" : "deactivated") + " for the next launch."); 
					break;
					
				case "gui":
					//Minecraft.getMinecraft().displayGuiScreen(new UIWindow(100, 100).createScreenProxy());
					break;
					
				default:
					helpCommand(sender);
					break;
			}

		}

	}

	public void helpCommand(ICommandSender sender)
	{

	}

	public void renderCommand(ICommandSender sender, String[] params)
	{

	}

}
