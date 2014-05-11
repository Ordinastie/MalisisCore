package net.malisis.core;

import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.light.ColoredLight;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChunkCoordinates;

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
				case "light":
					lightCommand(sender, params);
					break;
				case "render":
					renderCommand(sender, params);
					break;
                case "gui":
                    Minecraft.getMinecraft().displayGuiScreen(new UIWindow(100, 100).createScreenProxy());
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

	private void lightCommand(ICommandSender sender, String[] params)
	{
		if (params.length <= 1)
		{
			MalisisCore.Message("Not enough parameters");
			return;
		}

		switch (params[1])
		{
			case "clear":
				ColoredLight.clear();
				updateLights(0, 0, 0, 16);
				MalisisCore.Message("Lights cleared");
				return;
			case "default":
				new ColoredLight("red", -5, 5, 0, 0xFF0000, 16).on();
				new ColoredLight("green", 5, 5, 0, 0x00FF00, 16).on();
				new ColoredLight("blue", 0, 5, 5, 0x0000FF, 16).on();
				updateLights(0, 0, 0, 16);
				MalisisCore.Message("Lights added");
				return;
			default:
				ColoredLight l = ColoredLight.getLight(params[1]);
				if (l != null)
				{
					switch (params[2])
					{
						case "move":
							ChunkCoordinates c = sender.getPlayerCoordinates();
							l.x = c.posX + 0.5F;
							l.y = c.posY + 0.5F;
							l.z = c.posZ + 0.5F;
							MalisisCore.Message(params[1] + " light set at " + l.x + ", " + l.y + ", " + l.z);
							break;
						case "off":
							l.off();
							MalisisCore.Message("Lights " + params[1] + " set to off");
							break;
						case "on":
							l.on();
							MalisisCore.Message("Lights " + params[1] + " set to on");
							break;

					}
				}
				else if (params[1].equals("add") && params[3] != null)
				{
					int color = Integer.parseInt(params[3], 16);
					float x, y, z;
					if (params.length == 7 && params[4] != null && params[5] != null && params[6] != null)
					{
						x = Float.parseFloat(params[4]);
						y = Float.parseFloat(params[5]);
						z = Float.parseFloat(params[6]);
					}
					else
					{
						ChunkCoordinates c = sender.getPlayerCoordinates();
						x = c.posX + 0.5F;
						y = c.posY + 0.5F;
						z = c.posZ + 0.5F;
					}

					l = new ColoredLight(params[2], x, y, z, color, 16);
					l.on();
					MalisisCore.Message("Lights added " + l);
				}

				if (l != null)
				{
					updateLights((int) l.x, (int) l.y, (int) l.z, 16);
				}
		}

	}

	private void updateLights(int x, int y, int z, int range)
	{
		Minecraft.getMinecraft().theWorld.markBlockRangeForRenderUpdate(x - range, y - range, z - range, x + range, y + range, z + range);

	}
}
