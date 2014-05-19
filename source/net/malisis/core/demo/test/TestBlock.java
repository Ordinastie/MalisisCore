package net.malisis.core.demo.test;

import net.malisis.core.client.gui.component.container.UIInfoTab;
import net.malisis.core.client.gui.component.container.UIScrollPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.layout.FlowLayoutManager;
import net.malisis.core.client.gui.renderer.DrawableIcon;
import net.malisis.core.client.gui.util.Orientation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class TestBlock extends Block
{
	
	
	public TestBlock()
	{
		super(Material.ground);
		setCreativeTab(CreativeTabs.tabBlock);
	}
	
	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_)
	{
	}
	
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return Blocks.quartz_block.getIcon(side, metadata);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		UIWindow window = new UIWindow(300, 200);
//        UIInfoTab tab = new UIInfoTab("Test", new DrawableIcon(Items.apple.getIconFromDamage(0), DrawableIcon.ITEM_SHEET), Orientation.HORIZONTAL_LEFT, 0x0060ff);
//        window.addInfoTab(tab);
//        tab.setLayout(new FlowLayoutManager(1, 1));
//        tab.add(new UILabel("Test", 0xe0e0e0));
//        tab.add(new UIButton("Button", 50, 20));
//        window.setLayout(new FlowLayoutManager(2, 2));
//        UIScrollPanel panel = new UIScrollPanel(90, 90, 5);
//        panel.getBar().setBarHeight(30);
//        panel.setLayout(new FlowLayoutManager(2, 2));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        panel.add(new UILabel("testtesttesttest"));
//        UIButton button = new UIButton("Button", 50, 20);
//        button.setTooltip("test");
//        panel.add(button);
		window.add(new UIButton("Wow Button!", 60, 20));
		
		TestGui gui = new TestGui(window);
		gui.display();
		
		return true;
	}
	
	@Override
	public boolean isNormalCube()
	{
		return false;
	}
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
	
	@Override
	public int getRenderType()
	{
		return TestRenderer.renderId;
	}

}
