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

import static net.malisis.core.client.gui.element.position.Positions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.google.common.eventbus.Subscribe;

import net.malisis.core.IMalisisMod;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.element.position.Position;
import net.malisis.core.client.gui.element.size.Size;
import net.malisis.core.client.gui.element.size.Sizes;
import net.malisis.core.client.gui.event.component.StateChangeEvent.HoveredStateChange;
import net.malisis.core.configuration.setting.Setting;
import net.malisis.core.renderer.font.FontOptions;

/**
 * @author Ordinastie
 *
 */
public class ConfigurationGui extends MalisisGui
{
	private IMalisisMod mod;
	private Settings settings;
	protected ArrayList<UIContainer> pannels = new ArrayList<>();
	protected HashMap<UIComponent, Setting<?>> componentSettings = new HashMap<>();

	protected UIContainer window;

	protected UILabel comment;
	protected UIButton btnCancel;
	protected UIButton btnSave;

	public ConfigurationGui(IMalisisMod mod, Settings settings)
	{
		this.mod = mod;
		this.settings = settings;
	}

	@Override
	public void construct()
	{
		Set<String> categories = settings.getCategories();

		if (categories.size() > 1)
		{
			//TODO: build tabs
		}

		window = UIContainer.window();
		window.setSize(Size.of(400, 300));
		UILabel title = new UILabel(mod.getName() + " {malisiscore.config.title}");
		window.add(title);

		for (String category : categories)
		{
			window.add(createSettingContainer(category));
		}

		//window.setSize(windowWidth, windowHeight);

		comment = new UILabel(true);
		comment.setFontOptions(FontOptions.builder().color(0xFFFFFF).shadow().build());
		UIContainer panelComment = UIContainer.panel();
		panelComment.setPosition(Position.topRight(panelComment));
		panelComment.setSize(Size.of(140, Sizes.parentHeight(panelComment, 1.0F, -35)));
		panelComment.add(comment);

		btnCancel = new UIButton("gui.cancel");
		btnCancel.setPosition(Position.of(centered(btnCancel, -32), middleAligned(btnCancel, 0)));
		btnCancel.onClick(this::close);
		btnSave = new UIButton("gui.done");
		btnSave.setPosition(Position.of(centered(btnSave, 32), middleAligned(btnSave, 0)));
		btnSave.onClick(() -> {
			settings.getCategories().forEach((cat) -> settings.getSettings(cat).forEach((setting) -> setting.applySettingFromComponent()));
			settings.save();
			close();
		});

		window.add(panelComment);
		window.add(btnCancel);
		window.add(btnSave);

		addToScreen(window);
	}

	private UIContainer createSettingContainer(String category)
	{
		List<Setting<?>> categorySettings = settings.getSettings(category);
		UIContainer container = new UIContainer();
		container.setPosition(Position.of(5, 12));
		container.setSize(Size.of(Sizes.parentWidth(container, 1.0F, -105), Sizes.parentHeight(container, 1.0F, -35)));

		UIComponent last = null;
		for (Setting<?> setting : categorySettings)
		{
			UIComponent component = setting.getComponent();
			component.setPosition(last != null ? Position.below(component, last, 2) : Position.zero(component));
			component.register(this);
			container.add(component);
			componentSettings.put(component, setting);
			last = component;
		}

		return container;
	}

	@Subscribe
	public void onMouseOver(HoveredStateChange<?> event)
	{
		if (event.getState() == true)
		{
			Setting<?> setting = componentSettings.get(event.getComponent());
			if (setting != null)
			{
				String str = StringUtils.join(setting.getComments(), "\r\n\r\n");
				comment.setText(str);
			}
		}
		else
			comment.setText("");
	}
}
