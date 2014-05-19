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

package net.malisis.core.client.gui.component;

import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.event.GuiEvent;
import net.malisis.core.client.gui.proxy.Context;
import net.malisis.core.client.gui.proxy.GuiManager;
import net.malisis.core.client.gui.renderer.GuiRenderer;
import net.malisis.core.client.gui.util.Size;
import net.malisis.core.client.gui.util.shape.Point;
import net.malisis.core.client.gui.util.shape.Rectangle;
import net.malisis.core.demo.test.GuiIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import cpw.mods.fml.client.FMLClientHandler;

/**
 * UIComponent
 *
 * @author PaleoCrafter
 */
public abstract class UIComponent
{

    /**
     * A {@link net.minecraft.client.Minecraft Minecraft} instance to
     * be used for rendering. Should never be needed with
     * {@link net.malisis.core.util.RenderHelper RenderHelper} in place
     */
    protected Minecraft mc;
    /**
     * The position of this <code>UIComponent</code>. Shouldn't be used for
     * rendering. Will be ignored for most layout managers apart from
     * absolute layouts.
     */
    protected Point position;
    /**
     * The {@link net.malisis.core.client.gui.util.Size Size} of this <code>UIComponent</code>.
     * Could be ignored by various layout managers.
     */
    protected Size size;
    /**
     * Determines whether this <code>UIComponent</code> is visible.
     * If set to false, {@link #size size} will be ignored by
     * most layout managers.
     */
    protected boolean visible;
    /**
     * Determines whether this <code>UIComponent</code> is enabled.
     * If set to false, no {@link net.malisis.core.client.gui.event.GuiEvent events} will
     * be passed through to this component.
     */
    protected boolean enabled;
    /**
     * The z-index of this component, used for prevent Z fighting of overlapping components.
     */
    protected int zIndex;
    /**
     * The screen position of this <code>UIComponent</code>.
     * To be set by the layout manager.
     */
    protected Point screenPosition;
    /**
     * The {@link net.malisis.core.client.gui.proxy.Context Context} of this
     * component. Can be used to publish events, but should never be needed
     * to call yourself. The context is the GUI proxy containing all components.
     *
     * @see #getContext()
     */
    private Context context;
    /**
     * The parent <code>UIComponent</code> of this <code>UIComponent</code>.
     * Can be used to pass through things or manipulate the parent's other
     * children.
     */
    private UIComponent parent;
    /**
     * The name of this <code>UIComponent</code>. Can be used to retrieve this back
     * from a container.
     */
    private String name;
    /**
     * The tooltip for this <code>UIComponent</code>.
     * Automatically displayed when the <code>UIComponent</code> is hovered.
     */
    private String tooltip;
    /**
     * The bounds of this <code>UIComponent</code>.
     * Basically a rectangle with start point
     * at the {@link #position position} with the component's
     * size.
     */
    private Rectangle bounds;
    /**
     * The screen bounds of this <code>UIComponent</code>.
     * Basically a rectangle with start point
     * at the {@link #screenPosition "screen position"} with the component's
     * size. Used for hover checks
     */
    private Rectangle screenBounds;

    protected UIComponent()
    {
        this.mc = FMLClientHandler.instance().getClient();
        this.zIndex = 0;
        this.visible = true;
        this.enabled = true;
        this.size = new Size(0, 0);
        this.position = new Point(0, 0);
    }

    /**
     * Initializes the component once the GUI was opened.
     * Use this to register the <code>UIComponent</code>'s event listeners.
     */
    public void initComponent()
    {
        getContext().register(this);
    }

    /**
     * Called whenever the GUI is in the background drawing phase.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public void drawBackground(int mouseX, int mouseY)
    {
    }

    /**
     * Called after {@link #drawBackground(int, int)} and is supposed
     * to be used for the main drawing.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public abstract void draw(int mouseX, int mouseY);

    /**
     * Called whenever the GUI is in the foreground drawing phase, always
     * after {@link #draw(int, int)}.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public void drawForeground(int mouseX, int mouseY)
    {
    }

    /**
     * Called every tick the GUI updates.
     *
     * @param mouseX the x position of the mouse
     * @param mouseY the y position of the mouse
     */
    public abstract void update(int mouseX, int mouseY);

