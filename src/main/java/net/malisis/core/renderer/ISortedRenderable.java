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
import java.util.WeakHashMap;
import java.util.stream.Stream;

import net.malisis.core.block.IComponent;
import net.malisis.core.registry.AutoLoad;
import net.malisis.core.renderer.component.AnimatedModelComponent;
import net.malisis.core.util.Timer;
import net.malisis.core.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author Ordinastie
 *
 */
public interface ISortedRenderable
{
	public BlockPos getPos();

	public boolean inFrustrum(ICamera camera);

	public boolean isPositionStillValid();

	public void render(float partialTick);

	@AutoLoad
	public static class SortedRenderableManager
	{
		private static SortedRenderableManager instance = new SortedRenderableManager();

		private SortedRenderableManager()
		{
			MinecraftForge.EVENT_BUS.register(this);
		}

		private Map<Chunk, Map<BlockPos, ISortedRenderable>> sortedRenderable = new WeakHashMap<>();

		public static Stream<ISortedRenderable> getRenderables(Chunk chunk)
		{
			Map<BlockPos, ISortedRenderable> amcs = instance.sortedRenderable.get(chunk);
			if (amcs == null || amcs.isEmpty())
				return Stream.empty();

			return ImmutableList.copyOf(amcs.values())
								.stream()
								.filter(e -> /* TODO: frustrum check */true)
								.filter(instance::isPositionStillValid);
		}

		/**
		 * Checks if position is still valid for animated rendering.
		 *
		 * @param sr the amc
		 * @return true, if is position still valid
		 */
		private boolean isPositionStillValid(ISortedRenderable sr)
		{
			Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), sr.getPos());
			if (!chunk.isPresent())
				return false; //should never happen,

			if (sr.isPositionStillValid())
				return true;

			sortedRenderable.get(chunk.get()).remove(sr.getPos());
			return false;
		}

		@SubscribeEvent
		public void onChunkLoad(ChunkEvent.Unload event)
		{
			if (!event.getWorld().isRemote)
				return;

			sortedRenderable.remove(event.getChunk());
		}

		@SuppressWarnings("unchecked")
		public static <T extends ISortedRenderable> Optional<T> getSortedRenderable(BlockPos pos)
		{
			Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), pos);
			Optional<Map<BlockPos, ISortedRenderable>> renderables = chunk.map(instance.sortedRenderable::get);
			return (Optional<T>) renderables.map(map -> map.get(pos));
		}

		/**
		 * Checks whether an {@link AnimatedModelComponent} is present at the {@link BlockPos}.<br>
		 * If the position wasn't registered yet to be rendered,
		 * {@link AnimatedModelComponent#checkState(IBlockAccess, BlockPos, IBlockState)} is called.
		 *
		 * @param world the world
		 * @param pos the pos
		 * @param state the state
		 */
		public static void checkRenderable(IBlockAccess world, BlockPos pos, IBlockState state)
		{
			AnimatedModelComponent comp = IComponent.getComponent(AnimatedModelComponent.class, state.getBlock());

			//TODO: check if really necessary to insert empty maps if we use Optional
			Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), pos);
			Optional<Map<BlockPos, ISortedRenderable>> renderables = chunk.map(instance.sortedRenderable::get);

			if (!renderables.isPresent())
			{
				//first time rendered for the chunk, init the map
				renderables = Optional.of(Maps.newHashMap());
				instance.sortedRenderable.put(chunk.get(), renderables.get());
				//no comp to add, return early
				if (comp == null)
					return;
			}

			//TODO: FIX WITH CALLBACKS ?
			Map<BlockPos, ISortedRenderable> amcs = renderables.get();
			AMC amc = (AMC) amcs.get(pos);
			//old amc, no new one
			if (comp == null && amc != null)
				amcs.remove(pos);
			//no previous AMC or for a different component
			else if (comp != null && (amc == null || amc.getComponent() != comp))
			{
				amc = new AMC(pos, comp);
				amcs.put(pos, amc);
			}
			//else amc already in map, nothing more needed
		}

		public static SortedRenderableManager get()
		{
			return instance;
		}

	}

	public static class TE implements ISortedRenderable
	{
		private TileEntity te;

		public TE(TileEntity te)
		{
			this.te = te;
		}

		@Override
		public boolean inFrustrum(ICamera camera)
		{
			return camera.isBoundingBoxInFrustum(te.getRenderBoundingBox());
		}

		@Override
		public boolean isPositionStillValid()
		{
			return true;
		}

		@Override
		public BlockPos getPos()
		{
			return te.getPos();
		}

		@Override
		public void render(float partialTicks)
		{
			TileEntityRendererDispatcher.instance.renderTileEntity(te, partialTicks, -1);
		}
	}

	public static class AMC implements ISortedRenderable, IAnimatedRenderable
	{
		private BlockPos pos;
		private AnimatedModelComponent amc;
		private Map<String, Timer> timers = Maps.newHashMap();

		public AMC(BlockPos pos, AnimatedModelComponent amc)
		{
			this.pos = pos;
			this.amc = amc;
		}

		@Override
		public BlockPos getPos()
		{
			return pos;
		}

		public AnimatedModelComponent getComponent()
		{
			return amc;
		}

		@Override
		public Timer addTimer(String animation, Timer timer)
		{
			return timers.put(animation, Preconditions.checkNotNull(timer));
		}

		@Override
		public Timer removeTimer(String animation)
		{
			return timers.remove(animation);
		}

		@Override
		public Timer getTimer(String animation)
		{
			return timers.get(animation);
		}

		public Map<String, Timer> getTimers()
		{
			return ImmutableMap.copyOf(timers);
		}

		@Override
		public boolean inFrustrum(ICamera camera)
		{
			//FIXME : find AABB for the currently block/state
			return true;
		}

		@Override
		public boolean isPositionStillValid()
		{
			IBlockState state = Minecraft.getMinecraft().theWorld.getBlockState(getPos());
			AnimatedModelComponent blockComponent = IComponent.getComponent(AnimatedModelComponent.class, state.getBlock());
			return getComponent() == blockComponent;
		}

		@Override
		public void render(float partialTick)
		{
			double x = pos.getX() - TileEntityRendererDispatcher.staticPlayerX;
			double y = pos.getY() - TileEntityRendererDispatcher.staticPlayerY;
			double z = pos.getZ() - TileEntityRendererDispatcher.staticPlayerZ;
			DefaultRenderer.animated.renderAnimated(TileEntityRendererDispatcher.instance.worldObj, pos, this, x, y, z, partialTick);
		}

		@Override
		public void renderAnimated(Block block, AnimatedRenderer renderer)
		{
			amc.renderAnimated(block, this, renderer);
		}

	}
}