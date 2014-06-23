package net.malisis.core.client.gui;

import net.malisis.core.client.gui.component.container.UIContainer;

public class ClipArea
{
	public boolean noClip = false;
	public int x;
	public int y;
	public int X;
	public int Y;
	public int clipPadding;

	public ClipArea(UIContainer container)
	{
		this(container, 0, true);
	}

	public ClipArea(UIContainer container, int clipPadding)
	{
		this(container, clipPadding, true);
	}

	public ClipArea(UIContainer container, int clipPadding, boolean intersect)
	{
		if (!container.clipContent)
			this.noClip = true;
		else
		{
			this.x = container.screenX() + clipPadding;
			this.y = container.screenY() + clipPadding;
			this.X = this.x + container.getWidth() - clipPadding * 2;
			this.Y = this.y + container.getHeight() - clipPadding * 2;
			this.clipPadding = clipPadding;
		}

		if (intersect && container.getParent() != null)
			this.intersect(container.getParent().getClipArea());
	}

	public void intersect(ClipArea area)
	{
		if (this.noClip)
		{
			x = area.x;
			y = area.y;
			X = area.X;
			Y = area.Y;
			this.clipPadding = area.clipPadding;
		}
		else if (!area.noClip)
		{
			x = Math.max(x, area.x);
			y = Math.max(y, area.y);
			X = Math.min(X, area.X);
			Y = Math.min(Y, area.Y);
		}
	}

	public int width()
	{
		return X - x;
	}

	public int height()
	{
		return Y - y;
	}

	@Override
	public String toString()
	{
		return x + "->" + X + " , " + y + "->" + Y + " (" + width() + "," + height() + ")";
	}

}
