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

import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.SouthFace;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.renderer.model.loader.TextureModelLoader;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.TransformBuilder;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class DefaultRenderer
{
	public static MalisisRenderer<?> nullRender = new Null();
	public static Block block = new Block();
	public static Item item = new Item();

	public static class Null extends MalisisRenderer<TileEntity>
	{
		@Override
		public void render()
		{}
	}

	public static class Block extends MalisisRenderer<TileEntity>
	{
		//		{
		//		    "display": {
		//		        "gui": {
		//		            "rotation": [ 30, 225, 0 ],
		//		            "translation": [ 0, 0, 0],
		//		            "scale":[ 0.625, 0.625, 0.625 ]
		//		        },
		//		        "ground": {
		//		            "rotation": [ 0, 0, 0 ],
		//		            "translation": [ 0, 3, 0],
		//		            "scale":[ 0.25, 0.25, 0.25 ]
		//		        },
		//		        "fixed": {
		//		            "rotation": [ 0, 0, 0 ],
		//		            "translation": [ 0, 0, 0],
		//		            "scale":[ 0.5, 0.5, 0.5 ]
		//		        },
		//		        "thirdperson_righthand": {
		//		            "rotation": [ 75, 45, 0 ],
		//		            "translation": [ 0, 2.5, 0],
		//		            "scale": [ 0.375, 0.375, 0.375 ]
		//		        },
		//		        "firstperson_righthand": {
		//		            "rotation": [ 0, 45, 0 ],
		//		            "translation": [ 0, 0, 0 ],
		//		            "scale": [ 0.40, 0.40, 0.40 ]
		//		        },
		//		        "firstperson_lefthand": {
		//		            "rotation": [ 0, 225, 0 ],
		//		            "translation": [ 0, 0, 0 ],
		//		            "scale": [ 0.40, 0.40, 0.40 ]
		//		        }
		//		    }
		//		}
		private Matrix4f gui = new TransformBuilder().rotate(30, 45, 0).scale(0.625F).get();
		private Matrix4f firstPersonLeftHand = new TransformBuilder().rotate(0, 225, 0).scale(0.4F).get();
		private Matrix4f firstPersonRightHand = new TransformBuilder().rotate(0, 45, 0).scale(0.4F).get();
		private Matrix4f thirdPerson = new TransformBuilder().translate(0, 0.155F, 0).rotateAfter(75, 45, 0).scale(0.375F).get();
		private Matrix4f fixed = new TransformBuilder().scale(0.5F).get();
		private Matrix4f ground = new TransformBuilder().translate(0, 0.3F, 0).scale(0.25F).get();

		private Shape shape = new Cube();
		private RenderParameters rp = new RenderParameters();

		@Override
		public boolean isGui3d()
		{
			return true;
		}

		@Override
		public Matrix4f getTransform(net.minecraft.item.Item item, TransformType tranformType)
		{
			switch (tranformType)
			{
				case GUI:
					return gui;
				case FIRST_PERSON_LEFT_HAND:
					return firstPersonLeftHand;
				case FIRST_PERSON_RIGHT_HAND:
					return firstPersonRightHand;
				case THIRD_PERSON_LEFT_HAND:
				case THIRD_PERSON_RIGHT_HAND:
					return thirdPerson;
				case GROUND:
					return ground;
				case FIXED:
					return fixed;
				default:
					return null;
			}
		}

		@Override
		public void render()
		{
			AxisAlignedBB[] aabbs;
			if (block instanceof IBoundingBox)
			{
				aabbs = ((MalisisBlock) block).getRenderBoundingBox(world, pos, blockState);
				rp.useBlockBounds.set(false);
			}
			else
			{
				aabbs = AABBUtils.identities();
				rp.useBlockBounds.set(true);
			}

			for (AxisAlignedBB aabb : aabbs)
			{
				if (aabb != null)
				{
					//shape = new Cube();
					//shape = new Shape(new SouthFace());
					shape.resetState().limit(aabb);
					rp.renderBounds.set(aabb);
					drawShape(shape, rp);
				}
			}
		}
	}

	public static class Item extends MalisisRenderer<TileEntity>
	{
		//		{
		//		    "display": {
		//		        "thirdperson_righthand": {
		//		            "rotation": [ 0, -90, 55 ],
		//		            "translation": [ 0, 4.0, 0.5 ],
		//		            "scale": [ 0.85, 0.85, 0.85 ]
		//		        },
		//		        "thirdperson_lefthand": {
		//		            "rotation": [ 0, 90, -55 ],
		//		            "translation": [ 0, 4.0, 0.5 ],
		//		            "scale": [ 0.85, 0.85, 0.85 ]
		//		        },
		//		        "firstperson_righthand": {
		//		            "rotation": [ 0, -90, 25 ],
		//		            "translation": [ 1.13, 3.2, 1.13 ],
		//		            "scale": [ 0.68, 0.68, 0.68 ]
		//		        },
		//		        "firstperson_lefthand": {
		//		            "rotation": [ 0, 90, -25 ],
		//		            "translation": [ 1.13, 3.2, 1.13 ],
		//		            "scale": [ 0.68, 0.68, 0.68 ]
		//		        }
		//		    }
		//		}
		// translations manually ajusted
		private Matrix4f firstPersonRightHand = new TransformBuilder().translate(0, .2F, .13F).scale(0.68F).rotate(0, -90, 25).get();
		private Matrix4f firstPersonLeftHand = new TransformBuilder().translate(0, .2F, .13F).scale(0.68F).rotate(0, 90, -25).get();
		private Matrix4f thirdPersonRightHand = new TransformBuilder().translate(0, 0.3F, 0.02F).scale(0.85F).rotate(0, -90, 55).get();
		private Matrix4f thirdPersonLeftHand = new TransformBuilder().translate(0, 0.3F, 0.02F).scale(0.85F).rotate(0, 90, -55).get();
		private Shape gui;
		private Map<Icon, MalisisModel> itemModels = new HashMap<>();

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
		public Matrix4f getTransform(net.minecraft.item.Item item, TransformType tranformType)
		{

			Matrix4f ground = new TransformBuilder().scale(0.5F).get();

			switch (tranformType)
			{
				case FIRST_PERSON_LEFT_HAND:
					return firstPersonLeftHand;
				case FIRST_PERSON_RIGHT_HAND:
					return firstPersonRightHand;
				case THIRD_PERSON_LEFT_HAND:
					return thirdPersonLeftHand;
				case THIRD_PERSON_RIGHT_HAND:
					return thirdPersonRightHand;
				case GROUND:
					return ground;
				default:
					return null;
			}
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
		}

		protected Shape getModelShape()
		{
			Icon icon = getIcon(null, new RenderParameters());
			MalisisModel model = itemModels.get(icon);
			if (model == null)
			{
				model = new MalisisModel(new TextureModelLoader(icon));
				itemModels.put(icon, model);
			}

			return model.getShape("shape");
		}

		public void clearModels()
		{
			itemModels.clear();
		}

	};
}
