package net.malisis.core.client.gui.renderer;

import net.malisis.core.client.gui.util.Size;

/**
 * Drawable
 *
 * @author PaleoCrafter
 */
public interface Drawable
{

    public void draw(int x, int y);

    /**
     * Set the size to given width and height.
     *
     * @param width  the new width for this <code>Drawable</code>
     * @param height the new height for this <code>Drawable</code>
     */
    public void setSize(int width, int height);

    /**
     * Set the {@link net.malisis.core.client.gui.util.Size size} of this <code>Drawable</code>.
     *
     * @param size the size for this <code>Drawable</code>
     */
    public void setSize(Size size);

}
