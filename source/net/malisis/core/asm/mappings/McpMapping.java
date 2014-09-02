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
