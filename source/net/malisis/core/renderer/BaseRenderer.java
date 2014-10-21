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

package net.malisis.core.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

public class BaseRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer
{
	/**
	 * Set rendering for world
	 */
	public static final int TYPE_ISBRH_WORLD = 1;
	/**
	 * Set rendering for inventory with ISBRH
	 */
	public static final int TYPE_ISBRH_INVENTORY = 2;
	/**
	 * Set rendering for inventory with IItemRenderer
	 */
	public static final int TYPE_ITEM_INVENTORY = 3;
	/**
	 * Set rendering for TESR
	 */
	public static final int TYPE_TESR_WORLD = 4;

	//Reference to Minecraft.renderGlobal.damagedBlocks (lazy loaded)
	private static Map damagedBlocks;
	protected static IIcon[] damagedIcons;
	/**
	 * Id for the renderer
	 */
	protected int renderId = -1;
	/**
	 * Tessellator
	 */
	protected Tessellator t = Tessellator.instance;
	/**
	 * Current world reference (ISBRH/TESR)
	 */
	protected IBlockAccess world;
	/**
	 * RenderBlocks reference (ISBRH)
	 */
	protected RenderBlocks renderBlocks;
	/**
	 * Block to render (ISBRH/TESR)
	 */
	protected Block block;
	/**
	 * Metadata of the block to render (ISBRH/TESR)
	 */
	protected int blockMetadata;
	/**
	 * Position of the block (ISBRH/TESR)
	 */
	protected int x, y, z;
	/**
	 * ItemStack to render (ITEM)
	 */
	protected ItemStack itemStack;
	/**
	 * Type of item rendering (ITEM)
	 *
	 * @see {@link ItemRenderType}
	 */
	protected ItemRenderType itemRenderType;
	/**
	 * Type of rendering : <code>TYPE_ISBRH_WORLD</code>, <code>TYPE_ISBRH_INVENTORY</code>, <code>TYPE_ITEM_INVENTORY</code> or
	 * <code>TYPE_TESR_WORLD</code>
	 */
	protected int renderType;
	/**
	 * Mode of rendering (GL constants)
	 */
	protected int drawMode;
	/**
	 * Are render coordinates already shifted (ISBRH)
	 */
	protected boolean isShifted = false;
	/**
	 * Current shape being rendered
	 */
	protected Shape shape;
	/**
	 * Current face being rendered
	 */
	protected Face face;
	/**
	 * Current parameters for the shape being rendered
	 */
	protected RenderParameters rp;
	/**
	 * Current parameters for the face being rendered
	 */
	protected RenderParameters params;
	/**
	 * Base brightness of the block
	 */
	protected int baseBrightness;
	/**
	 * An override texture set by the renderer
	 */
	protected IIcon overrideTexture;
	/**
	 * TileEntity currently drawing (for TESR)
	 */
	protected TileEntity tileEntity;
	/**
	 * Partial tick time (for TESR)
	 */
	protected float partialTick = 0;
	/**
	 * Get the damage for the block (for TESR)
	 */
	protected boolean getBlockDamage = false;
	/**
	 * Current block destroy progression (for TESR)
	 */
	protected DestroyBlockProgress destroyBlockProgress = null;
	/**
	 * Is at least one vertex been drawn
	 */
	protected boolean vertexDrawn = false;

	public BaseRenderer()
	{
		this.renderId = RenderingRegistry.getNextAvailableRenderId();
		this.t = Tessellator.instance;
		initShapes();
		initParameters();
	}

	/**
	 * Gets the partialTick for this frame. Used for TESR and ITEMS
	 *
	 * @return
	 */
	public float getPartialTick()
	{
		return partialTick;
	}

