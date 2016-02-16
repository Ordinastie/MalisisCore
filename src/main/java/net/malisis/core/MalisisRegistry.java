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
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IIconRegister;
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
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
public class MalisisRegistry
{
	/** Unique instance of {@link MalisisRegistry}. */
	@SideOnly(Side.CLIENT)
	private static ClientRegistry instance;
	static
	{
		if (MalisisCore.isClient())
		{
			instance = new ClientRegistry();
			MinecraftForge.EVENT_BUS.register(instance);
		}
	}

	@SideOnly(Side.CLIENT)
	private static class ClientRegistry
	{
		/** List of registered {@link IBlockRenderer}. */
		private Map<Block, IBlockRenderer> blockRenderers = new HashMap<>();
		/** List of registered {@link IItemRenderer} */
		private Map<Item, IItemRenderer> itemRenderers = new HashMap<>();
		/** List of registered {@link IRenderWorldLast} */
		private List<IRenderWorldLast> renderWorldLastRenderers = new ArrayList<>();
		/** List of registered {@link IIconProvider} */
		private Set<IIconRegister> iconRegisters = new HashSet<>();
		/** List of {@link DummyModel} for registered items */
		private Set<DummyModel> itemModels = new HashSet<>();
		private Map<Class<? extends MalisisRenderer>, MalisisRenderer> registeredRenderers = new HashMap<>();

		/** Empty {@link IStateMapper} **/
		private static final IStateMapper emptyMapper = new IStateMapper()
		{
			@Override
			public Map putStateModelLocations(Block block)
			{
				return ImmutableMap.of();
			}
		};

		/**
		 * Calls the {@link IRenderWorldLast} registered to render.<br>
		 *
		 * @param event the event
		 */
		@SubscribeEvent
		public void onRenderLast(RenderWorldLastEvent event)
		{
			for (IRenderWorldLast renderer : renderWorldLastRenderers)
			{
				if (renderer.shouldRender(event, Minecraft.getMinecraft().theWorld))
					renderer.renderWorldLastEvent(event, Minecraft.getMinecraft().theWorld);
			}
		}

		/**
		 * Registers {@link DummyModel DummyModels}.<br>
		 * {@code DummyModel} forwards transforms to the {@link IItemRenderer} for the {@link Item}.
		 *
		 * @param event the event
		 */
		@SubscribeEvent
		public void onModelBakeEvent(ModelBakeEvent event)
		{
			DefaultRenderer.item.clearModels();
			for (DummyModel model : itemModels)
				event.modelRegistry.putObject(model.getResourceLocation(), model);
		}

		/**
		 * Calls {@link IIconProvider#registerIcons(net.minecraft.client.renderer.texture.TextureMap)} for every registered
		 * {@link IIconProvider}.
		 *
		 * @param event the event
		 */
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public void onTextureStitchEvent(TextureStitchEvent.Pre event)
		{
			for (IIconRegister iconRegister : iconRegisters)
				iconRegister.registerIcons(event.map);
		}

		/**
		 * Registers the {@link IIconProvider} provided by the object, if it's a {@link IMetaIconProvider}.<br>
		 * Automatically called for blocks and items in the registries.
		 *
		 * @param object the object
		 */
		public void registerIconRegister(Object object)
		{
			if (object instanceof IMetaIconProvider)
			{
				((IMetaIconProvider) object).createIconProvider(null);
				MalisisRegistry.registerIconRegister(((IMetaIconProvider) object).getIconProvider());
			}
		}

