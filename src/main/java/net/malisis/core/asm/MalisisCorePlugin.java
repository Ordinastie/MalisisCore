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

package net.malisis.core.asm;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;

import net.malisis.core.MalisisCore;
import net.malisis.core.util.chunkblock.ChunkBlockTransformer;
import net.malisis.core.util.chunkcollision.ChunkCollisionTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

import org.apache.logging.log4j.LogManager;

import com.google.common.base.Throwables;

@TransformerExclusions({ "net.malisis.core.asm." })
@IFMLLoadingPlugin.SortingIndex(1001)
public class MalisisCorePlugin implements IFMLLoadingPlugin
{

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { MalisisCoreTransformer.class.getName(), ChunkBlockTransformer.class.getName(),
				ChunkCollisionTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		MalisisCore.isObfEnv = (boolean) data.get("runtimeDeobfuscationEnabled");

		MalisisCore.coremodLocation = (File) data.get("coremodLocation");
		if (MalisisCore.coremodLocation == null)
		{
			try
			{
				MalisisCore.coremodLocation = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
			}
			catch (URISyntaxException e)
			{
				LogManager.getLogger("malisiscore").error("Failed to acquire source location for MalisisCore!");
				throw Throwables.propagate(e);
			}
		}
	}

	@Override
	public String getAccessTransformerClass()
	{
		return MalisisCoreAccessTransformer.class.getName();
	}

}
