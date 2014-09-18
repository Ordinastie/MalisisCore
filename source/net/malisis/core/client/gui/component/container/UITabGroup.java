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

import net.malisis.core.client.gui.component.interaction.UITab;

/**
 * @author Ordinastie
 * 
 */
public class UITabGroup extends UIContainer<UITabGroup>
{
	private HashMap<UITab, UIContainer> listTabs = new HashMap<>();
	private UITab activeTab;

	public UITabGroup()
	{
		this.height = 14;
		this.width = 0;
		this.x = 3;
	}

	@Override
	public UITabGroup setPosition(int x, int y, int anchor)
	{
		return super.setPosition(x + 3, y, anchor);
	}

	/**
	 * Adds tab and its corresponding container to this <code>UITabGroup</code>.<br />
	 * Also sets the width of this <code>UITabGroup</code>.
	 * 
	 * @param tab
	 * @param container
	 */
	public void addTab(UITab tab, UIContainer container)
	{
		add(tab);
		tab.setPosition(getWidth(), 0);
		tab.setContainer(container);
		tab.setActive(false);
		listTabs.put(tab, container);
		width += tab.getWidth();

		container.setPosition(getX() - 3, getY() + getHeight() - 1);

		if (activeTab == null)
			setActiveTab(tab);
	}

	public void addTab(String tabName, UIContainer container)
	{
		addTab(new UITab(tabName), container);
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
