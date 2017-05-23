/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Ordinastie
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

import java.util.Map;

import com.google.common.collect.Maps;

import net.malisis.core.block.IComponentProvider;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.component.ModelComponent;
import net.malisis.core.renderer.icon.Icon;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Ordinastie
 *
 */
public interface IModelIconProvider extends IIconProvider
{
	@Override
	public default void onComponentAdded(IComponentProvider provider)
	{
		ModelComponent mc = provider.getComponent(ModelComponent.class);
		if (mc != null)
			mc.setIconProvider(this);
	}

	@Override
	public default Icon getIcon()
	{
		return null;
	}

	public Icon getIcon(MalisisRenderer<? extends TileEntity> renderer, String shapeName);

	public static class ModelIconProvider implements IModelIconProvider
	{
		private Icon icon;
		private Map<String, Icon> icons = Maps.newHashMap();

		public ModelIconProvider(IconProviderBuilder builder)
		{
			this.icon = builder.defaultIcon;
			this.icons = Maps.newHashMap(builder.shapeIcons);
		}

		@Override
		public Icon getIcon(MalisisRenderer<? extends TileEntity> renderer, String shapeName)
		{
			return icons.getOrDefault(shapeName, icon);
		}

		@Override
		public Icon getIcon()
		{
			return icon;
		}
	}
}
