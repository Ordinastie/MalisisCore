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

package net.malisis.core.block.component;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterComponent;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.item.MalisisItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This {@link ColorComponent} automatically handles blocks with color variants.<br>
 * The component determines if the color is dynamically calculated and render. If not, it's up to the user to provide the different
 * textures.<br>
 * Expected localization is in the form tile.unlocalizedBlockName.color.name.
 *
 * @author Ordinastie
 */
public class ColorComponent extends SubtypeComponent<EnumDyeColor> implements IRegisterComponent
{
	/** Whether the color is handled by the renderer. */
	private boolean useColorMultiplier = true;

	/**
	 * Instantiates a new {@link ColorComponent}.
	 *
	 * @param useColorMultiplier true if the renderer should handle the color, false if the color is already in the texture
	 */
	public ColorComponent(boolean useColorMultiplier)
	{
		super(EnumDyeColor.class, BlockColored.COLOR);
		this.useColorMultiplier = useColorMultiplier;
	}

	/**
	 * Gets whether the color is handled by the renderer or by the texture.
	 *
	 * @return true, if the renderer should handle the color, false if the color is already in the texture
	 */
	public boolean useColorMultiplier()
	{
		return useColorMultiplier;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), EnumDyeColor.WHITE);
	}

	@Override
	public void register(IComponentProvider block)
	{
		if (MalisisCore.isClient() && block instanceof Block)
			registerColorHandler((Block) block);
	}

	@SideOnly(Side.CLIENT)
	private void registerColorHandler(Block block)
	{
		Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler(this::getRenderColor, block);
	}

	@Override
	public String getUnlocalizedName(Block block, IBlockState state)
	{
		return block.getUnlocalizedName() + "." + getColor(state).getUnlocalizedName();
	}

	@Override
	public Item getItem(Block block)
	{
		if (block instanceof MalisisBlock)
			return new MalisisItemBlock((MalisisBlock) block);

		return new ItemBlock(block);
	}

	@Override
	public int damageDropped(Block block, IBlockState state)
	{
		return state.getValue(getProperty()).getMetadata();
	}

	@Override
	public boolean getHasSubtypes(Block block, Item item)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Block block, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		for (EnumDyeColor color : EnumDyeColor.values())
			list.add(new ItemStack(block, 1, color.getMetadata()));
	}

	/**
	 * Gets the render color for this {@link Block}.<br>
	 * If {@link #useColorMultiplier()} is false, color is already in the texture, and white (0xFFFFFF) is returned.
	 *
	 * @param state the state
	 * @param world the world
	 * @param pos the pos
	 * @param tintIndex the tint index
	 * @return the render color
	 */
	public int getRenderColor(IBlockState state, IBlockAccess world, BlockPos pos, int tintIndex)
	{
		if (!useColorMultiplier())
			return 0xFFFFFF;
		return ItemDye.DYE_COLORS[getColor(state).getDyeDamage()];
	}

	/**
	 * Get the {@link MapColor} for this {@link Block} and the given {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the map color
	 */
	@Override
	public MapColor getMapColor(Block block, IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return MapColor.func_193558_a(state.getValue(getProperty()));
	}

	/**
	 * Get the {@link IBlockState} from the metadata
	 *
	 * @param block the block
	 * @param state the state
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), EnumDyeColor.byMetadata(meta));
	}

	/**
	 * Get the metadata from the {@link IBlockState}
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return state.getValue(getProperty()).getMetadata();
	}

	/**
	 * Gets the {@link EnumDyeColor color} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the EnumDyeColor, null if the block is not {@link ColorComponent}
	 */
	public static EnumDyeColor getColor(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? getColor(world.getBlockState(pos)) : EnumDyeColor.WHITE;
	}

	/**
	 * Gets the {@link EnumDyeColor color} for the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return the EnumDyeColor, null if the block is not {@link ColorComponent}
	 */
	public static EnumDyeColor getColor(IBlockState state)
	{
		ColorComponent cc = IComponent.getComponent(ColorComponent.class, state.getBlock());
		if (cc == null)
			return EnumDyeColor.WHITE;

		PropertyEnum<EnumDyeColor> property = cc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return EnumDyeColor.WHITE;

		return state.getValue(property);
	}
}
