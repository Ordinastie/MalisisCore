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

	@SuppressWarnings("deprecation")
	private AsmHook updateCoordsHook()
	{
		McpMethodMapping func_150807_a = new McpMethodMapping("setBlockIDWithMetadata", "func_150807_a", "net.minecraft.world.chunk.Chunk",
				"(IIILnet/minecraft/block/Block;I)Z");
		McpMethodMapping setExtBlockID = new McpMethodMapping("setExtBlockID", "func_150818_a",
				"net.minecraft.world.chunk.storage.ExtendedBlockStorage", "(IIILnet/minecraft/block/Block;)V");

		AsmHook ah = new AsmHook(func_150807_a);

		//L653: extendedblockstorage.setExtBlockID(p_150807_1_, p_150807_2_ & 15, p_150807_3_, p_150807_4_);
		//	    ALOAD 10
		//	    ILOAD 1
		//	    ILOAD 2
		//	    BIPUSH 15
		//	    IAND
		//	    ILOAD 3
		//	    ALOAD 4
		//	    INVOKEVIRTUAL net/minecraft/world/chunk/storage/ExtendedBlockStorage.setExtBlockID (IIILnet/minecraft/block/Block;)V

		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 10));
		match.add(new VarInsnNode(ILOAD, 1));
		match.add(new VarInsnNode(ILOAD, 2));
		match.add(new IntInsnNode(BIPUSH, 15));
		match.add(new InsnNode(IAND));
		match.add(new VarInsnNode(ILOAD, 3));
		match.add(new VarInsnNode(ALOAD, 4));
		match.add(setExtBlockID.getInsnNode(INVOKEVIRTUAL));

		//		if (!ChunkBlockHandler.get().updateCoordinates(chunk, x, y, z, old, block))
		//			return false;

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkblock/ChunkBlockHandler", "get",
				"()Lnet/malisis/core/util/chunkblock/ChunkBlockHandler;"));
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ILOAD, 12));
		insert.add(new VarInsnNode(ILOAD, 2));
		insert.add(new VarInsnNode(ILOAD, 13));
		insert.add(new VarInsnNode(ALOAD, 8));
		insert.add(new VarInsnNode(ALOAD, 4));
		insert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/chunkblock/ChunkBlockHandler", "updateCoordinates",
				"(Lnet/minecraft/world/chunk/Chunk;IIILnet/minecraft/block/Block;Lnet/minecraft/block/Block;)Z"));
		insert.add(new JumpInsnNode(IFNE, falseLabel));
		insert.add(new InsnNode(ICONST_0));
		insert.add(new InsnNode(IRETURN));
		insert.add(falseLabel);

		ah.jumpTo(match).insert(insert);

		return ah;
	}
}
