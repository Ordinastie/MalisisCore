package net.malisis.core.renderer.element;

import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Defines a list of parameters available for rendering. Block Level means the
 * parameter will be used for each face to draw Face Level means the parameter
 * need to be specified for each face
 * 
 * @author Malisis
 */
public class RenderParameters
{
	public static RenderParameters defaultParameters = new RenderParameters();
	static
	{
		// block
		defaultParameters.renderAllFaces = false;
		defaultParameters.useBlockBounds = true;
		defaultParameters.renderBounds = new double[][] { { 0, 0, 0 }, { 1, 1, 1 } };
		defaultParameters.vertexPositionRelativeToRenderBounds = true;
		defaultParameters.scale = 1.0F;
		defaultParameters.useTexture = true;
		defaultParameters.interpolateUV = true;
		defaultParameters.calculateAOColor = true;
		defaultParameters.calculateBrightness = true;
		defaultParameters.usePerVertexColor = false;
		defaultParameters.usePerVertexAlpha = false;
		defaultParameters.useBlockBrightness = true;
		defaultParameters.useNormals = false;
		defaultParameters.colorMultiplier = 0xFFFFFF;
		defaultParameters.colorFactor = 1.0F;
		defaultParameters.dynLights = true;
		defaultParameters.brightness = 15728640;
		defaultParameters.alpha = 255;
		defaultParameters.uvFactor = new float[][] { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } };

