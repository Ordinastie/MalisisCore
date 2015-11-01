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

package net.malisis.core.renderer.transformer;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpMethodMapping;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * @author Ordinastie
 *
 */
public class MalisisRendererTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(blockHook());
		register(itemHook());
		register(particleHook());
	}

	private AsmHook blockHook()
	{
		McpMethodMapping renderBlock = new McpMethodMapping(
				"renderBlock",
				"func_175018_a",
				"net/minecraft/client/renderer/BlockRendererDispatcher",
				"(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/BlockPos;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/WorldRenderer;)Z");
		AsmHook ah = new AsmHook(renderBlock);

		//int i = state.getBlock().getRenderType();
		//	    ALOAD 1
		//	    INVOKEINTERFACE net/minecraft/block/state/IBlockState.getBlock ()Lnet/minecraft/block/Block;
		//	    INVOKEVIRTUAL net/minecraft/block/Block.getRenderType ()I
		//	    ISTORE 5
		McpMethodMapping getBlock = new McpMethodMapping("getBlock", "func_177230_c", "net/minecraft/block/state/IBlockState",
				"()Lnet/minecraft/block/Block;");
		McpMethodMapping getRenderType = new McpMethodMapping("getRenderType", "func_149645_b", "net/minecraft/block/Block", "()I");

		InsnList match = new InsnList();
		match.add(new VarInsnNode(ALOAD, 1));
		match.add(getBlock.getInsnNode(INVOKEINTERFACE));
		match.add(getRenderType.getInsnNode(INVOKEVIRTUAL));
		match.add(new VarInsnNode(ISTORE, 5));

		//if (MalisisRegistry.shouldRenderBlock(world, pos, state))
		//	return MalisisRegistry.render(wr, world, pos, state);
		//		ALOAD 6
		//		ALOAD 7
		//		ALOAD 8
		//		INVOKESTATIC net/malisis/core/MalisisRegistry.shouldRenderBlock (Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z
		//		IFEQ L20
		//		ALOAD 6
		//	    ALOAD 7
		//	    ALOAD 8
		//	    ALOAD 9
		//	    INVOKESTATIC net/malisis/core/renderer/BlockRendererRegistry.render (Lnet/minecraft/client/renderer/BlockModelRenderer;Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z
		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/MalisisRegistry", "shouldRenderBlock",
				"(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", false));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new VarInsnNode(ALOAD, 4));
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(
				INVOKESTATIC,
				"net/malisis/core/MalisisRegistry",
				"renderBlock",
				"(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
				false));
		insert.add(new InsnNode(IRETURN));
		insert.add(falseLabel);

		ah.jumpAfter(match).insert(insert);
		return ah;
	}

	private AsmHook itemHook()
	{
		McpMethodMapping renderModel = new McpMethodMapping("renderModel", "func_175045_a",
				"net/minecraft/client/renderer/entity/RenderItem",
				"(Lnet/minecraft/client/resources/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V");
		AsmHook ah = new AsmHook(renderModel);

		//		if (MalisisRegistry.renderItem(itemStack))
		//			return;

		//		ALOAD 3
		//	    INVOKESTATIC net/malisis/core/MalisisRegistry.renderItem (Lnet/minecraft/item/ItemStack;)Z
		//	    IFEQ L6
		//	   L7
		//	    LINENUMBER 119 L7
		//	    ACONST_NULL
		//	    RETURN
		//	   L6

		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/MalisisRegistry", "renderItem", "(Lnet/minecraft/item/ItemStack;)Z",
				false));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new InsnNode(RETURN));
		insert.add(falseLabel);

		ah.insert(insert);
		return ah;
	}

	private AsmHook particleHook()
	{
		McpMethodMapping getTexture = new McpMethodMapping("getTexture", "func_178122_a", "net/minecraft/client/renderer/BlockModelShapes",
				"(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;");
		AsmHook ah = new AsmHook(getTexture);

		//if (state.getBlock() instanceof IMetaIconProvider)
		//	return MalisisRegistry.getParticleIcon(state);
		//		ALOAD 1
		//		INVOKEINTERFACE net/minecraft/block/state/IBlockState.getBlock ()Lnet/minecraft/block/Block;
		//		INSTANCEOF net/malisis/core/renderer/IMalisisRendered
		//		IFEQ L2
		//	    ALOAD 1
		//	    INVOKESTATIC net/malisis/core/MalisisRegistry.getParticleIcon (Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;
		//		ARETURN

		McpMethodMapping getBlock = new McpMethodMapping("getBlock", "func_177230_c", "net/minecraft/block/state/IBlockState",
				"()Lnet/minecraft/block/Block;");
		LabelNode falseLabel = new LabelNode();
		InsnList insert = new InsnList();
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(getBlock.getInsnNode(INVOKEINTERFACE));
		insert.add(new TypeInsnNode(INSTANCEOF, "net/malisis/core/renderer/icon/IMetaIconProvider"));
		insert.add(new JumpInsnNode(IFEQ, falseLabel));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/MalisisRegistry", "getParticleIcon",
				"(Lnet/minecraft/block/state/IBlockState;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;", false));
		insert.add(new InsnNode(ARETURN));
		insert.add(falseLabel);

		ah.insert(insert);

		return ah;
	}
}
