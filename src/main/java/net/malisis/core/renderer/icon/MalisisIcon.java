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

package net.malisis.core.renderer.icon;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.malisis.core.asm.AsmUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

/**
 * Extension of {@link TextureAtlasSprite} to allow common operations like clipping and offset.<br>
 * Allows to have an IIcon that is not registered, but depending on a registered one.
 *
 * @author Ordinastie
 *
 */
public class MalisisIcon extends TextureAtlasSprite
{
	/** The private field for registered sprites. */
	private static Field mapRegisteredSprites = AsmUtils.changeFieldAccess(TextureMap.class, "mapRegisteredSprites", "field_110574_e");
	//TODO: fix missing icon not properly loaded
	/** Missing texture {@link MalisisIcon} **/
	public static MalisisIcon missing = new MalisisIcon("missingno");

	/** Width of the global texture sheet. */
	protected int sheetWidth;
	/** Height of the global texture sheet. */
	protected int sheetHeight;

	/** Is the icon flipped on the horizontal axis. */
	protected boolean flippedU = false;
	/** Is the icon flipped on the vertical axis. */
	protected boolean flippedV = false;
	/** Rotation value (clockwise). */
	protected int rotation = 0;

	/** Lists of MalisisIcon depending on this one. */
	protected Set<MalisisIcon> dependants = new HashSet<>();

	/**
	 * Instantiates a new {@link MalisisIcon}.
	 */
	public MalisisIcon()
	{
		super("");
		maxU = 1;
		maxV = 1;
	}

	/**
	 * Instantiates a new {@link MalisisIcon}.
	 *
	 * @param name the name
	 */
	public MalisisIcon(String name)
	{
		super(name);
		maxU = 1;
		maxV = 1;
	}

	/**
	 * Instantiates a new {@link MalisisIcon}.
	 *
	 * @param baseIcon the base icon
	 */
	public MalisisIcon(MalisisIcon baseIcon)
	{
		super(baseIcon.getIconName());
		maxU = 1;
		maxV = 1;
		baseIcon.addDependant(this);
	}

	/**
	 * Instantiates a new {@link MalisisIcon}.
	 *
	 * @param name the name
	 * @param u the u
	 * @param v the v
	 * @param U the u
	 * @param V the v
	 */
	public MalisisIcon(String name, float u, float v, float U, float V)
	{
		this(name);
		minU = u;
		minV = v;
		maxU = U;
		maxV = V;
	}

	public MalisisIcon(TextureAtlasSprite icon)
	{
		this(icon.getIconName());
		copyFrom(icon);
	}

	/**
	 * Adds a {@link MalisisIcon} to be dependant on this one. Will call {@link #initIcon(MalisisIcon, int, int, int, int, boolean)} when
	 * stiched to the sheet.
	 *
	 * @param icon the icon
	 */
	public void addDependant(MalisisIcon icon)
	{
		dependants.add(icon);
	}

