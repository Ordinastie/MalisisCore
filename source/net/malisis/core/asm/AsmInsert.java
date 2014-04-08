package net.malisis.core.asm;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class AsmInsert
{
	public static int INSERT_BEFORE = 0;
	public static int INSERT_AFTER = 1;
	
	private InsnList matchingInsn;
	private InsnList insertInsn;
	private int insertType = INSERT_AFTER;
	
	public AsmInsert(InsnList insertInsn)
	{
		this.insertInsn = insertInsn;
	}
	public AsmInsert(AbstractInsnNode insertIsnsn)
	{
		this.insertInsn = new InsnList();
		this.insertInsn.add(insertIsnsn);
	}
	
	public AsmInsert before(InsnList matchingInsn)
	{
		this.insertType = INSERT_BEFORE;
		this.matchingInsn = matchingInsn;
		return this;
	}
	
	public AsmInsert after(InsnList matchingInsn)
	{
		this.insertType = INSERT_AFTER;
		this.matchingInsn = matchingInsn;
		return this;
	}
	
	public boolean injectInto(MethodNode node)
	{
		AbstractInsnNode n = AsmUtils.findInstruction(node, matchingInsn);
		if(n != null)
		{
			if(insertType == INSERT_BEFORE)
				node.instructions.insertBefore(n, insertInsn);
			else
				node.instructions.insert(n, insertInsn);
			return true;
		}
		return false;
	}

}
