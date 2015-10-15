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

package net.malisis.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IMalisisRendered;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
	/** Unique instance of {@link MalisisRegistry}. */
	private static MalisisRegistry instance = new MalisisRegistry();
	/** List of registered {@link IBlockRenderer}. */
	private Map<Block, IBlockRenderer> blockRenderers = new HashMap<>();
	/** List of registered {@link IItemRenderer} */
	private Map<Item, IItemRenderer> itemRenderers = new HashMap<>();
	/** List of registered {@link IRenderWorldLast} */
	private List<IRenderWorldLast> renderWorldLastRenderers = new ArrayList<>();
	/** List of registered {@link IIconProvider} */
	private Set<IIconProvider> iconProviders = new HashSet<>();
	/** List of {@link DummyModel} for registered items */
	private Set<DummyModel> itemModels = new HashSet<>();
	/** Empty {@link IStateMapper} **/
	private static final IStateMapper emptyMapper = new IStateMapper()
	{
		@Override
		public Map putStateModelLocations(Block block)
		{
			return ImmutableMap.of();
		}
	};

	private MalisisRegistry()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Registers a {@link IRegisterable}.<br>
	 * The object has to be either a {@link Block} or an {@link Item}.
	 *
	 * @param registerable the registerable
	 */
	public static void register(IRegisterable registerable)
	{
		String name = registerable.getRegistryName();
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException("No name specified for registration for " + registerable.getClass().getName());
		if (!(registerable instanceof Block || registerable instanceof Item))
			throw new IllegalArgumentException("Cannot register " + registerable.getClass().getName() + " (" + name
					+ ") because it's neither a block or an item.");

		if (registerable instanceof Block)
		{
			Block block = (Block) registerable;
			GameRegistry.registerBlock(block, registerable.getItemClass(), name);

			if (MalisisCore.isClient())
			{
				ModelLoader.setCustomStateMapper(block, emptyMapper);
				Item item = Item.getItemFromBlock(block);
				if (item != null)
					registerItemModel(item);
			}
		}
		else if (registerable instanceof Item)
		{
			Item item = (Item) registerable;
			GameRegistry.registerItem(item, name);
			if (MalisisCore.isClient())
				registerItemModel(item);
		}
	}

	//#region IBlockRenderer
	/**
	 * Registers a {@link IBlockRenderer} for the {@link Block}.
	 *
	 * @param block the block
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerBlockRenderer(IMalisisRendered block, IBlockRenderer renderer)
	{
		if (!(block instanceof Block))
		{
			MalisisCore.log.error("[MalisisRegistry] Cannot register {} as it's not a block.", block.getClass().getSimpleName());
			return;
		}

		instance.blockRenderers.put((Block) block, renderer);
		instance.itemRenderers.put(Item.getItemFromBlock((Block) block), renderer);
	}

	/**
	 * Gets the {@link IBlockRenderer} registered for the {@link Block}.
	 *
	 * @param block the block
	 * @return the block renderer
	 */
	@SideOnly(Side.CLIENT)
	public static IBlockRenderer getBlockRenderer(Block block)
	{
		return instance.blockRenderers.get(block);
	}

	/**
	 * Renders a {@link IBlockState} with a registered {@link IBlockRenderer}.
	 *
	 * @param wr the wr
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @return true, if successful
	 */
	@SideOnly(Side.CLIENT)
	public static boolean renderBlock(WorldRenderer wr, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		IBlockRenderer renderer = getBlockRenderer(state.getBlock());
		if (renderer == null)
			return false;
		return renderer.renderBlock(wr, world, pos, state);
	}

	/**
	 * Gets the {@link TextureAtlasSprite} to used for the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return the particle icon
	 */
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getParticleIcon(IBlockState state)
	{
		Block block = state.getBlock();
		MalisisIcon icon = null;
		if (block instanceof IMetaIconProvider)
		{
			IIconProvider provider = ((IMetaIconProvider) block).getIconProvider();
			if (provider instanceof IBlockIconProvider)
				icon = ((IBlockIconProvider) provider).getParticleIcon(state);
			else if (provider != null)
				icon = provider.getIcon();
		}

		return icon != null ? icon : MalisisIcon.missing;
	}

	//#end IBlockRenderer

	//#region IItemRenderer
	/**
	 * Registers a {@link IItemRenderer} for the {@link Item}.
	 *
	 * @param item the item
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerItemRenderer(IMalisisRendered item, IItemRenderer renderer)
	{
		if (!(item instanceof Item))
		{
			MalisisCore.log.error("[MalisisRegistry] Cannot register {} as it's not a block.", item.getClass().getSimpleName());
			return;
		}

		instance.itemRenderers.put((Item) item, renderer);
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
		return instance.itemRenderers.get(item);
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

		Item item = itemStack.getItem();
		IItemRenderer ir = getItemRenderer(item);
		if (ir == null)
			return false;

		ir.renderItem(itemStack, MalisisRenderer.getPartialTick());
		return true;
	}

	//#end IItemRenderer

	//#region IIconProvider

	/**
	 * Registers an {@link IIconProvider}.<br>
	 * When the texture is stitched, {@link IIconProvider#registerIcons(net.minecraft.client.renderer.texture.TextureMap)} will be called
	 * for all registered providers.
	 *
	 * @param iconProvider the icon provider
	 */
	@SideOnly(Side.CLIENT)
	public static void registerIconProvider(IIconProvider iconProvider)
	{
		if (iconProvider != null)
			instance.iconProviders.add(iconProvider);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		for (IIconProvider iconProvider : instance.iconProviders)
			iconProvider.registerIcons(event.map);
	}

	//#end IIconProvider

	//#region RenderWorldLast
	/**
	 * Registers a {@link IRenderWorldLast}.
	 *
	 * @param renderer the renderer
	 */
	public static void registerRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.add(renderer);
	}

	/**
	 * Unregisters a {@link IRenderWorldLast}
	 *
	 * @param renderer the renderer
	 */
	public static void unregisterRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.remove(renderer);
	}

	@SubscribeEvent
	public void onRenderLast(RenderWorldLastEvent event)
	{
		for (IRenderWorldLast renderer : renderWorldLastRenderers)
		{
			if (renderer.shouldRender(event, Minecraft.getMinecraft().theWorld))
				renderer.renderWorldLastEvent(event, Minecraft.getMinecraft().theWorld);
		}
	}

	//#end RenderWorldLast

	//#region ItemModels
	/**
	 * Registers a {@link DummyModel} for the {@link Item}.<br>
	 * Registered <code>DummyModels<code> will prevent complaints from MC about missing model definitions.<br>
	 *
	 * @param item the item
	 */
	private static void registerItemModel(Item item)
	{
		String modid = Loader.instance().activeModContainer().getModId();
		String name = item.getUnlocalizedName().substring(5);

		DummyModel model = new DummyModel(item, modid + ":" + name);
		ModelLoader.setCustomModelResourceLocation(item, 0, model.getResourceLocation());
		instance.itemModels.add(model);
	}

	@SubscribeEvent
	public void onModelBakeEvent(ModelBakeEvent event)
	{
		for (DummyModel model : itemModels)
			event.modelRegistry.putObject(model.getResourceLocation(), model);
	}

	//#end ItemModels
}
