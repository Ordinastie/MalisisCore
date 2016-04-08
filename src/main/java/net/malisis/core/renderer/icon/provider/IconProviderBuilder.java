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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING withDefault,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.renderer.icon.provider;

import java.util.Map;
import java.util.Objects;

import net.malisis.core.block.component.WallComponent;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider.ISidesIconProvider;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider.IStatesIconProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Maps;
import com.google.common.collect.Table;

/**
 * @author Ordinastie
 *
 */
@SideOnly(Side.CLIENT)
public class IconProviderBuilder
{
	private enum Type
	{
		DEFAULT, SIDES, CONNECTED, STATE, WALL
	}

	Type type = Type.DEFAULT;
	String prefix;
	String defaultName;

	//IDefaultIconProvider
	MalisisIcon defaultIcon;

	//ConnectedIconProvider
	MalisisIcon connectedIcon;

	//ISidesIconProvider
	Map<EnumFacing, MalisisIcon> sidesIcons = Maps.newHashMap();

	//IStateIconProvider
	Table<IProperty<?>, Object, MalisisIcon> stateIcons;
	IProperty<?> currentProperty;

	//WALL
	MalisisIcon insideIcon;

	/**
	 * Instantiates a new {@link IconProviderBuilder} with default {@link MalisisIcon}.
	 *
	 * @param icon the icon
	 */
	IconProviderBuilder(MalisisIcon icon)
	{
		defaultIcon = icon;
	}

	/**
	 * Instantiates a new {@link IconProviderBuilder} with default {@link MalisisIcon}.
	 *
	 * @param name the name
	 */
	IconProviderBuilder(String name)
	{
		defaultName = name;
		defaultIcon = icon(name);
	}

	/**
	 * Gets a {@link MalisisIcon} from the name after prepending the set prefix.
	 *
	 * @param name the name
	 * @return the malisis icon
	 */
	private MalisisIcon icon(String name)
	{
		return MalisisIcon.from(prefix + name);
	}

	/**
	 * Sets the prefix to use for all the icon passed using the name.
	 *
	 * @param prefix the prefix
	 * @return the icon provider builder
	 */
	public IconProviderBuilder prefix(String prefix)
	{
		this.prefix = prefix;
		//defautlIcon was constructed from name, recreate it with the prefix
		if (defaultName != null)
		{
			defaultIcon = icon(defaultName);
			//assume only first call to prefix actually intends on applying to default icon
			defaultName = null;
		}
		return this;
	}

	/**
	 * Sets the {@link MalisisIcon} to use for specific side.
	 *
	 * @param side the side
	 * @param icon the icon
	 * @return the icon provider builder
	 */
	public IconProviderBuilder withSide(EnumFacing side, MalisisIcon icon)
	{
		type = Type.SIDES;
		sidesIcons.put(Objects.requireNonNull(side), icon);
		return this;
	}

	/**
	 * Sets the {@link MalisisIcon} to use for specific side.
	 *
	 * @param side the side
	 * @param iconName the name
	 * @return the icon provider builder
	 */
	public IconProviderBuilder withSide(EnumFacing side, String iconName)
	{
		return withSide(side, icon(iconName));
	}

	/**
	 * Sets the {@link IIconProvider} to use connected textures based on the defaultIcon.
	 *
	 * @param icon the icon
	 * @return the icon provider builder
	 */
	public IconProviderBuilder connected()
	{
		return connectedWith(MalisisIcon.from(defaultIcon.getIconName() + "2"));
	}

	/**
	 * Sets the second {@link MalisisIcon} for connected textures.
	 *
	 * @param icon the icon
	 * @return the icon provider builder
	 */
	public IconProviderBuilder connectedWith(MalisisIcon icon)
	{
		type = Type.CONNECTED;
		connectedIcon = Objects.requireNonNull(icon);
		return this;
	}

	/**
	 * Sets the second {@link MalisisIcon} for connected textures.
	 *
	 * @param iconName the name
	 * @return the icon provider builder
	 */
	public IconProviderBuilder connectedWith(String iconName)
	{
		return connectedWith(icon(iconName));
	}

	/**
	 * Sets the {@link IProperty} to check against for values passed with {@link #withValue(Object, MalisisIcon)}.
	 *
	 * @param property the property
	 * @return the icon provider builder
	 */
	public IconProviderBuilder forProperty(IProperty<?> property)
	{
		type = Type.STATE;
		currentProperty = property;
		return this;
	}

	/**
	 * Sets the {@link MalisisIcon} to use for the state value.<br>
	 * {@link #forProperty(IProperty)} must be called before with the corresponding {@link IProperty}.
	 *
	 * @param value the value
	 * @param icon the icon
	 * @return the icon provider builder
	 */
	public IconProviderBuilder withValue(Object value, MalisisIcon icon)
	{
		if (currentProperty == null)
			throw new IllegalStateException("You must set the property first.");
		if (!currentProperty.getAllowedValues().contains(value))
			throw new IllegalStateException("The property " + currentProperty + "(" + currentProperty.getClass().getSimpleName()
					+ ") doesn't not contain value " + value);

		stateIcons.put(currentProperty, value, icon);
		return this;
	}

	/**
	 * Sets the icon to use for WALL type blocks (with {@link WallComponent}) for the inside.<br>
	 * The defaultIcon is used for the outside.
	 *
	 * @param insideIcon the inside icon
	 * @return the icon provider builder
	 */
	public IconProviderBuilder wall(MalisisIcon insideIcon)
	{
		type = Type.WALL;
		this.insideIcon = insideIcon;
		return this;
	}

	public IconProviderBuilder wall(String insideIconName)
	{
		return wall(icon(insideIconName));
	}

	/**
	 * Gets the {@link IStatesIconProvider} from this {@link IconProviderBuilder}.
	 *
	 * @return the state icon provider
	 */
	private IStatesIconProvider getStateIconProvider()
	{
		return state -> {
			return state.getProperties().keySet().stream().map(prop -> stateIcons.get(prop, state.getValue(prop))).findFirst().orElse(null);
		};
	}

	/**
	 * Gets the {@link IBlockIconProvider} to use for {@link WallComponent}.
	 *
	 * @param insideIcon the inside icon
	 * @return the icon provider builder
	 */
	private IBlockIconProvider getWallIconProvider()
	{
		return (state, side) -> {
			if (side == EnumFacing.SOUTH || (side == EnumFacing.WEST && WallComponent.isCorner(state)))
				return insideIcon;
			return defaultIcon;
		};
	}

	/**
	 * Creates the {@link IIconProvider}
	 *
	 * @return the i icon provider
	 */
	public IIconProvider build()
	{
		switch (type)
		{
			case DEFAULT:
				return (IIconProvider) () -> defaultIcon;
			case SIDES:
				return (ISidesIconProvider) side -> com.google.common.base.Objects.firstNonNull(sidesIcons.get(side), defaultIcon);
			case STATE:
				return getStateIconProvider();
			case WALL:
				return getWallIconProvider();
			case CONNECTED:
				return new ConnectedIconsProvider(this);

			default:
				return null;
		}
	}
}
