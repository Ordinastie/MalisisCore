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

import java.util.ArrayList;
import java.util.HashMap;

import net.malisis.core.MalisisCore;
import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class MalisisClassTransformer implements IClassTransformer
{
	public HashMap<String, ArrayList<AsmHook>> listHooks = new HashMap();
	protected String logString;
	protected Logger log;

	public MalisisClassTransformer()
	{
		logString = "malisiscore";
		if (MalisisCore.getJavaVersion() >= 1.7)
			registerHooks();
		else
			LogManager.getLogger(logString).warn("Java version detected is {}. Java 1.7 is required to use the event hooks",
					MalisisCore.getJavaVersion());
	}

	public void register(AsmHook ah)
	{
		ArrayList<AsmHook> hooks = listHooks.get(ah.getTargetClass());
		if (hooks == null)
			hooks = new ArrayList<AsmHook>();
		hooks.add(ah);
		listHooks.put(ah.getTargetClass(), hooks);
		LogManager.getLogger(logString).info("Hook registered for {}", ah.getTargetClass());
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ArrayList<AsmHook> hooks = listHooks.get(transformedName);
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
					LogManager.getLogger(logString).error("The instruction list was not found in {}:{}{}", hook.getTargetClass(),
							hook.getMethodName(), hook.getMethodDescriptor());

				if (hook.isDebug() == true && !MalisisCore.isObfEnv)
				{
					System.err.println(AsmUtils.getMethodNodeAsString(methodNode));
				}
			}
			else
			{
				LogManager.getLogger(logString).error("Method not found : {}:{}{}", hook.getTargetClass(), hook.getMethodName(),
						hook.getMethodDescriptor());
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS /* | ClassWriter.COMPUTE_FRAMES */);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public abstract void registerHooks();

}
