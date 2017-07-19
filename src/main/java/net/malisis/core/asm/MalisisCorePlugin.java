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

import java.util.Map;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IEnvironmentTokenProvider;

@TransformerExclusions({ "net.malisis.core.asm." })
@IFMLLoadingPlugin.SortingIndex(1001)
public class MalisisCorePlugin implements IFMLLoadingPlugin
{

	public MalisisCorePlugin()
	{
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.malisiscore.core.json");
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] {	"net.malisis.core.util.chunkcollision.ChunkCollisionTransformer",
								"net.malisis.core.util.chunkblock.ChunkBlockTransformer",
								"net.malisis.core.renderer.transformer.MalisisRendererTransformer",
								"net.malisis.core.renderer.icon.asm.TextureMapTransformer",
								"net.malisis.core.util.clientnotif.ClientNotifTransformer", };
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
	{}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