		/**
		 * Registers a {@link MalisisRenderer} associated with the object.<br>
		 * The object class needs to have the {@link MalisisRendered} annotation.<br>
		 * Automatically called for blocks and items in the registries.
		 *
		 * @param object the object
		 */
		private void registerRenderer(Object object)
		{
			//get the classes to use to render
			Pair<Class<? extends MalisisRenderer>, Class<? extends MalisisRenderer>> rendererClasses = getRendererClasses(object);
			if (rendererClasses == null)
				return;

			//MalisisCore.log.info("[MalisisRegistry] Found annotation for {}", object.getClass().getSimpleName());
			//get the block renderer
			MalisisRenderer renderer = null;
			try
			{
				renderer = getRenderer(rendererClasses.getLeft());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				MalisisCore.log.error("[MalisisRegistry] Failed to load {} renderer for {}", rendererClasses.getLeft().getSimpleName(),
						object.getClass().getSimpleName(), e);
				return;
			}

			//register the block renderer
			if (object instanceof Block && renderer != null)
			{
				registerBlockRenderer((Block) object, renderer);
				//MalisisCore.log.info("[MalisisRegistry] Registered block {} for {}", renderer.getClass().getSimpleName(), object.getClass()
				//		.getSimpleName());
				object = Item.getItemFromBlock((Block) object);
			}

			//get the item renderer
			try
			{
				renderer = getRenderer(rendererClasses.getRight());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				MalisisCore.log.error("[MalisisRegistry] Failed to load {} renderer for {}", rendererClasses.getLeft().getSimpleName(),
						object.getClass().getSimpleName());
				return;
			}
			//register the item renderer
			if (object instanceof Item && renderer != null)
			{
				registerItemRenderer((Item) object, renderer);
				//MalisisCore.log.info("[MalisisRegistry] Registered item {} for {}", renderer.getClass().getSimpleName(), object.getClass()
				//		.getSimpleName());
			}
		}

		/**
		 * Gets the {@link MalisisRenderer} classes to use for the object.<br>
		 * the Classes are given by the {@link MalisisRendered} annotation on that object class.
		 *
		 * @param object the object
		 * @return the renderer classes
		 */
		private Pair<Class<? extends MalisisRenderer>, Class<? extends MalisisRenderer>> getRendererClasses(Object object)
		{
			Class<?> objClass = object.getClass();
			MalisisRendered annotation = objClass.getAnnotation(MalisisRendered.class);
			if (annotation == null)
				return null;

			if (annotation.value() != DefaultRenderer.Null.class)
				return Pair.of(annotation.value(), annotation.value());
			else
				return Pair.of(annotation.block(), annotation.item());
		}

		/**
		 * Gets and eventually instantiates the render {@link MalisisRenderer} class.
		 *
		 * @param clazz the class
		 * @return the renderer
		 * @throws InstantiationException the instantiation exception
		 * @throws IllegalAccessException the illegal access exception
		 */
		private MalisisRenderer getRenderer(Class<? extends MalisisRenderer> clazz) throws InstantiationException, IllegalAccessException
		{
			if (clazz == DefaultRenderer.Block.class)
				return DefaultRenderer.block;
			else if (clazz == DefaultRenderer.Block.class)
				return DefaultRenderer.item;
			else if (clazz == DefaultRenderer.Null.class)
				return DefaultRenderer.nullRender;

			MalisisRenderer renderer = registeredRenderers.get(clazz);
			if (renderer == null)
			{
				renderer = clazz.newInstance();
				registeredRenderers.put(clazz, renderer);
			}

			return renderer;
		}
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

