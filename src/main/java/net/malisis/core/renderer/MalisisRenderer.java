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
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;

/**
 * Base class for rendering. Handle the rendering for {@link ISimpleBlockRenderingHandler}, {@link TileEntitySpecialRenderer}, and
 * {@link IItemRenderer}. Provides easy registration of the renderer, and automatically sets up the context for the rendering.
 *
 * @author Ordinastie
 *
 */
public class MalisisRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer, IRenderWorldLast
{
	//Reference to Minecraft.renderGlobal.damagedBlocks (lazy loaded)
	/** The damaged blocks. */
	private static Map damagedBlocks;
	/** The damaged icons. */
	protected static IIcon[] damagedIcons;
	/** Reference to Tessellator.isDrawing field **/
	private static Field isDrawingField;
	/** Whether this {@link MalisisRenderer} initialized. (initialize() already called) */
	private boolean initialized = false;
	/** Id of this {@link MalisisRenderer}. */
	protected int renderId = -1;
	/** Tessellator reference. */
	protected Tessellator t = Tessellator.instance;
	/** Current world reference (ISBRH/TESR/IRWL). */
	protected IBlockAccess world;
	/** RenderBlocks reference (ISBRH). */
	protected RenderBlocks renderBlocks;
	/** Whether render coordinates already shifted (ISBRH). */
	protected boolean isShifted = false;
	/** Block to render (ISBRH/TESR). */
	protected Block block;
	/** Metadata of the block to render (ISBRH/TESR). */
	protected int blockMetadata;
	/** Position of the block (ISBRH/TESR). */
	protected int x, y, z;
	/** TileEntity currently drawing (for TESR). */
	protected TileEntity tileEntity;
	/** Partial tick time (TESR/IRWL). */
	protected float partialTick = 0;
	/** ItemStack to render (ITEM). */
	protected ItemStack itemStack;
	/** ItemRenderType of item rendering (ITEM). */
	protected ItemRenderType itemRenderType;
	/** RenderGlobal reference (IRWL) */
	protected RenderGlobal renderGlobal;
	/** Type of rendering. */
	protected RenderType renderType;
	/** Mode of rendering (GL constant). */
	protected int drawMode;
	/** Current shape being rendered. */
	protected Shape shape = new Cube();
	/** Current face being rendered. */
	protected Face face;
	/** Current parameters for the shape being rendered. */
	protected RenderParameters rp = new RenderParameters();
	/** Current parameters for the face being rendered. */
	protected RenderParameters params;
	/** Base brightness of the block. */
	protected int baseBrightness;
	/** An override texture set by the renderer. */
	protected IIcon overrideTexture;

	/** Whether the damage for the blocks should be handled by this {@link MalisisRenderer} (for TESR). */
	protected boolean getBlockDamage = false;
	/** Current block destroy progression (for TESR). */
	protected DestroyBlockProgress destroyBlockProgress = null;

	/** Whether at least one vertex has been drawn. */
	protected boolean vertexDrawn = false;

	/**
	 * Instantiates a new {@link MalisisRenderer}.
	 */
	public MalisisRenderer()
	{
		this.renderId = RenderingRegistry.getNextAvailableRenderId();
		this.t = Tessellator.instance;
	}

	/**
	 * Gets the partialTick for this frame. Used for TESR and ITEMS
	 *
	 * @return the partial tick
	 */
	public float getPartialTick()
	{
		return partialTick;
	}

