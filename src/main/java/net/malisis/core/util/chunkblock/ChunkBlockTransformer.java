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

package net.malisis.core.util.chunkblock;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpMethodMapping;
import net.malisis.core.registry.Registries;
import net.malisis.core.util.callback.ASMCallbackRegistry.CallbackResult;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Ordinastie
 *
 */
public class ChunkBlockTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(updateCoordsHook());
	}

	private AsmHook updateCoordsHook()
	{
		McpMethodMapping setBlockState = new McpMethodMapping("setBlockState", "func_177436_a", "net.minecraft.world.chunk.Chunk",
				"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/block/state/IBlockState;");
		McpMethodMapping set = new McpMethodMapping("set", "func_177484_a", "net.minecraft.world.chunk.storage.ExtendedBlockStorage",
				"(IIILnet/minecraft/block/state/IBlockState;)V");

		AsmHook ah = new AsmHook(setBlockState);

		//	    ALOAD 12
		//	    ILOAD 3
		//	    ILOAD 4
		//	    BIPUSH 15
		//	    IAND
		//	    ILOAD 5
		//	    ALOAD 2
		//	    INVOKEVIRTUAL net/minecraft/world/chunk/storage/ExtendedBlockStorage.set (IIILnet/minecraft/block/state/IBlockState;)V
		//L:714 extendedblockstorage.set(i, j & 15, k, state);
		//		 L25
		//		    LINENUMBER 714 L25
		//		    ALOAD 12
		//		    ILOAD 3
		//		    ILOAD 4
		//		    BIPUSH 15
		//		    IAND
		//		    ILOAD 5
		//		    ALOAD 2
		//		    INVOKEVIRTUAL net/minecraft/world/chunk/storage/ExtendedBlockStorage.set (IIILnet/minecraft/block/state/IBlockState;)V

		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 12));
		match.add(new VarInsnNode(ILOAD, 3));
		match.add(new VarInsnNode(ILOAD, 4));
		match.add(new IntInsnNode(BIPUSH, 15));
		match.add(new InsnNode(IAND));
		match.add(new VarInsnNode(ILOAD, 5));
		match.add(new VarInsnNode(ALOAD, 2));
		match.add(set.getInsnNode(INVOKEVIRTUAL));

		//		CallbackResult<Boolean> cb = Registries.processPreSetBlock(this, pos, iblockstate, state);
		//		if (cb.shouldReturn())
		//			return null;
		//		ALOAD 0
		//	    ALOAD 1
		//	    ALOAD 8
		//	    ALOAD 2
		//		INVOKESTATIC net/malisis/core/registry/Registries.processRenderBlockCallbacks (Lnet/minecraft/client/renderer/VertexBuffer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Lnet/malisis/core/util/callback/ASMCallbackRegistry$CallbackResult;
		//		ASTORE 8
		//		L2
		//		LINENUMBER 69 L2
		//		ALOAD 8
		//		INVOKEVIRTUAL net/malisis/core/util/callback/ASMCallbackRegistry$CallbackResult.shouldReturn ()Z
		//		IFEQ L3
		//		L4
		//		LINENUMBER 70 L4
		//		ACONST_NULL
		//		ARETURN

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new VarInsnNode(ALOAD, 8));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new MethodInsnNode(
				INVOKESTATIC,
				"net/malisis/core/registry/Registries",
				"processPreSetBlock",
				"(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;)Lnet/malisis/core/util/callback/ASMCallbackRegistry$CallbackResult;",
				false));
		//		insert.add(new VarInsnNode(ASTORE, 15));
		//		insert.add(new VarInsnNode(ALOAD, 15));
		insert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/callback/ASMCallbackRegistry$CallbackResult", "shouldReturn",
				"()Z", false));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new InsnNode(ACONST_NULL));
		insert.add(new InsnNode(ARETURN));
		insert.add(falseLabel);

		//		if (!ChunkBlockHandler.get().updateCoordinates(this, pos, blockState1, blockState))
		//			return null;

		//		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkblock/ChunkBlockHandler", "get",
		//				"()Lnet/malisis/core/util/chunkblock/ChunkBlockHandler;"));
		//		insert.add(new VarInsnNode(ALOAD, 0));
		//		insert.add(new VarInsnNode(ALOAD, 1));
		//		insert.add(new VarInsnNode(ALOAD, 8));
		//		insert.add(new VarInsnNode(ALOAD, 2));
		//		insert.add(new MethodInsnNode(
		//				INVOKEVIRTUAL,
		//				"net/malisis/core/util/chunkblock/ChunkBlockHandler",
		//				"updateCoordinates",
		//				"(Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/state/IBlockState;)Z"));
		//		insert.add(new JumpInsnNode(IFNE, falseLabel));
		//		insert.add(new InsnNode(ACONST_NULL));
		//		insert.add(new InsnNode(ARETURN));
		//		insert.add(falseLabel);

		ah.jumpTo(match).insert(insert).debug();

		return ah;
	}

	private IBlockState test(BlockPos pos, IBlockState state)
	{
		IBlockState oldState = null;
		Chunk chunk = null;
		CallbackResult<Boolean> cb = Registries.processPreSetBlock(chunk, pos, oldState, state);
		if (cb.shouldReturn())
			return null;
		return null;
	}
}
