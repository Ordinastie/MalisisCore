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

package net.malisis.core.configuration.setting;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.base.Preconditions;

/**
 * The Class Setting.
 *
 * @author Ordinastie
 * @param <T> the generic type
 */
public abstract class Setting<T>
{
	protected Property property;
	protected Property.Type type;
	protected String category = "General";
	protected String key;
	protected String[] comments = new String[0];
	protected T defaultValue;
	protected T value;

	/**
	 * Instantiates a new setting.
	 *
	 * @param key the key
	 * @param defaultValue the default value
	 */
	public Setting(String key, T defaultValue)
	{
		this.type = Property.Type.STRING;
		this.key = key;
		this.defaultValue = Preconditions.checkNotNull(defaultValue);
	}

	/**
	 * Sets the category.
	 *
	 * @param category the new category
	 */
	public void setCategory(String category)
	{
		this.category = category;
	}

	/**
	 * Sets the comment.
	 *
	 * @param comment the new comment
	 */
	public void setComment(String... comment)
	{
		this.comments = comment;
	}

	/**
	 * Sets the value.
	 *
	 * @param value the value
	 */
	public void set(T value)
	{
		this.value = Preconditions.checkNotNull(value);
	}

	/**
	 * Gets the value.
	 *
	 * @return the t
	 */
	public T get()
	{
		return value;
	}

	/**
	 * Loads the configuration.
	 *
	 * @param config the config
	 */
	public void load(Configuration config)
	{
		String comment = null;
		for (String c : comments)
			comment += I18n.format(c) + " ";
		property = config.get(category, key, writeValue(defaultValue), comment, type);
		value = readValue(property.getString());
		if (value == null)
			throw new NullPointerException("readPropertyValue should not return null!");
	}

	/**
	 * Save the configuration.
	 */
	public void save()
	{
		property.set(writeValue(value));
	}

	/**
	 * Gets the comments.
	 *
	 * @return the comments
	 */
	public String[] getComments()
	{
		return comments;
	}

	public void applySettingFromComponent()
	{
		set(getValueFromComponent());
	}

	/**
	 * Reads the value.
	 *
	 * @param stringValue the string value
	 * @return the t
	 */
	public abstract T readValue(String stringValue);

	/**
	 * Writes the value.
	 *
	 * @param value the value
	 * @return the string
	 */
	public abstract String writeValue(T value);

	/**
	 * Gets the component.
	 *
	 * @param gui the gui
	 * @return the component
	 */
	@SideOnly(Side.CLIENT)
	public abstract UIComponent<?> getComponent(MalisisGui gui);

	/**
	 * Gets the value from component.
	 *
	 * @return the value from component
	 */
	@SideOnly(Side.CLIENT)
	public abstract T getValueFromComponent();
}
