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

import java.util.Collection;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public abstract class MalisisClassTransformer implements IClassTransformer
{
	public Multimap<String, AsmHook> listHooks = HashMultimap.create();
	protected String logString;
	protected Logger log;

	public MalisisClassTransformer()
	{
		logString = "malisiscore";
		registerHooks();
	}

	public void register(AsmHook ah)
	{
		ah.setTransformer(this.getClass().getSimpleName());
		listHooks.put(ah.getTargetClass(), ah);
		LogManager.getLogger(logString).info("[{}] Hook registered for {}", ah.getTransformer(), ah.getTargetClass());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		Collection<AsmHook> hooks = listHooks.get(transformedName);
		if (hooks == null || hooks.size() == 0)
			return bytes;

		LogManager.getLogger(logString).info("Found hooks for {} ({})", transformedName, name);

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for (AsmHook hook : hooks)
		{
			MethodNode methodNode = AsmUtils.findMethod(classNode, hook.getMethodName(), hook.getMethodDescriptor());
			if (methodNode != null)
			{
				if (!hook.walkSteps(methodNode))
					LogManager.getLogger(logString).error("[{}] The instruction list was not found in {}:{}{}", hook.getTransformer(),
							hook.getTargetClass(), hook.getMethodName(), hook.getMethodDescriptor());

				if (hook.isDebug() == true && (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment"))
				{
					System.err.println(AsmUtils.getMethodNodeAsString(methodNode));
				}
			}
			else
			{
				LogManager.getLogger(logString).error("[{}] Method not found : {}:{}{}", hook.getTransformer(), hook.getTargetClass(),
						hook.getMethodName(), hook.getMethodDescriptor());
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS /* | ClassWriter.COMPUTE_FRAMES */);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public abstract void registerHooks();

}
