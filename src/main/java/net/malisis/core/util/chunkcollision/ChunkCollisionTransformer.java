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

package net.malisis.core.util.chunkcollision;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpFieldMapping;
import net.malisis.core.asm.mappings.McpMethodMapping;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
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
		register(rayTraceHook());
		register(placeBlockHook());
		register(blockReachDistanceHook());
	}

	@SuppressWarnings("deprecation")
	private AsmHook getBoundingBoxesHook()
	{
		McpMethodMapping getCubes = new McpMethodMapping("getCollisionBoxes", "func_184144_a", "net.minecraft.world.World",
				"(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;");

		AsmHook ah = new AsmHook(getCubes);

		//   public static getCollisionBoundingBoxes(Lnet/minecraft/world/World;Lnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V
		//ALOAD 0 this,
		//ALOAD 2 param2 (AABB)
		//ALOAD 3
		//ALOAD 1 param1 (entity)
		InsnList insert = new InsnList();
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "get",
				"()Lnet/malisis/core/util/chunkcollision/ChunkCollision;"));
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/chunkcollision/ChunkCollision", "getCollisionBoundingBoxes",
				"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V"));

		//ALOAD 0
		//ALOAD 1
		//ALOAD 2
		//LDC 0.25
		InsnList match = new InsnList();

		match.add(new VarInsnNode(ALOAD, 0));
		match.add(new VarInsnNode(ALOAD, 1));
		match.add(new VarInsnNode(ALOAD, 2));
		match.add(new LdcInsnNode(0.25));

		ah.jumpTo(match).jump(-1).insert(insert);

		return ah;
	}

	@SuppressWarnings("deprecation")
	private AsmHook rayTraceHook()
	{
		McpMethodMapping func_147447_a = new McpMethodMapping("rayTraceBlocks", "func_147447_a", "net.minecraft.world.World",
				"(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;ZZZ)Lnet/minecraft/util/math/RayTraceResult;");

		AsmHook ah = new AsmHook(func_147447_a);

		//setRayTraceInfos(Lnet/minecraft/world/World;Lnet/minecraft/util/Vec3d;Lnet/minecraft/util/Vec3;)V
		InsnList setRayTraceInfos = new InsnList();
		setRayTraceInfos.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "get",
				"()Lnet/malisis/core/util/chunkcollision/ChunkCollision;"));
		setRayTraceInfos.add(new VarInsnNode(ALOAD, 1));
		setRayTraceInfos.add(new VarInsnNode(ALOAD, 2));
		setRayTraceInfos.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/chunkcollision/ChunkCollision", "setRayTraceInfos",
				"(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)V"));

		//L966 return movingobjectposition;
		//ALOAD 15
		//ARETURN
		InsnList returnMop = new InsnList();
		returnMop.add(new VarInsnNode(ALOAD, 15));
		returnMop.add(new InsnNode(ARETURN));

		//Before L966
		//getRayTraceResult(World world, MovingObjectPosition mop, Vec3 src, Vec3 dest)
		InsnList insertMop = new InsnList();
		insertMop.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "get",
				"()Lnet/malisis/core/util/chunkcollision/ChunkCollision;"));
		insertMop.add(new VarInsnNode(ALOAD, 0));
		insertMop.add(new VarInsnNode(ALOAD, 15));
		insertMop.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/chunkcollision/ChunkCollision", "getRayTraceResult",
				"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/RayTraceResult;)Lnet/minecraft/util/math/RayTraceResult;"));
		insertMop.add(new VarInsnNode(ASTORE, 15));

		//L982 return returnLastUncollidableBlock ? movingobjectposition2 : null;
		//ILOAD 5
		//IFEQ L26
		//ALOAD 14
		//GOTO L27
		InsnList returnMop2OrNull = new InsnList();
		returnMop2OrNull.add(new VarInsnNode(ILOAD, 5));
		returnMop2OrNull.add(new JumpInsnNode(IFEQ, null));
		returnMop2OrNull.add(new VarInsnNode(ALOAD, 15));
		returnMop2OrNull.add(new JumpInsnNode(GOTO, null));

		//Before L982
		//getRayTraceResult(World world, MovingObjectPosition mop, Vec3d src, Vec3d dest)
		//insertMop_2 = insertMop
		InsnList insertMop_2 = AsmUtils.cloneList(insertMop);

		//L1101 return movingobjectposition1;
		//ALOAD 41
		//ARETURN
		InsnList returnMop1 = new InsnList();
		returnMop1.add(new VarInsnNode(ALOAD, 41));
		returnMop1.add(new InsnNode(ARETURN));

		//Before L1101
		//getRayTraceResult(World world, MovingObjectPosition mop, Vec3d src, Vec3d dest)
		InsnList insertMop1 = new InsnList();
		insertMop1.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "get",
				"()Lnet/malisis/core/util/chunkcollision/ChunkCollision;"));
		insertMop1.add(new VarInsnNode(ALOAD, 0));
		insertMop1.add(new VarInsnNode(ALOAD, 41));
		insertMop1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/util/chunkcollision/ChunkCollision", "getRayTraceResult",
				"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/RayTraceResult;)Lnet/minecraft/util/math/RayTraceResult;"));
		insertMop1.add(new VarInsnNode(ASTORE, 41));

		//L1111 return returnLastUncollidableBlock ? movingobjectposition2 : null;
		//returnMop2OrNull

		//Before L1111
		//insertMop
		//insertMop_3 = insertMop
		InsnList insertMop_3 = AsmUtils.cloneList(insertMop);

		//@formatter:off
		ah.insert(setRayTraceInfos)
			.jumpTo(returnMop).insert(insertMop) //L966
			.jumpTo(returnMop2OrNull).insert(insertMop_2) //L982
			.jumpTo(returnMop1).insert(insertMop1) //L1101
			.jumpTo(returnMop2OrNull).insert(insertMop_3); //L1111
		//@formatter:on

		return ah;
	}

	@SuppressWarnings("deprecation")
	private AsmHook placeBlockHook()
	{
		McpMethodMapping onItemUse = new McpMethodMapping(
				"onItemUse",
				"func_180614_a",
				"net/minecraft/item/ItemBlock",
				"(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;");

		McpMethodMapping getItemStackMetadata = new McpMethodMapping("getMetadata", "func_77960_j", "net/minecraft/item/ItemStack", "()I");
		McpMethodMapping getItemMetadata = new McpMethodMapping("getMetadata", "func_77647_b", "net/minecraft/item/ItemBlock", "(I)I");
		McpFieldMapping block = new McpFieldMapping("block", "field_150939_a", "net/minecraft/item/ItemBlock",
				"Lnet/minecraft/block/Block;");
		McpFieldMapping fail = new McpFieldMapping("FAIL", "FAIL", "net/minecraft/util/EnumActionResult",
				"Lnet/minecraft/util/EnumActionResult;");

		AsmHook ah = new AsmHook(onItemUse);

		//		int i1 = this.getMetadata(p_77648_1_.getMetadata());
		//	    ALOAD 0
		//	    ALOAD 1
		//	    INVOKEVIRTUAL net/minecraft/item/ItemStack.getMetadata ()I
		//	    INVOKEVIRTUAL net/minecraft/item/ItemBlock.getMetadata (I)I
		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 0));
		match.add(new VarInsnNode(ALOAD, 1));
		match.add(getItemStackMetadata.getInsnNode(INVOKEVIRTUAL));
		match.add(getItemMetadata.getInsnNode(INVOKEVIRTUAL));

		//		if (!ChunkCollision.get().canPlaceBlockAt(itemStack, player, world, block, pos, hand, side))
		//			return null;
		//		 IFNE L1
		//		   L2
		//		    LINENUMBER 412 L2
		//		    ICONST_0
		//		    IRETURN
		//		   L1
		InsnList insert = new InsnList();
		LabelNode label = new LabelNode();
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/chunkcollision/ChunkCollision", "get",
				"()Lnet/malisis/core/util/chunkcollision/ChunkCollision;"));
		insert.add(new VarInsnNode(ALOAD, 1)); //itemStack,
		insert.add(new VarInsnNode(ALOAD, 2)); //player,
		insert.add(new VarInsnNode(ALOAD, 3)); //world,
		insert.add(new VarInsnNode(ALOAD, 0)); //this.
		insert.add(block.getInsnNode(GETFIELD)); //blockInstance
		insert.add(new VarInsnNode(ALOAD, 4)); //pos,
		insert.add(new VarInsnNode(ALOAD, 5)); //hand,
		insert.add(new VarInsnNode(ALOAD, 6)); //side,
		insert.add(new MethodInsnNode(
				INVOKEVIRTUAL,
				"net/malisis/core/util/chunkcollision/ChunkCollision",
				"canPlaceBlockAt",
				"(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;)Z"));
		insert.add(new JumpInsnNode(IFNE, label));
		insert.add(fail.getInsnNode(GETSTATIC));
		insert.add(new InsnNode(ARETURN));
		insert.add(label);

		ah.jumpTo(match).insert(insert);

		return ah;
	}

	private AsmHook blockReachDistanceHook()
	{
		McpMethodMapping processPlayerDigging = new McpMethodMapping("processPlayerDigging", "func_147345_a",
				"net/minecraft/network/NetHandlerPlayServer", "(Lnet/minecraft/network/play/client/CPacketPlayerDigging;)V");

		McpFieldMapping playerEntity = new McpFieldMapping("playerEntity", "field_147369_b", "net/minecraft/network/NetHandlerPlayServer",
				"Lnet/minecraft/entity/player/EntityPlayerMP;");
		McpFieldMapping worldObj = new McpFieldMapping("worldObj", "field_70170_p", "net/minecraft/entity/player/EntityPlayerMP",
				"Lnet/minecraft/world/World;");
		McpMethodMapping getBlockState = new McpMethodMapping("getBlockState", "func_180495_p", "net/minecraft/world/World",
				"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;");
		McpMethodMapping getBlock = new McpMethodMapping("getBlock", "func_177230_c", "net/minecraft/block/state/IBlockState",
				"()Lnet/minecraft/block/Block;");

		AsmHook ah = new AsmHook(processPlayerDigging);

		//double d3 = d0 * d0 + d1 * d1 + d2 * d2;
		//		DMUL
		//		DADD
		//		DSTORE 11
		InsnList match = new InsnList();
		match.add(new InsnNode(DMUL));
		match.add(new InsnNode(DADD));
		match.add(new VarInsnNode(DSTORE, 11));

		//if (this.playerEntity.worldObj.getBlockState(pos).getBlock() instanceof IChunkCollidable)
		//			d3 = 0;
		//		ALOAD 0
		//	    GETFIELD net/minecraft/network/NetHandlerPlayServer.playerEntity : Lnet/minecraft/entity/player/EntityPlayer;
		//	    GETFIELD net/minecraft/entity/player/EntityPlayer.worldObj : Lnet/minecraft/world/World;
		//	    ALOAD 8
		//	    INVOKEVIRTUAL net/minecraft/world/World.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;
		//	    INVOKEINTERFACE net/minecraft/block/state/IBlockState.getBlock ()Lnet/minecraft/block/Block;
		//	    INSTANCEOF net/malisis/core/util/chunkcollision/IChunkCollidable
		//	    IFEQ L23
		//	   L24
		//	    LINENUMBER 274 L24
		//	    DCONST_0
		//	    DSTORE 11
		//	   L23
		InsnList insert = new InsnList();
		LabelNode label = new LabelNode();
		insert.add(new VarInsnNode(ALOAD, 0)); //this.
		insert.add(playerEntity.getInsnNode(GETFIELD)); //playerEntity.
		insert.add(worldObj.getInsnNode(GETFIELD)); //worldObj.
		insert.add(new VarInsnNode(ALOAD, 3)); //pos
		insert.add(getBlockState.getInsnNode(INVOKEVIRTUAL));//getBlockState()
		insert.add(getBlock.getInsnNode(INVOKEINTERFACE));//getBlock()
		insert.add(new TypeInsnNode(INSTANCEOF, "net/malisis/core/util/chunkcollision/IChunkCollidable")); //instanceof IChunkCollidable
		insert.add(new JumpInsnNode(IFEQ, label));
		insert.add(new InsnNode(DCONST_0));
		insert.add(new VarInsnNode(DSTORE, 11));
		insert.add(label);

		ah.jumpAfter(match).insert(insert);

		return ah;

	}
}
