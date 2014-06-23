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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.configuration.setting.Setting;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 * 
 */
public class ConfigurationGui extends MalisisGui
{
	private Settings settings;
	protected ArrayList<UIPanel> pannels = new ArrayList<>();

	protected int windowWidth = 250;
	protected int windowHeight = 120;

	protected UIButton btnCancel;
	protected UIButton btnSave;

	public ConfigurationGui(Settings settings)
	{
		this.settings = settings;

		Set<String> categories = settings.getCategories();

		if (categories.size() > 1)
		{
			//build tabs
		}

		UIWindow window = new UIWindow("config.title", windowWidth, windowHeight);

		for (String category : categories)
		{
			window.add(createSettingPannel(category));
		}

		btnCancel = new UIButton("gui.cancel").setPosition(-32, 0, Anchor.BOTTOM | Anchor.CENTER).register(this);
		btnSave = new UIButton("gui.done").setPosition(32, 0, Anchor.BOTTOM | Anchor.CENTER).register(this);

		window.add(btnCancel);
		window.add(btnSave);

		addToScreen(window);
	}

	private UIPanel createSettingPannel(String category)
	{
		List<Setting> categorySettings = settings.getSettings(category);
		UIPanel panel = new UIPanel(windowWidth - 10, windowHeight - 35);

		int y = 0;
		for (Setting setting : categorySettings)
		{
			UIComponent component = setting.getComponent();
			component.setPosition(0, y);
			panel.add(component);

			y += component.getHeight() + 2;
		}

		return panel;
	}

	@Subscribe
	public void onButtonClick(UIButton.ClickedEvent event)
	{
		if (event.getComponent() == btnCancel)
			close();
		else
		{
			Set<String> categories = settings.getCategories();
			for (String category : categories)
			{
				List<Setting> categorySettings = settings.getSettings(category);
				for (Setting setting : categorySettings)
				{
					setting.set(setting.getValueFromComponent());
				}
			}

			settings.save();
			close();
		}
	}
}
