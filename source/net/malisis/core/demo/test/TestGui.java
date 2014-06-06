package net.malisis.core.demo.test;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPanel;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.MouseEvent;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import com.google.common.eventbus.Subscribe;

public class TestGui extends MalisisGui
{
	UIPanel cbPanel, bulletsPanel, inner;
	UIButton btn1, btn2;
	UILabel label;
	UICheckBox cb;

	ResourceLocation bulletRl = new ResourceLocation("malisiscore", "textures/gui/bullet.png");

	public TestGui(MalisisInventoryContainer inventoryContainer, EntityPlayer player)
	{
		super();
		setInventoryContainer(inventoryContainer);

		UIWindow window = new UIWindow(300, 220);
		window.setPosition(0, -40, Anchor.CENTER | Anchor.MIDDLE);
		window.register(this);

		cbPanel = new UIPanel(290, 120);
		cbPanel.setVerticalScroll(true).register(this);
		cbPanel.setName("cbPanel");

		btn1 = (UIButton) new UIButton("Horizontal", 80).setPosition(-42, 0, Anchor.BOTTOM | Anchor.CENTER).register(this);
		btn2 = (UIButton) new UIButton("Vertical", 80).setPosition(42, 0, Anchor.BOTTOM | Anchor.CENTER).register(this);
		label = (UILabel) new UILabel().setPosition(0, -25, Anchor.BOTTOM | Anchor.CENTER);

		String tt = EnumChatFormatting.AQUA + "Diamond Sword" + "\n";
		tt += EnumChatFormatting.WHITE + "Sharpness X" + "\n";
		tt += "\n";
		tt += EnumChatFormatting.BLUE + "+7 Attack Damage";

		tt = "Some cb ptas O!!";

		cb = (UICheckBox) new UICheckBox("CheckBox with label").setTooltip(tt).register(this);

		UITextField tf = new UITextField(180, "Some blob text | s.");
		tf.setPosition(0, 50);

		UIContainer inv = setInventoryContainer(inventoryContainer.getContainerInventory());

		cbPanel.add(cb);
		cbPanel.add(new UIImage(Items.diamond_axe.getIconFromDamage(0), UIImage.ITEMS_TEXTURE).setPosition(0, 10));
		cbPanel.add(new UILabel("This is LABEL!").setPosition(20, 15));
		// cbPanel.add(new UIButton("Random", 200).setPosition(0, 25).register(this));
		cbPanel.add(tf);
		cbPanel.add(inv);
		cbPanel.add(btn1);
		cbPanel.add(btn2);
		cbPanel.add(label);

		bulletsPanel = (UIPanel) new UIPanel(100, 250).setPosition(0, 0, Anchor.RIGHT);
		bulletsPanel.register(this);
		bulletsPanel.setName("bulletsPanel");

		// cbPanel.add(bulletsPanel);
		UIPlayerInventory playerInv = new UIPlayerInventory(inventoryContainer.getPlayerInventory());
		playerInv.setPosition(0, 0, Anchor.BOTTOM | Anchor.CENTER);

		window.add(cbPanel);
		window.add(playerInv);

		addToScreen(window);
	}

	private UIContainer setInventoryContainer(MalisisInventory inventory)
	{
		UIContainer c = new UIContainer(100, 30);
		c.setPosition(0, 65);

		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			UISlot slot = (UISlot) new UISlot(inventory.getSlot(i)).setPosition(i * 18, 0);
			c.add(slot);
		}

		return c;
	}

	@Subscribe
	public void mouseClick(MouseEvent.Press event)
	{
		// // MalisisCore.Message(event.getComponent().getClass().getSimpleName());
		// if (event.getComponent() == bulletsPanel)
		// {
		// UIImage image = new UIImage(GuiIcon.iconFixedSized, bulletRl);
		// int x = event.getX() - event.getComponent().screenX() - image.getWidth() / 2 - bulletsPanel.getHorizontalPadding();
		// int y = event.getY() - event.getComponent().screenY() - image.getWidth() / 2 - bulletsPanel.getVerticalPadding();
		// image.setPosition(x, y);
		// bulletsPanel.add(image);
		// label.setText(x + ", " + y);
		// }
		// if (event.getComponent() == cbPanel)
		// {
		// label.setText(event.getX() + ", " + event.getY());
		// }
		// if (event.getComponent() instanceof UIButton)
		// {
		// if (event.getComponent() == btn1)
		// cbPanel.setHorizontalScroll(!cbPanel.getHorizontalScroll());
		// else if (event.getComponent() == btn2)
		// cbPanel.setVerticalScroll(!cbPanel.getVerticalScroll());
		// }
	}

}