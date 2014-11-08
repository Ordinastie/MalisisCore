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
import java.util.Map;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.client.gui.icon.GuiIcon;
import net.malisis.core.renderer.animation.transformation.ITransformable;

/**
 * @author Ordinastie
 *
 */
public class UITabGroup extends UIContainer<UITabGroup>
{
	public enum TabPosition
	{
		TOP, RIGHT, LEFT, BOTTOM
	}

	protected Map<UITab, UIContainer> listTabs = new LinkedHashMap<>();
	protected UITab activeTab;
	protected TabPosition tabPosition = TabPosition.TOP;
	protected UIContainer attachedContainer;
	protected int offset = 3;
	protected GuiIcon[] windowIcons;
	protected GuiIcon[] panelIcons;

	public UITabGroup(MalisisGui gui, TabPosition tabPosition)
	{
		super(gui);
		this.tabPosition = tabPosition;
		clipContent = false;
		setSize(0, 0);

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

	public UITabGroup(MalisisGui gui)
	{
		this(gui, TabPosition.TOP);
	}

	/**
	 * @return the relative position of the tabs around their containers.
	 */
	public TabPosition getTabPosition()
	{
		return tabPosition;
	}

	public GuiIcon getIcons()
	{
		if (attachedContainer instanceof UIWindow)
			return windowIcons[tabPosition.ordinal()];
		else
			return panelIcons[tabPosition.ordinal()];

	}

	/**
	 * Adds an {@link UITab} and its corresponding {@link UIContainer}to this {@link UITabGroup}.<br>
	 * Also sets the width of this {@link UITabGroup}.
	 *
	 * @param tab tab to add to the UITabGroup
	 * @param container {@link UIContainer} linked to the {@link UITab}
	 * @return this {@link UITab}
	 */
	public UITab addTab(UITab tab, UIContainer container)
	{
		add(tab);
		tab.setContainer(container);
		tab.setActive(false);
		listTabs.put(tab, container);

		calculateTabPosition();

		return tab;
	}

	public void calculateTabPosition()
	{
		int w = 0;
		int h = 0;

		for (UITab tab : listTabs.keySet())
		{
			if (tabPosition == TabPosition.TOP || tabPosition == TabPosition.BOTTOM)
			{
				tab.setPosition(w + offset, 1);
				w += tab.getWidth();
				h = Math.max(h, tab.getHeight());
			}
			else
			{
				tab.setPosition(1, h + offset);
				w = Math.max(w, tab.getWidth());
				h += tab.getHeight();
			}
		}

		boolean isHorizontal = tabPosition == TabPosition.TOP || tabPosition == TabPosition.BOTTOM;
		for (UITab tab : listTabs.keySet())
			tab.setSize(isHorizontal ? 0 : w, isHorizontal ? h : 0);

		if (isHorizontal)
			w += offset * 2;
		else
			h += offset * 2;

		setSize(w + 2, h + 2);
	}

	/**
	 * Activates the tab and deactivates currently active tab.
	 *
	 * @param tab the new active tab
	 */
	public void setActiveTab(UITab tab)
	{
		if (activeTab == tab)
			return;

		if (activeTab != null)
			activeTab.setActive(false);

		activeTab = tab;
		if (tab != null)
			tab.setActive(true);

		if (attachedContainer instanceof ITransformable.Color)
			((ITransformable.Color) attachedContainer).setColor(tab.getColor());

	}

	public UIContainer getAttachedContainer()
	{
		return attachedContainer;
	}

	/**
	 * Attach this {@link UITabGroup} to a {@link UIContainer}.
	 *
	 * @param container the container to attach to.
	 * @param displace if true, moves and resize the UIContainer to make place for the UITabGroup
	 * @return this {@link UITab}
	 */
	public UITabGroup attachTo(UIContainer container, boolean displace)
	{
		attachedContainer = container;
		if (activeTab != null && attachedContainer instanceof ITransformable.Color)
			((ITransformable.Color) attachedContainer).setColor(activeTab.getColor());

		if (!displace)
			return this;

		int cx = container.getX();
		int cy = container.getY();
		int cw = container.getRawWidth();
		int ch = container.getRawHeight();

		if (tabPosition == TabPosition.TOP)
		{
			cy += getHeight() - 1;
			ch = container.getRawHeight() - getHeight();
		}
		else if (tabPosition == TabPosition.BOTTOM)
		{
			ch = container.getRawHeight() - getHeight() + 1;
		}
		else if (tabPosition == TabPosition.LEFT)
		{
			cx += getWidth() - 1;
			cw = container.getRawWidth() - getWidth();
		}
		else if (tabPosition == TabPosition.RIGHT)
		{
			cw = container.getRawWidth() - getWidth() + 1;
		}

		//tab.setSize(w, h);
		container.setSize(cw, ch);
		container.setPosition(cx, cy);
		return this;
	}

	@Override
	public int screenX()
	{
		if (attachedContainer == null)
			return super.screenX();

		int x = this.x + attachedContainer.screenX();
		switch (tabPosition)
		{
			case LEFT:
				x += offset - getWidth();
				break;
			case RIGHT:
				x += attachedContainer.getWidth() - offset;
				break;
			default:
				break;

		}

		return x;
	}

	@Override
	public int screenY()
	{
		if (attachedContainer == null)
			return super.screenY();

		int y = this.y + attachedContainer.screenY();
		switch (tabPosition)
		{
			case TOP:
				y += offset - getHeight();
				break;
			case BOTTOM:
				y += attachedContainer.getHeight() - offset;
				break;
			default:
				break;

		}
		return y;
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
