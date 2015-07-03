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

package net.malisis.core.util.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import net.malisis.core.util.parser.token.Token;

import org.apache.commons.lang3.mutable.Mutable;

/**
 * @author Ordinastie
 *
 */
public abstract class Parser<T>
{
	protected String text;
	protected Token token = Token.None;
	protected boolean cached = false;
	protected int index = 0;
	protected String matched = "";

	protected Set<Token> listTokens = new LinkedHashSet<>();
	protected Set<Token> ignoreTokens = new HashSet<>();

	//	protected Token currentToken;
	//	protected int currentSize;
	//	protected Token lastToken;
	//	protected int lastSize;

	public Parser(String s)
	{
		text = s;
	}

	protected void withTokens(Token... tokens)
	{
		listTokens.addAll(Arrays.asList(tokens));
	}

	protected void ignoreTokens(Token... tokens)
	{
		withTokens(tokens);
		ignoreTokens.addAll(Arrays.asList(tokens));
	}

	private Token getToken()
	{
		cached = true;
		if (isEnd())
			return token = Token.EndOfInput;

		for (Token t : listTokens)
		{
			if (t.matches(text, index))
			{
				if (ignoreTokens.contains(t))
				{
					index += t.size();
					return token = getToken();
				}
				else
					return token = t;
			}
		}

		return token = Token.None;
	}

	public Token peekToken()
	{
		if (!cached)
			getToken();
		return token;
	}

	public Token readToken()
	{
		forward();
		peekToken();
		return token;
	}

	private void forward()
	{
		if (!cached)
			peekToken();
		cached = false;
		index += token.size();
	}

	public boolean isEnd()
	{
		return index >= text.length();
	}

	public char read()
	{
		return text.charAt(index++);
	}

	public char peek()
	{
		return text.charAt(index);
	}

	public boolean match(Token token)
	{
		return match(token, null);
	}

	public boolean match(Token t, Mutable obj)
	{
		if (!listTokens.contains(t))
			throw new IllegalArgumentException("Tried to match Token " + t + " not present is parser list tokens");

		if (obj != null)
			obj.setValue(null);

		peekToken();
		if (token != t)
			return false;

		if (obj != null)
			obj.setValue(token.getValue());

		matched += token.getValue();
		forward();
		return true;
	}

	public String readUntil(Token... tokens)
	{
		int s = index;
		int e = index;
		while (!isEnd() && !peekToken().isOneOf(tokens))
		{
			forward();
			e = index;
		}

		String txt = text.substring(s, e);
		return txt;

	}

	public abstract T parse();

	public void error(Token expected)
	{
		throw new ParserException("Expecting '" + expected + "' at " + index + " but found " + token);
	}

	public static class ParserException extends RuntimeException
	{
		private static final long serialVersionUID = -3913680544137921678L;//generated

		public ParserException(String message)
		{
			super(message);
		}
	}
}
