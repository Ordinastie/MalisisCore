/*******************************************************************************
 The MIT License (MIT)

 Copyright (c) 2014 MineFormers

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ******************************************************************************/

package net.malisis.core.client.gui.proxy;

import net.malisis.core.client.gui.event.GuiEvent;

/**
 * Context
 *
 * @author PaleoCrafter
 */
public interface Context
{

    /**
     * Publish an event to this context's bus.
     *
     * @param event the {@link net.malisis.core.client.gui.event.GuiEvent event} to publish
     * @return true, if the event was cancelled
     */
    public boolean publish(GuiEvent event);

    /**
     * Register a listener to this context's bus.
     *
     * @param listener the listener to register
     */
    public void register(Object listener);

    /**
     * Unregister a listener from this context's bus.
     *
     * @param object the listener to remove
     */
    public void unregister(Object object);

    /**
     * Unregister and remove all listeners from this context's bus.
     *
     * @see #unregister(Object)
     */
    public void removeAll();

}
