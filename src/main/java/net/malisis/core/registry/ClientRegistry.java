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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.IItemRenderer.DummyModel;
import net.malisis.core.renderer.IRenderWorldLast;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Ordinastie
 *
 */
@SideOnly(Side.CLIENT)
public class ClientRegistry
{
	static ClientRegistry instance = new ClientRegistry();

	public ClientRegistry()
	{
		MinecraftForge.EVENT_BUS.register(this);
		MalisisRegistry.onInit(this::registerRenderers);
	}

	/** List of registered {@link IBlockRenderer}. */
	Map<Block, IBlockRenderer> blockRenderers = Maps.newHashMap();
	/** List of registered {@link IItemRenderer} */
	Map<Item, IItemRenderer> itemRenderers = Maps.newHashMap();
	/** List of registered {@link IRenderWorldLast} */
	List<IRenderWorldLast> renderWorldLastRenderers = Lists.newArrayList();
	/** List of registered {@link IIconProvider} */
	/** List of {@link DummyModel} for registered items */
	Set<DummyModel> itemModels = Sets.newHashSet();
	/** List of all registered renderers. */
	Map<Class<? extends MalisisRenderer<?>>, MalisisRenderer<?>> registeredRenderers = Maps.newHashMap();
	/** List of {@link BlockRendererOverride}. */
	List<BlockRendererOverride> blockRendererOverrides = Lists.newArrayList();
	/** List of {@link ItemRendererOverride}. */
	List<ItemRendererOverride> itemRendererOverrides = Lists.newArrayList();

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
		Icon.registerIcons(event.getMap());
	}

	/**
	 * Registers a {@link MalisisRenderer} associated with the object.<br>
	 * The object class needs to have the {@link MalisisRendered} annotation.<br>
	 * Automatically called for blocks and items in the registries.
	 *
	 * @param object the object
	 */
	void registerRenderer(Object object)
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
			itemRenderers.put((Item) object, renderer);
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
	 * Gets and eventually instantiates the {@link MalisisRenderer} class.
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

	/**
	 * Registers a {@link IBlockRenderer} for the {@link Block}.
	 *
	 * @param block the block
	 * @param renderer the renderer
	 */
	public void registerBlockRenderer(Block block, IBlockRenderer renderer)
	{
		blockRenderers.put(checkNotNull(block), checkNotNull(renderer));
	}

	/**
	 * Registers a {@link IItemRenderer} for the {@link Item}.
	 *
	 * @param item the item
	 * @param renderer the renderer
	 */
	public void registerItemRenderer(Item item, IItemRenderer renderer)
	{
		itemRenderers.put(checkNotNull(item), checkNotNull(renderer));
	}

	/**
	 * Gets the {@link IBlockRenderer} registered for the {@link Block}.
	 *
	 * @param block the block
	 * @return the block renderer
	 */
	public IBlockRenderer getBlockRenderer(Block block)
	{
		return blockRenderers.get(block);
	}

	/**
	 * Registers the renderers for registered {@link Block Blocks} and {@link Item Items}.
	 */
	public void registerRenderers(FMLInitializationEvent event)
	{
		Block.REGISTRY.forEach(this::registerRenderer);
		Item.REGISTRY.forEach(this::registerRenderer);
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
