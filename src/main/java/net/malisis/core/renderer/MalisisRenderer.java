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

import java.util.Arrays;
import java.util.Set;

import javax.vecmath.Matrix4f;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Sets;

import net.malisis.core.MalisisCore;
import net.malisis.core.asm.AsmUtils;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.ISmartCull;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.font.FontOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.renderer.icon.provider.IItemIconProvider;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.BlockPosUtils;
import net.malisis.core.util.EnumFacingUtils;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.Silenced;
import net.malisis.core.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;

/**
 * Base class for rendering. Handles the rendering. Provides easy registration of the renderer, and automatically sets up the context for
 * the rendering.
 *
 * @author Ordinastie
 *
 */
public class MalisisRenderer<T extends TileEntity> extends TileEntitySpecialRenderer<T> implements IBlockRenderer, IRenderWorldLast
{
	/** Batched buffer reference. */
	protected static final VertexBuffer batchedBuffer = ((Tessellator) Silenced.get(() -> AsmUtils	.changeFieldAccess(	TileEntityRendererDispatcher.class,
																														"batchBuffer")
																									.get(TileEntityRendererDispatcher.instance))).getBuffer();

	public static VertexFormat malisisVertexFormat = new VertexFormat()
	{
		{
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.UV, 2));
			addElement(new VertexFormatElement(1, VertexFormatElement.EnumType.SHORT, VertexFormatElement.EnumUsage.UV, 2));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.NORMAL, 3));
			addElement(new VertexFormatElement(0, VertexFormatElement.EnumType.BYTE, VertexFormatElement.EnumUsage.PADDING, 1));
		}
	};

	/** Whether this {@link MalisisRenderer} initialized. (initialize() already called) */
	private boolean initialized = false;
	/** Currently used buffer. */
	protected VertexBuffer buffer = null;
	/** Current used vertex format. */
	protected VertexFormat vertexFormat = malisisVertexFormat;
	/** Current world reference (BLOCK/TESR/IRWL). */
	protected IBlockAccess world;
	/** Position of the block (BLOCK/TESR). */
	protected BlockPos pos;
	/** Block to render (BLOCK/TESR). */
	protected Block block;
	/** Metadata of the block to render (BLOCK/TESR). */
	protected IBlockState blockState;
	/** TileEntity currently drawing (TESR). */
	protected T tileEntity;
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
	protected int drawMode = GL11.GL_QUADS;
	/** Base brightness of the block. */
	protected int baseBrightness;
	/** Whether the rendering is batched (TESR/ANIMATED). **/
	private boolean isBatched = false;
	/** Vertex positions offset. **/
	protected Vec3d posOffset = null;

	/** List of classes the Block is allowed to be. */
	private Set<Class<?>> ensureBlocks = Sets.newHashSet();

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

	//#region Getters
	public RenderType getRenderType()
	{
		return renderType;
	}

	public IBlockAccess getWorldAccess()
	{
		return world;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public IBlockState getBlockState()
	{
		return blockState;
	}

	public ItemStack getItemStack()
	{
		return itemStack;
	}

	public T getTileEntity()
	{
		return tileEntity;
	}

	//#region

	// #region set()
	/**
	 * Resets data so this {@link MalisisRenderer} can be reused.
	 */
	public void reset()
	{
		this.buffer = null;
		this.renderType = RenderType.UNSET;
		this.drawMode = 0;
		this.world = null;
		this.pos = null;
		this.block = null;
		this.blockState = null;
		this.tileEntity = null;
		this.item = null;
		this.itemStack = null;
		this.destroyBlockProgress = null;
		this.tranformType = null;
		this.posOffset = null;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param world the world
	 * @param block the block
	 * @param pos the pos
	 * @param blockState the block state
	 */
	@SuppressWarnings("unchecked")
	public void set(IBlockAccess world, Block block, BlockPos pos, IBlockState blockState)
	{
		this.world = world;
		this.pos = new BlockPos(pos);
		this.block = block;
		this.blockState = blockState;
		this.tileEntity = (T) world.getTileEntity(pos);
	}

	public void set(IBlockAccess world, BlockPos pos)
	{
		this.world = world;
		this.pos = pos;
		set(world.getBlockState(pos));
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
		this.blockState = block.getDefaultState();
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param blockState the block state
	 */
	public void set(IBlockState blockState)
	{
		this.block = blockState.getBlock();
		this.blockState = blockState;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param pos the pos
	 */
	public void set(BlockPos pos)
	{
		this.pos = new BlockPos(pos);
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param te the te
	 * @param partialTick the partial tick
	 */
	public void set(T te, float partialTick)
	{
		set(te.getWorld(), te.getBlockType(), te.getPos(), te.getWorld().getBlockState(te.getPos()));
		this.partialTick = partialTick;
		this.tileEntity = te;
	}

	/**
	 * Sets informations for this {@link MalisisRenderer}.
	 *
	 * @param itemStack the item stack
	 */
	public void set(ItemStack itemStack)
	{
		this.itemStack = itemStack;
		this.item = itemStack.getItem();
		IBlockState state = ItemUtils.getStateFromItemStack(itemStack);
		if (state != null)
			this.set(state);
	}

	/**
	 * Limits the classes the block can be for this {@link MalisisRenderer}.
	 *
	 * @param blockClasses the block classes
	 */
	protected void ensureBlock(Class<?>... blockClasses)
	{
		ensureBlocks.clear();
		ensureBlocks.addAll(Arrays.asList(blockClasses));
	}

	/**
	 * Check if the current block is allowed. If not, no rendering will be done.
	 *
	 * @return true, if successful
	 */
	private boolean checkBlock()
	{
		if (block == null || ensureBlocks.size() == 0)
			return true;

		return ensureBlocks.contains(block.getClass());
	}

	protected void setBatched()
	{
		if (!FMLClientHandler.instance().hasOptifine())
			this.isBatched = true;
	}

	protected boolean isBatched()
	{
		return isBatched;
	}

	// #end

	//#region IBlockRenderer
	@Override
	public synchronized boolean renderBlock(VertexBuffer wr, IBlockAccess world, BlockPos pos, IBlockState state)
	{
		this.buffer = wr;
		set(world, state.getBlock(), pos, state);
		prepare(RenderType.BLOCK);
		if (checkBlock())
			render();
		clean();

		return vertexDrawn;
	}

	//#end IBlockRenderer

	//#region IItemRenderer
	@Override
	public synchronized boolean renderItem(ItemStack itemStack, float partialTick)
	{
		if (tranformType == TransformType.FIRST_PERSON_RIGHT_HAND || tranformType == TransformType.FIRST_PERSON_LEFT_HAND)
		{
			ItemStack stackInv = Utils	.getClientPlayer()
										.getHeldItem(tranformType == TransformType.FIRST_PERSON_RIGHT_HAND	? EnumHand.MAIN_HAND
																											: EnumHand.OFF_HAND);
			if (itemStack != stackInv && stackInv != null && stackInv.getItem() == itemStack.getItem())
				itemStack = stackInv;
		}

		this.buffer = Tessellator.getInstance().getBuffer();
		set(itemStack);
		prepare(RenderType.ITEM);

		if (checkBlock())
			render();
		clean();
		return true;
	}

	@Override
	public void setTransformType(TransformType transformType)
	{
		this.tranformType = transformType;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public Matrix4f getTransform(Item item, TransformType tranformType)
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
	public synchronized void renderTileEntityAt(T te, double x, double y, double z, float partialTick, int destroyStage)
	{
		if (te.getWorld().getBlockState(te.getPos()).getBlock() == Blocks.AIR)
			return;
		this.buffer = isBatched() ? batchedBuffer : Tessellator.getInstance().getBuffer();
		set(te, partialTick);
		prepare(RenderType.TILE_ENTITY, x, y, z);
		if (checkBlock())
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
		buffer = Tessellator.getInstance().getBuffer();
		partialTick = event.getPartialTicks();
		renderGlobal = event.getContext();
		double x = 0, y = 0, z = 0;
		if (shouldSetViewportPosition())
		{
			EntityPlayer p = Utils.getClientPlayer();
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

		if (renderType == RenderType.BLOCK)
		{
			vertexFormat = DefaultVertexFormats.BLOCK;
			//when drawing a block, the position to draw is relative to current chunk
			BlockPos chunkPos = BlockPosUtils.chunkPosition(pos);
			posOffset = new Vec3d(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ());
		}
		else if (renderType == RenderType.ITEM)
		{
			startDrawing(malisisVertexFormat);
		}
		else if (renderType == RenderType.TILE_ENTITY)
		{
			if (isBatched())
			{
				posOffset = new Vec3d(data[0], data[1], data[2]);
				vertexFormat = FMLClientHandler.instance().hasOptifine() ? DefaultVertexFormats.BLOCK : malisisVertexFormat;
			}
			else
			{
				GlStateManager.pushAttrib();
				GlStateManager.pushMatrix();
				GlStateManager.disableLighting();
				GlStateManager.translate(data[0], data[1], data[2]);

				bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				startDrawing(FMLClientHandler.instance().hasOptifine() ? DefaultVertexFormats.BLOCK : malisisVertexFormat);
				enableBlending();
			}
		}
		else if (renderType == RenderType.WORLD_LAST)
		{
			GlStateManager.pushAttrib();
			GlStateManager.pushMatrix();
			Minecraft.getMinecraft().entityRenderer.enableLightmap();
			GlStateManager.translate(data[0], data[1], data[2]);

			bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

			startDrawing(malisisVertexFormat);
			enableBlending();
		}
		else
			throw new IllegalArgumentException("Unknow renderType to handle for " + getClass().getSimpleName() + " : " + renderType);
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
			if (isBatched())
				buffer.setTranslation(0, 0, 0);
			else
			{
				draw();
				disableBlending();
				GlStateManager.enableLighting();
				GlStateManager.popMatrix();
				GlStateManager.popAttrib();
			}
		}
		else if (renderType == RenderType.WORLD_LAST)
		{
			draw();
			disableBlending();
			GlStateManager.popMatrix();
			GlStateManager.popAttrib();
		}
		reset();
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with GL11.GL_QUADS and {@link #malisisVertexFormat}.
	 */
	public void startDrawing()
	{
		startDrawing(GL11.GL_QUADS, vertexFormat);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with specified <b>drawMode</b> and {@link #malisisVertexFormat}.
	 *
	 * @param drawMode the draw mode
	 */
	public void startDrawing(int drawMode)
	{
		startDrawing(drawMode, vertexFormat);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with GL11.GL_QUADS and specified {@link VertexFormat}.
	 *
	 * @param vertexFormat the vertex format
	 */
	public void startDrawing(VertexFormat vertexFormat)
	{
		startDrawing(GL11.GL_QUADS, vertexFormat);
	}

	/**
	 * Tells the {@link Tessellator} to start drawing with specified <b>drawMode and specified {@link VertexFormat}.
	 *
	 * @param drawMode the draw mode
	 * @param vertexFormat the vertex format
	 */
	public void startDrawing(int drawMode, VertexFormat vertexFormat)
	{
		if (!canDraw())
			return;

		draw();

		buffer.begin(drawMode, vertexFormat);
		this.drawMode = drawMode;
		this.vertexFormat = vertexFormat;
	}

	/**
	 * Checks if the {@link Tessellator} is currently drawing.
	 *
	 * @return true, if is drawing
	 */
	public boolean isDrawing()
	{
		if (buffer == null)
			throw new NullPointerException("[MalisisRenderer] WorldRenderer not set for " + renderType);
		return buffer.isDrawing;
	}

	/**
	 * Triggers a draw and restart drawing with current {@link MalisisRenderer#drawMode}.
	 */
	public void next()
	{
		next(drawMode);
	}

	/**
	 * Triggers a draw and restart drawing with <b>drawMode</b> and current {@link VertexFormat}.
	 *
	 * @param drawMode the draw mode
	 */
	public void next(int drawMode)
	{
		next(drawMode, vertexFormat);

	}

	/**
	 * Triggers a draw and restart drawing with current {@link MalisisRenderer#drawMode} and specified {@link VertexFormat}
	 *
	 * @param vertexFormat the vertex format
	 */
	public void next(VertexFormat vertexFormat)
	{
		next(GL11.GL_QUADS, vertexFormat);
	}

	/**
	 * Triggers a draw and restart drawing with current {@link MalisisRenderer#drawMode} and {@link VertexFormat}
	 *
	 * @param drawMode the draw mode
	 * @param vertexFormat the vertex format
	 */
	public void next(int drawMode, VertexFormat vertexFormat)
	{
		draw();
		startDrawing(drawMode, vertexFormat);
	}

	/**
	 * Triggers a draw.
	 */
	public void draw()
	{
		if (canDraw() && isDrawing())
			Tessellator.getInstance().draw();
	}

	private boolean canDraw()
	{
		if (renderType == RenderType.BLOCK || renderType == RenderType.ANIMATED)
			return false;
		if (renderType == RenderType.TILE_ENTITY && isBatched())
			return false;
		return true;
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

		GlStateManager.disableColorMaterial();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
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

	/**
	 * Sets billboard mode.<br>
	 * Contents drawn will always be facing the player.
	 *
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void setBillboard(float x, float y, float z)
	{
		EntityPlayer player = Utils.getClientPlayer();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(180 - player.rotationYaw, 0, 1, 0);
	}

	/**
	 * End billboard mode.
	 */
	public void endBillboard()
	{
		GlStateManager.popMatrix();
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
	 */
	public void renderStandard()
	{
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(blockState, pos, world, buffer);
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
			MalisisCore.log.error(	"[MalisisRenderer] Attempting to render a face containing {} vertexes in BLOCK for {}. Ignored",
									vertexCount,
									block);
			return;
		}

		params = RenderParameters.merge(params, face.getParameters());

		if (!shouldRenderFace(face, params))
			return;

		if (params.applyTexture.get())
			applyTexture(face, params);

		baseBrightness = getBaseBrightness(params);

		//debug
		//		if (vertexCount != 4)
		//			params.colorMultiplier.set(0xFF0000);

		for (int i = 0; i < face.getVertexes().length; i++)
			drawVertex(face.getVertexes()[i], i, params);

		//use normals if available
		//		if ((renderType == RenderType.ITEM || params.useNormals.get()) && params.direction.get() != null)
		//			buffer.putNormal(params.direction.get().getFrontOffsetX(),
		//					params.direction.get().getFrontOffsetY(),
		//					params.direction.get().getFrontOffsetZ());

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
		//brightness = 255;
		vertex.setBrightness(brightness);

		// color
		int color = calcVertexColor(vertex, number, params);
		vertex.setColor(color);

		// alpha
		if (params != null && !params.usePerVertexAlpha.get())
			vertex.setAlpha(params.alpha.get());

		if (params != null && renderType == RenderType.ITEM)
			vertex.setNormal(params.direction.get());

		buffer.addVertexData(vertex.getVertexData(vertexFormat, posOffset));

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
	public void drawText(MalisisFont font, String text, float x, float y, float z, FontOptions fro)
	{
		if (font == null)
			font = MalisisFont.minecraftFont;
		if (fro == null)
			fro = FontOptions.builder().build();

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

		if (ISmartCull.shouldSmartCull(block))
			return smartCull(face, params);

		boolean b = blockState.shouldSideBeRendered(world, pos, p.direction.get());
		return b;
	}

	/**
	 * Culls a face based on the actual render bounds used and not block bounding box.
	 *
	 * @param face the face
	 * @param params the params
	 * @return true, if successful
	 */
	protected boolean smartCull(Face face, RenderParameters params)
	{
		EnumFacing side = params.direction.get();
		AxisAlignedBB bounds = getRenderBounds(params);

		if (side == EnumFacing.DOWN && bounds.minY > 0)
			return true;
		if (side == EnumFacing.UP && bounds.maxY < 1)
			return true;
		if (side == EnumFacing.NORTH && bounds.minZ > 0)
			return true;
		if (side == EnumFacing.SOUTH && bounds.maxZ < 1)
			return true;
		if (side == EnumFacing.WEST && bounds.minX > 0)
			return true;
		if (side == EnumFacing.EAST && bounds.maxX < 1)
			return true;

		return !world.getBlockState(pos.offset(side)).isOpaqueCube();
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
	 * @param face the face
	 * @param params the parameters
	 */
	public void applyTexture(Face face, RenderParameters params)
	{
		Icon icon = getIcon(face, params);
		if (icon == null)
			icon = Icon.missing;
		if (shouldRotateIcon(params))
		{
			if (params.textureSide.get() != null && params.textureSide.get().getAxis() == Axis.Y)
				icon.setRotation(EnumFacingUtils.getRotationCount(blockState));
			else
				icon.setRotation(0);
		}

		boolean flipU = params.flipU.get();
		if (params.direction.get() == EnumFacing.NORTH || params.direction.get() == EnumFacing.EAST)
			flipU = !flipU;
		face.setTexture(icon, flipU, params.flipV.get(), params.interpolateUV.get());
	}

	/**
	 * Gets the {@link Icon} corresponding to the specified {@link RenderParameters}.<br>
	 * If {@link #block} or {@link #item} is an {@link IIconProvider} and give the right provider for the current context, gets the icon
	 * from that provider.
	 *
	 * @param face the face
	 * @param params the params
	 * @return the icon
	 */
	protected Icon getIcon(Face face, RenderParameters params)
	{
		if (params != null && params.icon.get() != null)
			return params.icon.get();

		IIconProvider iconProvider = getIconProvider(params);
		if (iconProvider instanceof IItemIconProvider && itemStack != null)
			return ((IItemIconProvider) iconProvider).getIcon(itemStack);

		if (iconProvider instanceof IBlockIconProvider && block != null)
		{
			EnumFacing side = params != null ? params.textureSide.get() : EnumFacing.SOUTH;
			if (params != null && shouldRotateIcon(params))
				side = EnumFacingUtils.getRealSide(blockState, side);

			IBlockIconProvider iblockp = (IBlockIconProvider) iconProvider;
			if (renderType == RenderType.BLOCK || renderType == RenderType.TILE_ENTITY)
				return iblockp.getIcon(world, pos, blockState, side);
			else if (renderType == RenderType.ITEM)
				return iblockp.getIcon(itemStack, side);
		}

		return iconProvider != null ? iconProvider.getIcon() : Icon.missing;
	}

	/**
	 * Gets the {@link IIconProvider} either from parameters, the block or the item.
	 *
	 * @return the icon provider
	 */
	protected IIconProvider getIconProvider(RenderParameters params)
	{
		if (params != null && params.iconProvider.get() != null)
			return params.iconProvider.get();

		IIconProvider provider = IComponent.getComponent(IIconProvider.class, item);
		if (provider == null)
			provider = IComponent.getComponent(IIconProvider.class, block);
		return provider;
	}

	protected boolean shouldRotateIcon(RenderParameters params)
	{
		return blockState != null && params.rotateIcon.get();
	}

	/**
	 * Calculates the ambient occlusion for a {@link Vertex} and also applies the side dependent shade.<br>
	 * <b>aoMatrix</b> is the list of block coordinates necessary to compute AO. If it's empty, only the global face shade is applied.<br>
	 * Also, <i>params.colorMultiplier</i> is applied as well.
	 *
	 * @param vertex the vertex
	 * @param number the number
	 * @param params the params
	 * @return the int
	 */
	protected int calcVertexColor(Vertex vertex, int number, RenderParameters params)
	{
		int color = 0xFFFFFF;
		if (params == null)
			return color;

		if (params.usePerVertexColor.get()) //vertex should use their own colors
			color = vertex.getColor();
		else if (params.colorMultiplier.get() != null) //global color multiplier is set
			color = params.colorMultiplier.get();
		else if (block != null) //use block color multiplier
			color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(blockState, world, pos, 0);
		//color = world != null ? block.colorMultiplier(world, pos, 0) : block.getRenderColor(blockState);

		if (drawMode == GL11.GL_LINES) //no AO for lines
			return color;
		if (renderType != RenderType.BLOCK && renderType != RenderType.TILE_ENTITY) //no AO for item/inventories
			return color;

		int[][] aoMatrix = (int[][]) params.aoMatrix.get(number);
		float factor = 1;
		//calculate AO
		if (params.calculateAOColor.get() && aoMatrix != null && Minecraft.isAmbientOcclusionEnabled()
				&& blockState.getLightValue(world, pos) == 0 && params.direction.get() != null)
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
	 * @param params the params
	 * @return the base brightness
	 */
	@SuppressWarnings("deprecation")
	protected int getBaseBrightness(RenderParameters params)
	{
		if (!params.useEnvironmentBrightness.get())
			return params.brightness.get();

		if (block != null)
		{
			if (world != null && blockState.getLightValue(world, pos) != 0)
				return blockState.getLightValue(world, pos) << 4;
			else if (blockState.getLightValue() != 0)
				return blockState.getLightValue() << 4;
		}

		if (renderType == RenderType.ITEM)
			return Utils.getClientPlayer().getBrightnessForRender(getPartialTick());

		//not in world
		if (world == null || block == null)
			return params.brightness.get();

		//no direction, we can only use current block brightness
		if (params.direction.get() == null && block != null)
			return blockState.getPackedLightmapCoords(world, pos);

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
	 * @param number the number
	 * @param params the params
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
		if (!Minecraft.isAmbientOcclusionEnabled() || blockState.getLightValue(world, pos) != 0) // emit light
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
	 * @param pos the pos
	 * @return the block ambient occlusion
	 */
	protected float getBlockAmbientOcclusion(IBlockAccess world, BlockPos pos)
	{
		return world.getBlockState(pos).getAmbientOcclusionLightValue();
	}

	/**
	 * Gets the mix brightness for a block (sky + block source).
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the mixed brightness for block
	 */
	protected int getMixedBrightnessForBlock(IBlockAccess world, BlockPos pos)
	{
		// return world.getLightBrightnessForSkyBlocks(x, y, z, 0);
		return world.getBlockState(pos).getPackedLightmapCoords(world, pos);
	}

	/**
	 * Gets the rendering bounds. If <i>params.useBlockBounds</i> = false, <i>params.renderBounds</i> is used instead of the actual block
	 * bounds.
	 *
	 * @param params the params
	 * @return the render bounds
	 */
	protected AxisAlignedBB getRenderBounds(RenderParameters params)
	{
		if (block == null)
		{
			if (params != null && !params.useBlockBounds.get())
				return params.renderBounds.get();
			return AABBUtils.identity();
		}

		if (block instanceof IBoundingBox)
			return ((IBoundingBox) block).getBoundingBox(world, pos, blockState, BoundingBoxType.RENDER);

		if (world != null)
			return blockState.getBoundingBox(world, pos);

		return AABBUtils.identity();
	}

	public static float getPartialTick()
	{
		return Minecraft.getMinecraft().timer.elapsedPartialTicks;
	}

	/**
	 * Gets the current {@link BlockRenderLayer}.
	 *
	 * @return the render layer
	 */
	public static BlockRenderLayer getRenderLayer()
	{
		return MinecraftForgeClient.getRenderLayer();
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the {@link Block}.<br>
	 *
	 * @param block the block
	 */
	public void registerFor(Block block)
	{
		MalisisRegistry.registerBlockRenderer(block, this);

	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the {@link Item}.<br>
	 *
	 * @param item the item
	 */
	public void registerFor(Item item)
	{
		MalisisRegistry.registerItemRenderer(item, this);
	}

	/**
	 * Registers this {@link MalisisRenderer} to be used for rendering the {@link TileEntity}.
	 *
	 * @param clazz the clazz
	 */
	public void registerFor(Class<? extends T> clazz)
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

	public static int colorMultiplier(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, 0);
	}
}
