/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

package net.malisis.core.client.gui.component.control;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

/**
 * UIScrollBar
 *
 * @author Ordinastie
 */
public class UIScrollBar extends UIComponent<UIScrollBar> implements IControlComponent
{
	public enum Type
	{
		HORIZONTAL, VERTICAL
	}

	protected int scrollThickness = 10;
	protected int scrollHeight = 15;

	private static Map<UIComponent, Map<Type, UIScrollBar>> scrollbars = new WeakHashMap();

	protected GuiIcon disabledIcon;
	protected GuiIcon verticalIcon;
	protected GuiIcon verticalDisabledIcon;
	protected GuiIcon horizontalIcon;
	protected GuiIcon horizontalDisabledIcon;

	protected Type type;

	public boolean autoHide = false;

	protected GuiShape scrollShape;

	public <T extends UIComponent & IScrollable> UIScrollBar(MalisisGui gui, T parent, Type type)
	{
		super(gui);
		this.type = type;

		setZIndex(parent.getZIndex() + 1);

		parent.addControlComponent(this);
		parent.register(this);

		addScrollbar(parent, this);
		setPosition();
		updateScrollbars();

		createShape(gui);
	}

	/**
	 * Sets the scroll size.
	 *
	 * @param thickness the thickness
	 * @param height the height
	 */
	public void setScrollSize(int thickness, int height)
	{
		scrollThickness = thickness;
		scrollHeight = height;
		createShape(getGui());
	}

	protected void setPosition()
	{
		int vp = getScrollable().getVerticalPadding();
		int hp = getScrollable().getHorizontalPadding();

		if (type == Type.HORIZONTAL)
			setPosition(-hp, vp, Anchor.BOTTOM);
		else
			setPosition(hp, -vp, Anchor.RIGHT);
	}

	protected void createShape(MalisisGui gui)
	{
		int w = scrollThickness - 2;
		int h = scrollHeight;
		if (type == Type.HORIZONTAL)
		{
			w = scrollHeight;
			h = scrollThickness - 2;
		}

		//background shape
		shape = new XYResizableGuiShape(1);
		//scroller shape
		scrollShape = new SimpleGuiShape();
		scrollShape.setSize(w, h);
		scrollShape.storeState();

		icon = gui.getGuiTexture().getXYResizableIcon(215, 0, 15, 15, 1);
		disabledIcon = gui.getGuiTexture().getXYResizableIcon(215, 15, 15, 15, 1);

		verticalIcon = gui.getGuiTexture().getIcon(230, 0, 8, 15);
		verticalDisabledIcon = gui.getGuiTexture().getIcon(238, 0, 8, 15);
		horizontalIcon = gui.getGuiTexture().getIcon(230, 15, 15, 8);
		horizontalDisabledIcon = gui.getGuiTexture().getIcon(230, 23, 15, 8);
	}

	protected IScrollable getScrollable()
	{
		return (IScrollable) getParent();
	}

	public boolean hasVisibleOtherScrollbar()
	{
		UIScrollBar scrollbar = getScrollbar(getParent(), isHorizontal() ? Type.VERTICAL : Type.HORIZONTAL);
		return scrollbar != null && scrollbar.isVisible();
	}

	public boolean isHorizontal()
	{
		return type == Type.HORIZONTAL;
	}

	public UIScrollBar setAutoHide(boolean autoHide)
	{
		this.autoHide = autoHide;
		return this;
	}

	@Override
	public int getWidth()
	{
		return isHorizontal() ? getParent().getWidth() - (hasVisibleOtherScrollbar() ? scrollThickness : 0) : scrollThickness;
	}

	@Override
	public int getHeight()
	{
		return isHorizontal() ? scrollThickness : getParent().getHeight() - (hasVisibleOtherScrollbar() ? scrollThickness : 0);
	}

	public int getLength()
	{
		return isHorizontal() ? getWidth() : getHeight();
	}

	public UIScrollBar setLength(int length)
	{
		return null;
	}

	public float getOffset()
	{
		return isHorizontal() ? getScrollable().getOffsetX() : getScrollable().getOffsetY();
	}

