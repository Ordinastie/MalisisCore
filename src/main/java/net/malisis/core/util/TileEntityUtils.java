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

package net.malisis.core.util;

import static org.objectweb.asm.Opcodes.*;

import java.util.List;
import java.util.stream.Stream;

import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisCoreTransformer;
import net.malisis.core.asm.mappings.McpFieldMapping;
import net.malisis.core.asm.mappings.McpMethodMapping;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.renderer.ISortedRenderable;
import net.malisis.core.renderer.ISortedRenderable.SortedRenderableManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.ImmutableList;

/**
 * Utility class for {@link TileEntity}.
 *
 * @author Ordinastie
 */
public class TileEntityUtils
{
	/** Reference to the {@link TileEntity} currently being use for current opened {@link MalisisGui}. */
	@SideOnly(Side.CLIENT)
	private static TileEntity currentTileEntity;

	/** Reference the to currently opened {@link MalisisGui}. */
	@SideOnly(Side.CLIENT)
	private static MalisisGui currenGui;

	/**
	 * Gets the {@link TileEntity} of type <b>T</b> at the specified {@link BlockPos}.<br>
	 * If no <code>TileEntity</code> was found at the coordinates, or if the <code>TileEntity</code> is not of type <b>T</b>, returns
	 * <code>null</code> instead.
	 *
	 * @param <T> type of TileEntity requested
	 * @param clazz the class of the TileEntity
	 * @param world the world
	 * @param pos the pos
	 * @return the tile entity at the coordinates, or null if no tile entity, or not of type T
	 */
	public static <T> T getTileEntity(Class<T> clazz, IBlockAccess world, BlockPos pos)
	{
		if (world == null)
			return null;

		TileEntity te = world.getTileEntity(pos);
		return te != null ? Silenced.get(() -> clazz.cast(te)) : null;
	}

	/**
	 * Links the {@link TileEntity} to the {@link MalisisGui}.<br>
	 * Allows the TileEntity to notify the MalisisGui of updates.
	 *
	 * @param te the TileEntity
	 * @param gui the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void linkTileEntityToGui(TileEntity te, MalisisGui gui)
	{
		currentTileEntity = te;
		currenGui = gui;
		currenGui.updateGui();
	}

	/**
	 * Notifies the currently opened {@link MalisisGui} to update.
	 *
	 * @param te the {@link TileEntity} linked to the MalisisGui
	 */
	@SideOnly(Side.CLIENT)
	public static void updateGui(TileEntity te)
	{
		if (te != currentTileEntity)
			return;
		currenGui.updateGui();
	}

	public static AxisAlignedBB getRenderingBounds(TileEntity tileEntity)
	{
		Block block = tileEntity.getBlockType();
		BlockPos pos = tileEntity.getPos();
		World world = tileEntity.getWorld();
		AxisAlignedBB aabb = null;
		if (block instanceof IBoundingBox)
		{
			aabb = AABBUtils.combine(((IBoundingBox) block).getRenderBoundingBox(world, pos, world.getBlockState(pos)));
			aabb = AABBUtils.offset(pos, aabb);
		}

		return aabb != null ? aabb : AABBUtils.identity(pos);
	}

	public static void notifyUpdate(TileEntity te)
	{
		if (te.getWorld() == null)
			return;
		IBlockState state = te.getWorld().getBlockState(te.getPos());
		te.getWorld().notifyBlockUpdate(te.getPos(), state, state, 3);
	}

	//fix for TESR sorting
	@SideOnly(Side.CLIENT)
	public static List<TileEntity> renderSortedTileEntities(RenderChunk renderChunk, List<TileEntity> list, ICamera camera, float partialTick)
	{
		boolean b = false;
		if (b)
			return list;

		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		World world = entity.worldObj;
		Chunk chunk = world.getChunkFromBlockCoords(renderChunk.getPosition());

		double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTick;
		double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTick;
		double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTick;

		Stream.concat(list.stream().map(ISortedRenderable.TE::new), SortedRenderableManager.getRenderables(chunk))
				.filter(r -> r.inFrustrum(camera))
				.sorted((r1, r2) -> r1.getPos().distanceSqToCenter(x, y, z) > r2.getPos().distanceSqToCenter(x, y, z) ? -1 : 1)
				.forEach(r -> r.render(partialTick));

		return ImmutableList.of();
	}

