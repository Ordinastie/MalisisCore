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

package net.malisis.core.util.clientnotif;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.malisis.core.asm.AsmHook;
import net.malisis.core.asm.MalisisClassTransformer;
import net.malisis.core.asm.mappings.McpMethodMapping;

/**
 * @author Ordinastie
 *
 */
public class ClientNotifTransformer extends MalisisClassTransformer
{
	@Override
	public void registerHooks()
	{
		register(clientNotifHook());
		register(worldServerTick());
	}

	@SuppressWarnings("deprecation")
	private AsmHook clientNotifHook()
	{
		McpMethodMapping neighborChanged = new McpMethodMapping("neighborChanged", "func_190524_a", "net/minecraft/world/World",
				"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V");

		AsmHook ah = new AsmHook(neighborChanged);

		//ALOAD 3
		//ALOAD 1
		//ALOAD 2
		//INVOKESTATIC net/malisis/core/util/clientnotif/ClientNotificationManager.notify (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V

		InsnList insert = new InsnList();

		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new VarInsnNode(ALOAD, 1));
		insert.add(new VarInsnNode(ALOAD, 2));
		insert.add(new VarInsnNode(ALOAD, 3));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/clientnotif/ClientNotificationManager", "notify",
				"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;)V"));

		return ah.insert(insert);

	}

	@SuppressWarnings("deprecation")
	private AsmHook worldServerTick()
	{
		McpMethodMapping tick = new McpMethodMapping("tick", "func_72835_b", "net/minecraft/world/WorldServer", "()V");

		AsmHook ah = new AsmHook(tick);

		InsnList insert = new InsnList();

		//ClientNotificationManager.sendNeighborNotification(this);
		insert.add(new VarInsnNode(ALOAD, 0));
		insert.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/util/clientnotif/ClientNotificationManager",
				"sendNeighborNotification", "(Lnet/minecraft/world/World;)V"));

		return ah.jumpToEnd().jump(-2).insert(insert);
	}
}
