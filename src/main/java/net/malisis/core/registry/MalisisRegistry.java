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

import java.util.stream.StreamSupport;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterComponent;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.registry.ClientRegistry.BlockRendererOverride;
import net.malisis.core.registry.ClientRegistry.ItemRendererOverride;
import net.malisis.core.registry.ModEventRegistry.IFMLEventCallback;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IRenderBlockCallback;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.ISortedRenderable.SortedRenderableManager;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.Utils;
import net.malisis.core.util.clientnotif.ClientNotificationManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLStateEvent;
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
	/** {@link ClientRegistry} instance. */
	private static ModEventRegistry modEventRegistry = new ModEventRegistry();

	static
	{
		//Calls IRegisterComponent.register for all the IBlockComponent that implement the interface.
		//Fired in FMLInitializationEvent, so all the blocks should already be registered
		onInit(event -> StreamSupport.stream(Block.REGISTRY.spliterator(), false)
										.filter(IComponentProvider.class::isInstance)
										.map(IComponentProvider.class::cast)
										.forEach(p -> p.getComponents()
														.stream()
														.filter(IRegisterComponent.class::isInstance)
														.map(IRegisterComponent.class::cast)
														.forEach(comp -> comp.register(p))));
	}

	public static void onPreInit(IFMLEventCallback<FMLPreInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLPreInitializationEvent.class, callback);
	}

	public static void onInit(IFMLEventCallback<FMLInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLInitializationEvent.class, callback);
	}

	public static void onPostInit(IFMLEventCallback<FMLPostInitializationEvent> callback)
	{
		modEventRegistry.registerCallback(FMLPostInitializationEvent.class, callback);
	}

	public static void onLoadComplete(IFMLEventCallback<FMLLoadCompleteEvent> callback)
	{
		modEventRegistry.registerCallback(FMLLoadCompleteEvent.class, callback);
	}

	public static void processCallbacks(FMLStateEvent event)
	{
		modEventRegistry.processCallbacks(event);
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
					registerItemModel(item, name);
			}

			ClientNotificationManager.get().discover(block);
		}
		else if (registerable instanceof Item)
		{
			Item item = (Item) registerable;
			GameRegistry.register(item, res);
			if (MalisisCore.isClient())
				registerItemModel(item, res);
		}
	}

	//	CallbackRegistry<IRenderBlockCallback, Boolean> renderBlockCallbacks = new CallbackRegistry<>();

	public static void addRenderBlockCallback(IRenderBlockCallback callback)
	{
		//renderB
	}

	/**
	 * Registers a {@link IBlockRenderer} for the {@link Block}.
	 *
	 * @param block the block
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerBlockRenderer(Block block, IBlockRenderer renderer)
	{
		ClientRegistry.instance.registerBlockRenderer(block, renderer);
		ClientRegistry.instance.registerItemRenderer(Item.getItemFromBlock(block), renderer);
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
		ClientRegistry.instance.registerItemRenderer(item, renderer);
	}

	//TODO: move methods below to IRenderBlockCallback
	/**
	 * Gets the {@link IBlockRenderer} registered for the {@link Block}.
	 *
	 * @param block the block
	 * @return the block renderer
	 */
	@SideOnly(Side.CLIENT)
	public static IBlockRenderer getBlockRenderer(Block block)
	{
		return ClientRegistry.instance.getBlockRenderer(block);
	}

	public static boolean shouldRenderBlock(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		SortedRenderableManager.checkRenderable(world, new BlockPos(pos), state);

		IBlockRenderer renderer = getBlockRendererOverride(world, pos, state);
		if (renderer == null)
			renderer = getBlockRenderer(state.getBlock());

		return renderer != null;
	}

	/**
	 * Renders a {@link IBlockState} with a registered {@link IBlockRenderer}.
	 *
	 * @param buffer the wr
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @return true, if successful
	 */
	@SideOnly(Side.CLIENT)
	public static boolean renderBlock(VertexBuffer buffer, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		IBlockRenderer renderer = getBlockRendererOverride(world, pos, state);
		if (renderer == null)
			renderer = getBlockRenderer(state.getBlock());
		if (renderer == null)
			return false;
		return renderer.renderBlock(buffer, world, pos, state);
	}

