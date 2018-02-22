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

package net.malisis.core.client.gui.component.container;

import java.util.LinkedHashMap;

import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.element.Position;
import net.malisis.core.client.gui.component.element.Position.IPosition;
import net.malisis.core.client.gui.component.element.Size;
import net.malisis.core.client.gui.component.element.Size.ISize;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.icon.GuiIcon;

/**
 * @author Ordinastie
 *
 */
public class UITabGroup extends UIContainer<UITabGroup>
{

	/** The list of {@link UITab} added to this {@link UITabGroup}. */
	protected LinkedHashMap<UITab, UIContainer<?>> listTabs = new LinkedHashMap<>();
	/** The currently active {@link UITab}. */
	protected UITab activeTab;
	/** The position of this {@link UITabGroup} relative to its {@link #attachedContainer}. */
	protected ComponentPosition tabPosition = ComponentPosition.TOP;
	/** The {@link UIContainer} this {@link UITabGroup} is attached to. */
	protected UIContainer<?> attachedContainer;
	/** Number of pixels this {@link UITabGroup} is offset to the border of the {@link #attachedContainer}. */
	protected int offset = 3;
	/** Number of pixels between each tab. */
	protected int spacing = 0;
	/** Icons to use if this {@link UITabGroup} is attached to a {@link UIWindow}. */
	protected GuiIcon[] windowIcons;
	/** Icons to use if this {@link UITabGroup} is attached to a {@link UIPanel}. */
	protected GuiIcon[] panelIcons;

