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
import java.util.List;
import java.util.Map;

import net.malisis.core.MalisisCore;
import net.malisis.core.MalisisRegistry;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.IIconMetaProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.IFlexibleBakedModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;

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
	/** TileEntity currently drawing (for TESR). */
	protected TileEntity tileEntity;
	/** Partial tick time (TESR/IRWL). */
	protected float partialTick = 0;
	/** ItemStack to render (ITEM). */
	protected ItemStack itemStack;
	/** RenderGlobal reference (IRWL) */
	protected RenderGlobal renderGlobal;
	/** Old vertex format to restore when done drawing (BLOCK) */
	protected VertexFormat oldVertexFormat;
	/** Type of rendering. */
	protected RenderType renderType;
	/** Mode of rendering (GL constant). */
	protected int drawMode;
	//	/** Current model being rendered. */
	//	protected MalisisModel model = new MalisisModel(new Cube());
	//	/** Current face being rendered. */
	//	protected Face face;
	//	/** Current parameters for the shape being rendered. */
	//	protected RenderParameters rp = new RenderParameters();
	//	/** Current parameters for the face being rendered. */
	//	protected RenderParameters params;
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
		if (itemStack.getItem() instanceof ItemBlock)
			set(Block.getBlockFromItem(itemStack.getItem()));
		this.itemStack = itemStack;
	}

	// #end

	//#region IBlockRenderer
	@Override
	public boolean renderBlock(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		this.wr = Tessellator.getInstance().getWorldRenderer();
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
			oldVertexFormat = wr.getVertexFormat();
			wr.setVertexFormat(DefaultVertexFormats.BLOCK);
		}
		else if (renderType == RenderType.ITEM)
		{
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			startDrawing();
		}
		else if (renderType == RenderType.TILE_ENTITY)
		{
			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			//GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			//GlStateManager.shadeModel(GL11.GL_SMOOTH);

			GlStateManager.translate(data[0], data[1], data[2]);

			bindTexture(TextureMap.locationBlocksTexture);

			startDrawing();
			wr.setVertexFormat(DefaultVertexFormats.BLOCK);
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
	 * Cleans the current renderer state.
	 */
	public void clean()
	{
		if (renderType == RenderType.BLOCK)
		{
			wr.setVertexFormat(oldVertexFormat);
		}
		else if (renderType == RenderType.ITEM)
		{
			draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
			GL11.glPopAttrib();
		}
		else if (renderType == RenderType.TILE_ENTITY)
		{
			draw();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
			wr.setVertexFormat(oldVertexFormat);
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
	 * Enables the blending for the rendering. Ineffective for BLOCK.
	 */
	public void enableBlending()
	{
		if (renderType == RenderType.BLOCK)
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
	protected void drawShape(Shape shape)
	{
		drawShape(shape, null);
	}

	/**
	 * Draws a {@link Shape} with specified {@link RenderParameters}.
	 *
	 * @param s the s
	 * @param params the params
	 */
	protected void drawShape(Shape s, RenderParameters params)
	{
		if (s == null)
			return;

		if (params == null)
			params = new RenderParameters();

		//apply transformations
		s.applyMatrix();

		if (params.applyTexture.get())
			applyTexture(s, params);

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

		params = new RenderParameters();
		params.merge(params);
		params.merge(face.getParameters());

		if (!shouldRenderFace(face, params))
			return;

		//use normals if available
		if ((renderType == RenderType.ITEM || renderType == RenderType.ITEM || params.useNormals.get()) && params.direction.get() != null)
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

		//TODO: refactor when done
		if (renderType == RenderType.BLOCK)
			wr.addVertexData(vertex.toVertexData(pos));
		else if (renderType == RenderType.TILE_ENTITY)
			wr.addVertexData(vertex.toVertexData(null));

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
	 * Gets the MalisisIcon corresponding to the specified {@link RenderParameters}.
	 *
	 * @param params the params
	 * @return the icon
	 */
	protected MalisisIcon getIcon(Face face, RenderParameters params)
	{
		IIconProvider iconProvider = params.iconProvider.get();
		if (iconProvider == null && block instanceof IIconMetaProvider)
			iconProvider = ((IIconMetaProvider) block).getIconProvider();

		if (iconProvider == null)
			return new MalisisIcon();

		MalisisIcon icon = iconProvider.getIcon(face);
		return icon != null ? icon : new MalisisIcon();
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
	 * @param parameters the parameters
	 */
	public void applyTexture(Shape shape, RenderParameters parameters)
	{
		//shape.applyMatrix();
		for (Face f : shape.getFaces())
		{
			RenderParameters params = new RenderParameters();
			params.merge(f.getParameters());
			params.merge(parameters);

			MalisisIcon icon = getIcon(f, params);
			boolean flipU = params.flipU.get();
			if (params.direction.get() == EnumFacing.NORTH || params.direction.get() == EnumFacing.EAST)
				flipU = !flipU;
			f.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
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
		if (block != null)
		{
			if (world != null && block.getLightValue(world, pos) != 0)
				return block.getLightValue(world, pos) << 4;
			else if (block.getLightValue() != 0)
				return block.getLightValue() << 4;
		}

		//not in world
		if ((renderType != RenderType.BLOCK && renderType != RenderType.TILE_ENTITY) || world == null || !params.useBlockBrightness.get())
			return params.brightness.get();

		//no direction, we can only use current block brightness
		if (params.direction.get() == null)
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
		MalisisRegistry.registerBlockRenderer(block, this);
		ModelLoader.setCustomStateMapper(block, new IStateMapper()
		{
			@Override
			public Map putStateModelLocations(Block block)
			{
				return ImmutableMap.of();
			}
		});

		//MinecraftForge.EVENT_BUS.register(new ItemBakeHandler(block));
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the specified <b>item</b>.
	 *
	 * @param item the item
	 */
	public void registerFor(Item item)
	{
		//MalisisRegistry.registerItemRenderer(item, this);
		MinecraftForge.EVENT_BUS.register(new ItemBakeHandler(item));
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
		RenderWorldEventHandler.register(this);
	}

	//@formatter:off
	private static BuiltIn builtin = new BuiltIn();
	private static class BuiltIn implements IFlexibleBakedModel
	{
		@Override
		public boolean isAmbientOcclusion() { return false; }
		@Override
		public boolean isGui3d() { return false; }
		@Override
		public boolean isBuiltInRenderer() { return true; }
		@Override
		public TextureAtlasSprite getTexture() { return null; }
		@Override
		public ItemCameraTransforms getItemCameraTransforms() { return ItemCameraTransforms.DEFAULT; }
		@Override
		public List<BakedQuad> getFaceQuads(EnumFacing side) { return null; }
		@Override
		public List<BakedQuad> getGeneralQuads() { return null; }
		@Override
		public VertexFormat getFormat() { return null; }
	}
	//@formatter:on

	private static class ItemBakeHandler
	{
		private final ModelResourceLocation rl;

		public ItemBakeHandler(Item item)
		{
			rl = new ModelResourceLocation(item.getUnlocalizedName(), "inventory");
		}

		public ItemBakeHandler(Block block)
		{
			String modid = Loader.instance().activeModContainer().getModId();
			String name = block.getUnlocalizedName().substring(5);
			rl = new ModelResourceLocation(modid + ":" + name, "inventory");
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, rl);
		}

		@SubscribeEvent
		public void onModelBakeEvent(ModelBakeEvent event)
		{
			event.modelRegistry.putObject(rl, builtin);
		}
	}

}