/**
	 * Gets the {@link TextureAtlasSprite} to used for the {@link IBlockState}.<br>
	 * Called via ASM from {@link BlockModelShapes#getTexture(IBlockState))
	 *
	 * @param state the state
	 * @return the particle icon
	 */
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getParticleIcon(IBlockState state)
	{
		Icon icon = null;
		IIconProvider provider = IComponent.getComponent(IIconProvider.class, state.getBlock());
		if (provider instanceof IBlockIconProvider)
			icon = ((IBlockIconProvider) provider).getParticleIcon(state);
		else if (provider != null)
			icon = provider.getIcon();

		return icon != null ? icon : Icon.missing;
	}

	/**
	 * Gets the {@link IItemRenderer} registered for the {@link Item}.
	 *
	 * @param item the item
	 * @return the item renderer
	 */
	@SideOnly(Side.CLIENT)
	public static IItemRenderer getItemRenderer(Item item)
	{
		return ClientRegistry.instance.itemRenderers.get(item);
	}

	/**
	 * Renders the {@link ItemStack} with a registered {@link IItemRenderer}.
	 *
	 * @param itemStack the item stack
	 * @return true, if successful
	 */
	@SideOnly(Side.CLIENT)
	public static boolean renderItem(ItemStack itemStack)
	{
		if (itemStack == null)
			return false;

		IItemRenderer renderer = getItemRendererOverride(itemStack);
		if (renderer == null)
			renderer = getItemRenderer(itemStack.getItem());
		if (renderer == null)
			return false;

		renderer.renderItem(itemStack, MalisisRenderer.getPartialTick());
		return true;
	}

	/**
	 * Registers a {@link IRenderWorldLast}.
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderWorldLast(IRenderWorldLast renderer)
	{
		ClientRegistry.instance.renderWorldLastRenderers.add(renderer);
	}

	/**
	 * Unregisters a {@link IRenderWorldLast}
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void unregisterRenderWorldLast(IRenderWorldLast renderer)
	{
		ClientRegistry.instance.renderWorldLastRenderers.remove(renderer);
	}

	/**
	 * Registers a {@link DummyModel} for the {@link Item}.<br>
	 * Registered DummyModels will prevent complaints from MC about missing model definitions and will redirect method calls to the
	 * registered renderer for the item.
	 *
	 * @param item the item
	 */
	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, String name)
	{
		registerItemModel(item, Utils.getResourceLocation(name));
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, ResourceLocation rl)
	{
		DummyModel model = new DummyModel(item, rl);
		//ModelLoader.setCustomModelResourceLocation(item, 0, model.getResourceLocation());
		ModelLoader.setCustomMeshDefinition(item, stack -> model.getResourceLocation());
		ClientRegistry.instance.itemModels.add(model);
	}

	@SideOnly(Side.CLIENT)
	public static void registerBlockRendererOverride(BlockRendererOverride override)
	{
		ClientRegistry.instance.blockRendererOverrides.add(checkNotNull(override));
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemRendererOverride(ItemRendererOverride override)
	{
		ClientRegistry.instance.itemRendererOverrides.add(checkNotNull(override));
	}

	//TODO : move ?
	@SideOnly(Side.CLIENT)
	public static IBlockRenderer getBlockRendererOverride(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		for (BlockRendererOverride overrides : ClientRegistry.instance.blockRendererOverrides)
		{
			IBlockRenderer renderer = overrides.get(world, pos, state);
			if (renderer != null)
				return renderer;
		}

		return null;
	}

	//TODO : move ?
	@SideOnly(Side.CLIENT)
	public static IItemRenderer getItemRendererOverride(ItemStack itemStack)
	{
		for (ItemRendererOverride overrides : ClientRegistry.instance.itemRendererOverrides)
		{
			IItemRenderer renderer = overrides.get(itemStack);
			if (renderer != null)
				return renderer;
		}

		return null;
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