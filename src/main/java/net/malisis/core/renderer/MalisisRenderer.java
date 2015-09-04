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

import javax.vecmath.Matrix4f;

import net.malisis.core.MalisisCore;
import net.malisis.core.MalisisRegistry;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.metaprovider.IBlockMetaIconProvider;
import net.malisis.core.renderer.icon.metaprovider.IItemMetaIconProvider;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.renderer.icon.provider.IItemIconProvider;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.BlockPosUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import org.lwjgl.opengl.GL11;

/**
 * Base class for rendering. Handles the rendering. Provides easy registration of the renderer, and automatically sets up the context for
 * the rendering.
 *
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class MalisisRenderer extends TileEntitySpecialRenderer implements IBlockRenderer, IRenderWorldLast
{
	/** Reference to Tessellator.isDrawing field **/
	private static Field isDrawingField;

	public static VertexFormat vertexFormat = new VertexFormat()
	{
		{
			setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
			setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
			setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2));
			setElement(new VertexFormatElement(1, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.UV, 2));
			setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.NORMAL, 3));
			setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.PADDING, 1));
		}
	};

	/** Whether this {@link MalisisRenderer} initialized. (initialize() already called) */
	private boolean initialized = false;
	/** Tessellator reference. */
	protected WorldRenderer wr = Tessellator.getInstance().getWorldRenderer();
	/** Current world reference (BLOCK/TESR/IRWL). */
	protected IBlockAccess world;
	/** Position of the block (BLOCK/TESR). */
	protected BlockPos pos;
	/** Block to render (BLOCK/TESR). */
	protected Block block;
	/** Metadata of the block to render (BLOCK/TESR). */
	protected IBlockState blockState;
	/** TileEntity currently drawing (TESR). */
	protected TileEntity tileEntity;
	/** Partial tick time (TESR/IRWL). */
	protected float partialTick = 0;
	/** ItemStack to render (ITEM). */
	protected ItemStack itemStack;
	/** Item to render (ITEM) */
	protected Item item;
	/** Type of render for item (ITEM) **/
	protected TransformType tranformType;
	/** RenderGlobal reference (IRWL) */
	protected RenderGlobal renderGlobal;
	/** Type of rendering. */
	protected RenderType renderType;
	/** Mode of rendering (GL constant). */
	protected int drawMode;
	/** Base brightness of the block. */
	protected int baseBrightness;
	/** An override texture set by the renderer. */
	protected MalisisIcon overrideTexture;

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
		//this.renderId = RenderingRegistry.getNextAvailableRenderId();
	}

	// #region set()
	/**
	 * Resets data so this {@link MalisisRenderer} can be reused.
	 */
	public void reset()
	{
		this.wr = null;
		this.renderType = RenderType.UNSET;
		this.drawMode = 0;
		this.world = null;
		this.pos = null;
		this.block = null;
		this.blockState = null;
		this.item = null;
		this.itemStack = null;
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
	public void set(IBlockAccess world, Block block, BlockPos pos, IBlockState blockState)
	{
		this.world = world;
		this.pos = pos;
		this.block = block;
		this.blockState = blockState;
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
		this.block = block;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param blockMetadata the block metadata
	 */
	public void set(IBlockState blockState)
	{
		this.blockState = blockState;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void set(BlockPos pos)
	{
		this.pos = pos;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param te the te
	 * @param partialTick the partial tick
	 */
	public void set(TileEntity te, float partialTick)
	{
		set(te.getWorld(), te.getBlockType(), te.getPos(), te.getWorld().getBlockState(te.getPos()));
		this.partialTick = partialTick;
		this.tileEntity = te;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param type the type
	 * @param itemStack the item stack
	 */
	public void set(ItemStack itemStack)
	{
		this.itemStack = itemStack;
		this.item = itemStack.getItem();
		if (item instanceof ItemBlock)
			set(Block.getBlockFromItem(itemStack.getItem()));

	}

	// #end

	//#region IBlockRenderer
	@Override
	public boolean renderBlock(WorldRenderer wr, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		this.wr = wr;
		set(world, state.getBlock(), pos, state);
		prepare(RenderType.BLOCK);
		render();
		clean();

		return vertexDrawn;
	}

	//#end IBlockRenderer

	//#region IItemRenderer
	@Override
	public boolean renderItem(ItemStack itemStack, float partialTick)
	{
		this.wr = Tessellator.getInstance().getWorldRenderer();
		set(itemStack);
		prepare(RenderType.ITEM);
		render();
		clean();
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		this.tranformType = tranformType;
		return null;
	}

	//#end IItemRenderer

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
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick, int destroyStage)
	{
		this.wr = Tessellator.getInstance().getWorldRenderer();
		set(te, partialTick);
		prepare(RenderType.TILE_ENTITY, x, y, z);
		render();
		//TODO
		//		if (getBlockDamage)
		//		{
		//			destroyBlockProgress = getBlockDestroyProgress();
		//			if (destroyBlockProgress != null)
		//			{
		//				next();
		//
		//				GL11.glEnable(GL11.GL_BLEND);
		//				OpenGlHelper.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);
		//				GL11.glAlphaFunc(GL11.GL_GREATER, 0);
		//				GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
		//
		//				t.disableColor();
		//				renderDestroyProgress();
		//				next();
		//				GL11.glDisable(GL11.GL_BLEND);
		//			}
		//		}
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
		wr = Tessellator.getInstance().getWorldRenderer();
		partialTick = event.partialTicks;
		renderGlobal = event.context;
		double x = 0, y = 0, z = 0;
		if (shouldSetViewportPosition())
		{
			EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
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

		this.renderType = renderType;

		if (renderType == RenderType.BLOCK)
		{
			wr.setVertexFormat(DefaultVertexFormats.BLOCK);
		}
		else if (renderType == RenderType.ITEM)
		{
			startDrawing();
		}
		else if (renderType == RenderType.TILE_ENTITY)
		{
			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			GlStateManager.translate(data[0], data[1], data[2]);

			bindTexture(TextureMap.locationBlocksTexture);

			startDrawing();
		}
		else if (renderType == RenderType.WORLD_LAST)
		{
			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();

			GlStateManager.translate(data[0], data[1], data[2]);

			bindTexture(TextureMap.locationBlocksTexture);

			startDrawing();
		}
	}

	/**
	 * Cleans the current renderer state.
	 */
	public void clean()
	{
		if (renderType == RenderType.ITEM)
		{
			draw();
			//			GlStateManager.enableLighting();
			//			GlStateManager.popMatrix();
			//			GlStateManager.popAttrib();
		}
		else if (renderType == RenderType.TILE_ENTITY)
		{
			draw();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}
		else if (renderType == RenderType.WORLD_LAST)
		{
			draw();
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}
		reset();
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

		wr.startDrawing(drawMode);
		wr.setVertexFormat(vertexFormat);
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
			isDrawingField = AsmUtils.changeFieldAccess(WorldRenderer.class, "isDrawing", "field_179010_r");

		try
		{
			if (wr == null)
				throw new NullPointerException("[MalisisRenderer] WorldRenderer not set for " + renderType);
			return isDrawingField.getBoolean(wr);
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
			Tessellator.getInstance().draw();
	}

	/**
	 * Enables the blending for the rendering. Ineffective for BLOCK renderType.
	 */
	public void enableBlending()
	{
		if (renderType == RenderType.BLOCK)
			return;

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableColorMaterial();
	}

	/**
	 * Disables blending for the rendering. Ineffective for BLOCK renderType.
	 */
	public void disableBlending()
	{
		if (renderType == RenderType.BLOCK)
			return;

		GlStateManager.disableBlend();
		GlStateManager.disableColorMaterial();
	}

	/**
	 * Enables textures
	 */
	public void enableTextures()
	{
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Disables textures.
	 */
	public void disableTextures()
	{
		GL11.glDisable(GL11.GL_TEXTURE_2D);
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
	 * Renders the blocks using the default Minecraft rendering system.
	 *
	 * @param renderer the renderer
	 */
	public void renderStandard()
	{
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(blockState, pos, world, wr);
	}

	/**
	 * Main rendering method. Draws simple cube by default.<br>
	 * Should be overridden to handle the rendering.
	 */
	public void render()
	{
		drawShape(new Cube());
	}

	protected void drawModel(MalisisModel model, RenderParameters params)
	{
		for (Shape s : model)
			drawShape(s, params);
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

		s.applyMatrix();

		for (Face f : s.getFaces())
			drawFace(f, params);
	}

	/**
	 * Draws a {@link Face} with its own {@link RenderParameters}.
	 *
	 * @param face the face
	 */
	protected void drawFace(Face face)
	{
		drawFace(face, null);
	}

	/**
	 * Draws a {@link Face} with specified {@link RenderParameters}.
	 *
	 * @param face the f
	 * @param params the face params
	 */
	protected void drawFace(Face face, RenderParameters params)
	{
		if (face == null)
			return;

		int vertexCount = face.getVertexes().length;
		if (vertexCount != 4 && renderType == RenderType.BLOCK)
		{
			MalisisCore.log.error("[MalisisRenderer] Attempting to render a face containing {} vertexes in BLOCK for {}. Ignored",
					vertexCount, block);
			return;
		}

		params = RenderParameters.merge(params, face.getParameters());

		if (!shouldRenderFace(face, params))
			return;

		if (params.applyTexture.get())
			applyTexture(face, params);

		//use normals if available
		if ((renderType == RenderType.ITEM || params.useNormals.get()) && params.direction.get() != null)
			wr.setNormal(params.direction.get().getFrontOffsetX(), params.direction.get().getFrontOffsetY(), params.direction.get()
					.getFrontOffsetZ());

		baseBrightness = getBaseBrightness(params);

		for (int i = 0; i < face.getVertexes().length; i++)
			drawVertex(face.getVertexes()[i], i, params);

		//we need to separate each face
		if (drawMode == GL11.GL_POLYGON || drawMode == GL11.GL_LINE || drawMode == GL11.GL_LINE_STRIP || drawMode == GL11.GL_LINE_LOOP)
			next();
	}

	/**
	 * Draws a single {@link Vertex}.
	 *
	 * @param vertex the vertex
	 * @param number the offset inside the face. (Used for AO)
	 */
	protected void drawVertex(Vertex vertex, int number, RenderParameters params)
	{
		if (vertex == null)
			vertex = new Vertex(0, 0, 0);

		// brightness
		int brightness = calcVertexBrightness(vertex, number, params);
		vertex.setBrightness(brightness);

		// color
		int color = calcVertexColor(vertex, number, params);
		vertex.setColor(color);

		// alpha
		if (!params.usePerVertexAlpha.get())
			vertex.setAlpha(params.alpha.get());

		if (renderType == RenderType.ITEM)
			vertex.setNormal(params.direction.get());

		wr.addVertexData(getVertexData(vertex));

		vertexDrawn = true;
	}

	/**
	 * Gets the vertex data.
	 *
	 * @param vertex the vertex
	 * @return the vertex data
	 */
	private int[] getVertexData(Vertex vertex)
	{
		float x = (float) vertex.getX();
		float y = (float) vertex.getY();
		float z = (float) vertex.getZ();

		int size = vertexFormat.getNextOffset();
		if (renderType == RenderType.BLOCK)
		{
			size = DefaultVertexFormats.BLOCK.getNextOffset();
			//when drawing a block, the position to draw is relative to current chunk
			BlockPos chunkPos = BlockPosUtils.chunkPosition(pos);
			x += chunkPos.getX();
			y += chunkPos.getY();
			z += chunkPos.getZ();
		}

		int[] data = new int[size / 4];
		data[0] = Float.floatToRawIntBits(x);
		data[1] = Float.floatToRawIntBits(y);
		data[2] = Float.floatToRawIntBits(z);
		data[3] = vertex.getRGBA();
		data[4] = Float.floatToRawIntBits((float) vertex.getU());
		data[5] = Float.floatToRawIntBits((float) vertex.getV());
		data[6] = vertex.getBrightness();
		if (renderType != RenderType.BLOCK)
			data[7] = vertex.getNormal();

		return data;
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
	 * Checks if a {@link Face} should be rendered. {@link RenderParameters#direction} needs to be defined for the <b>face</b>.
	 *
	 * @param face the face
	 * @return true, if successful
	 */
	protected boolean shouldRenderFace(Face face, RenderParameters params)
	{
		if (renderType != RenderType.BLOCK || world == null || block == null)
			return true;
		if (params != null && params.renderAllFaces.get())
			return true;

		RenderParameters p = face.getParameters();
		if (p.direction.get() == null || p.renderAllFaces.get())
			return true;

		boolean b = block.shouldSideBeRendered(world, pos.offset(p.direction.get()), p.direction.get());
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
	 * @param params the parameters
	 */
	public void applyTexture(Shape shape, RenderParameters params)
	{
		//shape.applyMatrix();
		for (Face f : shape.getFaces())
		{
			RenderParameters rp = RenderParameters.merge(params, f.getParameters());
			applyTexture(f, rp);
		}
	}

	/**
	 * Applies the texture to the {@link Face} with specified {@link RenderParameters}.<br>
	 *
	 * @param shape the shape
	 * @param params the parameters
	 */
	public void applyTexture(Face face, RenderParameters params)
	{
		MalisisIcon icon = getIcon(face, params);
		boolean flipU = params.flipU.get();
		if (params.direction.get() == EnumFacing.NORTH || params.direction.get() == EnumFacing.EAST)
			flipU = !flipU;
		face.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
	}

	/**
	 * Gets the {@link MalisisIcon} corresponding to the specified {@link RenderParameters}.<br>
	 * If {@link #block} or {@link #item} is an {@link IIconProvider} and give the right provider for the current context, gets the icon
	 * from that provider.
	 *
	 * @param face the face
	 * @param params the params
	 * @return the icon
	 */
	protected MalisisIcon getIcon(Face face, RenderParameters params)
	{
		IIconProvider ip = getIconProvider(params);
		if (ip instanceof IItemIconProvider && itemStack != null)
			return ((IItemIconProvider) ip).getIcon(itemStack);

		if (ip instanceof IBlockIconProvider && block != null)
		{
			IBlockIconProvider iblockp = (IBlockIconProvider) ip;
			if (renderType == RenderType.BLOCK)
				return iblockp.getIcon(world, pos, blockState, params.direction.get());
			else if (renderType == RenderType.ITEM)
				return iblockp.getIcon(itemStack, params.direction.get());
		}

		IIconProvider iconProvider = params.iconProvider.get();
		return iconProvider != null ? iconProvider.getIcon() : null;
	}

	/**
	 * Gets the {@link IIconProvider} either from parameters, the block or the item.
	 *
	 * @return the icon provider
	 */
	private IIconProvider getIconProvider(RenderParameters params)
	{
		if (params.iconProvider.get() != null)
			return params.iconProvider.get();

		if (item instanceof IItemMetaIconProvider && ((IItemMetaIconProvider) item).getItemIconProvider() != null)
			return ((IItemMetaIconProvider) item).getItemIconProvider();

		if (block instanceof IBlockMetaIconProvider && ((IBlockMetaIconProvider) block).getBlockIconProvider() != null)
			return ((IBlockMetaIconProvider) block).getBlockIconProvider();

		return null;
	}

	/**
	 * Calculates the ambient occlusion for a {@link Vertex} and also applies the side dependent shade.<br>
	 * <b>aoMatrix</b> is the list of block coordinates necessary to compute AO. If it's empty, only the global face shade is applied.<br>
	 * Also, <i>params.colorMultiplier</i> is applied as well.
	 *
	 * @param vertex the vertex
	 * @param aoMatrix the ao matrix
	 * @return the int
	 */
	protected int calcVertexColor(Vertex vertex, int number, RenderParameters params)
	{
		int color = 0xFFFFFF;
		if (params == null)
			return color;

		if (params.usePerVertexColor.get()) //vertex should use their own colors
			color = vertex.getColor();

		if (params.colorMultiplier.get() != null) //global color multiplier is set
			color = params.colorMultiplier.get();
		else if (block != null) //use block color multiplier
			color = world != null ? block.colorMultiplier(world, pos, 0) : block.getRenderColor(blockState);

		if (drawMode == GL11.GL_LINE) //no AO for lines
			return color;
		if (renderType != RenderType.BLOCK && renderType != RenderType.TILE_ENTITY) //no AO for item/inventories
			return color;

		int[][] aoMatrix = (int[][]) params.aoMatrix.get(number);
		float factor = 1;
		//calculate AO
		if (params.calculateAOColor.get() && aoMatrix != null && Minecraft.isAmbientOcclusionEnabled()
				&& block.getLightValue(world, pos) == 0)
		{
			factor = getBlockAmbientOcclusion(world, pos.offset(params.direction.get()));

			for (int i = 0; i < aoMatrix.length; i++)
				factor += getBlockAmbientOcclusion(world, pos.add(aoMatrix[i][0], aoMatrix[i][1], aoMatrix[i][2]));

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
	protected int getBaseBrightness(RenderParameters params)
	{
		if (!params.useEnvironmentBrightness.get())
			return params.brightness.get();

		if (block != null)
		{
			if (world != null && block.getLightValue(world, pos) != 0)
				return block.getLightValue(world, pos) << 4;
			else if (block.getLightValue() != 0)
				return block.getLightValue() << 4;
		}

		if (renderType == RenderType.ITEM)
			return Minecraft.getMinecraft().thePlayer.getBrightnessForRender(getPartialTick());

		//not in world
		if (world == null || block == null)
			return params.brightness.get();

		//no direction, we can only use current block brightness
		if (params.direction.get() == null && block != null)
			return block.getMixedBrightnessForBlock(world, pos);

		AxisAlignedBB bounds = getRenderBounds(params);
		EnumFacing dir = params.direction.get();
		BlockPos p = pos;
		if (dir != null)
			p = p.offset(dir);

		//use the brightness of the block next to it
		if (bounds != null)
		{
			if (dir == EnumFacing.WEST && bounds.minX > 0)
				p = p.east();
			else if (dir == EnumFacing.EAST && bounds.maxX < 1)
				p = p.west();
			else if (dir == EnumFacing.NORTH && bounds.minZ > 0)
				p = p.south();
			else if (dir == EnumFacing.SOUTH && bounds.maxZ < 1)
				p = p.north();
			else if (dir == EnumFacing.DOWN && bounds.minY > 0)
				p = p.up();
			else if (dir == EnumFacing.UP && bounds.maxY < 1)
				p = p.down();
		}

		return getMixedBrightnessForBlock(world, p);
	}

	/**
	 * Calculates the ambient occlusion brightness for a {@link Vertex}. <b>aoMatrix</b> is the list of block coordinates necessary to
	 * compute AO. Only first 3 blocks are used.<br>
	 *
	 * @param vertex the vertex
	 * @param aoMatrix the ao matrix
	 * @return the int
	 */
	protected int calcVertexBrightness(Vertex vertex, int number, RenderParameters params)
	{
		if (params == null)
			return baseBrightness;
		if (params.usePerVertexBrightness.get())
			return vertex.getBrightness();
		if (drawMode == GL11.GL_LINE) //no AO for lines
			return baseBrightness;
		if (renderType != RenderType.BLOCK && renderType != RenderType.TILE_ENTITY) //not in world
			return baseBrightness;
		int[][] aoMatrix = (int[][]) params.aoMatrix.get(number);
		if (!params.calculateBrightness.get() || aoMatrix == null) //no data
			return baseBrightness;
		if (!Minecraft.isAmbientOcclusionEnabled() || block.getLightValue(world, pos) != 0) // emit light
			return baseBrightness;

		int[] b = new int[Math.max(3, aoMatrix.length)];

		for (int i = 0; i < b.length; i++)
			b[i] += getMixedBrightnessForBlock(world, pos.add(aoMatrix[i][0], aoMatrix[i][1], aoMatrix[i][2]));

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
	protected float getBlockAmbientOcclusion(IBlockAccess world, BlockPos pos)
	{
		Block block = world.getBlockState(pos).getBlock();
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
	protected int getMixedBrightnessForBlock(IBlockAccess world, BlockPos pos)
	{
		// return world.getLightBrightnessForSkyBlocks(x, y, z, 0);
		return world.getBlockState(pos).getBlock().getMixedBrightnessForBlock(world, pos);
	}

	/**
	 * Gets the rendering bounds. If <i>params.useBlockBounds</i> = false, <i>params.renderBounds</i> is used instead of the actual block
	 * bounds.
	 *
	 * @return the render bounds
	 */
	protected AxisAlignedBB getRenderBounds(RenderParameters params)
	{
		if (block == null || !params.useBlockBounds.get())
			return params.renderBounds.get();

		if (world != null)
			block.setBlockBoundsBasedOnState(world, pos);

		return new AxisAlignedBB(block.getBlockBoundsMinX(), block.getBlockBoundsMinY(), block.getBlockBoundsMinZ(),
				block.getBlockBoundsMaxX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ());
	}

	private static Timer timer = null;

	public static float getPartialTick()
	{
		if (timer == null)
		{
			Field f = AsmUtils.changeFieldAccess(Minecraft.class, "timer", "field_71428_T");
			try
			{
				timer = (Timer) f.get(Minecraft.getMinecraft());
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				MalisisCore.log.info("[MalisisRenderer] Failed to acces Minecraft timer.");
				timer = new Timer(20F);
			}
		}

		return timer.elapsedPartialTicks;
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the specified <b>block</b>.
	 *
	 * @param block the block
	 */
	public void registerFor(Block block)
	{
		registerFor(block, getDefaultRenderInfos());
	}

	public void registerFor(Block block, IItemRenderInfo renderInfos)
	{
		MalisisRegistry.registerBlockRenderer(block, this, renderInfos);
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the specified <b>item</b>.
	 *
	 * @param item the item
	 */
	public void registerFor(Item item)
	{
		registerFor(item, getDefaultRenderInfos());
	}

	public void registerFor(Item item, IItemRenderInfo renderInfos)
	{
		MalisisRegistry.registerItemRenderer(item, this, renderInfos);
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering for a specified class.<br>
	 * Class has to extend TileEntity.<br>
	 *
	 * @param clazz the clazz
	 */
	public void registerFor(Class<? extends TileEntity> clazz)
	{
		ClientRegistry.bindTileEntitySpecialRenderer(clazz, this);
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for {@link RenderWorldLastEvent}.
	 */
	public void registerForRenderWorldLast()
	{
		MalisisRegistry.registerRenderWorldLast(this);
	}

	private IItemRenderInfo getDefaultRenderInfos()
	{
		return new IItemRenderInfo()
		{
			@Override
			public boolean isGui3d()
			{
				return MalisisRenderer.this.isGui3d();
			}

			@Override
			public Matrix4f getTransform(TransformType tranformType)
			{
				return MalisisRenderer.this.getTransform(tranformType);
			}
		};
	}

}
