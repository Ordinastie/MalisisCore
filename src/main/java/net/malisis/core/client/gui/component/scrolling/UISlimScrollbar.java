package net.malisis.core.client.gui.component.scrolling;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.control.IScrollable;
import net.malisis.core.client.gui.element.Size;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.client.gui.render.shape.GuiShape;

/**
 * @author Ordinastie
 *
 */
public final class UISlimScrollbar extends UIScrollBar
{
	/** Background color of the scroll. */
	protected int backgroundColor = 0x999999;
	/** Scroll color **/
	protected int scrollColor = 0xFFFFFF;
	/** Whether the scrollbar should fade in/out */
	protected boolean fade = true;

	public <T extends UIComponent & IScrollable<?>> UISlimScrollbar(T parent, Type type)
	{
		super(parent, type);

		scrollSize = isHorizontal() ? Size.of(15, 2) : Size.of(2, 15);

		setBackground(GuiShape.builder(this).color(this::getBackgroundColor).build());
		setForeground(GuiShape.builder(this).position().set(scrollPosition).back().size(scrollSize).color(this::scrollColor).build());

	}

	public void setFade(boolean fade)
	{
		this.fade = fade;
	}

	public boolean isFade()
	{
		return fade;
	}

	/**
	 * Sets the color of the scroll.
	 *
	 * @param scrollColor the new color
	 */
	@Override
	public void setColor(int scrollColor)
	{
		setColor(scrollColor, backgroundColor);
	}

	/**
	 * Sets the color of the scroll and the background.
	 *
	 * @param scrollColor the scroll color
	 * @param backgroundColor the background color
	 */
	public void setColor(int scrollColor, int backgroundColor)
	{
		this.scrollColor = scrollColor;
		this.backgroundColor = backgroundColor;
	}

	public int scrollColor()
	{
		return scrollColor;
	}

	public int getBackgroundColor()
	{
		return backgroundColor;
	}

	@Subscribe
	public void onMouseOver(HoveredStateChange<?> event)
	{
		if (!fade)
			return;

		if (isFocused() && !event.getState())
			return;

		//int from = event.getState() ? 0 : 255;
		//int to = event.getState() ? 255 : 0;

		//Animation<ITransformable.Alpha> anim = new Animation<>(this, new AlphaTransform(from, to).forTicks(5));

		//MalisisGui.currentGui().animate(anim);
	}
}