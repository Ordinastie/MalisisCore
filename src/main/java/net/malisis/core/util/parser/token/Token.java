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

package net.malisis.core.util.parser.token;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */

public abstract class Token<T>
{
	//EMPTY TOKENS
	public static final EmptyToken None = (EmptyToken) new EmptyToken().name("None");
	public static final EmptyToken Unknown = (EmptyToken) new EmptyToken().name("Unknown");
	public static final EmptyToken Error = (EmptyToken) new EmptyToken().name("Error");
	public static final EmptyToken EndOfInput = (EmptyToken) new EmptyToken().name("EndOfInput");

	//CHAR TOKEN
	public static final CharToken Plus = (CharToken) new CharToken('+').name("Plus");
	public static final CharToken Minus = (CharToken) new CharToken('-').name("Minus");
	public static final CharToken Mult = (CharToken) new CharToken('*').name("Mult");
	public static final CharToken Div = (CharToken) new CharToken('/').name("Div");
	public static final CharToken Sharp = (CharToken) new CharToken('#').name("Sharp");

	public static final CharToken OpenPar = (CharToken) new CharToken('(').name("OpenPar");
	public static final CharToken ClosePar = (CharToken) new CharToken(')').name("ClosePar");
	public static final CharToken OpenCar = (CharToken) new CharToken('[').name("OpenCar");
	public static final CharToken CloseCar = (CharToken) new CharToken(']').name("CloseCar");

	public static final CharToken Not = (CharToken) new CharToken('!').name("Not");
	public static final CharToken Equal = (CharToken) new CharToken('=').name("Equal");
	public static final CharToken Superior = (CharToken) new CharToken('>').name("Superior");
	public static final CharToken Inferior = (CharToken) new CharToken('<').name("Inferior");

	public static final CharToken AndOperator = (CharToken) new CharToken('&').name("AndOperator");
	public static final CharToken OrOperator = (CharToken) new CharToken('|').name("OrOperator");

	public static final CharToken StartWith = (CharToken) new CharToken('$').name("StartWith");
	public static final CharToken EndWith = (CharToken) new CharToken('^').name("EndWith");

	public static final CharToken Quote = (CharToken) new CharToken('"').name("Quote");
	public static final CharToken Apostrophe = (CharToken) new CharToken('\'').name("Apostrophe");
	public static final CharToken UnderScore = (CharToken) new CharToken('_').name("UnderScore");

	public static CharToken Dot = (CharToken) new CharToken('.').name("Dot");
	public static CharToken Comma = (CharToken) new CharToken(',').name("Comma");
	public static CharToken Colon = (CharToken) new CharToken(':').name("Colon");
	public static CharToken SemiColon = (CharToken) new CharToken(';').name("SemiColon");

	//SPECIAL TOKENS
	public static ExpressionToken Identifier = (ExpressionToken) new ExpressionToken("^[^\\d\\W]\\w*").name("Identifier");
	public static ExpressionToken Number = (ExpressionToken) new ExpressionToken("^\\d+").name("Number");
	public static ExpressionToken HexNumber = (ExpressionToken) new ExpressionToken("^#[0-9a-fA-F]+").name("HexNumber");
	public static StringToken StringToken = (StringToken) new StringToken().name("StringToken");
	public static SpaceToken Space = (SpaceToken) new SpaceToken().name("Space");
	public static DigitToken Digit = (DigitToken) new DigitToken().name("Digit");
	public static LetterToken Letter = (LetterToken) new LetterToken().name("Letter");
	//public static KeywordToken Keyword = (KeywordToken) new KeywordToken(null).name("Keyword");

	//Macro,
	//SupOrEq(Superior, Equal),
	//InfOrEq(Inferior, Equal),
	//Different(Not, Equal),
	//IsInclude,
	//IsNotInclude,

	protected String name = "";
	protected T value = null;

	public Token<T> name(String name)
	{
		this.name = name;
		return this;
	}

	public T getValue()
	{
		return value;
	}

	public boolean isOneOf(Token<?>... tokens)
	{
		return ArrayUtils.contains(tokens, this);
	}

	public abstract boolean matches(String s, int index);

	public abstract int size();

	@Override
	public String toString()
	{
		return name + (value != null ? " " + value : "");
	}
}