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

import static org.objectweb.asm.tree.AbstractInsnNode.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.ListIterator;

import net.malisis.core.MalisisCore;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class AsmUtils
{
	/**
	 * Finds the method with the given name. If multiple methods with the same name exist, the first one will be returned
	 *
	 * @param clazz the class
	 * @param name the method name to search for
	 * @return the first method with the given name or null if no such method is found
	 */
	public static MethodNode findMethod(ClassNode clazz, String name)
	{
		for (MethodNode method : clazz.methods)
		{
			if (method.name.equals(name))
			{
				return method;
			}
		}
		return null;
	}

	/**
	 * Finds the method with the given name and method descriptor.
	 *
	 * @param clazz the class
	 * @param name the method name to search for
	 * @param desc the method descriptor to search for
	 * @return the method with the given name and descriptor or null if no such method is found
	 * @see org.objectweb.asm.Type#getMethodDescriptor
	 */
	public static MethodNode findMethod(ClassNode clazz, String name, String desc)
	{
		for (MethodNode method : clazz.methods)
		{
			if (method.name.equals(name) && method.desc.equals(desc))
			{
				return method;
			}
		}
		return null;
	}

	/**
	 * Finds instruction a specific instruction list inside a method, starting from the begining.
	 *
	 * @param method the method
	 * @param matches the matches
	 * @return the abstract insn node
	 */
	public static AbstractInsnNode findInstruction(MethodNode method, InsnList matches)
	{
		return findInstruction(method, matches, 0);
	}

	/**
	 * Finds instruction a specific instruction list inside a method, starting from the specified index.
	 *
	 * @param method the method
	 * @param matches the matches
	 * @param index the index
	 * @return the abstract insn node
	 */
	public static AbstractInsnNode findInstruction(MethodNode method, InsnList matches, int index)
	{
		AbstractInsnNode node = method.instructions.get(index);
		AbstractInsnNode match = matches.getFirst();
		while (node != null)
		{
			if (insnEqual(node, match))
			{
				AbstractInsnNode m = match.getNext();
				AbstractInsnNode n = node.getNext();
				while (m != null && n != null && insnEqual(m, n))
				{
					m = m.getNext();
					n = n.getNext();
				}
				if (m == null)
					return node;
			}

			node = node.getNext();
		}
		return null;
	}

	/**
	 * Checks if two {@link AbstractInsnNode} are equals.
	 *
	 * @param node1 the node1
	 * @param node2 the node2
	 * @return true, if equal
	 */
	public static boolean insnEqual(AbstractInsnNode node1, AbstractInsnNode node2)
	{
		if (node1 == null || node2 == null || node1.getOpcode() != node2.getOpcode())
			return false;

		switch (node2.getType())
		{
			case VAR_INSN:
				return varInsnEqual((VarInsnNode) node1, (VarInsnNode) node2);
			case TYPE_INSN:
				return typeInsnEqual((TypeInsnNode) node1, (TypeInsnNode) node2);
			case FIELD_INSN:
				return fieldInsnEqual((FieldInsnNode) node1, (FieldInsnNode) node2);
			case METHOD_INSN:
				return methodInsnEqual((MethodInsnNode) node1, (MethodInsnNode) node2);
			case LDC_INSN:
				return ldcInsnEqual((LdcInsnNode) node1, (LdcInsnNode) node2);
			case IINC_INSN:
				return iincInsnEqual((IincInsnNode) node1, (IincInsnNode) node2);
			case INT_INSN:
				return intInsnEqual((IntInsnNode) node1, (IntInsnNode) node2);
			default:
				return true;
		}
	}

	/**
	 * Checks if two {@link VarInsnNode} are equals.
	 *
	 * @param insn1 the insn1
	 * @param insn2 the insn2
	 * @return true, if successful
	 */
	public static boolean varInsnEqual(VarInsnNode insn1, VarInsnNode insn2)
	{
		if (insn1.var == -1 || insn2.var == -1)
			return true;

		return insn1.var == insn2.var;
	}

	/**
	 * Checks if two {@link MethodInsnNode} are equals.
	 *
	 * @param insn1 the insn1
	 * @param insn2 the insn2
	 * @return true, if successful
	 */
	public static boolean methodInsnEqual(MethodInsnNode insn1, MethodInsnNode insn2)
	{
		return insn1.owner.equals(insn2.owner) && insn1.name.equals(insn2.name) && insn1.desc.equals(insn2.desc);
	}

	/**
	 * Checks if two {@link FieldInsnNode} are equals.
	 *
	 * @param insn1 the insn1
	 * @param insn2 the insn2
	 * @return true, if successful
	 */
	public static boolean fieldInsnEqual(FieldInsnNode insn1, FieldInsnNode insn2)
	{
		return insn1.owner.equals(insn2.owner) && insn1.name.equals(insn2.name) && insn1.desc.equals(insn2.desc);
	}

	/**
	 * Checks if two {@link LdcInsnNode} are equals.
	 *
	 * @param insn1 the insn1
	 * @param insn2 the insn2
	 * @return true, if successful
	 */
	public static boolean ldcInsnEqual(LdcInsnNode insn1, LdcInsnNode insn2)
	{
		if (insn1.cst.equals("~") || insn2.cst.equals("~"))
			return true;

		return insn1.cst.equals(insn2.cst);
	}

	/**
	 * Checks if two {@link TypeInsnNode} are equals.
	 *
	 * @param insn1 the insn1
	 * @param insn2 the insn2
	 * @return true, if successful
	 */
	public static boolean typeInsnEqual(TypeInsnNode insn1, TypeInsnNode insn2)
	{
		if (insn1.desc.equals("~") || insn2.desc.equals("~"))
			return true;

		return insn1.desc.equals(insn2.desc);
	}

	/**
	 * Checks if two {@link IincInsnNode} are equals.
	 *
	 * @param node1 the node1
	 * @param node2 the node2
	 * @return true, if successful
	 */
	public static boolean iincInsnEqual(IincInsnNode node1, IincInsnNode node2)
	{
		return node1.var == node2.var && node1.incr == node2.incr;
	}

	/**
	 * Checks if two {@link IntInsnNode} are equals.
	 *
	 * @param node1 the node1
	 * @param node2 the node2
	 * @return true, if successful
	 */
	public static boolean intInsnEqual(IntInsnNode node1, IntInsnNode node2)
	{
		if (node1.operand == -1 || node2.operand == -1)
			return true;

		return node1.operand == node2.operand;
	}

	/**
	 * Gets the {@link MethodNode} as string.
	 *
	 * @param methodNode the method node
	 * @return the method node as string
	 */
	public static String getMethodNodeAsString(MethodNode methodNode)
	{
		Printer printer = new Textifier();
		TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);

		methodNode.accept(methodPrinter);

		StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();

		return sw.toString();
	}

	/**
	 * Clones a {@link InsnList}.
	 *
	 * @param list the list
	 * @return the insn list
	 */
	public static InsnList cloneList(InsnList list)
	{
		InsnList clone = new InsnList();
		ListIterator<AbstractInsnNode> it = list.iterator();
		while (it.hasNext())
			clone.add(it.next().clone(Collections.EMPTY_MAP));

		return clone;
	}

	/**
	 * Changes the access level for the specified field for a class.
	 *
	 * @param clazz the clazz
	 * @param fieldName the field name
	 * @return the field
	 */
	public static Field changeFieldAccess(Class clazz, String fieldName)
	{
		return changeFieldAccess(clazz, fieldName, fieldName, false);
	}

	/**
	 * Changes the access level for the specified field for a class.
	 *
	 * @param clazz the clazz
	 * @param fieldName the field name
	 * @param srgName the srg name
	 * @return the field
	 */
	public static Field changeFieldAccess(Class clazz, String fieldName, String srgName)
	{
		return changeFieldAccess(clazz, fieldName, srgName, false);
	}

	/**
	 * Changes the access level for the specified field for a class.
	 *
	 * @param clazz the clazz
	 * @param fieldName the field name
	 * @param srgName the srg name
	 * @param silenced the silenced
	 * @return the field
	 */
	public static Field changeFieldAccess(Class clazz, String fieldName, String srgName, boolean silenced)
	{
		try
		{
			Field f = clazz.getDeclaredField(MalisisCore.isObfEnv ? srgName : fieldName);
			f.setAccessible(true);
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);

			return f;
		}
		catch (ReflectiveOperationException e)
		{
			if (!silenced)
				MalisisCore.log.error("Could not change access for field " + clazz.getSimpleName() + "."
						+ (MalisisCore.isObfEnv ? srgName : fieldName), e);
			return null;
		}

	}

	/**
	 * Changes the access level for the specified method for a class.
	 *
	 * @param clazz the clazz
	 * @param methodName the method name
	 * @param params the params
	 * @return the method
	 */
	public static Method changeMethodAccess(Class clazz, String methodName, String params)
	{
		return changeMethodAccess(clazz, methodName, methodName, params);
	}

	/**
	 * Changes the access level for the specified method for a class.
	 *
	 * @param clazz the clazz
	 * @param methodName the method name
	 * @param srgName the srg name
	 * @param params the params
	 * @return the method
	 */
	public static Method changeMethodAccess(Class clazz, String methodName, String srgName, String params)
	{
		return changeMethodAccess(clazz, methodName, srgName, false, new MethodDescriptor(params).getParams());
	}

	/**
	 * Changes the access level for the specified method for a class.
	 *
	 * @param clazz the clazz
	 * @param methodName the field name
	 * @return the field
	 */
	public static Method changeMethodAccess(Class clazz, String methodName, Class<?>... params)
	{
		return changeMethodAccess(clazz, methodName, methodName, false, params);
	}

	/**
	 * Changes the access level for the specified method for a class.
	 *
	 * @param clazz the clazz
	 * @param methodName the method name
	 * @param srgName the srg name
	 * @param params the params
	 * @return the method
	 */
	public static Method changeMethodAccess(Class clazz, String methodName, String srgName, Class<?>... params)
	{
		return changeMethodAccess(clazz, methodName, srgName, false, params);
	}

	/**
	 * Changes the access level for the specified method for a class.
	 *
	 * @param clazz the clazz
	 * @param methodName the field name
	 * @param srgName the srg name
	 * @param silenced the silenced
	 * @param params the params
	 * @return the field
	 */
	public static Method changeMethodAccess(Class clazz, String methodName, String srgName, boolean silenced, Class<?>... params)
	{
		try
		{
			Method m = clazz.getDeclaredMethod(MalisisCore.isObfEnv ? srgName : methodName, params);
			m.setAccessible(true);
			return m;
		}
		catch (ReflectiveOperationException e)
		{
			MalisisCore.log.error("Could not change access for method " + clazz.getSimpleName() + "."
					+ (MalisisCore.isObfEnv ? srgName : methodName), e);
		}

		return null;
	}
}
