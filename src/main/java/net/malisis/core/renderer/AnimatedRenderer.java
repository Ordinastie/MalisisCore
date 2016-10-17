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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.malisis.core.MalisisCoreSettings;
import net.malisis.core.block.IComponent;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.renderer.component.AnimatedModelComponent;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.Point;
import net.malisis.core.util.Utils;
import net.malisis.core.util.WeakNested;
import net.malisis.core.util.callback.CallbackResult;
import net.malisis.core.util.callback.ICallback.CallbackOption;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderGlobal.ContainerLocalRenderInformation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.MinecraftForgeClient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author Ordinastie
 *
 */
public class AnimatedRenderer extends MalisisRenderer<TileEntity> implements IAnimatedRenderer
{
	IAnimatedRenderable renderable;

	public AnimatedRenderer()
	{
		setBatched();
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
	public void renderAnimated(World world, BlockPos pos, IAnimatedRenderable renderable, double x, double y, double z, float partialTicks)
	{
		set(world, pos);
		prepare(RenderType.ANIMATED, x, y, z);
		this.buffer = isBatched() ? batchedBuffer : Tessellator.getInstance().getBuffer();
		this.renderable = renderable;

		render();

		clean();
	}

	@Override
	public void prepare(RenderType renderType, double... data)
	{
		this.renderType = renderType;
		if (isBatched())
		{
			posOffset = new Vec3d(data[0], data[1], data[2]);
			vertexFormat = DefaultVertexFormats.BLOCK;
		}
		else
		{
			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			GlStateManager.translate(data[0], data[1], data[2]);

			bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			startDrawing();
		}
	}

	@Override
	public void clean()
	{
		if (isBatched())
			buffer.setTranslation(0, 0, 0);
		else
		{
			draw();
			disableBlending();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}

		reset();
		renderable = null;
	}

	@Override
	public void render()
	{
		renderable.renderAnimated(block, this);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/** Map of {@link ISortedRenderable} per {@link Chunk Chunks}. */
	private static WeakNested.Map<Chunk, BlockPos, ISortedRenderable> sortedRenderables = new WeakNested.Map<>(Maps::newHashMap);
	static
	{
		MalisisRegistry.onPostSetBlock(AnimatedRenderer::removeRenderable, CallbackOption.of());
	}

	/**
	 * Removes the stored {@link ISortedRenderable} in the chunk for the position, if necessary.<br>
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

		//TODO: make IRenderableProvider ?
		AnimatedModelComponent comp = IComponent.getComponent(AnimatedModelComponent.class, oldState.getBlock());
		if (comp != null)
			sortedRenderables.remove(chunk, pos);

		return CallbackResult.noResult();
	}

	/**
	 * Gets the {@link Stream} of {@link ISortedRenderable} for the specified {@link Chunk}.
	 *
	 * @param chunk the chunk
	 * @return the renderables
	 */
	public static Stream<ISortedRenderable> getRenderables(Chunk chunk)
	{
		Collection<ISortedRenderable> renderables = sortedRenderables.get(chunk).values();
		if (renderables.isEmpty())
			return Stream.empty();

		return ImmutableList.copyOf(renderables).stream();//.filter(instance::isPositionStillValid);
	}

	/**
	 * Gets the {@link ISortedRenderable} for the specified {@link BlockPos}, if the chunk is loaded.
	 *
	 * @param <T> the generic type
	 * @param pos the pos
	 * @return the renderable
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ISortedRenderable> Optional<T> getRenderable(BlockPos pos)
	{
		Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), pos);
		return (Optional<T>) chunk.map(c -> sortedRenderables.get(c, pos));
	}

	/**
	 * Registers the {@link ISortedRenderable} at the specified position if there isn't already one.
	 *
	 * @param pos the pos
	 * @param amc the amc
	 */
	public static void registerRenderable(BlockPos pos, AnimatedModelComponent amc)
	{
		Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), pos);
		if (!chunk.isPresent())
			return;
		ISortedRenderable r = sortedRenderables.get(chunk.get(), pos);
		if (r == null)
			sortedRenderables.put(chunk.get(), pos, amc.createRenderable(chunk.get().getWorld(), pos));
	}

	/**
	 * Checks if position is still valid for animated rendering.
	 *
	 * @param sr the amc
	 * @return true, if is position still valid
	 */
	//	private boolean isPositionStillValid(ISortedRenderable sr)
	//	{
	//		Optional<Chunk> chunk = Utils.getLoadedChunk(Utils.getClientWorld(), sr.getPos());
	//		if (!chunk.isPresent())
	//			return false; //should never happen,
	//
	//		if (sr.isPositionStillValid())
	//			return true;
	//
	//		sortedRenderables.get(chunk.get()).remove(sr.getPos());
	//		return false;
	//	}

	private static Point getRenderViewOffset(float partialTick)
	{
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (partialTick == 0)
			return new Point(entity.posX, entity.posY, entity.posZ);

		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;
		return new Point(x, y, z);
	}

	public static List<RenderGlobal.ContainerLocalRenderInformation> sortRenderInfos(List<RenderGlobal.ContainerLocalRenderInformation> renderInfos)
	{
		if (MinecraftForgeClient.getRenderPass() != 0 || !MalisisCoreSettings.tileEntitySorting.get())
			return renderInfos;

		Point viewOffset = getRenderViewOffset(0).add(-8, -8, -8);
		return renderInfos.stream().sorted((r1, r2) -> compareRenderInfos(viewOffset, r1, r2)).collect(Collectors.toList());
	}

	public static int compareRenderInfos(Point viewOffset, ContainerLocalRenderInformation r1, ContainerLocalRenderInformation r2)
	{
		BlockPos p1 = r1.renderChunk.getPosition();
		BlockPos p2 = r2.renderChunk.getPosition();

		return -BlockPosUtils.compare(viewOffset, p1, p2);
	}

	/**
	 * Render {@link TileEntity TileEntities} and {@link ISortedRenderable} to fix the transparency sorting.<br>
	 * Called from ASM from {@link RenderGlobal#renderEntities(Entity, ICamera, float)}
	 *
	 * @param renderChunk the render chunk
	 * @param list the list
	 * @param camera the camera
	 * @param partialTick the partial tick
	 * @return the list
	 */
	public static List<TileEntity> renderSortedTileEntities(RenderChunk renderChunk, List<TileEntity> list, ICamera camera, float partialTick)
	{
		if (MinecraftForgeClient.getRenderPass() == 1)
			return list;

		boolean sorting = MalisisCoreSettings.tileEntitySorting.get();

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		World world = entity.worldObj;
		Chunk chunk = world.getChunkFromBlockCoords(renderChunk.getPosition());
		Point viewOffset = getRenderViewOffset(partialTick);

		Stream<ISortedRenderable> stream = Stream.concat(list.stream().map(ISortedRenderable.TE::new),
				AnimatedRenderer.getRenderables(chunk)).filter(r -> r.inFrustrum(camera));
		if (sorting)
			stream = stream.sorted((r1, r2) -> -BlockPosUtils.compare(viewOffset, r1.getPos(), r2.getPos()));
		stream.forEach(r -> r.render(partialTick));

		return ImmutableList.of();
	}
}