	public static class SortingTileEntitiesTransformer extends MalisisCoreTransformer
	{
		@Override
		@SuppressWarnings("deprecation")
		public void registerHooks()
		{
			AsmHook hook = new AsmHook(new McpMethodMapping("renderEntities", "func_180446_a",
					"net/minecraft/client/renderer/RenderGlobal",
					"(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V"));

			//List<TileEntity> list3 = renderglobal$containerlocalrenderinformation1.renderChunk.getCompiledChunk().getTileEntities();
			//			ALOAD 23
			//		    GETFIELD net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation.renderChunk : Lnet/minecraft/client/renderer/chunk/RenderChunk;
			//		    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/RenderChunk.getCompiledChunk ()Lnet/minecraft/client/renderer/chunk/CompiledChunk;
			//		    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/CompiledChunk.getTileEntities ()Ljava/util/List;
			//		    ASTORE 24
			McpFieldMapping renderChunk = new McpFieldMapping("renderChunk", "field_178036_a",
					"net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation",
					"Lnet/minecraft/client/renderer/chunk/RenderChunk;");
			McpMethodMapping getCompiledChunk = new McpMethodMapping("getCompiledChunk", "func_178571_g",
					"net/minecraft/client/renderer/chunk/RenderChunk", "()Lnet/minecraft/client/renderer/chunk/CompiledChunk;");
			McpMethodMapping getTileEntities = new McpMethodMapping("getTileEntities", "func_178485_b",
					"net/minecraft/client/renderer/chunk/CompiledChunk", "()Ljava/util/List;");
			InsnList match = new InsnList();
			match.add(new VarInsnNode(ALOAD, 23));
			match.add(renderChunk.getInsnNode(GETFIELD));
			match.add(getCompiledChunk.getInsnNode(INVOKEVIRTUAL));
			match.add(getTileEntities.getInsnNode(INVOKEVIRTUAL));
			match.add(new VarInsnNode(ASTORE, 24));

			//list3 = TileEntityUtils.renderSortedTileEntities(renderglobal$containerlocalrenderinformation.renderChunk, list3, camera, partialTicks);
			//		    ALOAD 23
			//			GETFIELD net/minecraft/client/renderer/RenderGlobal$ContainerLocalRenderInformation.renderChunk : Lnet/minecraft/client/renderer/chunk/RenderChunk;
			//			ALOAD 24
			//		    ALOAD 1
			//		    FLOAD 3
			//		    INVOKESTATIC net/malisis/core/util/TileEntityUtils.renderSortedTileEntities (Lnet/minecraft/client/renderer/chunk/RenderChunk;Ljava/util/List;Lnet/minecraft/client/renderer/culling/ICamera;F)Ljava/util/List;
			//		    ASTORE 2

			InsnList insert = new InsnList();
			insert.add(new VarInsnNode(ALOAD, 23));
			insert.add(renderChunk.getInsnNode(GETFIELD));
			insert.add(new VarInsnNode(ALOAD, 24));
			insert.add(new VarInsnNode(ALOAD, 2));
			insert.add(new VarInsnNode(FLOAD, 3));
			insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/TileEntityUtils", "renderSortedTileEntities",
					"(Lnet/minecraft/client/renderer/chunk/RenderChunk;Ljava/util/List;Lnet/minecraft/client/renderer/culling/ICamera;F)Ljava/util/List;"));
			insert.add(new VarInsnNode(ASTORE, 24));

			hook.jumpAfter(match).insert(insert);
			register(hook);
		}
	}

}
