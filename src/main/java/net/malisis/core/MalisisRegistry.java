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

package net.malisis.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.malisis.core.renderer.IBlockRenderer;
import net.malisis.core.renderer.IItemRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.icon.IIconMetaProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class MalisisRegistry
{
	private static MalisisRegistry instance = new MalisisRegistry();

	private MalisisRegistry()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	//#region IBlockRenderer
	private Map<Block, IBlockRenderer> blockRenderers = new HashMap<>();

	public static void registerBlockRenderer(Block block, IBlockRenderer renderer)
	{
		if (block.getRenderType() != MalisisCore.malisisRenderType)
		{
			MalisisCore.log.error("[MalisisRenderer] Tried to register {} block with wrong render type : {}", block.getUnlocalizedName(),
					block.getRenderType());
			return;
		}

		instance.blockRenderers.put(block, renderer);
		instance.itemRenderers.put(Item.getItemFromBlock(block), renderer);
	}

	public static boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		IBlockRenderer renderer = instance.blockRenderers.get(state.getBlock());
		if (renderer == null)
			return false;
		return renderer.renderBlock(world, pos, state);
	}

	public static TextureAtlasSprite getParticleIcon(IBlockState state)
	{
		Block block = state.getBlock();
		IIconProvider iconProvider = null;
		if (block instanceof IIconMetaProvider)
			iconProvider = ((IIconMetaProvider) block).getIconProvider();

		return iconProvider != null && iconProvider.getParticleIcon() != null ? iconProvider.getParticleIcon() : MalisisIcon.missing();
	}

	//#end IBlockRenderer

	//#region IItemRenderer
	private Map<Item, IItemRenderer> itemRenderers = new HashMap<>();

	public static void registerItemRenderer(Item item, IItemRenderer renderer)
	{
		instance.itemRenderers.put(item, renderer);
	}

	public static boolean renderItem(ItemStack itemStack)
	{
		Item item = itemStack.getItem();
		IItemRenderer ir = instance.itemRenderers.get(item);
		if (ir == null)
			return false;

		ir.renderItem(itemStack, MalisisRenderer.getPartialTick());
		return true;
	}

	//#end IItemRenderer

	//#region IIconProvider
	@SideOnly(Side.CLIENT)
	private Set<IIconProvider> iconProviders = new HashSet<>();

	@SideOnly(Side.CLIENT)
	public static void registerIconProvider(IIconProvider iconProvider)
	{
		if (iconProvider != null)
			instance.iconProviders.add(iconProvider);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitchEvent(TextureStitchEvent.Pre event)
	{
		for (IIconProvider iconProvider : instance.iconProviders)
			iconProvider.registerIcons(event.map);
	}

	//#end IIconProvider
}