	// #region set()
	/**
	 * Resets data so this {@link MalisisRenderer} can be reused.
	 */
	public void reset()
	{
		this.renderType = RenderType.UNSET;
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
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param world the world
	 * @param block the block
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param metadata the metadata
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
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param world the world
	 */
	public void set(IBlockAccess world)
	{
		this.world = world;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param block the block
	 */
	public void set(Block block)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param blockMetadata the block metadata
	 */
	public void set(int blockMetadata)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param block the block
	 * @param blockMetadata the block metadata
	 */
	public void set(Block block, int blockMetadata)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void set(int x, int y, int z)
	{
		set(world, block, x, y, z, blockMetadata);
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param te the te
	 * @param partialTick the partial tick
	 */
	public void set(TileEntity te, float partialTick)
	{
		set(te.getWorld(), te.getBlockType(), te.xCoord, te.yCoord, te.zCoord, te.getBlockMetadata());
		this.partialTick = partialTick;
		this.tileEntity = te;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param type the type
	 * @param itemStack the item stack
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
	/**
	 * Renders inventory block.
	 *
	 * @param block the block
	 * @param metadata the metadata
	 * @param modelId renderId
	 * @param renderer RenderBlocks
	 */
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		set(block, metadata);
		renderBlocks = renderer;
		prepare(RenderType.ISBRH_INVENTORY);
		render();
		clean();
	}

	/**
	 * Renders world block.
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param block the block
	 * @param modelId renderId
	 * @param renderer RenderBlocks
	 * @return true, if something was drawn, false otherwise
	 */
	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		set(world, block, x, y, z, world.getBlockMetadata(x, y, z));
		tileEntity = world.getTileEntity(x, y, z);
		renderBlocks = renderer;
		vertexDrawn = false;

		prepare(RenderType.ISBRH_WORLD);
		if (renderer.hasOverrideBlockTexture())
			overrideTexture = renderer.overrideBlockTexture;
		render();
		clean();
		return vertexDrawn;
	}

	/**
	 * Checks whether this {@link MalisisRenderer} should handle the rendering in inventory
	 *
	 * @param modelId renderId
	 * @return true, if this {@link MalisisRenderer} should be used for rendering the block in inventory
	 */
	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

	// #end ISBRH

	// #region IItemRenderer
	/**
	 * Checks whether to use this {@link MalisisRenderer} for the specified {@link net.minecraftforge.client.IItemRenderer.ItemRenderType}.
	 *
	 * @param item the item
	 * @param type ItemRenderType
	 * @return true, if this {@link MalisisRenderer} should be used for rendering the block in inventory
	 */
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	/**
	 * Checks whether a render helper should be used for this {@link MalisisRenderer}.
	 *
	 * @param type the type
	 * @param item the item
	 * @param helper the helper
	 * @return true, if successful
	 */
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	/**
	 * Renders the item.
	 *
	 * @param type the type
	 * @param item the item
	 * @param data the data
	 */
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		set(type, item);
		prepare(RenderType.ITEM_INVENTORY);
		render();
		clean();
	}

	// #end IItemRenderer

	// #region TESR
	/**
	 * Renders a {@link TileEntitySpecialRenderer}.
	 *
	 * @param te the TileEntity
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param partialTick the partial tick
	 */
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick)
	{
		set(te, partialTick);
		prepare(RenderType.TESR_WORLD, x, y, z);
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

	// #region IRenderWorldLast
	@Override
	public boolean shouldSetViewportPosition()
	{
		return true;
	}

	@Override
	public boolean shouldRender(RenderWorldLastEvent event, IBlockAccess world)
	{
		return true;
	}

	@Override
	public void renderWorldLastEvent(RenderWorldLastEvent event, IBlockAccess world)
	{
		set(world);
		partialTick = event.partialTicks;
		renderGlobal = event.context;
		double x = 0, y = 0, z = 0;
		if (shouldSetViewportPosition())
		{
			EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
			x = -(p.lastTickPosX + (p.posX - p.lastTickPosX) * partialTick);
			y = -(p.lastTickPosY + (p.posY - p.lastTickPosY) * partialTick);
			z = -(p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partialTick);
		}

		prepare(RenderType.WORLD_LAST, x, y, z);

		render();

		clean();
	}

	// #end IRenderWorldLast

	// #region prepare()
	/**
	 * Prepares the {@link Tessellator} and the GL states for the <b>renderType</b>. <b>data</b> is only used for TESR and IRWL.<br>
	 * TESR and IRWL rendering are surrounded by glPushAttrib(GL_LIGHTING_BIT) and block texture sheet is bound.
	 *
	 * @param renderType the render type
	 * @param data the data
	 */
	public void prepare(RenderType renderType, double... data)
	{
		_initialize();
		vertexDrawn = false;
		this.renderType = renderType;
		if (renderType == RenderType.ISBRH_WORLD)
		{
			tessellatorShift();
		}
		else if (renderType == RenderType.ISBRH_INVENTORY)
		{
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			startDrawing();
		}
		else if (renderType == RenderType.ITEM_INVENTORY)
		{
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			startDrawing();
		}
		else if (renderType == RenderType.TESR_WORLD)
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
		else if (renderType == RenderType.WORLD_LAST)
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
	 * Tells the {@link Tessellator} to start drawing GL_QUADS.
	 */
	public void startDrawing()
	{
		startDrawing(GL11.GL_QUADS);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing <b>drawMode</b>.
	 *
	 * @param drawMode the draw mode
	 */
	public void startDrawing(int drawMode)
	{
		if (isDrawing())
			draw();
		t.startDrawing(drawMode);
		this.drawMode = drawMode;
	}

	/**
	 * Checks if the {@link Tessellator} is currently drawing.
	 *
	 * @return true, if is drawing
	 */
	public boolean isDrawing()
	{
		if (isDrawingField == null)
			isDrawingField = AsmUtils.changeFieldAccess(Tessellator.class, "isDrawing", "field_78415_z");

		try
		{
			return isDrawingField.getBoolean(t);
		}
		catch (IllegalArgumentException | IllegalAccessException e)
		{
			MalisisCore.log.error("[MalisisRenderer] Failed to get Tessellator.isDrawing value", e);
			return false;
		}
	}

	/**
	 * Triggers a draw and restart drawing with current {@link MalisisRenderer#drawMode}.
	 */
	public void next()
	{
		next(drawMode);
	}

	/**
	 * Triggers a draw and restart drawing with <b>drawMode</b>.
	 *
	 * @param drawMode the draw mode
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
		if (isDrawing())
			t.draw();
	}

	/**
	 * Cleans the current renderer state.
	 */
	public void clean()
	{
		if (renderType == RenderType.ISBRH_WORLD)
		{
			tessellatorUnshift();
		}
		else if (renderType == RenderType.ISBRH_INVENTORY)
		{
			draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		else if (renderType == RenderType.ITEM_INVENTORY)
		{
			draw();
			GL11.glPopAttrib();
		}
		else if (renderType == RenderType.TESR_WORLD)
		{
			draw();
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
		else if (renderType == RenderType.WORLD_LAST)
		{
			draw();
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
		reset();
	}

	/**
	 * Shifts the {@link Tessellator} for ISBRH rendering.
	 */
	public void tessellatorShift()
	{
		if (isShifted)
			return;

		isShifted = true;
		t.addTranslation(x, y, z);
	}

	/**
	 * Unshifts the {@link Tessellator} for ISBRH rendering.
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
		if (renderType == RenderType.ISBRH_WORLD)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

	}

	/**
	 * Disables textures.
	 */
	public void disableTextures()
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Enables textures
	 */
	public void enableTextures()
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	@Override
	protected void bindTexture(ResourceLocation resourceLocaltion)
	{
		Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocaltion);
	}

	// #end prepare()

	/**
	 * _initialize.
	 */
	protected final void _initialize()
	{
		if (initialized)
			return;
		initialize();
		initialized = true;
	}

	/**
	 * Initializes this {@link MalisisRenderer}. Does nothing by default.<br>
	 * Called the first time a rendering is done and should be overridden if some setup is needed for the rendering (building shape and
	 * parameters).
	 */
	protected void initialize()
	{}

	/**
	 * Renders the block using the default Minecraft rendering system.
	 */
	public void renderStandard()
	{
		renderStandard(renderBlocks);
	}

	/**
	 * Renders the blocks using the default Minecraft rendering system with the specified <b>renderer</b>.
	 *
	 * @param renderer the renderer
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
	 * Main rendering method. Draws simple cube by default.<br>
	 * Should be overridden to handle the rendering.
	 */
	public void render()
	{
		drawShape(shape, rp);
	}

	/**
	 * Renders the destroy progress manually for TESR. Called if {@link MalisisRenderer#destroyBlockProgress} is not <code>null</code>.
	 */
	public void renderDestroyProgress()
	{
		if (destroyBlockProgress != null)
			overrideTexture = damagedIcons[destroyBlockProgress.getPartialBlockDamage()];
		//	enableBlending();
		render();
	}

	/**
	 * Draws a {@link Shape} without {@link RenderParameters} (default will be used).
	 *
	 * @param shape the shape
	 */
	public void drawShape(Shape shape)
	{
		drawShape(shape, null);
	}

	/**
	 * Draws a {@link Shape} with specified {@link RenderParameters}.
	 *
	 * @param s the s
	 * @param params the params
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

		for (Face f : s.getFaces())
			drawFace(f, f.getParameters());
	}

	/**
	 * Draws a {@link Face} with its own {@link RenderParameters}.
	 *
	 * @param face the face
	 */
	public void drawFace(Face face)
	{
		drawFace(face, face.getParameters());
	}

	/**
	 * Draws a {@link Face} with specified {@link RenderParameters}.
	 *
	 * @param f the f
	 * @param faceParams the face params
	 */
	protected void drawFace(Face f, RenderParameters faceParams)
	{
		if (f == null)
			return;

		int vertexCount = f.getVertexes().length;
		if (vertexCount != 4 && renderType == RenderType.ISBRH_WORLD)
		{
			MalisisCore.log.error("[MalisisRenderer] Attempting to render a face containing {} vertexes in ISBRH. Ignored", vertexCount);
			return;
		}

		face = f;
		params = new RenderParameters();
		params.merge(rp);
		params.merge(faceParams);

		if (!shouldRenderFace(face))
			return;

		//use normals if available
		if ((renderType == RenderType.ITEM_INVENTORY || renderType == RenderType.ISBRH_INVENTORY || params.useNormals.get())
				&& params.direction.get() != null)
			t.setNormal(params.direction.get().offsetX, params.direction.get().offsetY, params.direction.get().offsetZ);

		baseBrightness = getBaseBrightness();

		drawVertexes(face.getVertexes());

		//we need to separate each face
		if (drawMode == GL11.GL_POLYGON || drawMode == GL11.GL_LINE || drawMode == GL11.GL_LINE_STRIP || drawMode == GL11.GL_LINE_LOOP)
			next();
	}

	/**
	 * * Draws an array of {@link Vertex vertexes} (usually {@link Face#getVertexes()}).
	 *
	 * @param vertexes the vertexes
	 */
	protected void drawVertexes(Vertex[] vertexes)
	{
		for (int i = 0; i < vertexes.length; i++)
			drawVertex(vertexes[i], i);
	}

	/**
	 * Draws a single {@link Vertex}.
	 *
	 * @param vertex the vertex
	 * @param number the offset inside the face. (Used for AO)
	 */
	protected void drawVertex(Vertex vertex, int number)
	{
		if (vertex == null)
			vertex = new Vertex(0, 0, 0);

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

		if (params.useTexture.get())
			t.addVertexWithUV(vertex.getX(), vertex.getY(), vertex.getZ(), vertex.getU(), vertex.getV());
		else
			t.addVertex(vertex.getX(), vertex.getY(), vertex.getZ());

		vertexDrawn = true;
	}

	/**
	 * Draws a string at the specified coordinates, with color and shadow. The string gets translated. Uses FontRenderer.drawString().
	 *
	 * @param font the font
	 * @param text the text
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @param fro the fro
	 */
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontRenderOptions fro)
	{
		if (font == null)
			font = MalisisFont.minecraftFont;
		if (fro == null)
			fro = new FontRenderOptions();

		font.render(this, text, x, y, z, fro);
	}

	/**
	 * Gets the IIcon corresponding to the specified {@link RenderParameters}.
	 *
	 * @param params the params
	 * @return the icon
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
	 * Checks if a {@link Face} should be rendered. {@link RenderParameters#direction} needs to be defined for the <b>face</b>.
	 *
	 * @param face the face
	 * @return true, if successful
	 */
	protected boolean shouldRenderFace(Face face)
	{
		if (renderType != RenderType.ISBRH_WORLD || world == null || block == null)
			return true;
		if (rp != null && rp.renderAllFaces.get())
			return true;
		if (renderBlocks != null && renderBlocks.renderAllFaces == true)
			return true;
		RenderParameters p = face.getParameters();
		if (p.direction.get() == null || p.renderAllFaces.get())
			return true;

		boolean b = block.shouldSideBeRendered(world, x + p.direction.get().offsetX, y + p.direction.get().offsetY, z
				+ p.direction.get().offsetZ, p.direction.get().ordinal());
		return b;
	}

	/**
	 * Applies the texture to the {@link Shape}.<br>
	 * Usually necessary before some shape transformations in conjunction with {@link RenderParameters#applyTexture} set to
	 * <code>false</code> to prevent reapplying texture when rendering.
	 *
	 * @param shape the shape
	 */
	public void applyTexture(Shape shape)
	{
		applyTexture(shape, null);
	}

	/**
	 * Applies the texture to the {@link Shape} with specified {@link RenderParameters}.<br>
	 * Usually necessary before some shape transformations in conjunction with {@link RenderParameters#applyTexture} set to
	 * <code>false</code> to prevent reapplying texture when rendering.
	 *
	 * @param shape the shape
	 * @param parameters the parameters
	 */
	public void applyTexture(Shape shape, RenderParameters parameters)
	{
		//shape.applyMatrix();
		for (Face f : shape.getFaces())
		{
			face = f;
			RenderParameters params = new RenderParameters();
			params.merge(f.getParameters());
			params.merge(parameters);

			IIcon icon = getIcon(params);
			if (icon != null)
			{
				boolean flipU = params.flipU.get();
				if (params.direction.get() == ForgeDirection.NORTH || params.direction.get() == ForgeDirection.EAST)
					flipU = !flipU;
				f.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
			}
		}
	}

	/**
	 * Calculates the ambient occlusion for a {@link Vertex} and also apply the side dependent shade.<br>
	 * <b>aoMatrix</b> is the list of block coordinates necessary to compute AO. If it's empty, only the global face shade is applied.<br>
	 * Also, <i>params.colorMultiplier</i> is applied as well.
	 *
	 * @param vertex the vertex
	 * @param aoMatrix the ao matrix
	 * @return the int
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
		if (renderType != RenderType.ISBRH_WORLD && renderType != RenderType.TESR_WORLD) //no AO for item/inventories
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
	 * Gets the base brightness for the current {@link Face}.<br>
	 * If <i>params.useBlockBrightness</i> = false, <i>params.brightness</i>. Else, the brightness is determined based on
	 * <i>params.offset</i> and <i>getBlockBounds()</i>
	 *
	 * @return the base brightness
	 */
	protected int getBaseBrightness()
	{
		if (!params.useEnvironmentBrightness.get())
			return params.brightness.get();

		if (block != null)
		{
			if (world != null && block.getLightValue(world, x, y, z) != 0)
				return block.getLightValue(world, x, y, z) << 4;
			else if (block.getLightValue() != 0)
				return block.getLightValue() << 4;
		}

		if (renderType == RenderType.ISBRH_INVENTORY || renderType == RenderType.ITEM_INVENTORY)
			return Minecraft.getMinecraft().thePlayer.getBrightnessForRender(getPartialTick());

		//not in world
		if (world == null)
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
	 * Calculates the ambient occlusion brightness for a {@link Vertex}. <b>aoMatrix</b> is the list of block coordinates necessary to
	 * compute AO. Only first 3 blocks are used.<br>
	 *
	 * @param vertex the vertex
	 * @param aoMatrix the ao matrix
	 * @return the int
	 */
	protected int calcVertexBrightness(Vertex vertex, int[][] aoMatrix)
	{
		if (params.usePerVertexBrightness.get())
			return vertex.getBrightness();
		if (drawMode == GL11.GL_LINE) //no AO for lines
			return baseBrightness;
		if (renderType != RenderType.ISBRH_WORLD && renderType != RenderType.TESR_WORLD) //not in world
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
	 * @param b1 the b1
	 * @param b2 the b2
	 * @param b3 the b3
	 * @param base the base
	 * @return the ao brightness
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
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the block ambient occlusion
	 */
	protected float getBlockAmbientOcclusion(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (block == null)
			return 1.0F;

		return block.getAmbientOcclusionLightValue();
	}

	/**
	 * Gets the mix brightness for a block (sky + block source).
	 *
	 * @param world the world
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 * @return the mixed brightness for block
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
	 * @return the render bounds
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
	 * Modifies the {@link Vertex vertexes} coordinates relative to the bounds specified.<br>
	 * Eg : if x = 0.5, minX = 1, maxX = 3, x becomes 2
	 *
	 * @param bounds the bounds
	 */
	protected void calcVertexesPosition(AxisAlignedBB bounds)
	{
		if (bounds == null)
			return;

		for (Face f : shape.getFaces())
		{
			face = f;
			for (Vertex v : f.getVertexes())
				v.interpolateCoord(bounds);
		}
	}

	/**
	 * Gets and hold reference to damagedBlocks from Minecraft.renderGlobal via reflection.
	 *
	 * @return the damaged blocks
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
	 * @return the block destroy progress
	 */
	protected DestroyBlockProgress getBlockDestroyProgress()
	{
		if (renderType != RenderType.TESR_WORLD)
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
	 * @param dbp the dbp
	 * @return true, if is current block destroy progress
	 */
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z;
	}

	/**
	 * Gets the render id of this {@link MalisisRenderer}.
	 *
	 * @return the render id
	 */
	@Override
	public int getRenderId()
	{
		return renderId;
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering for specified classes.<br>
	 * Classes have to extend Block or TileEntity.<br>
	 * <font color="990000">A static <b>renderId</b> field is required inside the class extending Block !</font>
	 *
	 * @param listClass the list class
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
					MalisisCore.log.error("[MalisisRenderer] Tried to register ISBRH for block class {} that does not have renderId field",
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
	 * Registers this {@link MalisisRenderer} to be used for rendering the specified <b>item</b>.
	 *
	 * @param item the item
	 */
	public void registerFor(Item item)
	{
		MinecraftForgeClient.registerItemRenderer(item, this);
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for {@link RenderWorldLastEvent}.
	 */
	public void registerForRenderWorldLast()
	{
		RenderWorldEventHandler.register(this);
	}
}
