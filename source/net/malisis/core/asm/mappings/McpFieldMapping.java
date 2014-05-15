package net.malisis.core.asm.mappings;

import org.objectweb.asm.tree.FieldInsnNode;

public class McpFieldMapping extends McpMapping<FieldInsnNode>
{

	public McpFieldMapping(String mcp, String srg, String owner, String descriptor)
	{
		super(mcp, srg, owner, descriptor);
	}

	@Override
	public FieldInsnNode getInsnNode(int opcode)
	{
		return new FieldInsnNode(opcode, getOwner(), getName(), getDescriptor());
	}

}
