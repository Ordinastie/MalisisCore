/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Ordinastie
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

package net.malisis.core.client.gui;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.icon.GuiIcon;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.renderer.texture.Stitcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class GuiAtlas
{
	Map<ResourceLocation, GuiIcon> registeredIcons = Maps.newHashMap();
	private Set<ResourceLocation> loadedIcons = Sets.newHashSet();
	private int glTextureId = -1;

	GuiIcon register(ResourceLocation resourceLocation)
	{
		//if (registeredIcons.get(resourceLocation) != null)
		return registeredIcons.get(resourceLocation);

		//GuiIcon icon = new GuiIcon(resourceLocation);
		//registeredIcons.put(resourceLocation, icon);
		//return icon;

	}

	public void loadTextureAtlas(IResourceManager resourceManager)
	{
		int maxSize = Minecraft.getGLMaximumTextureSize();
		Stitcher stitcher = new Stitcher(maxSize, maxSize, 0, 0);
		loadedIcons.clear();

		for (Entry<ResourceLocation, GuiIcon> entry : registeredIcons.entrySet())
		{
			ResourceLocation rl = entry.getKey();
			Icon icon = entry.getValue();

			//Keep custom loading ?
			//if (icon.hasCustomLoader(resourceManager, rl) && icon.load(resourceManager, rl, l -> registeredIcons.get(l)))
			//	continue;

			try (IResource res = resourceManager.getResource(rl))
			{
				PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(resourceManager.getResource(rl));
				icon.loadSprite(pngsizeinfo, res.getMetadata("animation") != null);
			}
			catch (RuntimeException | IOException e)
			{
				MalisisCore.log.error("Failed to load texture for gui atlas : {}", rl, e);
			}

			stitcher.addSprite(icon);
		}

		try
		{
			stitcher.doStitch();
		}
		catch (StitcherException e)
		{
			throw e;
		}

		MalisisCore.log.info("Created: {}x{} atlas for GUIs", stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
		TextureUtil.allocateTextureImpl(this.getGlTextureId(), 0, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
		//Map<ResourceLocation, TextureAtlasSprite> map = Maps.newHashMap(this.registeredIcons);

		for (TextureAtlasSprite icon : stitcher.getStichSlots())
		{
			try
			{
				TextureUtil.uploadTextureMipmap(icon.getFrameTextureData(0),
												icon.getIconWidth(),
												icon.getIconHeight(),
												icon.getOriginX(),
												icon.getOriginY(),
												false,
												false);
			}
			catch (Exception e)
			{
				MalisisCore.log.error("Failed to upload icon to texture : {}.", icon.getIconName(), e);
			}

			//Keep animations ?
			//			if (icon.hasAnimationMetadata())
			//				this.listAnimatedSprites.add(icon);
		}
	}

	public int getGlTextureId()
	{
		if (this.glTextureId == -1)
		{
			this.glTextureId = TextureUtil.glGenTextures();
		}

		return this.glTextureId;
	}

	public void deleteGlTexture()
	{
		if (this.glTextureId != -1)
		{
			TextureUtil.deleteTexture(this.glTextureId);
			this.glTextureId = -1;
		}
	}
}
