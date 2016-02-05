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

package net.malisis.core.block;

import java.util.List;

import net.minecraft.block.Block;

/**
 * The Interface IComponentProvider defines a {@link Block} that can handle {@link IBlockComponent}.
 *
 * @author Ordinastie
 */
public interface IComponentProvider
{
	/**
	 * Gets the {@link IBlockComponent} for this {@link IComponentProvider}.
	 *
	 * @return the components
	 */
	public List<IBlockComponent> getComponents();

	/**
	 * Adds the {@link IBlockComponent} to this {@link IComponentProvider}.
	 *
	 * @param component the component
	 */
	public void addComponent(IBlockComponent component);

	/**
	 * Gets the {@link IBlockComponent} of the specified type from this {@link IComponentProvider}.
	 *
	 * @param <T> the generic type
	 * @param type the type
	 * @return the component
	 */
	public <T> T getComponent(Class<T> type);

}