	public void scrollTo(float offset)
	{
		if (isDisabled())
			return;

		if (offset < 0)
			offset = 0;
		if (offset > 1)
			offset = 1;
		int delta = hasVisibleOtherScrollbar() ? scrollThickness : 0;
		if (isHorizontal())
			getScrollable().setOffsetX(offset, delta);
		else
			getScrollable().setOffsetY(offset, delta);
	}

	public void scrollBy(float amount)
	{
		scrollTo(getOffset() + amount);
	}

	public void updateScrollbars()
	{
		UIComponent component = getParent();
		IScrollable scrollable = getScrollable();
		int delta = hasVisibleOtherScrollbar() ? scrollThickness : 0;
		boolean hide = false;
		if (isHorizontal())
		{
			if (scrollable.getContentWidth() <= component.getWidth() - delta)
				hide = true;

		}
		else if (scrollable.getContentHeight() <= component.getHeight() - delta)
			hide = true;

		if (hide != isDisabled())
			scrollTo(0);
		setDisabled(hide);
		if (autoHide)
			setVisible(!hide);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		rp.icon.set(isDisabled() ? disabledIcon : icon);
		renderer.drawShape(shape, rp);
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		GuiIcon icon;
		int ox = 0, oy = 0;
		int l = getLength() - scrollHeight - 2;
		if (isHorizontal())
		{
			icon = isDisabled() ? horizontalDisabledIcon : horizontalIcon;
			ox = (int) (getOffset() * l);
		}
		else
		{
			icon = isDisabled() ? verticalDisabledIcon : verticalIcon;
			oy = (int) (getOffset() * l);
		}

		scrollShape.resetState();
		scrollShape.setPosition(ox + 1, oy + 1);

		rp.icon.set(icon);
		renderer.drawShape(scrollShape, rp);
	}

	@Subscribe
	public void onContentUpdate(ContentUpdateEvent event)
	{
		if (getParent() != event.getComponent())
			return;

		updateScrollbars();
	}

	@Override
	public boolean onButtonPress(int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return onButtonPress(x, y, button);

		onScrollTo(x, y);
		return true;
	}

	@Override
	public boolean onDrag(int lastX, int lastY, int x, int y, MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDrag(lastX, lastY, x, y, button);

		if (isFocused())
			onScrollTo(x, y);
		return true;
	}

	private void onScrollTo(int x, int y)
	{
		int l = getLength() - scrollHeight - 2;
		int pos = isHorizontal() ? relativeX(x) : relativeY(y);
		pos -= scrollHeight / 2;
		scrollTo((float) pos / l);
	}

	@Override
	public boolean onScrollWheel(int x, int y, int delta)
	{
		if ((isHorizontal() != GuiScreen.isShiftKeyDown()) && !isHovered())
			return super.onScrollWheel(x, y, delta);

		scrollBy(-delta * getScrollable().getScrollStep());
		return true;
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (MalisisGui.isGuiCloseKey(keyCode))
			return super.onKeyTyped(keyChar, keyCode);

		if (!isHovered() && !getParent().isHovered())
			return super.onKeyTyped(keyChar, keyCode);
		if (isHorizontal() != GuiScreen.isShiftKeyDown())
			return super.onKeyTyped(keyChar, keyCode);

		if (keyCode == Keyboard.KEY_HOME)
			scrollTo(0);
		else if (keyCode == Keyboard.KEY_END)
			scrollTo(1);
		else
			return super.onKeyTyped(keyChar, keyCode);

		return true;
	}

	@Override
	public String getPropertyString()
	{
		return type + " | " + super.getPropertyString();
	}

	public static UIScrollBar getScrollbar(UIComponent component, Type type)
	{
		Map<Type, UIScrollBar> bars = scrollbars.get(component);
		if (bars == null)
			return null;

		return bars.get(type);
	}

	public static void addScrollbar(UIComponent component, UIScrollBar scrollbar)
	{
		Map<Type, UIScrollBar> bars = scrollbars.get(component);
		if (bars == null)
		{
			bars = new HashMap<>();
			scrollbars.put(component, bars);
		}

		bars.put(scrollbar.type, scrollbar);
	}

}
