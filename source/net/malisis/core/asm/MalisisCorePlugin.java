package net.malisis.core.asm;

import java.util.Map;

import net.malisis.core.MalisisCore;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

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
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