    /**
     * @return the parent of this <code>UIComponent</code>
     * @see #parent
     */
    public UIComponent getParent()
    {
        return parent;
    }

    /**
     * Set the parent of this <code>UIComponent</code>.
     *
     * @param parent the parent to be used
     * @see #parent
     */
    public void setParent(UIComponent parent)
    {
        this.parent = parent;
    }

    /**
     * @return the name of this <code>UIComponent</code>
     * @see #name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Set the name of this <code>UIComponent</code>.
     *
     * @param name the name to be used
     * @see #name
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the tooltip of this <code>UIComponent</code>.
     * @see #tooltip
     */
    public String getTooltip()
    {
        return tooltip;
    }

    /**
     * Set the tooltip of this <code>UIComponent</code>.
     *
     * @param tooltip the tooltip for this <code>UIComponent</code>
     * @see #tooltip
     */
    public void setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
    }

    /**
     * @return the X coordinate of this <code>UIComponent</code>'s position
     */
    public int getX()
    {
        return position.getX();
    }

    /**
     * @return the Y coordinate of this <code>UIComponent</code>'s position
     */
    public int getY()
    {
        return position.getY();
    }

    /**
     * Set the position of this <code>UIComponent</code>
     * to the given coordinates.
     *
     * @param x the X coordinate for this <code>UIComponent</code>
     * @param y the Y coordinate for this <code>UIComponent</code>
     */
    public void setPosition(int x, int y)
    {
        setPosition(new Point(x, y));
    }

    /**
     * @return the size of this <code>UIComponent</code>
     * @see #size
     */
    public Size getSize()
    {
        return size;
    }

    /**
     * Set the {@link net.malisis.core.client.gui.util.Size size} of this <code>UIComponent</code>.
     *
     * @param size the size for this <code>UIComponent</code>
     * @see #size
     */
    public void setSize(Size size)
    {
        this.size = size;
    }

    /**
     * Set the size to given width and height.
     *
     * @param width  the new width for this <code>UIComponent</code>
     * @param height the new height for this <code>UIComponent</code>
     */
    public void setSize(int width, int height)
    {
        setSize(new Size(width, height));
    }

    /**
     * @return a {@link net.malisis.core.client.gui.util.shape.Rectangle rectangle} representing
     * this component's bounds
     */
    public Rectangle getBounds()
    {
        if (bounds == null || bounds.getWidth() != getWidth() || bounds.getHeight() != getHeight() || !bounds.getStart().equals(getPosition()))
            bounds = new Rectangle(position, getWidth(), getHeight());
        return bounds;
    }

    /**
     * @return the position of this <code>UIComponent</code>
     * @see #position
     */
    public Point getPosition()
    {
        return position;
    }

    /**
     * Set the position of this <code>UIComponent</code>.
     *
     * @param position the position for this <code>UIComponent</code>
     * @see #position
     */
    public void setPosition(Point position)
    {
        this.position = position;
    }

    /**
     * @return the width of this <code>UIComponent</code>
     */
    public int getWidth()
    {
        return size.getWidth();
    }

    /**
     * @return the height of this <code>UIComponent</code>
     */
    public int getHeight()
    {
        return size.getHeight();
    }

    /**
     * @return the visibility of this component
     */
    public boolean isVisible()
    {
        return visible;
    }

    /**
     * Set the visibility of this component.
     *
     * @param visible the visibility for this component
     */
    public void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * @return the state of this component
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * Set the state of this <code>UIComponent</code>.
     *
     * @param enabled true for the component to be enabled
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Checks the hovering state of this component.
     * Defaults to the region in between the rectangle at the screen
     * position with according size.
     *
     * @param mousePosition the mouse {@link net.malisis.core.client.gui.util.shape.Point position}
     *                      on screen
     * @return true, if the component is hovered
     */
    public boolean isHovered(Point mousePosition)
    {
        return getScreenBounds().contains(mousePosition);
    }

    /**
     * @return a {@link net.malisis.core.client.gui.util.shape.Rectangle rectangle} representing
     * this component's screen bounds
     */
    public Rectangle getScreenBounds()
    {
        if (screenBounds == null || screenBounds.getWidth() != getWidth() || screenBounds.getHeight() != getHeight() || !screenBounds.getStart().equals(getScreenPosition()))
            screenBounds = new Rectangle(getScreenPosition(), getWidth(), getHeight());
        return screenBounds;
    }

    /**
     * @return the screen position of this <code>UIComponent</code>
     */
    public Point getScreenPosition()
    {
        return screenPosition;
    }

    /**
     * Set the screen position of this <code>UIComponent</code>.
     * Only to be used by the layout manager.
     *
     * @param screenPosition the screen position for this <code>UIComponent</code>
     */
    public void setScreenPosition(Point screenPosition)
    {
        this.screenPosition = screenPosition;
    }

    /**
     * @return the X coordinate on screen for this <code>UIComponent</code>
     */
    public int getScreenX()
    {
        return screenPosition.x;
    }

    /**
     * @return the Y coordinate on screen for this <code>UIComponent</code>
     */
    public int getScreenY()
    {
        return screenPosition.y;
    }

    /**
     * Publish an event to all listeners of the active context.
     *
     * @param event the {@link net.malisis.core.client.gui.event.GuiEvent event} to publish
     */
    public void publish(GuiEvent event)
    {
        this.getContext().publish(event);
    }

    /**
     * Get the {@link net.malisis.core.client.gui.proxy.Context context} of this component.
     * Gets the active context if none is set.
     *
     * @return the context of this component
     */
    public Context getContext()
    {
        if (context == null)
            context = GuiManager.getActiveContext();
        return this.context;
    }

    /**
     * Set the {@link net.malisis.core.client.gui.proxy.Context context} of this <code>UIComponent</code>.
     * <b>MUST NOT</b> be called by anything else but the Minecraft proxy class for the GUI.
     * The context is the GUI proxy containing all components.
     *
     * @param context the context for this component
     */
    public void setContext(Context context)
    {
        if (this.context != null)
            this.context.unregister(this);
        this.context = context;
    }

    /**
     * Register a listener to the <code>UIComponent</code>'s context.
     * Methods within the listener are supposed to have the
     * {@link cpw.mods.fml.common.eventhandler.SubscribeEvent @SubscribeEvent} annotation
     * on there event handling methods.
     *
     * @param listener the listener to register.
     */
    public void registerToContext(Object listener)
    {
        getContext().register(listener);
    }

    /**
     * Dispose this component.
     * When overridden, there <b>always</b> must be a super call.
     */
    public void dispose()
    {
        this.getContext().unregister(this);
        this.context = null;
    }

    public String getPropertyString()
    {
        return "size=" + this.getSize() + ", position=" + this.getPosition() + ", screenPosition=" + this.getScreenPosition();
    }

    
    /***
     * V2 Ordinastie
     */
    public abstract ResourceLocation getTexture(int mouseX, int mouseY);
    public abstract GuiIcon getIcon(int face);
   
    public void draw(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
    {
    	renderer.currentComponent = this;
    	renderer.bindTexture(getTexture(mouseX, mouseY));
    	drawBackground(renderer, mouseX, mouseY, partialTick);
    	renderer.next();
    	drawForeground(renderer, mouseX, mouseY, partialTick);
    	renderer.next();
    }
    
    public abstract void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);
    public abstract void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick);
    
    
    public int screenX()
    {
    	int x = position.x;
    	if(parent != null)
    	{
    		x += parent.screenX();
    		if(parent instanceof UIContainer)
    			x += ((UIContainer) parent).getPadding().x;
    	}
    	return x;
    }
    public int screenY()
    {
    	int y = position.y;
    	if(parent != null)
    	{
    		y += parent.screenY();
    		if(parent instanceof UIContainer)
    			y += ((UIContainer) parent).getPadding().y;
    	}
    	return y;
    }
}
