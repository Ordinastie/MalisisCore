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

package net.malisis.core.util.bbcode.gui;

import java.util.EnumSet;

import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.util.bbcode.BBString;
import net.malisis.core.util.bbcode.node.BBColorNode;
import net.malisis.core.util.bbcode.node.BBItemNode;
import net.malisis.core.util.bbcode.node.BBNode;
import net.malisis.core.util.bbcode.node.BBShadowNode;
import net.malisis.core.util.bbcode.node.BBStyleNode;
import net.minecraft.client.gui.GuiScreen;

/**
 * @author Ordinastie
 *
 */
public class BBCodeEditor extends UIContainer<BBCodeEditor>
{
	enum Tag
	{
		BOLD(new BBStyleNode("b")),
		ITALIC(new BBStyleNode("i")),
		UNDERLINE(new BBStyleNode("u")),
		STRIKETHOUGH(new BBStyleNode("s")),
		SHADOW(new BBShadowNode()),
		COLOR(new BBColorNode("color")),
		BGCOLOR(new BBColorNode("bgcolor")),
		ITEM(new BBItemNode(""));

		public BBNode node;

		private Tag(BBNode node)
		{
			this.node = node;
		}
	};

	protected UIContainer<?> menu;

	protected UIButton btnBold;
	protected UIButton btnItalic;
	protected UIButton btnUnderline;
	protected UIButton btnStrikethrough;

	protected UIButton btnColor;
	protected UIButton btnBgColor;
	protected UIButton btnItem;

	protected UIButton btnWysiwyg;

	protected BBTextField bbTexfield;

	protected ComponentPosition menuPosition = ComponentPosition.TOP;
	protected int buttonAnchor = Anchor.LEFT | Anchor.MIDDLE;

	protected EnumSet<Tag> activeStyles = EnumSet.noneOf(Tag.class);

	protected boolean isWysiwyg = false;

	private int defaultColor = 0xFFFFFF;

	//private int activeColor = 0x006633;

	public BBCodeEditor(MalisisGui gui)
	{
		super(gui);

		bbTexfield = new BBTextField(gui, this);

		createMenu(gui);

		add(bbTexfield);
		add(menu);

		setMenuPosition(ComponentPosition.TOP);

		setWysiwyg(true);
	}

	public BBCodeEditor(MalisisGui gui, ISize size)
	{
		this(gui);
		setSize(size);
	}

	//#region Getters/Setters
	public ComponentPosition getMenuPosition()
	{
		return menuPosition;
	}

	public String getRawText()
	{
		return bbTexfield.getBBText().getRawText();
	}

	public BBString getBBText()
	{
		return bbTexfield.getBBText();
	}

	public String getBBFormattedTex()
	{
		return bbTexfield.getBBText().getBBString();
	}

	public boolean isWysiwyg()
	{
		return bbTexfield.isWysiwyg();
	}

	public BBCodeEditor setWysiwyg(boolean w)
	{
		bbTexfield.setWysiwyg(w);

		//		btnWysiwyg.setTextColor(w ? 0x66CC77 : defaultColor);
		//		btnWysiwyg.setBgColor(w ? 0xBBFFCC : defaultColor);

		return this;
	}

	public BBCodeEditor setMenuPosition(ComponentPosition position)
	{
		menuPosition = position;
		calculateTextfieldPosition();
		calculateMenuPosition();
		return this;
	}

	public BBCodeEditor setButtonAnchor(int anchor)
	{
		this.buttonAnchor = anchor;
		calculateTextfieldPosition();
		calculateMenuPosition();
		return this;
	}

	//#end Getters/Setters

	protected void createMenu(MalisisGui gui)
	{
		menu = new UIContainer<>();
		menu.setParent(this);

		createButtons(gui);

		setMenuPosition(ComponentPosition.TOP);
	}

	protected void createButtons(MalisisGui gui)
	{
		ISize size = Size.of(10, 10);
		btnBold = new UIButton("B");
		btnBold.setSize(size);
		btnBold.setTooltip("Bold").register(this);

		btnItalic = new UIButton("I");
		btnItalic.setSize(size);
		btnItalic.setTooltip("Italic").register(this);

		btnUnderline = new UIButton("U");
		btnUnderline.setSize(size);
		btnUnderline.setTooltip("Underline").register(this);

		btnStrikethrough = new UIButton("S");
		btnStrikethrough.setSize(size);
		btnStrikethrough.setTooltip("Strikethrough").register(this);

		btnColor = new UIButton("C");
		btnStrikethrough.setSize(size);
		btnStrikethrough.setTooltip("Color").register(this);

		btnBgColor = new UIButton("BC");
		btnBgColor.setSize(Size.of(16, 10));
		btnBgColor.setTooltip("Background Color").register(this);

		btnItem = new UIButton("Item");
		btnItem.setSize(Size.of(22, 10));
		btnItem.setTooltip("Item").register(this);

		btnWysiwyg = new UIButton("WYSIWYG");
		btnWysiwyg.setSize(Size.of(45, 10));
		btnWysiwyg.register(this);

		menu.add(btnBold);
		menu.add(btnItalic);
		menu.add(btnUnderline);
		menu.add(btnStrikethrough);

		menu.add(btnColor);
		menu.add(btnBgColor);
		menu.add(btnItem);

		menu.add(btnWysiwyg);

	}

