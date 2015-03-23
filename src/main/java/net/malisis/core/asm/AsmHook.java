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

import net.malisis.core.asm.mappings.McpMethodMapping;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

public class AsmHook
{
	private enum HookStep
	{
		FIND, INSERT, JUMP,
	}

	private static int END = Short.MIN_VALUE;

	private String transformer;
	private McpMethodMapping mapping;
	private boolean debug = false;

	private ArrayList<InsnList> inserts = new ArrayList<>();
	private ArrayList<InsnList> matches = new ArrayList<>();
	private ArrayList<Integer> jumps = new ArrayList<>();
	private ArrayList<HookStep> steps = new ArrayList<>();

	public AsmHook(McpMethodMapping mapping)
	{
		this.mapping = mapping;
	}

	public void setTransformer(String transformerName)
	{
		this.transformer = transformerName;
	}

	public String getTransformer()
	{
		return transformer;
	}

	public AsmHook jumpTo(InsnList match)
	{
		this.steps.add(HookStep.FIND);
		this.matches.add(match);
		this.jump(-1);
		return this;
	}

	public AsmHook jumpToEnd()
	{
		return this.jump(END);
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

	public boolean walkSteps(MethodNode methodNode)
	{
		int index = 0;
		for (HookStep step : steps)
		{
			switch (step)
			{
				case FIND:
					InsnList match = matches.remove(0);
					AbstractInsnNode node = AsmUtils.findInstruction(methodNode, match, index);
					if (node == null)
						return false;
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
					if (jump == END)
						index = methodNode.instructions.size() - 1;
					else
						index += jump;
				default:
					break;
			}
		}

		return true;
	}

	public void register(HashMap<String, ArrayList<AsmHook>> listHooks)
	{
		ArrayList<AsmHook> hooks = listHooks.get(mapping.getTargetClass());
		if (hooks == null)
			hooks = new ArrayList<AsmHook>();
		hooks.add(this);
		listHooks.put(mapping.getTargetClass(), hooks);
	}

	public String getTargetClass()
	{
		return mapping.getTargetClass();
	}

	public String getMethodName()
	{
		return mapping.getName();
	}

	public String getMethodDescriptor()
	{
		return mapping.getDescriptor();
	}

	public boolean isDebug()
	{
		return debug;
	}

}
