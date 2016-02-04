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

import static net.malisis.core.util.parser.token.Token.*;
import net.malisis.core.util.parser.Parser;
import net.malisis.core.util.parser.token.Token;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

/**
 * @author Ordinastie
 *
 */
public class MethodDescriptor
{
	private Class<?>[] params;
	private Class<?> returnType;

	public MethodDescriptor(Class<?> returnType, Class<?>... params)
	{
		this.params = params;
		this.returnType = returnType;
	}

	public MethodDescriptor(String descriptor)
	{
		new MethodSignatureParser(this, descriptor).parse();
	}

	public Class<?>[] getParams()
	{
		return params;
	}

	public Class<?> getReturnType()
	{
		return returnType;
	}

	@Override
	public String toString()
	{
		String str = "(";
		if (params != null)
			for (Class<?> c : params)
				str += c.getName();
		str += ")";
		if (returnType != null)
			str += returnType.getName();
		return str;
	}

	private static class MethodSignatureParser extends Parser<MethodDescriptor>
	{
		MethodDescriptor desc;

		public MethodSignatureParser(MethodDescriptor desc, String s)
		{
			super(s);
			this.desc = desc;
			withTokens(TypeToken.token, Colon, OpenCar, OpenPar, ClosePar);
		}

		@Override
		public MethodDescriptor parse()
		{
			boolean isParam = true;
			Mutable<Class> clazz = new MutableObject<>();
			while (!isEnd())
			{
				if (match(OpenPar))
					isParam = true;
				else if (match(ClosePar))
				{
					if (!isParam)
						error(OpenPar);
					isParam = false;
				}
				else if (match(TypeToken.token, clazz))
					if (isParam)
						desc.params = ArrayUtils.add(desc.params, clazz.getValue());
					else
						desc.returnType = clazz.getValue();
				else
					readToken();
			}

			return desc;
		}
	}

	private static class TypeToken extends Token<Class<?>>
	{
		private static TypeToken token = (TypeToken) new TypeToken().name("TypeToken");

		private int size;

		@Override
		public boolean matches(String s, int index)
		{
			value = null;
			size = 1;
			char c = s.charAt(index);
			switch (c)
			{
				case 'Z':
					value = boolean.class;
					break;
				case 'B':
					value = byte.class;
					break;
				case 'C':
					value = char.class;
					break;
				case 'S':
					value = short.class;
					break;
				case 'I':
					value = int.class;
					break;
				case 'J':
					value = long.class;
					break;
				case 'F':
					value = float.class;
					break;
				case 'D':
					value = double.class;
					break;
				case 'V':
					value = void.class;
					break;
				case '[':
				case 'L':
					value = getClass(s, index);
					break;
			}

			return value != null;
		}

		private Class<?> getClass(String s, int index)
		{
			boolean isArray = s.charAt(index) == '[';
			int e = s.indexOf(';', index);
			if (e == -1)
				return null;
			try
			{
				String name = s.substring(isArray ? index : index + 1, isArray ? e + 1 : e).replace('/', '.');
				size = name.length() + (isArray ? 0 : 2);
				return Class.forName(name);
			}
			catch (ClassNotFoundException e1)
			{
				e1.printStackTrace();
				return null;
			}
		}

		@Override
		public int size()
		{
			return size;
		}

	}
}
