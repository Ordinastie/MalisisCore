package net.malisis.core.asm;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import net.malisis.core.MalisisCore;

import com.google.common.base.Throwables;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({"net.malisis.core.asm."})
//@IFMLLoadingPlugin.SortingIndex(1001)
public class MalisisCorePlugin implements IFMLLoadingPlugin
{

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { MalisisCoreTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return MalisisCore.class.getName();
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		MalisisCore.isObfEnv = !((boolean) data.get("runtimeDeobfuscationEnabled"));
		
		MalisisCore.coremodLocation = (File) data.get("coremodLocation");
		if (MalisisCore.coremodLocation == null)
		{ 
			try
			{
				MalisisCore.coremodLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			}
			catch (URISyntaxException e)
			{
				System.err.println("Failed to acquire source location for ControlPack!");
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
