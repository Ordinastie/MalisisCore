package net.malisis.core.asm;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class MalisisClassTransformer implements IClassTransformer
{
	public HashMap<String, ArrayList<AsmHook>> listHooks = new HashMap<>();

	public MalisisClassTransformer()
	{
		registerHooks();
	}

	public void register(AsmHook ah)
	{
		ArrayList<AsmHook> hooks = listHooks.get(ah.getTargetClass());
		if(hooks == null)
			hooks = new ArrayList<>();
		hooks.add(ah);
		listHooks.put(ah.getTargetClass(), hooks);
	}

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ArrayList<AsmHook> hooks = listHooks.get(name);
		if(hooks == null || hooks.size() == 0)
			return bytes;

		System.out.println("Found hooks for " + name);

		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for(AsmHook hook : hooks)
		{
			MethodNode methodNode = AsmUtils.findMethod(classNode, hook.getMethodName(), hook.getMethodDescriptor());
			if(methodNode != null)
			{
				hook.walkSteps(methodNode);

				if(hook.isDebug())
				{
					AsmUtils.printMethodNode(methodNode);
				}
			}
			else
			{
				System.err.println("Method not found : " + hook.getTargetClass()  + "#" + hook.getMethodName() + hook.getMethodDescriptor());
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS /*| ClassWriter.COMPUTE_FRAMES*/);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	public abstract void registerHooks();

}
