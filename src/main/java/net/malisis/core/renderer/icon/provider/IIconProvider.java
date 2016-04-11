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

package net.malisis.core.renderer.icon.provider;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.icon.Icon;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The IIconProvider interface allows to pass {@link Icon} to the rendering processes.<br>
 * The interface extends {@link IComponent} so it can be added to {@link MalisisBlock} components.
 *
 * @author Ordinastie
 */
public interface IIconProvider extends IComponent
{
	@Override
	public default boolean isClientComponent()
	{
		return true;
	}

	/**
	 * Gets the {@link Icon} to use.
	 *
	 * @return the icon
	 */
	@SideOnly(Side.CLIENT)
	public Icon getIcon();

	/**
	 * Creates a {@link IconProviderBuilder}.
	 *
	 * @return the icon provider builder
	 */
	@SideOnly(Side.CLIENT)
	public static IconProviderBuilder create(Icon icon)
	{
		return new IconProviderBuilder("", icon);
	}

	/**
	 * Creates a {@link IconProviderBuilder} with a prefix.<br>
	 * The prefix will be prepend for all string passed to the builder.
	 *
	 * @param prefix the prefix
	 * @param icon the icon
	 * @return the icon provider builder
	 */
	@SideOnly(Side.CLIENT)
	public static IconProviderBuilder create(String prefix, Icon icon)
	{
		return new IconProviderBuilder(prefix, icon);
	}

	/**
	 * Creates a {@link IconProviderBuilder}.
	 *
	 * @param name the name
	 * @return the icon provider builder
	 */
	@SideOnly(Side.CLIENT)
	public static IconProviderBuilder create(String name)
	{
		return new IconProviderBuilder("", name);
	}

	/**
	 * Creates a {@link IconProviderBuilder} with a prefix.<br>
	 * The prefix will be prepend for all string passed to the builder.
	 *
	 * @param prefix the prefix
	 * @param name the name
	 * @return the icon provider builder
	 */
	@SideOnly(Side.CLIENT)
	public static IconProviderBuilder create(String prefix, String name)
	{
		return new IconProviderBuilder(prefix, name);
	}
}
