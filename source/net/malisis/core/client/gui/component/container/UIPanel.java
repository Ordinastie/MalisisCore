package net.malisis.core.client.gui.component.container;

import static net.malisis.core.client.gui.component.interaction.UIScrollBar.*;
import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ClipArea;
import net.malisis.core.client.gui.GuiIcon;
import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.IScrollable;
import net.malisis.core.client.gui.component.interaction.UIScrollBar;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;

public class UIPanel extends UIContainer implements IScrollable
{
	//@formatter:off
	public static GuiIcon[] icons = new GuiIcon[] { new GuiIcon(200, 	15, 	5, 	5),
													new GuiIcon(205, 	15, 	5, 	5),
													new GuiIcon(210, 	15, 	5, 	5),
													new GuiIcon(200, 	20, 	5, 	5),
													new GuiIcon(205, 	20, 	5, 	5),
													new GuiIcon(210, 	20, 	5, 	5),
													new GuiIcon(200, 	25, 	5, 	5),
													new GuiIcon(205, 	25, 	5, 	5),
													new GuiIcon(210, 	25, 	5, 	5)};
	//@formatter:on

	protected boolean allowVerticalScroll = false;
	protected boolean allowHorizontalScroll = false;

	protected UIScrollBar horizontalScroll;
	protected UIScrollBar verticalScroll;

	protected int contentWidth;
	protected int contentHeight;

	protected int xOffset;
	protected int yOffset;

	public UIPanel(int width, int height)
	{
		super(width, height);
		setPadding(3, 3);

		horizontalScroll = new UIScrollBar(this, width, HORIZONTAL);
		verticalScroll = new UIScrollBar(this, height, VERTICAL);
		setScrollBarsPosition();
	}

	@Override
	public boolean fireMouseEvent(MouseEvent event)
	{
		if (allowVerticalScroll
				&& (verticalScroll.isInsideBounds(event.getX(), event.getY()) || (verticalScroll.isFocused() && event instanceof MouseEvent.Drag)))
			return verticalScroll.fireMouseEvent(event);
		if (allowHorizontalScroll
				&& (horizontalScroll.isInsideBounds(event.getX(), event.getY()) || (horizontalScroll.isFocused() && event instanceof MouseEvent.Drag)))
			return horizontalScroll.fireMouseEvent(event);

		if (isInsideBounds(event.getX(), event.getY()))
			return super.fireMouseEvent(event);

		return false;
	}

	@Override
	public boolean isInsideBounds(int x, int y)
	{
		return super.isInsideBounds(x, y);
	}

	public boolean isInsideBounds(int x, int y, boolean scrolls)
	{
		if (scrolls && allowVerticalScroll && verticalScroll.isInsideBounds(x, y))
			return true;
		if (scrolls && allowHorizontalScroll && horizontalScroll.isInsideBounds(x, y))
			return true;
		if (super.isInsideBounds(x, y))
			return true;

		return false;
	}

	// #region getters/setters
	public UIPanel setHorizontalScroll(boolean allow)
	{
		int shift = allow ? -SCROLL_THICKNESS : SCROLL_THICKNESS;
		verticalScroll.setLength(verticalScroll.getLength() + shift);
		allowHorizontalScroll = allow;
		return this;
	}

	public boolean getHorizontalScroll()
	{
		return allowHorizontalScroll;
	}

	public UIPanel setVerticalScroll(boolean allow)
	{
		int shift = allow ? -SCROLL_THICKNESS : SCROLL_THICKNESS;
		horizontalScroll.setLength(horizontalScroll.getLength() + shift);
		allowVerticalScroll = allow;
		return this;
	}

	public boolean getVerticalScroll()
	{
		return allowVerticalScroll;
	}

	public void setScrollBarsPosition()
	{
		verticalScroll.setPosition(horizontalPadding - xOffset, -verticalPadding - yOffset, Anchor.RIGHT);
		horizontalScroll.setPosition(-verticalPadding - xOffset, horizontalPadding - yOffset, Anchor.BOTTOM);
	}

	@Override
	public void setOffsetX(int offset)
	{
		this.xOffset = -offset;
		setScrollBarsPosition();
	}

	@Override
	public void setOffsetY(int offset)
	{
		this.yOffset = -offset;
		setScrollBarsPosition();
	}

	@Override
	public int getOffsetX()
	{
		return xOffset;
	}

	@Override
	public int getOffsetY()
	{
		return yOffset;
	}

	// #end getters/setters

	@Override
	public void add(UIComponent component)
	{
		super.add(component);
		calculateContentSize();
	}

	@Override
	public void onContentUpdate()
	{
		calculateContentSize();
	}

	public void calculateContentSize()
	{
		int w = width - (allowVerticalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
		int h = height - (allowHorizontalScroll ? UIScrollBar.SCROLL_THICKNESS + 1 : 0);
		int contentWidth = w;
		int contentHeight = h;

		for (UIComponent c : components)
		{
			if (c.isVisible())
			{
				contentWidth = Math.max(contentWidth, c.containerX() + c.getWidth());
				contentHeight = Math.max(contentHeight, c.containerY() + c.getHeight());
			}
		}

		this.contentHeight = contentHeight;
		if (verticalScroll != null)
		{
			verticalScroll.setScrollableLength(contentHeight);
			if (contentHeight == h)
				verticalScroll.scrollTo(0);
		}

		this.contentWidth = contentWidth;
		if (horizontalScroll != null)
		{
			horizontalScroll.setScrollableLength(contentWidth);
			if (contentWidth == w)
				horizontalScroll.scrollTo(0);
		}
	}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		// GL11.glTranslatef(xOffset, yOffset, 0);
		super.drawForeground(renderer, mouseX, mouseY, partialTick);
		// GL11.glTranslatef(-xOffset, -yOffset, 0);

		if (allowVerticalScroll)
			verticalScroll.draw(renderer, mouseX, mouseY, partialTick);
		if (allowHorizontalScroll)
			horizontalScroll.draw(renderer, mouseX, mouseY, partialTick);

	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		Shape shape = ShapePreset.GuiXYResizable(width, height);
		renderer.drawShape(shape);
	}

	@Override
	public GuiIcon getIcon(int face)
	{
		if (face < 0 || face > icons.length)
			return null;

		return icons[face];
	}

	@Override
	public ClipArea getClipArea()
	{
		ClipArea area = new ClipArea(this, 1);
		if (allowVerticalScroll)
			area.X -= UIScrollBar.SCROLL_THICKNESS - 1;
		if (allowHorizontalScroll)
			area.Y -= UIScrollBar.SCROLL_THICKNESS - 1;
		return area;
	}
}
