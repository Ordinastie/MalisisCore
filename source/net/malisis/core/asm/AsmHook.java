package net.malisis.core.asm;

import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;


public class AsmHook
{
	private enum HookStep
	{
		FIND,
		INSERT,
		JUMP,
	}
	
	private String targetClass;
	private String targetMethod;
	private String targetMethodObf;
	private String targetMethodDescriptor;
	private boolean debug = false;
	
	private ArrayList<InsnList> inserts = new ArrayList<InsnList>();
	private ArrayList<InsnList> matches = new ArrayList<InsnList>();
	private ArrayList<Integer> jumps = new ArrayList<Integer>();
	private ArrayList<HookStep> steps = new ArrayList<HookStep>();
	
	
	public AsmHook(String targetClass, String methodName, String methodDesc)
	{
		this.targetClass = targetClass; 
		this.targetMethod = methodName;
		this.targetMethodObf = getObfuscatedMethodName(methodName);
		this.targetMethodDescriptor = methodDesc;
	}
	
	public AsmHook jumpTo(InsnList match)
	{
		this.steps.add(HookStep.FIND);
		this.matches.add(match);
		this.jump(-1);
		return this;
	}
	public AsmHook jumpAfter(InsnList match)
	{
		this.jumpTo(match);
		this.jump(match.size());
		return this;
	}
	
	
	public AsmHook insert(AbstractInsnNode insert)
	{
		InsnList list = new InsnList();
		list.add(insert);
		return insert(list); 
	}
	public AsmHook insert(InsnList insert)
	{
		this.steps.add(HookStep.INSERT);
		this.inserts.add(insert);
		return this;
	}
	public AsmHook jump(int jump)
	{
		this.steps.add(HookStep.JUMP);
		this.jumps.add(jump);
		return this;
	}
	public AsmHook previous()
	{
		return jump(-1);
	}
	public AsmHook next()
	{
		return jump(1);
	}
	public AsmHook debug()
	{
		this.debug = true;
		return this;
	}
	
	public void walkSteps(MethodNode methodNode)
	{
		int index = 0;
		for(HookStep step : steps)
		{
			switch (step)
			{
				case FIND:
					InsnList match = matches.remove(0);
					AbstractInsnNode node = AsmUtils.findInstruction(methodNode, match);
					if(node == null)
						System.err.println("COULDN'T FIND INSTRUCTION LIST IN " + targetClass + ":" + targetMethod + targetMethodDescriptor);
					else
						index = methodNode.instructions.indexOf(node);
					break;
				case INSERT:
					InsnList insert = inserts.remove(0);
					index += insert.size();
					methodNode.instructions.insert(methodNode.instructions.get(index - insert.size()), insert);
					break;
				case JUMP:
					int jump = jumps.remove(0);
					index += jump;
				default:
					break;
			}
		
		}
		
	}
	

	public void register(HashMap<String, ArrayList<AsmHook>> listHooks)
	{
		ArrayList<AsmHook> hooks = listHooks.get(targetClass);
		if(hooks == null)
			hooks = new ArrayList<AsmHook>();
		hooks.add(this);
		listHooks.put(targetClass, hooks);
	}
	
	
	private String getObfuscatedMethodName(String methodName)
	{
		return targetMethodObf;
	}

	public String getMethodName()
	{
		return targetMethod;
	}

	public String getMethodDescriptor()
	{
		return targetMethodDescriptor;
	}
	
	public boolean isDebug()
	{
		return debug;
	}

	public String getTargetClass()
	{
		return targetClass;
	}

}
