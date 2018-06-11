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

package net.malisis.core.client.gui.component.scrolling;

import static com.google.common.base.Preconditions.*;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.IntSupplier;

import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.content.IContent;
import net.malisis.core.client.gui.component.control.IControlComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.element.Padding;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.element.Size.ISize;
import net.malisis.core.client.gui.element.Size.ISized;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.position.Position.IPosition;
import net.malisis.core.client.gui.event.component.ContentUpdateEvent;
import net.malisis.core.util.MouseButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.MathHelper;

/**
 * UIScrollBar
 *
 * @author Ordinastie
 */
public abstract class UIScrollBar extends UIComponent implements IControlComponent
{
	public enum Type
	{
		HORIZONTAL,
		VERTICAL
	}

	private static final Map<IScrollable<?>, UIScrollBar> verticalScrollbars = new WeakHashMap<>();
	private static final Map<IScrollable<?>, UIScrollBar> horizontalScrollbars = new WeakHashMap<>();

	protected IPosition scrollPosition = new ScrollPosition();
	protected ISize scrollSize = Size.of(5, 5);

	/** The type of scrollbar. */
	protected Type type;

	protected float offset;

	public boolean autoHide = false;

	public <T extends UIComponent & IScrollable<?>> UIScrollBar(T parent, Type type)
	{
		this.type = type;

		parent.addControlComponent(this);
		parent.register(this);

		if (type == Type.VERTICAL)
			verticalScrollbars.put(parent, this);

		setPosition(new ScrollbarPosition());
		setSize(new ScrollbarSize());
		updateScrollbar();
	}

	@SuppressWarnings("unchecked")
	public <T extends UIComponent & IScrollable<?>> T parent()
	{
		return (T) getParent();
	}

	/**
	 * Gets the parent as a {@link IScrollable}.
	 *
	 * @return the scrollable
	 */
	protected IScrollable<?> getScrollable()
	{
		return (IScrollable<?>) getParent();
	}

	/**
	 * Scroll thickness.
	 *
	 * @return the int
	 */
	protected int scrollThickness()
	{
		return isHorizontal() ? scrollSize.height() : scrollSize.width();
	}

	/**
	 * Scroll length.
	 *
	 * @return the int
	 */
	protected int scrollLength()
	{
		return isHorizontal() ? scrollSize.width() : scrollSize.height();
	}

	/**
	 * Gets the length of this {@link UIScrollBar} (width if {@link Type#HORIZONTAL}, height if {@link Type#VERTICAL}.
	 *
	 * @return the length
	 */
	public int getLength()
	{
		return isHorizontal() ? size().width() : size().height();
	}

	/**
	 * Checks if this {@link UIScrollBar} is {@link Type#HORIZONTAL}.
	 *
	 * @return true, if is horizontal
	 */
	public boolean isHorizontal()
	{
		return type == Type.HORIZONTAL;
	}

	/**
	 * Sets whether this {@link UIScrollBar} should automatically hide when scrolling is not possible (content size is inferior to component
	 * size).
	 *
	 * @param autoHide the auto hide
	 * @return this {@link UIScrollBar}
	 */
	public UIScrollBar setAutoHide(boolean autoHide)
	{
		this.autoHide = autoHide;
		return this;
	}

	@Override
	public int getZIndex()
	{
		return getParent() != null ? getParent().getZIndex() + 5 : 0;
	}

	protected int sizeDiff()
	{
		ISize s = parent().size();
		IPosition cp = parent().contentPosition();
		ISize cs = parent().contentSize();
		Padding pad = Padding.of(parent());

		return Math.min((s.height() - pad.vertical() - cp.y() - cs.height()), 0);
	}

	public void setOffset(float offset)
	{
		this.offset = MathHelper.clamp(offset, 0, 1);
	}

	public void setOffset(int offset)
	{
		if (offset == 0)
			this.offset = 0;
		else
			this.offset = (float) offset / sizeDiff();
	}

	/**
	 * Gets the offset of the parent component of this {@link UIScrollBar}.
	 *
	 * @return the offset
	 */
	public float offset()
	{
		return offset;
	}

	public int positionOffset()
	{
		if (offset == 0)
			return 0;

		int diff = sizeDiff();
		if (diff >= 0)
			offset = 0;
		return Math.round(diff * offset);
	}

	/**
	 * Scroll this {@link UIScrollBar} to the specified offset.
	 *
	 * @param offset the offset
	 */
	public void scrollTo(float offset)
	{
		if (!isEnabled())
			return;

		setOffset(offset);
	}

	/**
	 * Scroll this {@link UIScrollBar} by the specified amount.
	 *
	 * @param amount the amount
	 */
	public void scrollBy(float amount)
	{
		scrollTo(offset() + amount);
	}

