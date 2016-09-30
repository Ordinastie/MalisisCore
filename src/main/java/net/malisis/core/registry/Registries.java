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

import java.util.stream.StreamSupport;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterComponent;
import net.malisis.core.registry.ModEventRegistry.IFMLEventCallback;
import net.malisis.core.registry.RenderBlockRegistry.IRenderBlockCallback;
import net.malisis.core.registry.SetBlockCallbackRegistry.ISetBlockCallback;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.util.callback.CallbackResult;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.event.FMLStateEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class Registries
{
	@SideOnly(Side.CLIENT)
	/** {@link ClientRegistry} instance. */
	static ClientRegistry clientRegistry;
	/** {@link ModEventRegistry} instance. */
	static ModEventRegistry modEventRegistry = new ModEventRegistry();
	/** The {@link RenderBlockRegistry} instance. */
	static RenderBlockRegistry renderBlockRegistry = new RenderBlockRegistry();

	static SetBlockCallbackRegistry preSetBlockRegistry = new SetBlockCallbackRegistry();
	static SetBlockCallbackRegistry postSetBlockRegistry = new SetBlockCallbackRegistry();

	static
	{
		//Calls IRegisterComponent.register for all the IBlockComponent that implement the interface.
		//Fired in FMLInitializationEvent, so all the blocks should already be registered
		MalisisRegistry.onInit(event -> StreamSupport.stream(Block.REGISTRY.spliterator(), false)
														.filter(IComponentProvider.class::isInstance)
														.map(IComponentProvider.class::cast)
														.forEach(p -> p.getComponents()
																		.stream()
																		.filter(IRegisterComponent.class::isInstance)
																		.map(IRegisterComponent.class::cast)
																		.forEach(comp -> comp.register(p))));

		if (MalisisCore.isClient())
			clientRegistry = new ClientRegistry();
	}

	/**
	 * Processes {@link IFMLEventCallback IFMLEventCallbacks} for the specified event.
	 *
	 * @param event the event
	 */
	public static void processFMLStateEvent(FMLStateEvent event)
	{
		modEventRegistry.processCallbacks(event);
	}

	/**
	 * Processes {@link IRenderBlockCallback IRenderBlockCallbacks}.<br>
	 * Called by ASM from {@link BlockRendererDispatcher#renderBlock(IBlockState, BlockPos, IBlockAccess, VertexBuffer)}
	 *
	 * @param buffer the buffer
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 * @return the callback result
	 */
	@SideOnly(Side.CLIENT)
	public static CallbackResult<Boolean> processRenderBlockCallbacks(VertexBuffer buffer, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		//warning mutable BlockPos received
		return renderBlockRegistry.processCallbacks(buffer, world, pos, state);
	}

	/**
	 * Processes {@link ISetBlockCallback ISetBlockCallbacks}.<br>
	 * Called by ASM from {@link Chunk#setBlockState(BlockPos, IBlockState)}.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 * @param oldState the old state
	 * @param newState the new state
	 * @return the callback result
	 */
	public static CallbackResult<Void> processPreSetBlock(Chunk chunk, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		CallbackResult<Void> ret = preSetBlockRegistry.processCallbacks(chunk, pos, oldState, newState);
		return ret;
	}

	/**
	 * Processes {@link ISetBlockCallback ISetBlockCallbacks}.<br>
	 * Called by ASM from {@link Chunk#setBlockState(BlockPos, IBlockState)}.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 * @param oldState the old state
	 * @param newState the new state
	 */
	public static void processPostSetBlock(Chunk chunk, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		postSetBlockRegistry.processCallbacks(chunk, pos, oldState, newState);
	}

	/**
	 * Renders the {@link ItemStack} with a registered {@link IItemRenderer}.<br>
	 * Called via ASM from {@link RenderItem#renderModel}.
	 *
	 * @param itemStack the item stack
	 * @return true, if successful
	 */
	//TODO: make it a callback ?
	@SideOnly(Side.CLIENT)
	public static boolean renderItem(ItemStack itemStack)
	{
		return clientRegistry.renderItem(itemStack);
	}

	/**
	 * Gets the {@link TextureAtlasSprite} to used for the {@link IBlockState}.<br>
	 * Called via ASM from {@link BlockModelShapes#getTexture(IBlockState)}
	 *
	 * @param state the state
	 * @return the particle icon
	 */
	//TODO: make it a callback ?
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getParticleIcon(IBlockState state)
	{
		return ClientRegistry.getParticleIcon(state);
	}
}
