/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 PaleoCrafter, Ordinastie
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

package net.malisis.core.demo.stargate;

import net.malisis.core.client.gui.component.container.UIInfoTab;
import net.malisis.core.client.gui.component.container.UIScrollPanel;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.layout.FlowLayoutManager;
import net.malisis.core.client.gui.renderer.DrawableIcon;
import net.malisis.core.client.gui.util.Orientation;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

public class StargateBlock extends BlockContainer
{
    public static int deployTimer = 100;

    protected StargateBlock()
    {
        super(Material.iron);
        setCreativeTab(CreativeTabs.tabBlock);
        setBlockTextureName("malisiscore:sgplatformside");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
    {
        world.scheduleBlockUpdate(x, y, z, this, deployTimer);
        ((StargateTileEntity) world.getTileEntity(x, y, z)).placedTimer = world.getTotalWorldTime();
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand)
    {

        world.setBlockMetadataWithNotify(x, y, z, 1, 2);
    }


    @Override
    public IIcon getIcon(int side, int metadata)
    {
        return blockIcon;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        StargateTileEntity te = new StargateTileEntity();
        return te;
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
    public boolean onBlockActivated(World p_149727_1_, int p_149727_2_, int p_149727_3_, int p_149727_4_, EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
        if (p_149727_1_.isRemote)
        {
            UIWindow window = new UIWindow(100, 100);
            UIInfoTab tab = new UIInfoTab("Test", new DrawableIcon(Items.apple.getIconFromDamage(0), DrawableIcon.ITEM_SHEET), Orientation.HORIZONTAL_LEFT, 0x0060ff);
            window.addInfoTab(tab);
            tab.setLayout(new FlowLayoutManager(1, 1));
            tab.add(new UILabel("Test", 0xe0e0e0));
            tab.add(new UIButton("Button", 50, 20));
            window.setLayout(new FlowLayoutManager(2, 2));
            UIScrollPanel panel = new UIScrollPanel(90, 90, 5);
            panel.getBar().setBarHeight(30);
            panel.setLayout(new FlowLayoutManager(2, 2));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            panel.add(new UILabel("testtesttesttest"));
            UIButton button = new UIButton("Button", 50, 20);
            button.setTooltip("test");
            panel.add(button);
            window.add(panel);
            Minecraft.getMinecraft().displayGuiScreen(window.createScreenProxy());
        }
        return true;
    }

    @Override
    public int getRenderType()
    {
        return StargateRenderer.renderId;
    }
}
