package net.malisis.core.client.gui.event;

import net.malisis.core.client.gui.component.UIComponent;

/**
 * ComponentEvent
 *
 * @author PaleoCrafter
 */
public class ComponentEvent extends GuiEvent
{

    private UIComponent component;

    public ComponentEvent(UIComponent component)
    {
        this.component = component;
    }

    public UIComponent getComponent()
    {
        return component;
    }
}
