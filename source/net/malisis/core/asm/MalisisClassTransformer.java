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
		registerHooks();
	}

	public void register(AsmHook ah)
	{
		ArrayList<AsmHook> hooks = listHooks.get(ah.getTargetClass());
		if (hooks == null)
			hooks = new ArrayList<AsmHook>();
		hooks.add(ah);
		listHooks.put(ah.getTargetClass(), hooks);
		LogManager.getLogger(logString).info("Hook registered for {}", ah.getTargetClass());;
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
					LogManager.getLogger(logString).error("The instruction list was not found in {}:{}{}",
							hook.getTargetClass(), hook.getMethodName(), hook.getMethodDescriptor());

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
