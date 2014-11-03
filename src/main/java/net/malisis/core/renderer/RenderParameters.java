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

import java.util.LinkedList;
import java.util.List;

import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class RenderParameters implements ITransformable.Color, ITransformable.Alpha, ITransformable.Brightness
{
	private List<Parameter> listParams = new LinkedList<>();
	/**
	 * Defines whether to render all faces even if shoudSideBeRendered is false
	 */
	public Parameter<Boolean> renderAllFaces = new Parameter<>(false);
	/**
	 * Defines whether to use the block bounding box instead of renderBounds (Block Level)
	 */
	public Parameter<Boolean> useBlockBounds = new Parameter<>(true);
	/**
	 * Defines the rendering bounds to limit the vertex inside (Block Level)
	 */
	public Parameter<AxisAlignedBB> renderBounds = new Parameter<>(null);
	/**
	 * Defines whether the vertex coordinates are relatives to the bounding box. This means 0 = min_, 1 = max_
	 */
	public Parameter<Boolean> vertexPositionRelativeToRenderBounds = new Parameter<>(true);
	/**
	 * Define whether a custom texture for drawing. It disable default icon behavior. A ResourceLocation need to be bound.
	 */
	public Parameter<Boolean> useCustomTexture = new Parameter<>(false);
	/**
	 * Define whether to apply texture UV for the shape (Block level)
	 */
	public Parameter<Boolean> applyTexture = new Parameter<>(true);
	/**
	 * Defines an icon to use for the block/face (will override textureSide) (Block Level)
	 */
	public Parameter<IIcon> icon = new Parameter<>(null);
	/**
	 * Defines whether to use block.getIcon(world, x, y, z, side) instead of block.getIcon(side, metadata) to get the IIcon
	 */
	public Parameter<Boolean> useWorldSensitiveIcon = new Parameter<Boolean>(true);
	/**
	 * Defines whether to use a texture (will call addVertexWithUV instead of addVertex)
	 */
	public Parameter<Boolean> useTexture = new Parameter<>(true);
	/**
	 * Defines whether to calculate interpolated textures coordinates depending on block bounds (Block Level)
	 */
	public Parameter<Boolean> interpolateUV = new Parameter<>(true);
	/**
	 * Defines whether to calculate ambient occlusion color or not (Block Level)
	 */
	public Parameter<Boolean> calculateAOColor = new Parameter<>(true);
	/**
	 * Defines whether to calculate brightness or not (Block Level)
	 */
	public Parameter<Boolean> calculateBrightness = new Parameter<>(true);
	/**
	 * Define whether to override each vertex color. If false, FaceParams.colorMultiplier will be used instead (Block Level)
	 */
	public Parameter<Boolean> usePerVertexColor = new Parameter<>(false);
	/**
	 * Define whether to override each vertex alpha. If false, FaceParams.alpha will be used instead (Block Level)
	 */
	public Parameter<Boolean> usePerVertexAlpha = new Parameter<>(false);
	/**
	 * Define whether to override each vertex brightness. If false, FaceParams.brightness will be used instead (Block Level)
	 */
	public Parameter<Boolean> usePerVertexBrightness = new Parameter<>(false);
	/**
	 * Defines whether to use the block mixBlockBrightness (if false, RenderParameters.brightness will be used) (Block Level)
	 */
	public Parameter<Boolean> useBlockBrightness = new Parameter<>(true);
	/**
	 * Defines whether to use the defined normals
	 */
	public Parameter<Boolean> useNormals = new Parameter<>(false);
	/**
	 * Defines the color to apply to the face (useful for grass and leaves) usePerVertexColor must be false (Block Level).<br>
	 * Overrides the Block.colorMultiplier() and Block.getRenderColor()
	 */
	public Parameter<Integer> colorMultiplier = new Parameter<>(null);
	/**
	 * Defines the color factor for the face (Block Level). Used for shading the faces depending on their orientation : <br>
	 * - NORTH/SOUTH : 0.8<br>
	 * - EAST/WEST : 0.6<br>
	 * - TOP : 1<br>
	 * - BOTTOM : 0.5<br>
	 */
	public Parameter<Float> colorFactor = new Parameter<>(1.0F);
	/**
	 * Defines brightness of the face (only used if useBlockBrightness = false) (Block Level)
	 */
	public Parameter<Integer> brightness = new Parameter<>(15728640); // 983055 - 15728640
	/**
	 * Defines alpha transparency for the face (GL11.GL_BLEND needs to be set before). usePerVertexAlpha must be false (Block Level)
	 */
	public Parameter<Integer> alpha = new Parameter<>(255);
	/**
	 * Defines the general direction of a face. Used for normals, and offset for AO and brightness calculation (Face Level)
	 */
	public Parameter<ForgeDirection> direction = new Parameter<>(null);
	/**
	 * Defines which direction will be used to get the block icon. If ForgeDirection.UNKNOWN, no texture will be used (Face Level)
	 */
	public Parameter<ForgeDirection> textureSide = new Parameter<>(null);
	/**
	 * Defines which block to take into account for AO calculation (Face Level)
	 */
	public Parameter<int[][][]> aoMatrix = new Parameter<>(null);
	/**
	 * Defines whether to flip the texture on the U coordinates (Face Level)
	 */
	public Parameter<Boolean> flipU = new Parameter<>(false);
	/**
	 * Defines whether to flip the texture on the U coordinates (Face Level)
	 */
	public Parameter<Boolean> flipV = new Parameter<>(false);

	public RenderParameters()
	{
		listParams.add(renderAllFaces);
		listParams.add(useBlockBounds);
		listParams.add(renderBounds);
		listParams.add(vertexPositionRelativeToRenderBounds);
		listParams.add(useCustomTexture);
		listParams.add(applyTexture);
		listParams.add(icon);
		listParams.add(useWorldSensitiveIcon);
		listParams.add(useTexture);
		listParams.add(interpolateUV);
		listParams.add(calculateAOColor);
		listParams.add(calculateBrightness);
		listParams.add(usePerVertexColor);
		listParams.add(usePerVertexAlpha);
		listParams.add(usePerVertexBrightness);
		listParams.add(useBlockBrightness);
		listParams.add(useNormals);
		listParams.add(colorMultiplier);
		listParams.add(colorFactor);
		listParams.add(brightness);
		listParams.add(alpha);
		listParams.add(direction);
		listParams.add(textureSide);
		listParams.add(aoMatrix);
		listParams.add(flipU);
		listParams.add(flipV);
	}

	public RenderParameters(RenderParameters params)
	{
		this();
		merge(params);
	}

	private Parameter getParameter(int index)
	{
		if (index < 0 || index >= listParams.size())
			return null;
		return listParams.get(index);
	}

	public void reset()
	{
		for (Parameter param : listParams)
			param.reset();
	}

	public void merge(RenderParameters params)
	{
		if (params == null)
			return;

		for (int i = 0; i < listParams.size(); i++)
			getParameter(i).merge(params.getParameter(i));
	}

	public static RenderParameters merge(RenderParameters rp1, RenderParameters rp2)
	{
		RenderParameters rp = new RenderParameters();
		rp.merge(rp1);
		rp.merge(rp2);

		return rp;
	}

	@Override
	public void setAlpha(int alpha)
	{
		this.alpha.set(alpha);
	}

	@Override
	public void setColor(int color)
	{
		this.colorMultiplier.set(color);
	}

	@Override
	public void setBrightness(int brightness)
	{
		this.brightness.set(brightness);
	}
}
