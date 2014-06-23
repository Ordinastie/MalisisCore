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

package net.malisis.core.configuration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import net.malisis.core.configuration.setting.Setting;
import net.minecraftforge.common.config.Configuration;

/**
 * @author Ordinastie
 * 
 */
public class Settings
{
	private Configuration config;
	private HashMap<String, ArrayList<Setting>> categorySettings = new HashMap<>();

	public Settings(File file)
	{
		this(new Configuration(file));
	}

	public Settings(Configuration config)
	{
		this.config = config;
		config.load();
		getSettingFields();
		config.save();
	}

	public Configuration getConfiguration()
	{
		return config;
	}

	public Set<String> getCategories()
	{
		return categorySettings.keySet();
	}

	public List<Setting> getSettings(String category)
	{
		return categorySettings.get(category);
	}

	private void getSettingFields()
	{
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			ConfigurationSetting annotation;
			if ((annotation = field.getAnnotation(ConfigurationSetting.class)) == null || field.getType() != Setting.class)
				continue;

			Setting setting = null;
			try
			{
				setting = (Setting) field.get(this);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}

			if (setting != null)
			{
				String category = annotation.category();
				setting.setCategory(category);
				setting.load(config);

				ArrayList<Setting> settings = categorySettings.get(category);
				if (settings == null)
					settings = new ArrayList<>();
				settings.add(setting);
				categorySettings.put(category, settings);
			}
		}
	}

	public void save()
	{
		for (Entry<String, ArrayList<Setting>> entry : categorySettings.entrySet())
		{
			for (Setting setting : entry.getValue())
			{
				setting.save();
			}
		}
		config.save();
	}
}
