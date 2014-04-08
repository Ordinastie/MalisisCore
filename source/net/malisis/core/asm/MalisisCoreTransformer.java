package net.malisis.core.asm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import net.malisis.core.HookRegistering;
import net.minecraft.launchwrapper.IClassTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class MalisisCoreTransformer implements IClassTransformer
{
	public static HashMap<String, ArrayList<AsmHook>> listHooks = new HashMap<>();
	
	public MalisisCoreTransformer()
	{
		HookRegistering.register();
	}
	
	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes)
	{
		ArrayList<AsmHook> hooks = listHooks.get(name);
		if(hooks == null || hooks.size() == 0)
			return bytes;
		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);
		
		for(AsmHook hook : hooks)
		{
			MethodNode methodNode = AsmUtils.findMethod(classNode, hook.getMethodName(), hook.getMethodDescriptor());
			if(methodNode != null)
			{
				for(AsmInsert insert : hook.getInserts())
				{
					if(!insert.injectInto(methodNode))
						System.err.println("COULDN'T FIND INSTRUCTION LIST IN " + hook.getClass() + ":" + hook.getMethodName() + hook.getMethodDescriptor());
				}
			}
					
		}

//		try
//		{
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
		classNode.accept(writer);
		return writer.toByteArray();
//		}
//		catch(Exception e)
//		{
//			//classReader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.err)), 0);			
//		}
//		return bytes;
	}

//	public byte[] addHookToClass(String name, byte[] bytes, boolean obf)
//	{
//		String methodName = "runTick";
//
//		// set up ASM class manipulation stuff. Consult the ASM docs for details
//		ClassNode classNode = new ClassNode();
//		ClassReader classReader = new ClassReader(bytes);
//		classReader.accept(classNode, 0);
//
//		MethodNode m = AsmUtils.findMethod(classNode, methodName, "()V");
//		if (m != null)
//		{
//			MethodInsnNode match = new MethodInsnNode(INVOKESTATIC, "org/lwjgl/input/Keyboard", "getEventKey", "()I");
//			AbstractInsnNode n = AsmUtils.findInstruction(m, match);
//			if (n != null)
//			{
//				System.out.println("Instruction found!");
//				InsnList toInject = new InsnList();
//
//				toInject.add(new LdcInsnNode("Key pressed!"));
//				toInject.add(new MethodInsnNode(INVOKESTATIC, "net/malisis/core/MalisisCore", "Message", "(Ljava/lang/Object;)V"));
//				
//				m.instructions.insertBefore(n, toInject);
//			}
//		}
//
//		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
//		classNode.accept(writer);
//		return writer.toByteArray();
//	}

	public static void register(String className, AsmHook asmHook)
	{
		ArrayList<AsmHook> hooks = listHooks.get(className);
		if(hooks == null)
			hooks = new ArrayList<AsmHook>();
		hooks.add(asmHook);
		listHooks.put(className, hooks);
	}


}