	/**
	 * Instantiates a new {@link UITabGroup}.
	 *
	 * @param gui the gui
	 * @param tabPosition the tab position
	 */
	public UITabGroup(MalisisGui gui, ComponentPosition tabPosition)
	{
		super(gui);
		this.tabPosition = tabPosition;
		clipContent = false;

		//@formatter:off
		windowIcons = new GuiIcon[] {	gui.getGuiTexture().getXYResizableIcon(0, 60, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(15, 60, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(0, 75, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(15, 75, 15, 15, 5)};

		panelIcons = new GuiIcon[] {	gui.getGuiTexture().getXYResizableIcon(30, 60, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(45, 60, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(30, 75, 15, 15, 5),
										gui.getGuiTexture().getXYResizableIcon(45, 75, 15, 15, 5)};
		//@formatter:on
	}

	/**
	 * Instantiates a new {@link UITabGroup}.
	 *
	 * @param gui the gui
	 */
	public UITabGroup(MalisisGui gui)
	{
		this(gui, ComponentPosition.TOP);
	}

	/**
	 * Gets the relative position of the tabs around their containers.
	 *
	 * @return the tab position
	 */
	public ComponentPosition getTabPosition()
	{
		return tabPosition;
	}

	/**
	 * Gets the icons for this {@link UITabGroup}
	 *
	 * @return the icons
	 */
	public GuiIcon getIcons()
	{
		//if (attachedContainer instanceof UIWindow)
		//	return windowIcons[tabPosition.ordinal()];
		//else
		return panelIcons[tabPosition.ordinal()];
	}

	/**
	 * Gets the attached container for this {@link UITabGroup}.
	 *
	 * @return the attached container
	 */
	public UIContainer<?> getAttachedContainer()
	{
		return attachedContainer;
	}

	/**
	 * Gets the offset for this {@link UITabGroup}.
	 *
	 * @return the offset
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * Sets the offset for this {@link UITabGroup}.
	 *
	 * @param offset the offset
	 * @return this {@link UITabGroup}
	 */
	public UITabGroup setOffset(int offset)
	{
		this.offset = offset;
		return this;
	}

	/**
	 * Gets the spacing for this {@link UITabGroup}.
	 *
	 * @return the spacing
	 */
	public int getSpacing()
	{
		return spacing;
	}

	/**
	 * Sets the spacing for this {@link UITabGroup}.
	 *
	 * @param spacing the spacing
	 * @return this {@link UITabGroup}
	 */
	public UITabGroup setSpacing(int spacing)
	{
		this.spacing = spacing;
		return this;
	}

	/**
	 * Adds a {@link UITab} and its corresponding {@link UIContainer} to this {@link UITabGroup}.<br>
	 * Also sets the width of this {@code UITabGroup}.
	 *
	 * @param tab tab to add to the UITabGroup
	 * @param container {@link UIContainer} linked to the {@link UITab}
	 * @return this {@link UITab}
	 */
	public UITab addTab(UITab tab, UIContainer<?> container)
	{
		if (tab.isActive())
			activeTab = tab;

		add(tab);
		tab.setContainer(container);
		tab.setActive(false);
		listTabs.put(tab, container);
		updateSize();

		if (attachedContainer != null)
		{
			setupTabContainer(container);
			calculateTabPosition();
		}
		return tab;
	}

	private void setupTabContainer(UIContainer<?> container)
	{
		attachedContainer.add(container);
		container.setPosition(Position.zero());
		container.setSize(Size.inherited());
	}

	private void updateSize()
	{
		int width = offset;
		int height = offset;
		for (UITab tab : listTabs.keySet())
		{
			if (tabPosition == ComponentPosition.TOP || tabPosition == ComponentPosition.BOTTOM)
			{
				width += tab.size().width() + spacing;
				height = Math.max(height, tab.size().height());
			}
			else
			{
				width = Math.max(width, tab.size().width());
				height += tab.size().height() + spacing;
			}
		}
		setSize(Size.of(width, height));
	}

	/**
	 * Calculates the {@link UITab} position.<br>
	 * Sets the width and height of this {@link UITabGroup}.
	 */
	protected void calculateTabPosition()
	{
		boolean isHorizontal = tabPosition == ComponentPosition.TOP || tabPosition == ComponentPosition.BOTTOM;
		UITab lastTab = null;

		for (UITab tab : listTabs.keySet())
		{
			if (isHorizontal)
			{
				IPosition p = lastTab != null ? Position.rightOf(lastTab, spacing).y(0) : Position.of(offset, 1);
				tab.setPosition(p);
			}
			else
			{
				IPosition p = lastTab != null ? Position.x(0).below(lastTab, spacing) : Position.of(1, offset);
				tab.setPosition(p);
			}
			lastTab = tab;
		}

		//		for (UITab tab : listTabs.keySet())
		//			tab.setSize(isHorizontal ? 0 : s, isHorizontal ? s : 0);
	}

	public void setActiveTab(String tabName)
	{
		UIComponent<?> comp = getComponent(tabName);
		if (comp instanceof UITab)
			setActiveTab((UITab) comp);
	}

	/**
	 * Activates the {@link UITab} and deactivates currently active one.
	 *
	 * @param tab the new active tab
	 */
	public void setActiveTab(UITab tab)
	{
		if (attachedContainer == null)
		{
			activeTab = tab;
			return;
		}

		if (activeTab == tab)
			return;

		if (activeTab != null)
			activeTab.setActive(false);

		activeTab = tab;
		if (tab == null)
			return;

		tab.setActive(true);
	}

	/**
	 * Attach this {@link UITabGroup} to a {@link UIContainer}.
	 *
	 * @param container the container to attach to.
	 * @param displace if true, moves and resize the UIContainer to make place for the UITabGroup
	 * @return this {@link UITab}
	 */
	public UITabGroup attachTo(UIContainer<?> container, boolean displace)
	{
		attachedContainer = container;
		if (activeTab != null && attachedContainer instanceof ITransformable.Color)
			((ITransformable.Color) attachedContainer).setColor(activeTab.getBgColor());

		switch (tabPosition)
		{
			case TOP:
				setPosition(Position.leftAlignedTo(container).above(container, -2));
				break;
			case BOTTOM:
				setPosition(Position.leftAlignedTo(container).below(container, 2));
				break;
			case LEFT:
				setPosition(Position.leftOf(container, -2).topAlignedTo(container));
				break;
			case RIGHT:
				setPosition(Position.rightOf(container, 2).topAlignedTo(container));
				break;
		}

		for (UIContainer<?> tabContainer : listTabs.values())
			setupTabContainer(tabContainer);

		calculateTabPosition();

		if (activeTab != null)
		{
			UITab tab = activeTab;
			activeTab = null;
			setActiveTab(tab);
		}
		if (displace)
		{
			attachedContainer.setPosition(new AttachedContainerPosition(attachedContainer.position()));
			attachedContainer.setSize(new AttachedContainerSize(attachedContainer.size()));
		}

		return this;
		//		int cx = container.getX();
		//		int cy = container.getY();
		//		int cw = container.getRawWidth();
		//		int ch = container.getRawHeight();
		//		int av = Anchor.vertical(container.getAnchor());
		//		int ah = Anchor.horizontal(container.getAnchor());
		//
		//		if (tabPosition == ComponentPosition.TOP)
		//		{
		//			if (av == Anchor.TOP || av == Anchor.NONE)
		//				cy += getHeight() - 1;
		//			ch = container.getRawHeight() - getHeight();
		//		}
		//		else if (tabPosition == ComponentPosition.BOTTOM)
		//		{
		//			if (av == Anchor.BOTTOM)
		//				cy -= getHeight() - 1;
		//			ch = container.getRawHeight() - getHeight() + 1;
		//		}
		//		else if (tabPosition == ComponentPosition.LEFT)
		//		{
		//			if (ah == Anchor.LEFT || ah == Anchor.NONE)
		//				cx += getWidth() - 1;
		//			cw = container.getRawWidth() - getWidth();
		//		}
		//		else if (tabPosition == ComponentPosition.RIGHT)
		//		{
		//			if (ah == Anchor.RIGHT)
		//				cx -= getWidth() - 1;
		//			cw = container.getRawWidth() - getWidth() + 1;
		//		}
		//
		//		//tab.setSize(w, h);
		//		container.setSize(cw, ch);
		//		container.setPosition(cx, cy);
	}

	private class AttachedContainerPosition implements IPosition
	{
		private final IPosition originalPosition;

		public AttachedContainerPosition(IPosition position)
		{
			originalPosition = position;
		}

		@Override
		public int x()
		{
			return originalPosition.x() + (tabPosition == ComponentPosition.LEFT ? size().width() : 0);
		}

		@Override
		public int y()
		{
			return originalPosition.y() + (tabPosition == ComponentPosition.TOP ? size().height() : 0);
		}
	}

	private class AttachedContainerSize implements ISize
	{
		private final ISize originalSize;

		public AttachedContainerSize(ISize size)
		{
			originalSize = size;
		}

		@Override
		public int width()
		{
			if (tabPosition == ComponentPosition.TOP || tabPosition == ComponentPosition.BOTTOM)
				return originalSize.width();

			return originalSize.width() - size().width();
		}

		@Override
		public int height()
		{
			if (tabPosition == ComponentPosition.LEFT || tabPosition == ComponentPosition.RIGHT)
				return originalSize.height();

			return originalSize.height() - size().height();
		}
	}

	/**
	 * Event fired when an inactive {@link UITab} is clicked.<br>
	 * Canceling the event will keep the old tab active.
	 *
	 * @author Ordinastie
	 *
	 */
	public static class TabChangeEvent extends ComponentEvent<UITabGroup>
	{
		private UITab newTab;

		public TabChangeEvent(UITabGroup component, UITab newTab)
		{
			super(component);
			this.newTab = newTab;
		}

		/**
		 * @return the {@link UITab} deactivated
		 */
		public UITab getOldTab()
		{
			return component.activeTab;
		}

		/**
		 * @return the {@link UITab} activated
		 */
		public UITab getNewTab()
		{
			return newTab;
		}

	}
}