		// face
		defaultParameters.flipU = false;
		defaultParameters.flipV = false;
	}

	/**
	 * Defines whether to render all faces even if shoudSideBeRendered is false
	 */
	public Boolean renderAllFaces;
	/**
	 * Defines whether to use the block bounding box instead of renderBounds
	 * (Block Level)
	 */
	public Boolean useBlockBounds;
	/**
	 * Defines the rendering bounds to limit the vertex inside (Block Level)
	 */
	public double[][] renderBounds;
	/**
	 * Defines whether the vertex coordinates are relatives to the bounding box.
	 * This means 0 = min_, 1 = max_
	 */
	public Boolean vertexPositionRelativeToRenderBounds;
	/**
	 * Defines the scale factor to apply
	 */
	public Float scale;
	/**
	 * Defines an icon to use for the block/face (will override textureSide)
	 * (Block Level)
	 */
	public IIcon icon;
	/**
	 * Defines whether to use a texture
	 */
	public Boolean useTexture;
	/**
	 * Defines whether to calculate interpolated textures coordinates depending
	 * on block bounds (Block Level)
	 */
	public Boolean interpolateUV;
	/**
	 * Defines whether to calculate ambient occlusion color or not (Block Level)
	 */
	public Boolean calculateAOColor;
	/**
	 * Defines whether to calculate brightness or not (Block Level)
	 */
	public Boolean calculateBrightness;
	/**
	 * Define whether to override each vertex color. If true,
	 * FaceParams.colorMultiplier will be used instead (Block Level)
	 */
	public Boolean usePerVertexColor;
	/**
	 * Define whether to override each vertex alpha. If true, FaceParams.alpha
	 * will be used instead (Block Level)
	 */
	public Boolean usePerVertexAlpha;
	/**
	 * Defines whether to use the block mixBlockBrightness (if false,
	 * RenderParameters.brightness will be used) (Block Level)
	 */
	public Boolean useBlockBrightness;
	/**
	 * Defines whether to use the defined normals
	 */
	public Boolean useNormals;
	/**
	 * Defines the color to apply to the face (useful for grass and leaves)
	 * usePerVertexColor must be false (Block Level)
	 */
	public Integer colorMultiplier;
	/**
	 * Defines the color factor for the face (Block Level)
	 */
	public Float colorFactor;
	/**
	 * Defines whether to calculate dynamic lighting for the face
	 */
	public Boolean dynLights;
	/**
	 * Defines brightness of the face (only used if useBlockBrightness = false)
	 * (Block Level)
	 */
	public Integer brightness; // 983055 - 15728640
	/**
	 * Defines alpha transparency for the face (GL11.GL_BLEND needs to be set
	 * before). usePerVertexAlpha must be false (Block Level)
	 */
	public Integer alpha;
	/**
	 * Defines the general direction of a face. Used for normals, and offset for
	 * AO and brightness calculation (Face Level)
	 */
	public ForgeDirection direction;
	/**
	 * Defines which direction will be used to get the block icon. If
	 * ForgeDirection.UNKNOWN, no texture will be used (Face Level)
	 */
	public ForgeDirection textureSide;
	/**
	 * Defines which block to take into account for AO calculation (Face Level)
	 */
	public int[][][] aoMatrix;
	/**
	 * Defines UV factor for each vertex (Face Level)
	 */
	public float[][] uvFactor;
	/**
	 * Defines whether to flip the texture on the U coordinates (Face Level)
	 */
	public Boolean flipU;
	/**
	 * Defines whether to flip the texture on the U coordinates (Face Level)
	 */
	public Boolean flipV;

	public RenderParameters()
	{}

	public RenderParameters(RenderParameters params)
	{
		if (params == null)
			params = defaultParameters;

		renderAllFaces = params.renderAllFaces;
		useBlockBounds = params.useBlockBounds;
		vertexPositionRelativeToRenderBounds = params.vertexPositionRelativeToRenderBounds;
		scale = params.scale;
		icon = params.icon;
		useTexture = params.useTexture;
		interpolateUV = params.interpolateUV;
		calculateAOColor = params.calculateAOColor;
		calculateBrightness = params.calculateBrightness;
		usePerVertexColor = params.usePerVertexColor;
		usePerVertexAlpha = params.usePerVertexAlpha;
		useBlockBrightness = params.useBlockBrightness;
		useNormals = params.useNormals;
		colorMultiplier = params.colorMultiplier;
		colorFactor = params.colorFactor;
		dynLights = params.dynLights;
		brightness = params.brightness;
		alpha = params.alpha;
		direction = params.direction;
		textureSide = params.textureSide;
		aoMatrix = params.aoMatrix;
		flipU = params.flipU;
		flipV = params.flipV;

		if (params.uvFactor != null)
		{
			uvFactor = new float[params.uvFactor.length][];
			for (int i = 0; i < uvFactor.length; i++)
			{
				if (params.uvFactor[i] != null)
					uvFactor[i] = params.uvFactor[i].clone();
			}
		}

		if (params.renderBounds != null)
		{
			renderBounds = new double[2][];
			renderBounds[0] = params.renderBounds[0].clone();
			renderBounds[1] = params.renderBounds[1].clone();
		}
	}

	public static RenderParameters merge(RenderParameters rp1, RenderParameters rp2)
	{
		if (rp2 == null)
			return new RenderParameters(rp1);
		if (rp2 == defaultParameters)
			return Default();

		RenderParameters newRp = merge(defaultParameters, rp1);

		if (rp2.renderAllFaces != null)
			newRp.renderAllFaces = rp2.renderAllFaces;
		if (rp2.useBlockBounds != null)
			newRp.useBlockBounds = rp2.useBlockBounds;
		if (rp2.vertexPositionRelativeToRenderBounds != null)
			newRp.vertexPositionRelativeToRenderBounds = rp2.vertexPositionRelativeToRenderBounds;
		if (rp2.scale != null)
			newRp.scale = rp2.scale;
		if (rp2.icon != null)
			newRp.icon = rp2.icon;
		if(rp2.useTexture != null)
			newRp.useTexture = rp2.useTexture;
		if (rp2.interpolateUV != null)
			newRp.interpolateUV = rp2.interpolateUV;
		if (rp2.calculateAOColor != null)
			newRp.calculateAOColor = rp2.calculateAOColor;
		if (rp2.calculateBrightness != null)
			newRp.calculateBrightness = rp2.calculateBrightness;
		if (rp2.usePerVertexColor != null)
			newRp.usePerVertexColor = rp2.usePerVertexColor;
		if (rp2.usePerVertexAlpha != null)
			newRp.usePerVertexAlpha = rp2.usePerVertexAlpha;
		if (rp2.dynLights != null)
			newRp.dynLights = rp2.dynLights;
		if (rp2.useBlockBrightness != null)
			newRp.useBlockBrightness = rp2.useBlockBrightness;
		if (rp2.useNormals != null)
			newRp.useNormals = rp2.useNormals;
		if (rp2.colorMultiplier != null)
			newRp.colorMultiplier = rp2.colorMultiplier;
		if (rp2.colorFactor != null)
			newRp.colorFactor = rp2.colorFactor;
		if (rp2.brightness != null)
			newRp.brightness = rp2.brightness;
		if (rp2.alpha != null)
			newRp.alpha = rp2.alpha;
		if (rp2.direction != null)
			newRp.direction = rp2.direction;
		if (rp2.textureSide != null)
			newRp.textureSide = rp2.textureSide;
		if (rp2.aoMatrix != null)
			newRp.aoMatrix = rp2.aoMatrix;
		if (rp2.flipU != null)
			newRp.flipU = rp2.flipU;
		if (rp2.flipV != null)
			newRp.flipV = rp2.flipV;

		if (rp2.uvFactor != null)
		{
			newRp.uvFactor = new float[rp2.uvFactor.length][];
			for (int i = 0; i < newRp.uvFactor.length; i++)
			{
				if (rp2.uvFactor[i] != null)
					newRp.uvFactor[i] = rp2.uvFactor[i].clone();
			}
		}
		if (rp2.renderBounds != null)
		{
			newRp.renderBounds = new double[2][];
			newRp.renderBounds[0] = rp2.renderBounds[0].clone();
			newRp.renderBounds[1] = rp2.renderBounds[1].clone();
		}

		return newRp;
	}

	public static RenderParameters Default()
	{
		return new RenderParameters(defaultParameters);
	}
}
