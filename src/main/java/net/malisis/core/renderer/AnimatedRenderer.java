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

package net.malisis.core.renderer;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

import net.malisis.core.block.IComponent;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.renderer.component.AnimatedModelComponent;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.Point;
import net.malisis.core.util.callback.CallbackResult;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Ordinastie
 *
 */
public class AnimatedRenderer extends MalisisRenderer<TileEntity>
{
	/** Map of {@link ISortedRenderable} per {@link BlockPos}. */
	private static Map<BlockPos, IAnimatedRenderable> animatedRenderables = Maps.newConcurrentMap();
	static
	{
		//check renderable to be removed when a block changes.
		MalisisRegistry.onPostSetBlock(AnimatedRenderer::removeRenderable, CallbackOption.of());
	}

	/** Current {@link IAnimatedRenderable} being rendered. */
	IAnimatedRenderable renderable;

	public AnimatedRenderer()
	{
		registerForRenderWorldLast();
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**
	 * Gets the currently drawn {@link IAnimatedRenderable}.
	 *
	 * @return the renderable
	 */
	public IAnimatedRenderable getRenderable()
	{
		return renderable;
	}

	@Override
	public boolean shouldRender(RenderWorldLastEvent event, IBlockAccess world)
	{
		return animatedRenderables.size() > 0;
	}

	@Override
	public void render()
	{
		Point viewOffset = EntityUtils.getRenderViewOffset(partialTick);
		ICamera camera = new Frustum();
		//camera.setPosition(viewOffset.x, viewOffset.y, viewOffset.z);

		renderType = RenderType.ANIMATED;
		animatedRenderables	.values()
							.stream()
							.filter(r -> r.inFrustrum(camera))
							.sorted((r1, r2) -> -BlockPosUtils.compare(viewOffset, r1.getPos(), r2.getPos()))
							.forEach(this::renderRenderable);
		renderType = RenderType.WORLD_LAST;
	}

	/**
	 * Renders an {@link IAnimatedRenderable}.
	 *
	 * @param renderable the renderable
	 */
	private void renderRenderable(IAnimatedRenderable renderable)
	{
		this.renderable = renderable;
		set(renderable.getWorld(), renderable.getPos());
		posOffset = new Vec3d(pos.getX(), pos.getY(), pos.getZ());

		renderable.renderAnimated(block, this);
	}

	/**
	 * Removes the registered {@link IAnimatedRenderable} for the positions in the unloading chunk.
	 *
	 * @param event the event
	 */
	@SubscribeEvent
	public void onChunkUnload(ChunkEvent.Unload event)
	{
		animatedRenderables.keySet().removeIf(p -> (p.getX() >> 4) == event.getChunk().x && (p.getZ() >> 4) == event.getChunk().z);
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		animatedRenderables.clear();
	}

	/**
	 * Removes the stored {@link IAnimatedRenderable} for the position, if necessary.<br>
	 * Called every time a block is set in the world.
	 *
	 * @param chunk the chunk
	 * @param pos the pos
	 * @param oldState the old state
	 * @param newState the new state
	 * @return the callback result
	 */
	public static CallbackResult<Void> removeRenderable(Chunk chunk, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		if (oldState.getBlock() == newState.getBlock()) //same block, so same components
			return CallbackResult.noResult();

		if (!chunk.getWorld().isRemote)
			return CallbackResult.noResult();//callback is also called on server thread in SSP

		AnimatedModelComponent comp = IComponent.getComponent(AnimatedModelComponent.class, oldState.getBlock());
		if (comp != null)
			animatedRenderables.remove(pos);

		return CallbackResult.noResult();
	}

	/**
	 * Gets the {@link AnimatedModelComponent} for the specified {@link BlockPos}.
	 *
	 * @param pos the pos
	 * @return the renderable
	 */
	public static Optional<IAnimatedRenderable> getRenderable(BlockPos pos)
	{
		return Optional.ofNullable(animatedRenderables.get(pos));
	}

	/**
	 * Registers the {@link AnimatedModelComponent} at the specified position if there isn't already one.
	 *
	 * @param pos the pos
	 * @param amc the amc
	 */
	public static void registerRenderable(IBlockAccess world, BlockPos pos, AnimatedModelComponent amc)
	{
		IAnimatedRenderable r = animatedRenderables.get(pos);
		if (r == null)
			animatedRenderables.put(pos, amc.createRenderable(world, pos));
	}
}