		if (registerable instanceof Block)
		{
			Block block = (Block) registerable;
			Item item = registerable.getItem(block);
			GameRegistry.registerBlock(block, null, name);
			if (item != null)
			{
				GameRegistry.registerItem(item, name);
				GameData.getBlockItemMap().put(block, item);
			}

			if (MalisisCore.isClient())
			{
				ModelLoader.setCustomStateMapper(block, ClientRegistry.emptyMapper);
				if (item != null)
					registerItemModel(item, name);
			}
		}
		else if (registerable instanceof Item)
		{
			Item item = (Item) registerable;
			GameRegistry.registerItem(item, name);
			if (MalisisCore.isClient())
				registerItemModel(item, name);
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
	public static void registerBlockRenderer(Block block, IBlockRenderer renderer)
	{
		if (block == null || renderer == null)
			return;
		instance.blockRenderers.put(block, renderer);
		instance.itemRenderers.put(Item.getItemFromBlock(block), renderer);
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

	public static boolean shouldRenderBlock(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		IBlockRenderer renderer = getBlockRendererOverride(world, pos, state);
		if (renderer == null)
			renderer = getBlockRenderer(state.getBlock());

		return renderer != null;
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
		IBlockRenderer renderer = getBlockRendererOverride(world, pos, state);
		if (renderer == null)
			renderer = getBlockRenderer(state.getBlock());
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
	public static void registerItemRenderer(Item item, IItemRenderer renderer)
	{
		if (item == null || renderer == null)
			return;
		instance.itemRenderers.put(item, renderer);
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

		IItemRenderer renderer = getItemRendererOverride(itemStack);
		if (renderer == null)
			renderer = getItemRenderer(itemStack.getItem());
		if (renderer == null)
			return false;

		renderer.renderItem(itemStack, MalisisRenderer.getPartialTick());
		return true;
	}

	//#end IItemRenderer

	/**
	 * Registers an {@link IIconProvider}.<br>
	 * When the texture is stitched, {@link IIconProvider#registerIcons(net.minecraft.client.renderer.texture.TextureMap)} will be called
	 * for all registered providers.
	 *
	 * @param iconRegister the icon register
	 */
	@SideOnly(Side.CLIENT)
	public static void registerIconRegister(IIconRegister iconRegister)
	{
		if (iconRegister != null)
			instance.iconRegisters.add(iconRegister);
	}

	/**
	 * Registers a {@link IRenderWorldLast}.
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void registerRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.add(renderer);
	}

	/**
	 * Unregisters a {@link IRenderWorldLast}
	 *
	 * @param renderer the renderer
	 */
	@SideOnly(Side.CLIENT)
	public static void unregisterRenderWorldLast(IRenderWorldLast renderer)
	{
		instance.renderWorldLastRenderers.remove(renderer);
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
		registerItemModel(item, Loader.instance().activeModContainer().getModId(), name);
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, String modid, String name)
	{
		DummyModel model = new DummyModel(item, modid + ":" + name);
		ModelLoader.setCustomModelResourceLocation(item, 0, model.getResourceLocation());
		instance.itemModels.add(model);
	}

	private static List<BlockRendererOverride> blockRendererOverrides = Lists.newArrayList();
	private static List<ItemRendererOverride> itemRendererOverrides = Lists.newArrayList();

	@SideOnly(Side.CLIENT)
	public static void registerBlockRendererOverride(BlockRendererOverride override)
	{
		if (override != null)
			blockRendererOverrides.add(override);
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemRendererOverride(ItemRendererOverride override)
	{
		if (override != null)
			itemRendererOverrides.add(override);
	}

	@SideOnly(Side.CLIENT)
	public static IBlockRenderer getBlockRendererOverride(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		for (BlockRendererOverride overrides : blockRendererOverrides)
		{
			IBlockRenderer renderer = overrides.get(world, pos, state);
			if (renderer != null)
				return renderer;
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public static IItemRenderer getItemRendererOverride(ItemStack itemStack)
	{
		for (ItemRendererOverride overrides : itemRendererOverrides)
		{
			IItemRenderer renderer = overrides.get(itemStack);
			if (renderer != null)
				return renderer;
		}

		return null;
	}

	@SideOnly(Side.CLIENT)
	public static void registerRenderers()
	{
		GameData.getBlockRegistry().forEach(instance::registerRenderer);
		GameData.getItemRegistry().forEach(instance::registerRenderer);
	}

	@SideOnly(Side.CLIENT)
	public static void registerIconRegisters()
	{
		GameData.getBlockRegistry().forEach(instance::registerIconRegister);
		GameData.getItemRegistry().forEach(instance::registerIconRegister);
	}

	@SideOnly(Side.CLIENT)
	public static void clearIconRegisters()
	{
		instance.iconRegisters.clear();
	}

	public interface BlockRendererOverride
	{
		public IBlockRenderer get(IBlockAccess world, BlockPos pos, IBlockState state);
	}

	public interface ItemRendererOverride
	{
		public IItemRenderer get(ItemStack itemStack);
	}
}
