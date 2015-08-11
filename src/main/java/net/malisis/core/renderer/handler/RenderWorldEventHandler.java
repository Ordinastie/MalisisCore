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

package net.malisis.core.renderer.handler;

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.renderer.IRenderWorldLast;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Ordinastie
 *
 */
public class RenderWorldEventHandler
{
	private static RenderWorldEventHandler instance = new RenderWorldEventHandler();

	private List<IRenderWorldLast> renderers = new ArrayList<>();

	private void _register(IRenderWorldLast renderer)
	{
		if (renderers.size() == 0)
			MinecraftForge.EVENT_BUS.register(this);

		renderers.add(renderer);
	}

	private void _unregister(IRenderWorldLast renderer)
	{
		renderers.remove(renderer);
		if (renderers.size() == 0)
			MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event)
	{
		for (IRenderWorldLast renderer : renderers)
		{
			if (renderer.shouldRender(event, Minecraft.getMinecraft().theWorld))
				renderer.renderWorldLastEvent(event, Minecraft.getMinecraft().theWorld);
		}
	}

	public static void register(IRenderWorldLast renderer)
	{
		instance._register(renderer);
	}

	public static void unregister(IRenderWorldLast renderer)
	{
		instance._unregister(renderer);
	}

}
