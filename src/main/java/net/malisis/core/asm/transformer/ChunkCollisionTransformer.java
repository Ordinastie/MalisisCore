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

package net.malisis.core.asm.transformer;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpFieldMapping;
import net.malisis.core.asm.mappings.McpMethodMapping;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Ordinastie
 *
 */
public class ChunkCollisionTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(getBoundingBoxesHook());
		register(updateCoordsHook());
	}

	@SuppressWarnings("deprecation")
	private AsmHook getBoundingBoxesHook()
	{
		McpMethodMapping onChunkLoad = new McpMethodMapping("getCollidingBoundingBoxes", "func_72945_a", "net.minecraft.world.World",
				"(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;");
		McpFieldMapping collidingBoundingBoxes = new McpFieldMapping("collidingBoundingBoxes", "field_72998_d",
				"net.minecraft.world.World", "Ljava/util/ArrayList;");

		AsmHook ah = new AsmHook(onChunkLoad);

		//   public static getCollisionBoundingBoxes(Lnet/minecraft/world/World;Lnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V
		//ALOAD 0 this,
		//ALOAD 2 param2 (AABB)
		//ALOAD 0 this.
		//GETFIELD net/minecraft/world/World.collidingBoundingBoxes : Ljava/util/ArrayList;
		//ALOAD 1 param1 (entity)
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(collidingBoundingBoxes.getInsnNode(GETFIELD));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "getCollisionBoundingBoxes",
				"(Lnet/minecraft/world/World;Lnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V"));

		//LDC 0.25
		//DSTORE 9
		InsnList match = new InsnList();
		match.add(new LdcInsnNode(0.25));
		match.add(new VarInsnNode(DSTORE, 13));

		ah.jumpTo(match).jump(-1).insert(insert);

		return ah;
	}

	@SuppressWarnings("deprecation")
	private AsmHook updateCoordsHook()
	{
		McpMethodMapping func_150807_a = new McpMethodMapping("func_150807_a", "func_150807_a", "net.minecraft.world.chunk.Chunk",
				"(IIILnet/minecraft/block/Block;I)Z");
		McpMethodMapping setExtBlockMetadata = new McpMethodMapping("setExtBlockMetadata", "func_76654_b",
				"net.minecraft.world.chunk.storage.ExtendedBlockStorage", "(IIII)V");

		AsmHook ah = new AsmHook(func_150807_a);

		//		  ALOAD 10
		//		    ILOAD 1
		//		    ILOAD 2
		//		    BIPUSH 15
		//		    IAND
		//		    ILOAD 3
		//		    ILOAD 5
		//		    INVOKEVIRTUAL net/minecraft/world/chunk/storage/ExtendedBlockStorage.setExtBlockMetadata (IIII)V
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 10));
		match.add(new VarInsnNode(ILOAD, 1));
		match.add(new VarInsnNode(ILOAD, 2));
		match.add(new IntInsnNode(BIPUSH, 15));
		match.add(new InsnNode(IAND));
		match.add(new VarInsnNode(ILOAD, 3));
		match.add(new VarInsnNode(ILOAD, 5));
		match.add(setExtBlockMetadata.getInsnNode(INVOKEVIRTUAL));

		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ILOAD, 12));
		insert.add(new VarInsnNode(ILOAD, 2));
		insert.add(new VarInsnNode(ILOAD, 13));
		insert.add(new VarInsnNode(ALOAD, 8));
		insert.add(new VarInsnNode(ALOAD, 4));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "updateChunkCollision",
				"(Lnet/minecraft/world/chunk/Chunk;IIILnet/minecraft/block/Block;Lnet/minecraft/block/Block;)V"));

		ah.jumpAfter(match).jumpAfter(match).insert(insert).debug();

		return ah;
	}
}
