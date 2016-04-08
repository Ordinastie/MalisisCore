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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.malisis.core.asm.AsmUtils;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterComponent;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
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

	private static Method registerSound = AsmUtils.changeMethodAccess(SoundEvent.class, "registerSound", "func_187502_a", String.class);

	private static <T> Stream<T> registryStream(RegistryNamespaced<ResourceLocation, T> registry)
	{
		return StreamSupport.stream(registry.spliterator(), false);
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
		/** List of {@link DummyModel} for registered items */
		private Set<DummyModel> itemModels = new HashSet<>();
		/** List of all registered renderers. */
		private Map<Class<? extends MalisisRenderer<?>>, MalisisRenderer<?>> registeredRenderers = new HashMap<>();

		/** Empty {@link IStateMapper} **/
		private static final IStateMapper emptyMapper = new IStateMapper()
		{
			@Override
			public Map<IBlockState, ModelResourceLocation> putStateModelLocations(Block block)
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
				event.getModelRegistry().putObject(model.getResourceLocation(), model);
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
			MalisisIcon.registerIcons(event.getMap());
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
			Pair<Class<? extends MalisisRenderer<?>>, Class<? extends MalisisRenderer<?>>> rendererClasses = getRendererClasses(object);
			if (rendererClasses == null)
				return;

			//get the block renderer
			MalisisRenderer<?> renderer = null;
			try
			{
				renderer = getRenderer(rendererClasses.getLeft());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				MalisisCore.log.error("[MalisisRegistry] Failed to load {} renderer for {}",
						rendererClasses.getLeft().getSimpleName(),
						object.getClass().getSimpleName(),
						e);
				return;
			}

			//register the block renderer
			if (object instanceof Block && renderer != null)
			{
				registerBlockRenderer((Block) object, renderer);
				object = Item.getItemFromBlock((Block) object);
			}

			//get the item renderer
			try
			{
				renderer = getRenderer(rendererClasses.getRight());
			}
			catch (InstantiationException | IllegalAccessException e)
			{
				MalisisCore.log.error("[MalisisRegistry] Failed to load {} renderer for {}",
						rendererClasses.getLeft().getSimpleName(),
						object.getClass().getSimpleName());
				return;
			}

			//register the item renderer
			if (object instanceof Item && renderer != null)
			{
				registerItemRenderer((Item) object, renderer);
			}
		}

		/**
		 * Gets the {@link MalisisRenderer} classes to use for the object.<br>
		 * the Classes are given by the {@link MalisisRendered} annotation on that object class.
		 *
		 * @param object the object
		 * @return the renderer classes
		 */
		private Pair<Class<? extends MalisisRenderer<?>>, Class<? extends MalisisRenderer<?>>> getRendererClasses(Object object)
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
		private MalisisRenderer<?> getRenderer(Class<? extends MalisisRenderer<?>> clazz) throws InstantiationException, IllegalAccessException
		{
			if (clazz == DefaultRenderer.Block.class)
				return DefaultRenderer.block;
			else if (clazz == DefaultRenderer.Block.class)
				return DefaultRenderer.item;
			else if (clazz == DefaultRenderer.Null.class)
				return DefaultRenderer.nullRender;

			MalisisRenderer<?> renderer = registeredRenderers.get(clazz);
			if (renderer == null)
			{
				renderer = clazz.newInstance();
				registeredRenderers.put(clazz, renderer);
			}

			return renderer;
		}
	}

	private static ResourceLocation getResourceLocation(String name)
	{
		int index = name.lastIndexOf(':');
		String res = null;
		String modid = null;
		if (index == -1)
		{
			ModContainer container = Loader.instance().activeModContainer();
			modid = container != null ? container.getModId() : "minecraft";
			res = name;
		}
		else
		{
			modid = name.substring(0, index);
			res = name.substring(index + 1);
		}

		return new ResourceLocation(modid, res);
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

		ResourceLocation res = getResourceLocation(name);
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
				ModelLoader.setCustomStateMapper(block, ClientRegistry.emptyMapper);
				if (item != null)
					registerItemModel(item, name);
			}
		}
		else if (registerable instanceof Item)
		{
			Item item = (Item) registerable;
			GameRegistry.register(item, res);
			if (MalisisCore.isClient())
				registerItemModel(item, res);
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

	public static boolean hasParticleIcon(IBlockState state)
	{
		return IComponent.getComponent(IIconProvider.class, state.getBlock()) != null;
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
		MalisisIcon icon = null;
		IIconProvider provider = IComponent.getComponent(IIconProvider.class, state.getBlock());
		if (provider instanceof IBlockIconProvider)
			icon = ((IBlockIconProvider) provider).getParticleIcon(state);
		else if (provider != null)
			icon = provider.getIcon();

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
		registerItemModel(item, getResourceLocation(name));
	}

	@SideOnly(Side.CLIENT)
	public static void registerItemModel(Item item, ResourceLocation rl)
	{
		DummyModel model = new DummyModel(item, rl);
		//ModelLoader.setCustomModelResourceLocation(item, 0, model.getResourceLocation());
		ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
		{
			@Override
			public ModelResourceLocation getModelLocation(ItemStack stack)
			{
				return model.getResourceLocation();
			}
		});
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

	public static void registerBlockComponents()
	{
		registryStream(Block.blockRegistry).filter(IComponentProvider.class::isInstance)
											.map(IComponentProvider.class::cast)
											.forEach(p -> p.getComponents()
															.stream()
															.filter(IRegisterComponent.class::isInstance)
															.map(IRegisterComponent.class::cast)
															.forEach(comp -> comp.register(p)));

	}

	@SideOnly(Side.CLIENT)
	public static void registerRenderers()
	{
		Block.blockRegistry.forEach(instance::registerRenderer);
		Item.itemRegistry.forEach(instance::registerRenderer);
	}

	public static SoundEvent registerSound(String modId, String soundId)
	{
		ResourceLocation rl = new ResourceLocation(modId, soundId);
		try
		{
			registerSound.invoke(null, rl.toString());
			return SoundEvent.soundEventRegistry.getObject(rl);
		}
		catch (ReflectiveOperationException e)
		{
			MalisisCore.log.error("[MalisisRegistry] Failed to register sound :", e);
			return null;
		}
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
