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

package net.malisis.core.asm;

import static org.objectweb.asm.Opcodes.*;
import net.malisis.core.asm.mappings.McpMethodMapping;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class MalisisCoreTransformer extends MalisisClassTransformer
{

	@Override
	public void registerHooks()
	{
		register(keyboardEventHook());
		register(userAttackEntityEventHook());
	}

	public AsmHook userAttackEntityEventHook()
	{
		McpMethodMapping attackEntity = new McpMethodMapping("attackEntity", "func_78764_a",
				"net.minecraft.client.multiplayer.PlayerControllerMP",
				"(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V");

		AsmHook ah = new AsmHook(attackEntity);

		LabelNode falseLabel = new LabelNode();
		InsnList insert1 = new InsnList();
		insert1.add(new TypeInsnNode(NEW, "net/malisis/core/event/user/UserAttackEvent"));
		insert1.add(new InsnNode(DUP));
		insert1.add(new VarInsnNode(ALOAD, 1));
		insert1.add(new VarInsnNode(ALOAD, 2));
		insert1.add(new MethodInsnNode(INVOKESPECIAL, "net/malisis/core/event/user/UserAttackEvent", "<init>",
				"(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V"));
		insert1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/event/user/UserAttackEvent", "post", "()Z"));
		insert1.add(new JumpInsnNode(IFEQ, falseLabel));
		insert1.add(new InsnNode(RETURN));
		insert1.add(falseLabel);

		ah.insert(insert1);

		return ah;
	}

	public AsmHook keyboardEventHook()
	{
		McpMethodMapping runTick = new McpMethodMapping("runTick", "func_71407_l", "net.minecraft.client.Minecraft", "()V");
		McpMethodMapping setKeyBindState = new McpMethodMapping("setKeyBindState", "func_74510_a",
				"net/minecraft/client/settings/KeyBinding", "(IZ)V");

		AsmHook ah = new AsmHook(runTick);

		LabelNode falseLabel = new LabelNode();
		// if(new KeyboardEvent().post())
		InsnList insert1 = new InsnList();
		insert1.add(new TypeInsnNode(NEW, "net/malisis/core/event/user/KeyboardEvent"));
		insert1.add(new InsnNode(DUP));
		insert1.add(new MethodInsnNode(INVOKESPECIAL, "net/malisis/core/event/user/KeyboardEvent", "<init>", "()V"));
		insert1.add(new MethodInsnNode(INVOKEVIRTUAL, "net/malisis/core/event/user/KeyboardEvent", "post", "()Z"));
		insert1.add(new JumpInsnNode(IFNE, falseLabel));

		// L1844: KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
		InsnList match1 = new InsnList();
		match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKey", "()I"));
		match1.add(new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKeyState", "()Z"));
		match1.add(setKeyBindState.getInsnNode(INVOKESTATIC));

		// L1973: FMLCommonHandler.instance().fireKeyInput()
		InsnList match2 = new InsnList();
		match2.add(new MethodInsnNode(INVOKESTATIC, "cpw/mods/fml/common/FMLCommonHandler", "instance",
				"()Lcpw/mods/fml/common/FMLCommonHandler;"));
		match2.add(new MethodInsnNode(INVOKEVIRTUAL, "cpw/mods/fml/common/FMLCommonHandler", "fireKeyInput", "()V"));

		ah.jumpAfter(match1).insert(insert1).jumpAfter(match2).insert(falseLabel);

		return ah;

	}

}
