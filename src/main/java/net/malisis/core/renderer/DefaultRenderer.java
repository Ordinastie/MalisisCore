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

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.SouthFace;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.renderer.model.loader.TextureModelLoader;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.model.TRSRTransformation;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class DefaultRenderer
{
	public static MalisisRenderer nullRender = new Null();
	public static MalisisRenderer block = new Block();
	public static MalisisRenderer item = new Item();

	public static class Null extends MalisisRenderer
	{
		@Override
		public void render()
		{

		}
	}

	public static class Block extends MalisisRenderer
	{
		//"thirdperson": {
		//    "rotation": [ 10, -45, 170 ],
		//    "translation": [ 0, 1.5, -2.75 ] * 0.0625,
		//    "scale": [ 0.375, 0.375, 0.375 ]
		//}
		private Matrix4f defaultTransform = new TRSRTransformation(null, TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, 180, 0)),
				null, null).getMatrix();
		private Matrix4f thirdPerson = new TRSRTransformation(new Vector3f(0, 0.09375F, -0.171875F),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(10, -45, 170)), new Vector3f(0.375F, 0.375F, 0.375F),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, 180, 0))).getMatrix();
		private Shape shape = new Cube();

		@Override
		public boolean isGui3d()
		{
			return true;
		}

		@Override
		public Matrix4f getTransform(ItemCameraTransforms.TransformType tranformType)
		{
			return tranformType == TransformType.THIRD_PERSON ? thirdPerson : defaultTransform;
		}

		@Override
		public void render()
		{
			AxisAlignedBB[] aabbs = ((MalisisBlock) block).getRenderBoundingBox(world, pos, blockState);

			for (AxisAlignedBB aabb : aabbs)
			{
				if (aabb != null)
				{
					shape.resetState().limit(aabb);
					drawShape(shape);
				}
			}
		}
	}

	public static class Item extends MalisisRenderer
	{
		//"thirdperson": {
		//    "rotation": [ -90, 0, 0 ],
		//    "translation": [ 0, 1, -3 ],
		//    "scale": [ 0.55, 0.55, 0.55 ]
		//}
		//"firstperson": {
		//    "rotation": [ 0, -135, 25 ],
		//    "translation": [ 0, 4, 2 ],
		//    "scale": [ 1.7, 1.7, 1.7 ]
		//}
		private Matrix4f thirdPerson = new TRSRTransformation(new Vector3f(0.01F, 0.065F, -0.195F),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(-90, 0, 0)), new Vector3f(0.55F, 0.55F, 0.55F), null).getMatrix();
		private Matrix4f firstPerson = new TRSRTransformation(new Vector3f(0, 0.280F, 0.14F),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, -135, 25)), new Vector3f(1.7F, 1.7F, 1.7F), null).getMatrix();
		private Shape gui;
		private Map<MalisisIcon, MalisisModel> itemModels = new HashMap<>();

		@Override
		public void initialize()
		{
			gui = new Shape(new SouthFace());
		}

		@Override
		public boolean isGui3d()
		{
			return false;
		}

		@Override
		public Matrix4f getTransform(ItemCameraTransforms.TransformType tranformType)
		{
			this.tranformType = tranformType;
			if (tranformType == TransformType.THIRD_PERSON)
				return thirdPerson;
			else if (tranformType == TransformType.FIRST_PERSON)
				return firstPerson;
			return null;
		}

		@Override
		public void render()
		{
			RenderParameters rp = new RenderParameters();
			rp.applyTexture.set(false);
			if (tranformType == TransformType.GUI)
				drawShape(gui);
			else
			{
				//drawShape(gui);
				drawShape(getModelShape(), rp);
			}

			tranformType = null;
		}

		private Shape getModelShape()
		{
			MalisisIcon icon = getIcon(null, new RenderParameters());
			MalisisModel model = itemModels.get(icon);
			//if (model == null)
			{
				model = new MalisisModel(new TextureModelLoader(icon));
				itemModels.put(icon, model);
			}

			return model.getShape("shape");
		}

	};
}