	/**
	 * Update this {@link UIScrollBar}, hiding and disabling it if necessary, based on content size.
	 */
	public void updateScrollbar()
	{
		UIComponent parent = getParent();
		IScrollable<?> scrollable = getScrollable();
		int delta = 0;// hasVisibleOtherScrollbar() ? scrollThickness() : 0;
		boolean hide = false;
		float offset = offset();
		if (isHorizontal())
		{
			if (scrollable.contentSize().width() <= parent.size().width() - delta)
				hide = true;
		}
		else
		{
			if (scrollable.contentSize().height() <= parent.size().height() - delta)
				hide = true;
		}

		if (hide == isEnabled() || offset < 0)
			scrollTo(0);
		if (offset > 1)
			scrollTo(1);
		//setEnabled(!hide);
		if (autoHide)
			setVisible(!hide);
	}

	@Subscribe
	public void onContentUpdate(ContentUpdateEvent<UIScrollBar> event)
	{
		if (getParent() != event.getComponent())
			return;

		updateScrollbar();
	}

	@Override
	public boolean onButtonPress(MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onButtonPress(button);

		scrollToMouse();
		return true;
	}

	@Override
	public boolean onClick()
	{
		return true;
	}

	@Override
	public boolean onDrag(MouseButton button)
	{
		if (button != MouseButton.LEFT)
			return super.onDrag(button);

		if (isFocused())
			scrollToMouse();
		return true;
	}

	private void scrollToMouse()
	{
		int l = getLength() - scrollLength() - 2;
		int pos = isHorizontal() ? mousePosition().x() : mousePosition().y();
		pos -= scrollLength() / 2;
		scrollTo((float) pos / l);
	}

	@Override
	public boolean onScrollWheel(int delta)
	{
		if ((isHorizontal() != GuiScreen.isShiftKeyDown()) && !isHovered())
			return super.onScrollWheel(delta);

		scrollBy(-delta * getScrollable().getScrollStep());
		//true = stop
		float o = offset();
		return !(delta > 0 && o == 0 || delta < 0 && o == 1);
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
		ISize cs = getScrollable().contentSize();
		return type + " | O=" + offset() + "(" + (isHorizontal() ? cs.width() : cs.height()) + ") | " + super.getPropertyString();
	}

	public static UIScrollBar verticalScrollbar(Object component)
	{
		return verticalScrollbars.get(component);
	}

	public static UIScrollBar horizontalScrollbar(Object component)
	{
		return horizontalScrollbars.get(component);
	}

	public static int scrollbarWidth(Object component)
	{
		UIScrollBar scrollbar = verticalScrollbars.get(component);
		return scrollbar != null && scrollbar.isVisible() ? scrollbar.size().width() : 0;
	}

	public static int scrollbarHeight(Object component)
	{
		UIScrollBar scrollbar = horizontalScrollbars.get(component);
		return scrollbar != null && scrollbar.isVisible() ? scrollbar.size().height() : 0;
	}

	private class ScrollbarPosition implements IPosition
	{
		@Override
		public int x()
		{
			if (isHorizontal())
				return Padding.of(getParent()).left();
			else
				return getParent().size().width() - size().width() - Padding.of(getParent()).right();
		}

		@Override
		public int y()
		{
			if (isHorizontal())
				return getParent().size().height() - size().height();
			else
				return Padding.of(getParent()).right();
		}

		@Override
		public String toString()
		{
			return x() + "," + y();
		}
	}

	private class ScrollbarSize implements ISize
	{
		@Override
		public int width()
		{
			if (!isHorizontal())
				return scrollThickness() + 2;

			return getParent().innerSize().width();
		}

		@Override
		public int height()
		{
			if (isHorizontal())
				return scrollThickness() + 2;

			return getParent().innerSize().height();
		}

		@Override
		public String toString()
		{
			return width() + "," + height();
		}
	}

	private class ScrollPosition implements IPosition
	{
		@Override
		public int x()
		{
			int l = getLength() - scrollLength() - 1;
			return isHorizontal() ? (int) (UIScrollBar.this.offset() * l) + 1 : 1;
		}

		@Override
		public int y()
		{
			int l = getLength() - scrollLength() - 2;
			return isHorizontal() ? 1 : (int) (UIScrollBar.this.offset() * l) + 1;
		}

		@Override
		public String toString()
		{
			return x() + "," + y();
		}
	}

	public static <T extends ISized & IContent> IPosition scrollingOffset(T owner)
	{
		return Position.of(owner).x(UIScrollBar.horizontalScrolling(owner)).y(UIScrollBar.verticalScrolling(owner)).build();
	}

	public static <T extends ISized & IContent> IntSupplier verticalScrolling(T owner)
	{
		checkNotNull(owner);
		return () -> {
			UIScrollBar scrollbar = verticalScrollbar(owner);
			if (scrollbar == null)
				return 0;
			return scrollbar.positionOffset();
		};
	}

	public static <T extends ISized & IContent> IntSupplier horizontalScrolling(T owner)
	{
		checkNotNull(owner);
		return () -> {
			UIScrollBar scrollbar = horizontalScrollbar(owner);
			if (scrollbar == null)
				return 0;
			return scrollbar.positionOffset();
		};
	}

}
