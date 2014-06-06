package net.malisis.core.renderer;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class BaseRenderer extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer
{
	/**
	 * Set rendering for world
	 */
	public static final int TYPE_WORLD = 1;
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
	/**
	 * Set rendering as polygons
	 */
	public static final int MODE_POLYGONS = 0;
	/**
	 * Set rendering as lines
	 */
	public static final int MODE_LINES = 1;

	protected Tessellator t = Tessellator.instance;
	protected IBlockAccess world;
	protected RenderBlocks renderBlocks;

	/**
	 * Block to render
	 */
	protected Block block;
	/**
	 * Metadata of the block to render
	 */
	protected int blockMetadata;
	/**
	 * Position of the block
	 */
	protected int x, y, z;
	/**
	 * Type of rendering : <code>TYPE_WORLD</code> or <code>TYPE_INVENTORY</code>
	 */
	protected int typeRender;
	/**
	 * Mode of rendering
	 */
	protected int modeRender;

	/**
	 * Are render coordinates already shifted (<code>TYPE_WORLD</code> only)
	 */
	protected boolean isShifted = false;

	/**
	 * The shape to render
	 */
	protected Shape shape;
	/**
	 * Current face being rendered
	 */
	protected Face face;
	/**
	 * Global shape parameters
	 */
	protected RenderParameters shapeParams;
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
	public float partialTick = 0;

	/**
	 * Used for TESR
	 */
	public BaseRenderer()
	{
		this.t = Tessellator.instance;
	}

	// #region set()
	public BaseRenderer reset()
	{
		this.typeRender = 0;
		this.modeRender = 0;
		this.world = null;
		this.block = null;
		this.blockMetadata = 0;
		this.x = 0;
		this.y = 0;
		this.z = 0;
		this.overrideTexture = null;
		return this;
	}

	public BaseRenderer set(IBlockAccess world, Block block, int x, int y, int z, int metadata)
	{
		this.world = world;
		this.block = block;
		this.blockMetadata = metadata;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public BaseRenderer set(Block block)
	{
		return set(world, block, x, y, z, blockMetadata);
	}

	public BaseRenderer set(int blockMetadata)
	{
		return set(world, block, x, y, z, blockMetadata);
	}

	public BaseRenderer set(Block block, int blockMetadata)
	{
		return set(world, block, x, y, z, blockMetadata);
	}

	public BaseRenderer set(int x, int y, int z)
	{
		return set(world, block, x, y, z, blockMetadata);
	}

	// #end

	// #region ISBRH
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		renderInventoryBlock(block, metadata, modelId, renderer, null);
	}

	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer, RenderParameters rp)
	{
		renderBlocks = renderer;
		set(block, metadata);
		prepare(TYPE_ISBRH_INVENTORY);
		render();
		clean();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		renderBlocks = renderer;
		set(world, block, x, y, z, world.getBlockMetadata(x, y, z));
		prepare(TYPE_WORLD);
		if(renderer.hasOverrideBlockTexture())
			overrideTexture = renderer.overrideBlockTexture;
		render();
		clean();
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
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
		renderItem(type, item, null, data);
	}

	public void renderItem(ItemRenderType type, ItemStack item, RenderParameters rp, Object... data)
	{
		if (item.getItem() instanceof ItemBlock)
			set(Block.getBlockFromItem(item.getItem()));

		prepare(TYPE_ITEM_INVENTORY);
		render();
		clean();
	}

	// #end IItemRenderer

	// #region TESR
	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f)
	{
		renderTileEntityAt(te, x, y, z, f, null);
	}

	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f, RenderParameters rp)
	{
		set(te.getWorldObj(), te.getBlockType(), te.xCoord, te.yCoord, te.zCoord, te.getBlockMetadata());
		prepare(TYPE_TESR_WORLD, x, y, z);
		partialTick = f;
		tileEntity = te;
		render();
		clean();
	}

	// #end TESR

	// #region prepare()
	public void prepare(int typeRender, int modeRender)
	{
		this.modeRender = modeRender;
		prepare(typeRender);
	}

	public void prepare(int typeRender, double... data)
	{
		this.typeRender = typeRender;
		if (typeRender == TYPE_WORLD)
		{
			tessellatorShift();
		}
		else if (typeRender == TYPE_ISBRH_INVENTORY)
		{
			GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
			t.startDrawingQuads();
		}
		else if (typeRender == TYPE_ITEM_INVENTORY)
		{
			t.startDrawingQuads();
		}
		else if (typeRender == TYPE_TESR_WORLD)
		{
			RenderHelper.disableStandardItemLighting();
			GL11.glPushMatrix();
			GL11.glTranslated(data[0], data[1], data[2]);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			bindTexture(TextureMap.locationBlocksTexture);

			t = Tessellator.instance;
			t.startDrawingQuads();
		}
	}

	public void next()
	{
		t.draw();
		t.startDrawingQuads();
	}

	public void clean()
	{
		if (typeRender == TYPE_WORLD)
		{
			tessellatorUnshift();
		}
		else if (typeRender == TYPE_ISBRH_INVENTORY)
		{
			t.draw();
			GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		}
		else if (typeRender == TYPE_ITEM_INVENTORY)
		{
			t.draw();
		}
		else if (typeRender == TYPE_TESR_WORLD)
		{
			t.draw();
			GL11.glPopMatrix();
		}
		reset();
	}

	public void tessellatorShift()
	{
		if (isShifted)
			return;

		isShifted = true;
		t.addTranslation(x, y, z);
	}

	public void tessellatorUnshift()
	{
		if (!isShifted)
			return;

		isShifted = false;
		t.addTranslation(-x, -y, -z);
	}

	// #end prepare()

	/**
	 * Render the block using the default Minecraft rendering system
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
		renderer.setRenderBoundsFromBlock((Block) block);
		renderer.renderStandardBlock((Block) block, x, y, z);
		if (b)
			tessellatorShift();
	}

	public void render()
	{

	}

	/**
	 * Draw a shape with its own parameters
	 * 
	 * @param shape
	 */
	public void drawShape(Shape shape)
	{
		drawShape(shape, null);
	}

	/**
	 * Draw a shape with specified parameters
	 * 
	 * @param shape
	 * @param rp
	 */
	public void drawShape(Shape s, RenderParameters rp)
	{
		if(s == null)
			return;
		
		shape = s;
		shapeParams = new RenderParameters(rp);
		s.applyMatrix();
		for (Face face : s.getFaces())
		{
			if (shouldRenderFace(face))
			{
				drawFace(face, face.getParameters());
			}
		}
	}

	/**
	 * Draw a face with its own parameters
	 * 
	 * @param face
	 */
	public void drawFace(Face face)
	{
		drawFace(face, face.getParameters());
	}

	/**
	 * Draw a face with specified parameters.
	 * 
	 * @param f
	 * @param rp
	 */
	protected void drawFace(Face f, RenderParameters rp)
	{
		face = f;
		params = RenderParameters.merge(shapeParams, rp);

		// icon
		IIcon icon = params.icon;
		if(params.useCustomTexture)
			icon = new BaseIcon();
		else if(overrideTexture != null)
			icon = overrideTexture;
		else if (block != null && icon == null)
		{
			int side = 0;
			if (params.textureSide != null)
				side = params.textureSide.ordinal();
			icon = block.getIcon(side, blockMetadata);
		}

		if (params.interpolateUV && params.textureSide != null)
			interpolateUV(getRenderBounds());

		// texture
		if (icon != null)
			face.setTexture(icon, params.uvFactor, params.flipU, params.flipV);

		if (typeRender == TYPE_ITEM_INVENTORY || typeRender == TYPE_ISBRH_INVENTORY || params.useNormals)
			t.setNormal(params.direction.offsetX, params.direction.offsetY, params.direction.offsetZ);

		baseBrightness = getBaseBrightness();

		// vertex position
		if (params.vertexPositionRelativeToRenderBounds)
			calcVertexesPosition(getRenderBounds());

		drawVertexes(face.getVertexes());
	}

	/***
	 * Draw an array of vertexes (usually <i>Face.getVertexes()</i>)
	 * 
	 * @param vertexes
	 */
	protected void drawVertexes(Vertex[] vertexes)
	{
		for (int i = 0; i < vertexes.length; i++)
		{
			drawVertex(vertexes[i], i);
			if (modeRender == MODE_LINES)
			{
				drawVertex(vertexes[i == vertexes.length - 1 ? 0 : i + 1], i);
			}
		}
	}

	/**
	 * Draw a single vertex
	 * 
	 * @param vertex
	 */
	protected void drawVertex(Vertex vertex, int count)
	{
		// brightness
		int brightness = baseBrightness;
		if (modeRender == MODE_POLYGONS && typeRender == TYPE_WORLD && params.calculateBrightness)
			brightness = calcVertexBrightness(vertex, params.aoMatrix[count]);
		vertex.setBrightness(brightness);

		// color
		int color = params.colorMultiplier;
		if (modeRender == MODE_POLYGONS && typeRender == TYPE_WORLD && params.calculateAOColor)
			color = calcVertexColor(vertex, params.aoMatrix[count]);
		vertex.setColor(color);

		// alpha
		if (!params.usePerVertexAlpha)
			vertex.setAlpha(params.alpha);

		t.setColorRGBA_I(vertex.getColor(), vertex.getAlpha());
		t.setBrightness(vertex.getBrightness());

		if (modeRender == MODE_POLYGONS && params.useTexture)
			t.addVertexWithUV(vertex.getX(), vertex.getY(), vertex.getZ(), vertex.getU(), vertex.getV());
		else
			t.addVertex(vertex.getX(), vertex.getY(), vertex.getZ());
	}

	/**
	 * Check if <b>side</b> should be rendered
	 * 
	 * @param side
	 */
	protected boolean shouldRenderFace(Face face)
	{
		if (typeRender != TYPE_WORLD || world == null || block == null)
			return true;
		if (shapeParams != null && shapeParams.renderAllFaces != null && shapeParams.renderAllFaces)
			return true;
		RenderParameters p = face.getParameters();
		if (p == null || p.direction == null)
			return true;

		boolean b = block.shouldSideBeRendered(world, x + p.direction.offsetX, y + p.direction.offsetY, z + p.direction.offsetZ,
				p.direction.ordinal());
		return b;
	}

	protected void interpolateUV(double[][] bounds)
	{
		float x = (float) bounds[0][0];
		float X = (float) bounds[1][0];
		float Y = 1 - (float) bounds[0][1];
		float y = 1 - (float) bounds[1][1];
		float z = (float) bounds[0][2];
		float Z = (float) bounds[1][2];

		for (int i = 0; i < params.uvFactor.length; i++)
		{
			float[] uv = params.uvFactor[i];
			switch (params.textureSide)
			{
				case NORTH:
					uv[0] = limitUV(uv[0], 1 - X, 1 - x);
					uv[1] = limitUV(uv[1], y, Y);
					break;
				case SOUTH:
					uv[0] = limitUV(uv[0], x, X);
					uv[1] = limitUV(uv[1], y, Y);
					break;
				case EAST:
					uv[0] = limitUV(uv[0], 1 - Z, 1 - z);
					uv[1] = limitUV(uv[1], y, Y);
					break;
				case WEST:
					uv[0] = limitUV(uv[0], z, Z);
					uv[1] = limitUV(uv[1], y, Y);
					break;
				case UP:
				case DOWN:
					uv[0] = limitUV(uv[0], x, X);
					uv[1] = limitUV(uv[1], z, Z);
				default:
					break;
			}
			params.uvFactor[i] = uv;
		}
	}

	/**
	 * Limit a value between <b>min</b> and <b>max</b>
	 * 
	 * @param f
	 * @param min
	 * @param max
	 */
	protected float limitUV(float f, float min, float max)
	{
		if (max - min < 1)
			return Math.max(Math.min(f, max), min);
		else
			return f;
	}

	/**
	 * Calculate the ambient occlusion for a vertex and also apply the side dependent shade.<br />
	 * <b>aoMatrix</b> is the list of block coordinates necessary to compute AO. If it's empty, only the global face shade is applied.<br />
	 * Also, <i>params.colorMultiplier</i> is applied as well.
	 * 
	 * @param vertex
	 * @param aoMatrix
	 */
	protected int calcVertexColor(Vertex vertex, int[][] aoMatrix)
	{
		float factor = getBlockAmbientOcclusion(world, x + params.direction.offsetX, y + params.direction.offsetY, z
				+ params.direction.offsetZ);

		for (int i = 0; i < aoMatrix.length; i++)
			factor += getBlockAmbientOcclusion(world, x + aoMatrix[i][0], y + aoMatrix[i][1], z + aoMatrix[i][2]);

		factor *= params.colorFactor;

		int color = params.usePerVertexColor ? vertex.getColor() : params.colorMultiplier;

		int r = (int) ((color >> 16 & 255) * factor / (aoMatrix.length + 1));
		int g = (int) ((color >> 8 & 255) * factor / (aoMatrix.length + 1));
		int b = (int) ((color & 255) * factor / (aoMatrix.length + 1));

		color = r << 16 | g << 8 | b;

		return color;
	}

	/**
	 * Get the base brightness for the current face.<br />
	 * If <i>params.useBlockBrightness</i> = false, <i>params.brightness</i>. Else, the brightness is determined base on
	 * <i>params.offset</i> and <i>getBlockBounds()</i>
	 */
	protected int getBaseBrightness()
	{
		if ((typeRender != TYPE_WORLD && typeRender != TYPE_TESR_WORLD) || world == null || !params.useBlockBrightness || params.direction == null)
			return params.brightness;

		double[][] bounds = getRenderBounds();
		int ox = x + params.direction.offsetX;
		int oy = y + params.direction.offsetY;
		int oz = z + params.direction.offsetZ;

		if (params.direction == ForgeDirection.WEST && bounds[0][0] > 0)
			ox += 1;
		else if (params.direction == ForgeDirection.EAST && bounds[1][0] < 1)
			ox -= 1;
		else if (params.direction == ForgeDirection.NORTH && bounds[0][1] > 0)
			oz += 1;
		else if (params.direction == ForgeDirection.SOUTH && bounds[1][1] < 1)
			oz -= 1;
		else if (params.direction == ForgeDirection.DOWN && bounds[0][2] > 0)
			oy += 1;
		else if (params.direction == ForgeDirection.UP && bounds[1][2] < 1)
			oy -= 1;

		return getMixedBrightnessForBlock(world, ox, oy, oz);
	}

	/**
	 * Calculate the ambient occlusion brightness for a vertex. <b>aoMatrix</b> is the list of block coordinates necessary to compute AO.
	 * Only first 3 blocks are used.<br />
	 * 
	 * @param vertex
	 * @param baseBrightness
	 * @param aoMatrix
	 */
	protected int calcVertexBrightness(Vertex vertex, int[][] aoMatrix)
	{
		int[] b = new int[Math.max(3, aoMatrix.length)];

		for (int i = 0; i < aoMatrix.length; i++)
			b[i] += getMixedBrightnessForBlock(world, x + aoMatrix[i][0], y + aoMatrix[i][1], z + aoMatrix[i][2]);

		int brightness = getAoBrightness(b[0], b[1], b[2], baseBrightness);

		return brightness;
	}

	/**
	 * Do the actual brightness calculation (copied from net.minecraft.client.renderer.BlocksRenderer.java)
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
	 * Get the block ambient occlusion value. Contrary to base Minecraft code, it's the actual block at the <b>x</b>, <b>y</b> and <b>z</b>
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
	 * Get the mix brightness for a block (sky + block source) TODO: handle block light value for light emitting block sources (as base
	 * Minecraft does)
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
	 * Get the rendering bounds. If <i>params.useBlockBounds</i> = false, <i>params.renderBounds</i> is used instead of the actual block
	 * bounds.
	 * 
	 * @return
	 */
	protected double[][] getRenderBounds()
	{
		if (block == null || !params.useBlockBounds)
			return params.renderBounds;

		if (world != null)
			block.setBlockBoundsBasedOnState(world, x, y, z);
		return new double[][] { { block.getBlockBoundsMinX(), block.getBlockBoundsMinY(), block.getBlockBoundsMinZ() },
				{ block.getBlockBoundsMaxX(), block.getBlockBoundsMaxY(), block.getBlockBoundsMaxZ() } };
	}

	/**
	 * Modify the vertex coordinates relative to the bounds specified.<br />
	 * Eg : if x = 0.5, minX = 1, maxX = 3, x becomes 2
	 * 
	 * @param bounds
	 */
	protected void calcVertexesPosition(double[][] bounds)
	{
		for (Vertex v : face.getVertexes())
			v.interpolateCoord(bounds);
	}

	public static <T extends BaseRenderer> T create(Class<T> clazz, IBaseRendering...blocks)
	{
		T r = create(clazz);
		for(IBaseRendering b : blocks)
			b.setRenderId(r.getRenderId());
		return r;
	}

	public static <T extends BaseRenderer> T create(Class<T> clazz)
	{
		try
		{
			T r = clazz.newInstance();
			int nextId = RenderingRegistry.getNextAvailableRenderId();
			clazz.getField("renderId").set(null, nextId);

			return r;
		}
		catch (ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}

	}

	@Override
	public int getRenderId()
	{
		try
		{
			return (int) (this.getClass().getField("renderId")).get(null);
		}
		catch (ReflectiveOperationException e)
		{
			return -1;
		}
	}

	private class BaseIcon implements IIcon
	{
		@Override
		public int getIconWidth() {  return 0; }

		@Override
		public int getIconHeight() { return 0; }
		
		@Override
		public float getMinU() { return 0; }
		
		@Override
		public float getMaxU() { return 1; }

		@Override
		public float getInterpolatedU(double var1) { return (float) var1; }

		@Override
		public float getMinV() { return 0; }

		@Override
		public float getMaxV() { return 1; }

		@Override
		public float getInterpolatedV(double var1) { return (float) var1; }

		@Override
		public String getIconName() { return null; }

	}
}
