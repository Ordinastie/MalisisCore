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

package net.malisis.core.registry;

import static com.google.common.base.Preconditions.*;
import static net.malisis.core.registry.Registries.*;
import net.malisis.core.MalisisCore;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.registry.ClientRegistry.BlockRendererOverride;
import net.malisis.core.registry.ClientRegistry.ItemRendererOverride;
import net.malisis.core.registry.ModEventRegistry.IFMLEventCallback;
import net.malisis.core.registry.RenderBlockRegistry.IRenderBlockCallback;
import net.malisis.core.registry.RenderBlockRegistry.IRenderBlockCallbackPredicate;
import net.malisis.core.registry.SetBlockCallbackRegistry.ISetBlockCallback;
import net.malisis.core.registry.SetBlockCallbackRegistry.ISetBlockCallbackPredicate;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.util.Utils;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.malisis.core.util.clientnotif.ClientNotificationManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

/**
 * @author Ordinastie
 *
 */
public class MalisisRegistry
{
	/**
	 * Registers a {@link IFMLEventCallback} for when {@link FMLPreInitializationEvent} is called for {@link MalisisCore}.
	 *
	 * @param callback the callback
	 */
	public static void onPreInit(IFMLEventCallback<FMLPreInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLPreInitializationEvent.class, callback);
	}

	/**
	 * Registers a {@link IFMLEventCallback} for when {@link FMLInitializationEvent} is called for {@link MalisisCore}.
	 *
	 * @param callback the callback
	 */
	public static void onInit(IFMLEventCallback<FMLInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLInitializationEvent.class, callback);
	}

	/**
	 * Registers a {@link IFMLEventCallback} for when {@link FMLPostInitializationEvent} is called for {@link MalisisCore}.
	 *
	 * @param callback the callback
	 */
	public static void onPostInit(IFMLEventCallback<FMLPostInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLPostInitializationEvent.class, callback);
	}

	/**
	 * Registers a {@link IFMLEventCallback} for when {@link FMLLoadCompleteEvent} is called for {@link MalisisCore}.
	 *
	 * @param callback the callback
	 */
	public static void onLoadComplete(IFMLEventCallback<FMLLoadCompleteEvent> callback)
	{
		modEventRegistry.registerCallback(FMLLoadCompleteEvent.class, callback);
	}

	//TODO: register TEs so we can discover the @MalisisRendered annotation
	/**
	 * Registers a {@link IRegisterable}.<br>
	 * The object has to be either a {@link Block} or an {@link Item}.
	 *
	 * @param registerable the registerable
	 */
	public static void register(IRegisterable registerable)
	{
		String name = registerable.getName();
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No name specified for registration for " + registerable.getClass().getName());
		if (!(registerable instanceof Block || registerable instanceof Item))
			throw new IllegalArgumentException("Cannot register " + registerable.getClass().getName() + " (" + name
					+ ") because it's neither a block or an item.");

		ResourceLocation res = Utils.getResourceLocation(name);
		if (registerable instanceof Block)
		{
			Block block = (Block) registerable;
			Item item = registerable.getItem(block);
			GameRegistry.register(block, res);
			if (item != null)
			{
				GameRegistry.register(item, res);
				GameData.getBlockItemMap().put(block, item);
			}

			//register the mapper for the block and the model for the item
			if (MalisisCore.isClient())
			{
				ModelLoader.setCustomStateMapper(block, b -> ImmutableMap.of());
				if (item != null)
					registerDummyModel(item, name);
			}

			ClientNotificationManager.discover(block);
		}
		else if (registerable instanceof Item)
		{
			Item item = (Item) registerable;
			GameRegistry.register(item, res);
			if (MalisisCore.isClient())
				registerDummyModel(item, res);
		}
	}

	/**
	 * Registers a {@link IRenderBlockCallback} with the specified {@link CallbackOption} to be called when rendering blocks.
	 *
	 * @param callback the callback
	 * @param option the option
	 */
	@SideOnly(Side.CLIENT)
	public static void onRenderBlock(IRenderBlockCallback callback, CallbackOption<IRenderBlockCallbackPredicate> option)
	{
		renderBlockRegistry.registerCallback(callback, option);
	}

	/**
	 * Registers a {@link ISetBlockCallback} with the specified {@link CallbackOption} to be called after a {@link Block} is placed in the
	 * world.
	 *
	 * @param callback the callback
	 * @param option the option
	 */
	public static void onPreSetBlock(ISetBlockCallback callback, CallbackOption<ISetBlockCallbackPredicate> option)
	{
		preSetBlockRegistry.registerCallback(callback, option);
	}

	/**
	 * Registers a {@link ISetBlockCallback} with the specified {@link CallbackOption} to be called before a {@link Block} is placed in the
	 * world.
	 *
	 * @param callback the callback
	 * @param option the option
	 */
	public static void onPostSetBlock(ISetBlockCallback callback, CallbackOption<ISetBlockCallbackPredicate> option)
	{
		postSetBlockRegistry.registerCallback(callback, option);
	}

	/**
	 * Registers a {@link IBlockRenderer} for the {@link Block}, and its {@link Item} if any.
	 *
	 * @param block the block
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerBlockRenderer(Block block, IBlockRenderer renderer)
	{
		clientRegistry.blockRenderers.put(checkNotNull(block), checkNotNull(renderer));
		Item item = Item.getItemFromBlock(block);
		if (item != null)
			clientRegistry.itemRenderers.put(item, renderer);
	}

	/**
	 * Registers a {@link IItemRenderer} for the {@link Item}.
	 *
	 * @param item the item
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerItemRenderer(Item item, IItemRenderer renderer)
	{
		clientRegistry.itemRenderers.put(checkNotNull(item), checkNotNull(renderer));
	}

	/**
	 * Registers a {@link IRenderWorldLast}.
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderWorldLast(IRenderWorldLast renderer)
	{
		clientRegistry.renderWorldLastRenderers.add(renderer);
	}

	/**
	 * Unregisters a {@link IRenderWorldLast}
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void unregisterRenderWorldLast(IRenderWorldLast renderer)
	{
		clientRegistry.renderWorldLastRenderers.remove(renderer);
	}

	/**
	 * Registers a {@link DummyModel} for the {@link Item}.<br>
	 * Registered {@code DummyModels} will prevent complaints from MC about missing model definitions and will redirect method calls to the
	 * registered {@link IItemRenderer} for the item.
	 *
	 * @param item the item
	 */
	@SideOnly(Side.CLIENT)
	public static void registerDummyModel(Item item, String name)
	{
		registerDummyModel(item, Utils.getResourceLocation(name));
	}

	/**
	 * Registers a {@link DummyModel} for the {@link Item}.<br>
	 * Registered {@code DummyModels} will prevent complaints from MC about missing model definitions and will redirect method calls to the
	 * registered {@link IItemRenderer} for the item.
	 *
	 * @param item the item
	 * @param rl the rl
	 */
	@SideOnly(Side.CLIENT)
	public static void registerDummyModel(Item item, ResourceLocation rl)
	{
		DummyModel model = new DummyModel(item, rl);
		//ModelLoader.setCustomModelResourceLocation(item, 0, model.getResourceLocation());
		ModelLoader.setCustomMeshDefinition(item, stack -> model.getResourceLocation());
		clientRegistry.itemModels.add(model);
	}

	@SideOnly(Side.CLIENT)
	public static void registerBlockRendererOverride(BlockRendererOverride override)
	{
		clientRegistry.blockRendererOverrides.add(checkNotNull(override));
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemRendererOverride(ItemRendererOverride override)
	{
		clientRegistry.itemRendererOverrides.add(checkNotNull(override));
	}

	/**
	 * Registers a new {@link SoundEvent}.
	 *
	 * @param modId the mod id
	 * @param soundId the sound id
	 * @return the sound event
	 */
	public static SoundEvent registerSound(String modId, String soundId)
	{
		ResourceLocation rl = new ResourceLocation(modId, soundId);
		SoundEvent sound = new SoundEvent(rl);
		GameRegistry.register(sound, rl);
		return sound;
	}
}