	// #region set()
	/**
	 * Resets data so this <code>BaseRenderer</code> can be reused.
	 */
	public void reset()
	{
		this.renderType = 0;
		this.drawMode = 0;
		this.world = null;
		this.block = null;
		this.blockMetadata = 0;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.overrideTexture = null;
		this.destroyBlockProgress = null;
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param world
	 * @param block
	 * @param x
	 * @param y
	 * @param z
	 * @param metadata
	 */
	public void set(IBlockAccess world, Block block, int x, int y, int z, int metadata)
	{
		this.world = world;
		this.block = block;
		this.blockMetadata = metadata;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param block
	 */
	public void set(Block block)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param blockMetadata
	 */
	public void set(int blockMetadata)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param block
	 * @param blockMetadata
	 */
	public void set(Block block, int blockMetadata)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param x
	 * @param y
	 * @param z
	 */
	public void set(int x, int y, int z)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param te
	 * @param partialTick
	 */
	public void set(TileEntity te, float partialTick)
	{
		set(te.getWorldObj(), te.getBlockType(), te.xCoord, te.yCoord, te.zCoord, te.getBlockMetadata());
		this.partialTick = partialTick;
		this.tileEntity = te;
	}

	/**
	 * Sets informations for this <code>BaseRenderer</code>.
	 *
	 * @param type
	 * @param itemStack
	 */
	public void set(ItemRenderType type, ItemStack itemStack)
	{
		if (itemStack.getItem() instanceof ItemBlock)
			set(Block.getBlockFromItem(itemStack.getItem()));
		this.itemStack = itemStack;
		this.itemRenderType = type;
	}

	// #end

	// #region ISBRH
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		set(block, metadata);
		renderBlocks = renderer;
		prepare(TYPE_ISBRH_INVENTORY);
		render();
		clean();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		set(world, block, x, y, z, world.getBlockMetadata(x, y, z));
		renderBlocks = renderer;
		vertexDrawn = false;

		prepare(TYPE_ISBRH_WORLD);
		if (renderer.hasOverrideBlockTexture())
			overrideTexture = renderer.overrideBlockTexture;
		render();
		clean();
		return vertexDrawn;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

	// #end ISBRH

	// #region IItemRenderer
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		set(type, item);
		prepare(TYPE_ITEM_INVENTORY);
		render();
		clean();
	}

	// #end IItemRenderer

	// #region TESR
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick)
	{
		set(te, partialTick);
		prepare(TYPE_TESR_WORLD, x, y, z);
		render();
		if (getBlockDamage)
		{
			destroyBlockProgress = getBlockDestroyProgress();
			if (destroyBlockProgress != null)
			{
				next();

				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
				GL11.glAlphaFunc(GL11.GL_GREATER, 0);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);

				t.disableColor();
				renderDestroyProgress();
				next();
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
		clean();
	}

	// #end TESR

	// #region prepare()
	/**
	 * Prepares the tessellator and the GL states for the <b>renderType</b>. <b>data</b> is only used for TESR.<br />
	 * TESR rendering is surrounded by glPushAttrib(GL_LIGHTING_BIT) and block texture sheet is bound.
	 *
	 * @param renderType
	 * @param data
	 */
	public void prepare(int renderType, double... data)
	{
		this.renderType = renderType;
		if (renderType == TYPE_ISBRH_WORLD)
		{
			tessellatorShift();
		}
		else if (renderType == TYPE_ISBRH_INVENTORY)
		{
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			startDrawing();
		}
		else if (renderType == TYPE_ITEM_INVENTORY)
		{
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			startDrawing();
		}
		else if (renderType == TYPE_TESR_WORLD)
		{
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			RenderHelper.disableStandardItemLighting();
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glShadeModel(GL11.GL_SMOOTH);

			GL11.glPushMatrix();
			GL11.glTranslated(data[0], data[1], data[2]);

			bindTexture(TextureMap.locationBlocksTexture);

			startDrawing();
		}
	}

	/**
	 * Tells the tessellator to start drawing GL_QUADS.
	 */
	public void startDrawing()
	{
		startDrawing(GL11.GL_QUADS);
	}

	/**
	 * Tells the tessellator to start drawing <b>drawMode</b>.
	 *
	 * @param drawMode
	 */
	public void startDrawing(int drawMode)
	{
		t.startDrawing(drawMode);
		this.drawMode = drawMode;
	}

	/**
	 * Triggers a draw and restart drawing with current <i>drawMode</i>.
	 */
	public void next()
	{
		next(drawMode);
	}

	/**
	 * Triggers a draw and restart drawing with <b>drawMode</b>.
	 *
	 * @param drawMode
	 */
	public void next(int drawMode)
	{
		draw();
		startDrawing(drawMode);
	}

	/**
	 * Triggers a draw.
	 */
	public void draw()
	{
		t.draw();
	}

	/**
	 * Cleans the current renderer state.
	 */
	public void clean()
	{
		if (renderType == TYPE_ISBRH_WORLD)
		{
			tessellatorUnshift();
		}
		else if (renderType == TYPE_ISBRH_INVENTORY)
		{
			draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		else if (renderType == TYPE_ITEM_INVENTORY)
		{
			draw();
			GL11.glPopAttrib();
		}
		else if (renderType == TYPE_TESR_WORLD)
		{
			draw();
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
		reset();
	}

	/**
	 * Shifts the tessellator for ISBRH rendering.
	 */
	public void tessellatorShift()
	{
		if (isShifted)
			return;

		isShifted = true;
		t.addTranslation(x, y, z);
	}

	/**
	 * Unshifts the tessellator for ISBRH rendering.
	 */
	public void tessellatorUnshift()
	{
		if (!isShifted)
			return;

		isShifted = false;
		t.addTranslation(-x, -y, -z);
	}

	/**
	 * Enables the blending for the rendering. Ineffective for ISBRH.
	 */
	public void enableBlending()
	{
		if (renderType == TYPE_ISBRH_WORLD)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

	}

	// #end prepare()

	/**
	 * Called when the renderer is instantiated
	 */
	protected void initShapes()
	{
		shape = ShapePreset.Cube();
	}

	/**
	 * Called when the renderer is instantiated
	 */
	protected void initParameters()
	{
		rp = new RenderParameters();
	}

	/**
	 * Renders the block using the default Minecraft rendering system
	 *
	 * @param renderer
	 */
	public void renderStandard()
	{
		renderStandard(renderBlocks);
	}

	/**
	 * Renders the blocks using the default Minecraft rendering system with the specified <b>renderer</b>
	 *
	 * @param renderer
	 */
	public void renderStandard(RenderBlocks renderer)
	{
		if (renderer == null)
			return;

		boolean b = isShifted;
		if (b)
			tessellatorUnshift();
		renderer.setRenderBoundsFromBlock(block);
		renderer.renderStandardBlock(block, x, y, z);
		if (b)
			tessellatorShift();
	}

	/**
	 * Main rendering method. Draws simple cube by default.
	 */
	public void render()
	{
		drawShape(shape, rp);
	}

	/**
	 * Renders the destroy progress manually for TESR. Called if BaseRenderer.destroyBlockProgress is not null.
	 */
	public void renderDestroyProgress()
	{}

	/**
	 * Draws a shape without parameters (default will be used).
	 *
	 * @param shape
	 */
	public void drawShape(Shape shape)
	{
		drawShape(shape, null);
	}

	/**
	 * Draws a shape with specified parameters.
	 *
	 * @param shape
	 * @param params
	 */
	public void drawShape(Shape s, RenderParameters params)
	{
		if (s == null)
			return;

		shape = s;
		rp = params != null ? params : new RenderParameters();

		//apply transformations
		s.applyMatrix();

		// vertex position
		if (rp.vertexPositionRelativeToRenderBounds.get())
			calcVertexesPosition(getRenderBounds());

		if (rp.applyTexture.get())
			applyTexture(s, rp);

		for (Face face : s.getFaces())
			drawFace(face, face.getParameters());
	}

	/**
	 * Draws a face with its own parameters.
	 *
	 * @param face
	 */
	public void drawFace(Face face)
	{
		drawFace(face, face.getParameters());
	}

	/**
	 * Draws a face with specified parameters.
	 *
	 * @param f
	 * @param faceParams
	 */
	protected void drawFace(Face f, RenderParameters faceParams)
	{
		if (f == null)
			return;

		int vertexCount = f.getVertexes().length;
		if (vertexCount != 4 && renderType == TYPE_ISBRH_WORLD)
		{
			MalisisCore.log.error("[BaseRenderer] Attempting to render a face containing {} vertexes in ISBRH. Ignored", vertexCount);
			return;
		}

		face = f;
		params = RenderParameters.merge(rp, faceParams);

		if (!shouldRenderFace(face))
			return;

		//use normals if available
		if ((renderType == TYPE_ITEM_INVENTORY || renderType == TYPE_ISBRH_INVENTORY || params.useNormals.get())
				&& params.direction.get() != null)
			t.setNormal(params.direction.get().offsetX, params.direction.get().offsetY, params.direction.get().offsetZ);

		baseBrightness = getBaseBrightness();

		drawVertexes(face.getVertexes());

		//we need to separate each face
		if (drawMode == GL11.GL_POLYGON)
			next();
	}

	/***
	 * Draws an array of vertexes (usually <i>Face.getVertexes()</i>).
	 *
	 * @param vertexes
	 */
	protected void drawVertexes(Vertex[] vertexes)
	{
		for (int i = 0; i < vertexes.length; i++)
		{
			drawVertex(vertexes[i], i);
			if (drawMode == GL11.GL_LINE_STRIP)
				drawVertex(vertexes[i == vertexes.length - 1 ? 0 : i + 1], i);
		}
	}

	/**
	 * Draws a single vertex.
	 *
	 * @param vertex
	 * @param number
	 */
	protected void drawVertex(Vertex vertex, int number)
	{
		// brightness
		int brightness = calcVertexBrightness(vertex, (int[][]) params.aoMatrix.get(number));
		vertex.setBrightness(brightness);

		// color
		int color = calcVertexColor(vertex, (int[][]) params.aoMatrix.get(number));
		vertex.setColor(color);

		// alpha
		if (!params.usePerVertexAlpha.get())
			vertex.setAlpha(params.alpha.get());

		t.setColorRGBA_I(vertex.getColor(), vertex.getAlpha());
		t.setBrightness(vertex.getBrightness());

		if (drawMode != GL11.GL_LINE && params.useTexture.get())
			t.addVertexWithUV(vertex.getX(), vertex.getY(), vertex.getZ(), vertex.getU(), vertex.getV());
		else
			t.addVertex(vertex.getX(), vertex.getY(), vertex.getZ());

		vertexDrawn = true;
	}

	/**
	 * Gets the IIcon corresponding to <b>params</b>.
	 *
	 * @param params
	 * @return
	 */
	protected IIcon getIcon(RenderParameters params)
	{
		IIcon icon = params.icon.get();
		if (params.useCustomTexture.get())
			icon = new MalisisIcon(); //use a generic icon where UVs go from 0 to 1
		else if (overrideTexture != null)
			icon = overrideTexture;
		else if (block != null && icon == null)
		{
			int side = 0;
			if (params.textureSide.get() != null)
				side = params.textureSide.get().ordinal();
			if (world != null && params.useWorldSensitiveIcon.get())
				icon = block.getIcon(world, x, y, z, side);
			else
				icon = block.getIcon(side, blockMetadata);
		}

		return icon;
	}

	/**
	 * Checks if <b>face</b> should be rendered.
	 *
	 * @param side
	 */
	protected boolean shouldRenderFace(Face face)
	{
		if (renderType != TYPE_ISBRH_WORLD || world == null || block == null)
			return true;
		if (rp != null && rp.renderAllFaces.get())
			return true;
		if (renderBlocks != null && renderBlocks.renderAllFaces == true)
			return true;
		RenderParameters p = face.getParameters();
		if (p.direction.get() == null)
			return true;

		boolean b = block.shouldSideBeRendered(world, x + p.direction.get().offsetX, y + p.direction.get().offsetY, z
				+ p.direction.get().offsetZ, p.direction.get().ordinal());
		return b;
	}

	/**
	 * Applies the texture to the <b>shape</b>. Usually necessary before some shape transformations in conjunction with
	 * RenderParameters.applyTexture set to false to prevent reapplying texture when rendering.
	 *
	 * @param shape
	 */
	public void applyTexture(Shape shape)
	{
		applyTexture(shape, null);
	}

	/**
	 * Applies the texture to the <b>shape</b> with specified <b>parameters</b>. Usually necessary before some shape transformations in
	 * conjunction with RenderParameters.applyTexture set to false to prevent reapplying texture when rendering.
	 *
	 * @param shape
	 * @param parameters
	 */
	public void applyTexture(Shape shape, RenderParameters parameters)
	{
		//shape.applyMatrix();
		for (Face f : shape.getFaces())
		{
			RenderParameters params = RenderParameters.merge(f.getParameters(), parameters);
			IIcon icon = getIcon(params);
			if (icon != null)
				f.setTexture(icon, params.flipU.get(), params.flipV.get(), params.interpolateUV.get());
		}
	}

	/**
	 * Calculates the ambient occlusion for a vertex and also apply the side dependent shade.<br />
	 * <b>aoMatrix</b> is the list of block coordinates necessary to compute AO. If it's empty, only the global face shade is applied.<br />
	 * Also, <i>params.colorMultiplier</i> is applied as well.
	 *
	 * @param vertex
	 * @param aoMatrix
	 */
	protected int calcVertexColor(Vertex vertex, int[][] aoMatrix)
	{
		int color = 0xFFFFFF;

		if (params.usePerVertexColor.get()) //vertex should use their own colors
			color = vertex.getColor();
		if (params.colorMultiplier.get() != null) //global color multiplier is set
			color = params.colorMultiplier.get();
		else if (block != null) //use block color mulitplier
			color = world != null ? block.colorMultiplier(world, x, y, z) : block.getRenderColor(blockMetadata);

		if (drawMode == GL11.GL_LINE) //no AO for lines
			return color;
		if (renderType != TYPE_ISBRH_WORLD && renderType != TYPE_TESR_WORLD) //no AO for item/inventories
			return color;

		float factor = 1;
		//calculate AO
		if (params.calculateAOColor.get() && aoMatrix != null && Minecraft.isAmbientOcclusionEnabled()
				&& block.getLightValue(world, x, y, z) == 0)
		{
			factor = getBlockAmbientOcclusion(world, x + params.direction.get().offsetX, y + params.direction.get().offsetY, z
					+ params.direction.get().offsetZ);

			for (int i = 0; i < aoMatrix.length; i++)
				factor += getBlockAmbientOcclusion(world, x + aoMatrix[i][0], y + aoMatrix[i][1], z + aoMatrix[i][2]);

			factor /= (aoMatrix.length + 1);
		}

		//apply face dependent shading
		factor *= params.colorFactor.get();

		int r = (int) ((color >> 16 & 255) * factor);
		int g = (int) ((color >> 8 & 255) * factor);
		int b = (int) ((color & 255) * factor);

		color = r << 16 | g << 8 | b;

		return color;
	}

	/**
	 * Gets the base brightness for the current face.<br />
	 * If <i>params.useBlockBrightness</i> = false, <i>params.brightness</i>. Else, the brightness is determined base on
	 * <i>params.offset</i> and <i>getBlockBounds()</i>
	 *
	 * @return
	 */
	protected int getBaseBrightness()
	{
		//not in world
		if ((renderType != TYPE_ISBRH_WORLD && renderType != TYPE_TESR_WORLD) || world == null || !params.useBlockBrightness.get())
			return params.brightness.get();

		//no direction, we can only use current block brightness
		if (params.direction.get() == null)
			return block.getMixedBrightnessForBlock(world, x, y, z);

		AxisAlignedBB bounds = getRenderBounds();
		ForgeDirection dir = params.direction.get();
		int ox = x + dir.offsetX;
		int oy = y + dir.offsetY;
		int oz = z + dir.offsetZ;

		//use the brightness of the block next to it
		if (bounds != null)
		{
			if (dir == ForgeDirection.WEST && bounds.minX > 0)
				ox += 1;
			else if (dir == ForgeDirection.EAST && bounds.maxX < 1)
				ox -= 1;
			else if (dir == ForgeDirection.NORTH && bounds.minZ > 0)
				oz += 1;
			else if (dir == ForgeDirection.SOUTH && bounds.maxZ < 1)
				oz -= 1;
			else if (dir == ForgeDirection.DOWN && bounds.minY > 0)
				oy += 1;
			else if (dir == ForgeDirection.UP && bounds.maxY < 1)
				oy -= 1;
		}

		return getMixedBrightnessForBlock(world, ox, oy, oz);
	}

	/**
	 * Calculates the ambient occlusion brightness for a vertex. <b>aoMatrix</b> is the list of block coordinates necessary to compute AO.
	 * Only first 3 blocks are used.<br />
	 *
	 * @param vertex
	 * @param baseBrightness
	 * @param aoMatrix
	 */
	protected int calcVertexBrightness(Vertex vertex, int[][] aoMatrix)
	{
		if (drawMode == GL11.GL_LINE) //no AO for lines
			return baseBrightness;
		if (renderType != TYPE_ISBRH_WORLD && renderType != TYPE_TESR_WORLD) //not in world
			return baseBrightness;
		if (!params.calculateBrightness.get() || aoMatrix == null) //no data
			return baseBrightness;
		if (!Minecraft.isAmbientOcclusionEnabled() || block.getLightValue(world, x, y, z) != 0) // emit light
			return baseBrightness;

		int[] b = new int[Math.max(3, aoMatrix.length)];

		for (int i = 0; i < b.length; i++)
			b[i] += getMixedBrightnessForBlock(world, x + aoMatrix[i][0], y + aoMatrix[i][1], z + aoMatrix[i][2]);

		int brightness = getAoBrightness(b[0], b[1], b[2], baseBrightness);

		return brightness;
	}

	/**
	 * Does the actual brightness calculation (copied from net.minecraft.client.renderer.BlocksRenderer.java)
	 *
	 * @param b1
	 * @param b2
	 * @param b3
	 * @param base
	 * @return
	 */
	protected int getAoBrightness(int b1, int b2, int b3, int base)
	{
		if (b1 == 0)
			b1 = base;
		if (b2 == 0)
			b2 = base;
		if (b3 == 0)
			b3 = base;

		return b1 + b2 + b3 + base >> 2 & 16711935;
	}

	/**
	 * Gets the block ambient occlusion value. Contrary to base Minecraft code, it's the actual block at the <b>x</b>, <b>y</b> and <b>z</b>
	 * coordinates which is used to get the value, and not value of the block drawn. This allows to have different logic behaviors for AO
	 * values for a block.
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected float getBlockAmbientOcclusion(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (block == null)
			return 1.0F;

		return block.getAmbientOcclusionLightValue();
	}

	/**
	 * Gets the mix brightness for a block (sky + block source)
	 *
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	protected int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z)
	{
		// return world.getLightBrightnessForSkyBlocks(x, y, z, 0);
		return world.getBlock(x, y, z).getMixedBrightnessForBlock(world, x, y, z);
	}

	/**
	 * Gets the rendering bounds. If <i>params.useBlockBounds</i> = false, <i>params.renderBounds</i> is used instead of the actual block
	 * bounds.
	 *
	 * @return
	 */
	protected AxisAlignedBB getRenderBounds()
	{
		if (block == null || !rp.useBlockBounds.get())
			return rp.renderBounds.get();

		if (world != null)
			block.setBlockBoundsBasedOnState(world, x, y, z);

		return AxisAlignedBB.getBoundingBox(block.getBlockBoundsMinX(), block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(),
				block.getBlockBoundsMaxX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ());
	}

	/**
	 * Modifies the vertexes coordinates relative to the bounds specified.<br />
	 * Eg : if x = 0.5, minX = 1, maxX = 3, x becomes 2
	 *
	 * @param bounds
	 */
	protected void calcVertexesPosition(AxisAlignedBB bounds)
	{
		for (Face f : shape.getFaces())
			for (Vertex v : f.getVertexes())
				v.interpolateCoord(bounds);
	}

	/**
	 * Gets and hold reference to damagedBlocks from Minecraft.renderGlobal via reflection.
	 *
	 * @return
	 */
	protected Map getDamagedBlocks()
	{
		if (damagedBlocks != null)
			return damagedBlocks;

		try
		{
			Field modifiers = Field.class.getDeclaredField("modifiers");
			modifiers.setAccessible(true);

			Field f = ReflectionHelper.findField(RenderGlobal.class, MalisisCore.isObfEnv ? "field_94141_F" : "destroyBlockIcons");
			modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			damagedIcons = (IIcon[]) f.get(Minecraft.getMinecraft().renderGlobal);

			f = ReflectionHelper.findField(RenderGlobal.class, MalisisCore.isObfEnv ? "field_72738_E" : "damagedBlocks");

			modifiers.setInt(f, f.getModifiers());
			damagedBlocks = (HashMap) f.get(Minecraft.getMinecraft().renderGlobal);

			return damagedBlocks;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the destroy block progress for this rendering. Only used for TESR.
	 *
	 * @return
	 */
	protected DestroyBlockProgress getBlockDestroyProgress()
	{
		if (renderType != TYPE_TESR_WORLD)
			return null;
		Map damagedBlocks = getDamagedBlocks();
		if (damagedBlocks == null || damagedBlocks.isEmpty())
			return null;

		Iterator iterator = damagedBlocks.values().iterator();
		while (iterator.hasNext())
		{
			DestroyBlockProgress dbp = (DestroyBlockProgress) iterator.next();
			if (isCurrentBlockDestroyProgress(dbp))
				return dbp;
		}
		return null;

	}

	/**
	 * Checks whether the DestroyBlockProgress specified should apply for this TESR.
	 *
	 * @param dbp
	 * @return
	 */
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z;
	}

	@Override
	public int getRenderId()
	{
		return renderId;
	}

	/**
	 * Register this {@link BaseRenderer} to be used for rendering for specified classes.<br>
	 * Classes have to extend Block or TileEntity.<br>
	 * <font color="990000">A static <b>renderId</b> field is required inside the class extending Block !</font>
	 *
	 * @param listClass
	 */
	public void registerFor(Class... listClass)
	{
		for (Class clazz : listClass)
		{
			if (Block.class.isAssignableFrom(clazz))
			{
				try
				{
					clazz.getField("renderId").set(null, renderId);
					RenderingRegistry.registerBlockHandler(this);
				}
				catch (ReflectiveOperationException e)
				{
					MalisisCore.log.error("[BaseRenderer] Tried to register ISBRH for block class {} that does not have renderId field",
							clazz.getSimpleName());
					e.printStackTrace();
				}
			}
			else if (TileEntity.class.isAssignableFrom(clazz))
			{
				ClientRegistry.bindTileEntitySpecialRenderer(clazz, this);
			}
		}
	}

	/**
	 * Register this {@link BaseRenderer} to be used for rendering the specified <b>item</b>.
	 *
	 * @param item
	 */
	public void registerFor(Item item)
	{
		MinecraftForgeClient.registerItemRenderer(item, this);
	}
}
