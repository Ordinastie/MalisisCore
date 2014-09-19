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

import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.client.gui.component.interaction.UITab;

/**
 * @author Ordinastie
 * 
 */
public class UITabGroup extends UIContainer<UITabGroup>
{
	public enum Position
	{
		TOP, BOTTOM, LEFT, RIGHT
	}

	private HashMap<UITab, UIContainer> listTabs = new HashMap<>();
	private UITab activeTab;
	private Position tabPosition = Position.TOP;
	private int containerDiff = 0;

	public UITabGroup(Position tabPosition)
	{
		this.tabPosition = tabPosition;
		this.height = 3;
		this.width = 3;
	}

	public UITabGroup()
	{
		this(Position.TOP);
	}

	@Override
	public UITabGroup setPosition(int x, int y, int anchor)
	{
		return super.setPosition(x, y, anchor);
	}

	public Position getTabPosition()
	{
		return tabPosition;
	}

	/**
	 * Adds tab and its corresponding container to this <code>UITabGroup</code>.<br />
	 * Also sets the width of this <code>UITabGroup</code>.
	 * 
	 * @param tab
	 * @param container
	 */
	public UITab addTab(UITab tab, UIContainer container)
	{
		add(tab);
		tab.setContainer(container);
		tab.setActive(false);
		listTabs.put(tab, container);

		if (tabPosition == Position.TOP || tabPosition == Position.BOTTOM)
		{
			tab.setPosition(getWidth(), 0);
			containerDiff += Math.max(tab.getHeight() - height, 0);
			width += tab.getWidth();
			height = Math.max(height, tab.getHeight());

			if (tabPosition == Position.BOTTOM)
				setPosition(x, container.getHeight() - containerDiff);
		}
		else
		{
			tab.setPosition(0, getHeight());
			containerDiff += Math.max(tab.getWidth() - width, 0);
			width = Math.max(width, tab.getWidth());

			height += tab.getHeight();

			if (tabPosition == Position.RIGHT)
				setPosition(container.getWidth() - containerDiff, y);
		}

		updateTabs();

		return tab;
	}

	public UITab addTab(String tabName, UIContainer container)
	{
		return addTab(new UITab(tabName), container);
	}

	/**
	 * Aligns the height of each tab
	 */
	private void updateTabs()
	{
		for (Entry<UITab, UIContainer> entry : listTabs.entrySet())
		{
			UITab tab = entry.getKey();
			UIContainer container = entry.getValue();

			int w, h, cx, cy, cw, ch;
			if (tabPosition == Position.TOP || tabPosition == Position.BOTTOM)
			{
				w = tab.isAutoWidth() ? 0 : tab.getWidth();
				h = getHeight();

				cx = getX();
				if (tabPosition == Position.TOP)
					cy = getY() + getHeight() - 2;
				else
					cy = container.getY();

				cw = tab.getContainerWidth();
				ch = tab.getContainerHeight() - containerDiff + 2;
			}
			else
			{
				w = tab.getWidth();
				h = tab.isAutoHeight() ? 0 : getHeight();

				if (tabPosition == Position.LEFT)
					cx = getX() + getWidth() - 2;
				else
					cx = container.getX();
				cy = getY();

				cw = tab.getContainerWidth() - containerDiff + 2;
				ch = tab.getContainerHeight();
			}

			tab.setSize(w, h);
			container.setSize(cw, ch);
			container.setPosition(cx, cy);
		}
	}

	/**
	 * Activates the tab and deactivates currently active tab.
	 * 
	 * @param tab
	 */
	public void setActiveTab(UITab tab)
	{
		if (activeTab == tab)
			return;

		if (activeTab != null)
			activeTab.setActive(false);

		activeTab = tab;
		tab.setActive(true);
	}
}
