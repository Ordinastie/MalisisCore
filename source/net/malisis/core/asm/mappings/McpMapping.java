package net.malisis.core.asm.mappings;

import net.minecraft.launchwrapper.Launch;

import org.objectweb.asm.tree.AbstractInsnNode;

public abstract class McpMapping<T extends AbstractInsnNode>
{
	protected String srgName;
	protected String mcpName;
	protected String owner;
	protected String descriptor;
	private boolean isObfEnv = !(boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	
	public McpMapping(String mcp, String srg, String owner, String descriptor)
	{
		this.mcpName = mcp;
		this.srgName = srg;
		this.owner = owner;
		this.descriptor = descriptor;
	}
	
	public String getOwner()
	{
		return owner.replace('.', '/');
	}
	public String getTargetClass()
	{
		return owner.replace('/', '.');
	}
	
	public String getName()
	{
		return isObfEnv ? srgName : mcpName;
	}
	
	public String getDescriptor()
	{
		return descriptor;
	}
	
	public abstract T getInsnNode(int opcode);

}
