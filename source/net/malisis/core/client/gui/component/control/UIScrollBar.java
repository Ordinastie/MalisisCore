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
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.element.GuiShape;
import net.malisis.core.client.gui.element.SimpleGuiShape;
import net.malisis.core.client.gui.element.XYResizableGuiShape;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;

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

	public static final int SCROLL_THICKNESS = 10;
	public static final int SCROLLER_HEIGHT = 15;

	private static Map<UIComponent, Map<Type, UIScrollBar>> scrollbars = new WeakHashMap();

	protected GuiIcon disabledIcon;
	protected GuiIcon verticalIcon;
	protected GuiIcon verticalDisabledIcon;
	protected GuiIcon horizontalIcon;
	protected GuiIcon horizontalDisabledIcon;

	private Type type;
	public int length;
	public int scrollableLength;
	public float offset;

	public boolean autoHide = false;

	private GuiShape scrollShape;

	public <T extends UIComponent & IScrollable> UIScrollBar(MalisisGui gui, T parent, Type type)
	{
		super(gui);
		this.type = type;

		int vp = 0;
		int hp = 0;
		if (parent instanceof UIContainer)
		{
			vp = ((UIContainer) parent).getVerticalPadding();
			hp = ((UIContainer) parent).getHorizontalPadding();
			((UIContainer) parent).drawContentSize = true;
		}

		if (type == Type.HORIZONTAL)
			setPosition(-hp, vp, Anchor.BOTTOM);
		else
			setPosition(hp, -vp, Anchor.RIGHT);

		setZIndex(parent.getZIndex() + 1);

		parent.addControlComponent(this);
		parent.register(this);

		addScrollbar(parent, this);

		int w = SCROLL_THICKNESS - 2;
		int h = SCROLLER_HEIGHT;
		if (type == Type.HORIZONTAL)
		{
			w = SCROLLER_HEIGHT;
			h = SCROLL_THICKNESS - 2;
		}

		updateScrollbars();

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

	private IScrollable getScrollable()
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
		return isHorizontal() ? getParent().getWidth() - (hasVisibleOtherScrollbar() ? SCROLL_THICKNESS : 0) : SCROLL_THICKNESS;
	}

	@Override
	public int getHeight()
	{
		return isHorizontal() ? SCROLL_THICKNESS : getParent().getHeight() - (hasVisibleOtherScrollbar() ? SCROLL_THICKNESS : 0);
	}

	public int getLength()
	{
		return isHorizontal() ? getWidth() : getHeight();
	}

	public UIScrollBar setLength(int length)
	{
		return null;
	}

	public void setScrollableLength(int length)
	{

	}

	public void scrollTo(float offset)
	{
		if (isDisabled())
			return;

		if (offset < 0)
			offset = 0;
		if (offset > 1)
			offset = 1;
		this.offset = offset;
		int delta = hasVisibleOtherScrollbar() ? SCROLL_THICKNESS : 0;
		if (isHorizontal())
			getScrollable().setOffsetX(offset, delta);
		else
			getScrollable().setOffsetY(offset, delta);
	}

	public void scrollBy(float amount)
	{
		scrollTo(offset + amount);
	}

	public void updateScrollbars()
	{
		UIComponent component = getParent();
		IScrollable scrollable = getScrollable();
		int delta = hasVisibleOtherScrollbar() ? SCROLL_THICKNESS : 0;
		boolean hide = false;
		if (isHorizontal())
		{
			if (scrollable.getContentWidth() <= component.getWidth() - delta)
				hide = true;

		}
		else if (scrollable.getContentHeight() <= component.getHeight() - delta)
			hide = true;

		scrollTo(hide != isDisabled() ? 0 : offset);
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
		int l = getLength() - SCROLLER_HEIGHT - 2;
		if (isHorizontal())
		{
			icon = isDisabled() ? horizontalDisabledIcon : horizontalIcon;
			ox = (int) (offset * l);
		}
		else
		{
			icon = isDisabled() ? verticalDisabledIcon : verticalIcon;
			oy = (int) (offset * l);
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

	@Subscribe
	public void onClick(MouseEvent.Press event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (isInsideBounds(event.getX(), event.getY()))
			onScrollTo(event);
	}

	@Subscribe
	public void onDrag(MouseEvent.Drag event)
	{
		if (event.getButton() != MouseButton.LEFT)
			return;

		if (isFocused())
			onScrollTo(event);
	}

	private void onScrollTo(MouseEvent event)
	{
		int l = getLength() - SCROLLER_HEIGHT - 2;
		int pos = isHorizontal() ? relativeX(event.getX()) : relativeY(event.getY());
		pos -= SCROLLER_HEIGHT / 2;
		scrollTo((float) pos / l);
	}

	@Subscribe
	public void onScrollWheel(MouseEvent.ScrollWheel event)
	{
		if ((isHorizontal() != GuiScreen.isShiftKeyDown()) && !isHovered())
			return;

		scrollBy(-event.getDelta() * (GuiScreen.isCtrlKeyDown() ? 0.125F : 0.025F));
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
