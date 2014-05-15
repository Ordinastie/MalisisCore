package net.malisis.core.asm.mappings;

import org.objectweb.asm.tree.MethodInsnNode;

public class McpMethodMapping extends McpMapping<MethodInsnNode>
{

	public McpMethodMapping(String mcp, String srg, String owner, String descriptor)
	{
		super(mcp, srg, owner, descriptor);
	}

	@Override
	public MethodInsnNode getInsnNode(int opcode)
	{
		return new MethodInsnNode(opcode, getOwner(), getName(), getDescriptor());
	}

}