	protected void calculateTextfieldPosition()
	{
		int x = 0, y = 0, w = 0, h = 0;
		int s = 14;
		switch (menuPosition)
		{
			case TOP:
				y = s;
				h = -s;
				break;
			case BOTTOM:
				h = -s;
				break;
			case LEFT:
				x = s;
				w = -s;
				break;
			case RIGHT:
				w = -s;
				break;
		}

		//	bbTexfield.setPosition(x, y).setSize(w, h);
	}

	protected void calculateMenuPosition()
	{
		int x = 0, y = 0, w = 0, h = 0, a = Anchor.NONE;
		int s = 12;
		switch (menuPosition)
		{
			case TOP:
				h = s;
				break;
			case BOTTOM:
				h = s;
				a = Anchor.BOTTOM;
				break;
			case LEFT:
				w = s;
				break;
			case RIGHT:
				w = s;
				a = Anchor.RIGHT;
				break;
		}

		//	menu.setPosition(x, y, a).setSize(w, h);

		calculateButtonPositions();
	}

	protected void calculateButtonPositions()
	{
		int x = 0, y = 1;
		int a = Anchor.vertical(buttonAnchor) | Anchor.CENTER;
		if (menuPosition.isHorizontal())
		{
			x = 1;
			y = 0;
			a = Anchor.horizontal(buttonAnchor) | Anchor.MIDDLE;
		}

		if (Anchor.vertical(a) == Anchor.BOTTOM)
			y *= -1;
		if (Anchor.horizontal(a) == Anchor.RIGHT)
			x *= -1;

		//		btnBold.setPosition(0 * x, 0 * y, a);
		//		btnItalic.setPosition(11 * x, 11 * y, a);
		//		btnUnderline.setPosition(22 * x, 22 * y, a);
		//		btnStrikethrough.setPosition(33 * x, 33 * y, a);
		//
		//		btnColor.setPosition(44 * x + 2 * x, 44 * y + 2 * y, a);
		//		btnBgColor.setPosition(55 * x + 2 * x, 55 * y + 2 * y, a);
		//		btnItem.setPosition(72 * x + 2 * x, 66 * y + 2 * y, a);
		//
		//		btnWysiwyg.setPosition(100 * x + 2 * x, 66 * y + 2 * y, a);
	}

	public boolean isStyleActive(Tag s)
	{
		return activeStyles.contains(s);
	}

	public String getFormattedText()
	{
		return null;
	}

	@Subscribe
	public void onClick(UIButton.ClickEvent event)
	{
		UIButton button = event.getComponent();
		//boolean active = false;
		if (button == btnBold)
			bbTexfield.addTag(Tag.BOLD);
		else if (button == btnItalic)
			bbTexfield.addTag(Tag.ITALIC);
		else if (button == btnUnderline)
			bbTexfield.addTag(Tag.UNDERLINE);
		else if (button == btnStrikethrough)
			bbTexfield.addTag(Tag.STRIKETHOUGH);

		else if (button == btnColor)
			bbTexfield.addTag(Tag.COLOR);
		else if (button == btnBgColor)
			bbTexfield.addTag(Tag.BGCOLOR);
		else if (button == btnItem)
			bbTexfield.addTag(Tag.ITEM);

		else if (button == btnWysiwyg)
		{
			setWysiwyg(!isWysiwyg());
			return;
		}

		//button.setTextColor(active ? activeColor : defaultColor);
		bbTexfield.setFocused(true);
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (!GuiScreen.isCtrlKeyDown())
			return super.onKeyTyped(keyChar, keyCode);

		UIButton button;
		boolean active = false;
		switch (keyCode)
		{
			case Keyboard.KEY_B:
				bbTexfield.addTag(Tag.BOLD);
				button = btnBold;
				break;
			case Keyboard.KEY_I:
				bbTexfield.addTag(Tag.ITALIC);
				button = btnItalic;
				break;
			case Keyboard.KEY_U:
				bbTexfield.addTag(Tag.UNDERLINE);
				button = btnUnderline;
				break;
			case Keyboard.KEY_S:
				bbTexfield.addTag(Tag.STRIKETHOUGH);
				button = btnStrikethrough;
				break;
			default:
				return super.onKeyTyped(keyChar, keyCode);
		}

		if (button != null)
		{
			//button.setTextColor(active ? 0x66CC77 : defaultColor);
			//button.setBgColor(active ? 0xBBFFCC : defaultColor);
		}

		return true;
	}

	public static class BBCodeChangeEvent extends ComponentEvent<BBCodeEditor>
	{
		public BBCodeChangeEvent(BBCodeEditor component)
		{
			super(component);
		}
	}
}