	//#region getters/setters
	/**
	 * Sets the size in pixel of this {@link MalisisIcon}.
	 *
	 * @param width the width
	 * @param height the height
	 */
	public void setSize(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * Sets the u vs.
	 *
	 * @param u the u
	 * @param v the v
	 * @param U the u
	 * @param V the v
	 */
	public void setUVs(float u, float v, float U, float V)
	{
		minU = u;
		minV = v;
		maxU = U;
		maxV = V;
	}

	/**
	 * Gets the min u.
	 *
	 * @return the min u
	 */
	@Override
	public float getMinU()
	{
		return this.flippedU ? maxU : minU;
	}

	/**
	 * Gets the max u.
	 *
	 * @return the max u
	 */
	@Override
	public float getMaxU()
	{
		return this.flippedU ? minU : maxU;
	}

	/**
	 * Gets the min v.
	 *
	 * @return the min v
	 */
	@Override
	public float getMinV()
	{
		return this.flippedV ? maxV : minV;
	}

	/**
	 * Gets the max v.
	 *
	 * @return the max v
	 */
	@Override
	public float getMaxV()
	{
		return this.flippedV ? minV : maxV;
	}

	/**
	 * Sets this {@link MalisisIcon} to be flipped.
	 *
	 * @param horizontal whether to flip horizontally
	 * @param vertical whether to flip vertically
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon flip(boolean horizontal, boolean vertical)
	{
		flippedU = horizontal;
		flippedV = vertical;
		return this;
	}

	/**
	 * Checks if is flipped u.
	 *
	 * @return true if this {@link MalisisIcon} is flipped horizontally.
	 */
	public boolean isFlippedU()
	{
		return flippedU;
	}

	/**
	 * Checks if is flipped v.
	 *
	 * @return true if this {@link MalisisIcon} is flipped vertically.
	 */
	public boolean isFlippedV()
	{
		return flippedV;
	}

	/**
	 * Checks if is rotated.
	 *
	 * @return true fi this {@link MalisisIcon} is rotated.
	 */
	public boolean isRotated()
	{
		return rotation != 0;
	}

	/**
	 * Sets the rotation for this {@link MalisisIcon}. The icon will be rotated <b>rotation</b> x 90 degrees clockwise.
	 *
	 * @param rotation the rotation
	 */
	public void setRotation(int rotation)
	{
		this.rotation = rotation;
	}

	/**
	 * Gets the rotation.
	 *
	 * @return the rotation for this {@link MalisisIcon}.
	 */
	public int getRotation()
	{
		return rotation;
	}

	//#end getters/setters

	/**
	 * Initializes this {@link MalisisIcon}. Called from the icon this one depends on, copying the <b>baseIcon</b> values.
	 *
	 * @param baseIcon the base icon
	 * @param width the width
	 * @param height the height
	 * @param x the x
	 * @param y the y
	 * @param rotated the rotated
	 */
	protected void initIcon(MalisisIcon baseIcon, int width, int height, int x, int y, boolean rotated)
	{
		copyFrom(baseIcon);
	}

	/**
	 * Offsets this {@link MalisisIcon} by a specified amount. <b>offsetX</b> and <b>offsetY</b> are specified in pixels.
	 *
	 * @param offsetX the x offset
	 * @param offsetY the y offset
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon offset(int offsetX, int offsetY)
	{
		initSprite(sheetWidth, sheetHeight, getOriginX() + offsetX, getOriginY() + offsetY, isRotated());
		return this;
	}

	/**
	 * Clips this {@link MalisisIcon}. <b>offsetX</b>, <b>offsetY</b>, <b>width</b> and <b>height</b> are specified in pixels.
	 *
	 * @param offsetX the x offset
	 * @param offsetY the y offset
	 * @param width the width
	 * @param height the height
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon clip(int offsetX, int offsetY, int width, int height)
	{
		this.width = width;
		this.height = height;
		offset(offsetX, offsetY);

		return this;
	}

	/**
	 * Clips this {@link MalisisIcon}. <b>offsetXFactor</b>, <b>offsetYFactor</b>, <b>widthFactor</b> and <b>heightFactor</b> are values
	 * from zero to one.
	 *
	 * @param offsetXFactor the x factor for offset
	 * @param offsetYFactor the y factor for offset
	 * @param widthFactor the width factor
	 * @param heightFactor the height factor
	 * @return this {@link MalisisIcon}
	 */
	public MalisisIcon clip(float offsetXFactor, float offsetYFactor, float widthFactor, float heightFactor)
	{
		int offsetX = Math.round(width * offsetXFactor);
		int offsetY = Math.round(height * offsetYFactor);

		width = Math.round(width * widthFactor);
		height = Math.round(height * heightFactor);

		offset(offsetX, offsetY);

		return this;
	}

	/**
	 * Called when the part represented by this {@link MalisisIcon} is stiched to the texture. Sets most of the icon fields.
	 *
	 * @param width the width
	 * @param height the height
	 * @param x the x
	 * @param y the y
	 * @param rotated the rotated
	 */
	@Override
	public void initSprite(int width, int height, int x, int y, boolean rotated)
	{
		this.sheetWidth = width;
		this.sheetHeight = height;
		super.initSprite(width, height, x, y, rotated);
		for (TextureAtlasSprite dep : dependants)
		{
			if (dep instanceof MalisisIcon)
				((MalisisIcon) dep).initIcon(this, width, height, x, y, rotated);
			else
				copyFrom(this);
		}
	}

	/**
	 * Copies the values from {@link MalisisIcon base} to this {@link MalisisIcon}.
	 *
	 * @param base the icon to copy from
	 */
	@Override
	public void copyFrom(TextureAtlasSprite base)
	{
		super.copyFrom(base);
		for (int i = 0; i < base.getFrameCount(); i++)
			this.framesTextureData.add(base.getFrameTextureData(i));

		if (base instanceof MalisisIcon)
		{
			MalisisIcon mbase = (MalisisIcon) base;
			this.sheetWidth = mbase.sheetWidth;
			this.sheetHeight = mbase.sheetHeight;
			this.flippedU = mbase.flippedU;
			this.flippedV = mbase.flippedV;
		}
	}

	/**
	 * Creates a new {@link MalisisIcon} from this <code>MalisisIcon</code>.
	 *
	 * @return the new {@link MalisisIcon}
	 */
	public MalisisIcon copy()
	{
		MalisisIcon icon = new MalisisIcon();
		icon.copyFrom(this);
		return icon;
	}

	/**
	 * Attempts to register this {@link MalisisIcon} to the {@link TextureMap}. If a {@link MalisisIcon} is already registered with this
	 * name, that registered icon will be returned instead.
	 *
	 * @param textureMap the TextureMap
	 * @return this {@link MalisisIcon} if not already registered, otherwise, the MalisisIcon already inside the registry.
	 */
	public MalisisIcon register(TextureMap textureMap)
	{
		TextureAtlasSprite icon = textureMap.getTextureExtry(getIconName());
		if (icon instanceof MalisisIcon)
			return (MalisisIcon) icon;

		//make sure to replace only vanilla TextureAtlasSprite
		if (icon != null && icon.getClass() == TextureAtlasSprite.class)
			return replaceRegisteredIcon(textureMap);

		textureMap.setTextureEntry(getIconName(), this);
		return this;
	}

	/**
	 * Forcefully replaces the {@link TextureAtlasSprite} registered, by a {@link MalisisIcon} version of it.
	 *
	 * @param textureMap the texture map
	 * @return this {@link MalisisIcon}
	 */
	private MalisisIcon replaceRegisteredIcon(TextureMap textureMap)
	{
		try
		{
			HashMap<String, TextureAtlasSprite> map = (HashMap<String, TextureAtlasSprite>) mapRegisteredSprites.get(textureMap);
			map.put(getIconName(), this);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		return this;
	}
}
