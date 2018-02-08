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

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
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
	protected Map<UITab, UIContainer<?>> listTabs = new LinkedHashMap<>();
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

		calculateTabPosition();

		return tab;
	}

	/**
	 * Calculates the {@link UITab} position.<br>
	 * Sets the width and height of this {@link UITabGroup}.
	 */
	protected void calculateTabPosition()
	{
		int w = 0;
		int h = 0;
		int s = 0;

		for (UITab tab : listTabs.keySet())
		{
			int sa = tab.isActive() ? 2 : 0;
			if (tabPosition == ComponentPosition.TOP || tabPosition == ComponentPosition.BOTTOM)
			{
				tab.setPosition(w + offset + s, 1);
				w += tab.getWidth() + s;
				h = Math.max(h, tab.getHeight() - sa);
			}
			else
			{
				tab.setPosition(1, h + offset + s);
				w = Math.max(w, tab.getWidth() - sa);
				h += tab.getHeight() + s;
			}
			s = spacing;
		}

		boolean isHorizontal = tabPosition == ComponentPosition.TOP || tabPosition == ComponentPosition.BOTTOM;
		for (UITab tab : listTabs.keySet())
			tab.setSize(isHorizontal ? 0 : w, isHorizontal ? h : 0);

		if (isHorizontal)
			w += offset * 2;
		else
			h += offset * 2;

		setSize(w + 2, h + 2);
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
		if (attachedContainer instanceof ITransformable.Color)
			((ITransformable.Color) attachedContainer).setColor(tab.getBgColor());

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

		if (!displace)
		{
			if (activeTab != null)
			{
				UITab tab = activeTab;
				activeTab = null;
				setActiveTab(tab);
			}

			return this;
		}

		int cx = container.getX();
		int cy = container.getY();
		int cw = container.getRawWidth();
		int ch = container.getRawHeight();
		int av = Anchor.vertical(container.getAnchor());
		int ah = Anchor.horizontal(container.getAnchor());

		if (tabPosition == ComponentPosition.TOP)
		{
			if (av == Anchor.TOP || av == Anchor.NONE)
				cy += getHeight() - 1;
			ch = container.getRawHeight() - getHeight();
		}
		else if (tabPosition == ComponentPosition.BOTTOM)
		{
			if (av == Anchor.BOTTOM)
				cy -= getHeight() - 1;
			ch = container.getRawHeight() - getHeight() + 1;
		}
		else if (tabPosition == ComponentPosition.LEFT)
		{
			if (ah == Anchor.LEFT || ah == Anchor.NONE)
				cx += getWidth() - 1;
			cw = container.getRawWidth() - getWidth();
		}
		else if (tabPosition == ComponentPosition.RIGHT)
		{
			if (ah == Anchor.RIGHT)
				cx -= getWidth() - 1;
			cw = container.getRawWidth() - getWidth() + 1;
		}

		//tab.setSize(w, h);
		container.setSize(cw, ch);
		container.setPosition(cx, cy);

		if (activeTab != null)
		{
			UITab tab = activeTab;
			activeTab = null;
			setActiveTab(tab);
		}
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
