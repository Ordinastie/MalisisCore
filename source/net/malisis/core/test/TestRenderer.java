package net.malisis.core.test;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class TestRenderer extends BaseRenderer
{
	public static int renderId;
	public static ResourceLocation rlPlatform = new ResourceLocation("malisiscore", "textures/blocks/sgplatform.png");
	int slices = 5;
	float sliceHeight = 1F / slices;

	@Override
	public void render()
	{
		if (block instanceof TestBlock)
		{
			if (typeRender == TYPE_WORLD)
				renderTestBlock();
			else
				drawShape(ShapePreset.Cube());
		}
		if (block instanceof StargateBlock)
		{
			if (typeRender == TYPE_TESR_WORLD)
				renderStargateTileEntity();
			else if (typeRender == TYPE_WORLD)
				renderStargateBlock();
			else if (typeRender == TYPE_ISBRH_INVENTORY)
			{
				RenderParameters rp = RenderParameters.setDefault();
				rp.colorMultiplier = 0x6666AA;
				drawShape(ShapePreset.Cube());
			}
		}
	}

	private void renderStargateTileEntity()
	{
		boolean drawTopFace = false;
		int alpha = 255;

		if (blockMetadata == 0)
		{
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

			StargateTileEntity te = (StargateTileEntity) tileEntity;

			int openTimer = 10;
			int downTimer = 10;
			int rotationTimer = 10;
			int delay = rotationTimer / 2;
			int expandTimer = rotationTimer + 3 * delay;
			int fadeTimer = 20;
			int archTimer = 30;
			int maxTimer = openTimer + downTimer + expandTimer + fadeTimer + archTimer + 20;

			float comp;
			float elapsedTime = getWorldTime() - te.placedTimer + partialTick;
			boolean drawShapes2 = false;

			if (elapsedTime > maxTimer)
				Minecraft.getMinecraft().theWorld.setBlockMetadataWithNotify(x, y, z, 1, 3);

			if (elapsedTime < openTimer + downTimer + expandTimer)
			{
				Shape[] shapes = new Shape[2 * slices - 1];
				Shape[] shapes2 = new Shape[8 * slices];
				shapes[0] = ShapePreset.Cube().setBounds(0, 0, 0, 1F, sliceHeight, 1F);

				for (int i = 1; i < shapes.length; i++)
				{
					Shape s = ShapePreset.Cube().setBounds(0, 0, 0, 0.5F, sliceHeight, 1F);;
					s.translate(0.5F * (i % 2), sliceHeight * (float) Math.ceil((float) i / 2), 0F);
					shapes[i] = s;
				}

				comp = Math.min(elapsedTime / openTimer, 1);
				for (int i = 1; i < shapes.length; i++)
				{
					int dir = i % 2 == 0 ? -1 : 1;
					float factor = (float) Math.ceil((float) i / 2);
					shapes[i].translate(comp * dir * factor / 2, 0, 0);
				}

				if (elapsedTime > openTimer)
				{
					comp = Math.min((elapsedTime - openTimer) / downTimer, 1);
					for (int i = 1; i < shapes.length; i++)
					{
						shapes[i].translate(0F, -sliceHeight * comp * (float) Math.ceil((float) i / 2), 0F);
					}
				}

				if (elapsedTime > openTimer + downTimer)
				{
					drawShapes2 = true;

					for (int i = 0; i < shapes2.length; i++)
					{
						Shape s = ShapePreset.Cube().setBounds(0, 0, 0, 1, sliceHeight, 0.5F);
						s.translate(-2 + (float) Math.floor((float) i / 8), 0, 0.5F * (i % 2));
						shapes2[i] = s;
					}

					comp = Math.min((elapsedTime - openTimer - downTimer) / expandTimer, 1);
					float y = -0.5F + sliceHeight / 2;

					for (int i = 0; i < shapes2.length; i++)
					{
						boolean first = (i % 8) < 2;
						boolean second = (i % 8) < 4;
						boolean third = (i % 8) < 6;
						boolean even = i % 2 == 0;
						float angle = even ? -180 : 180;
						float comp1 = Math.min((elapsedTime - openTimer - downTimer) / rotationTimer, 1);
						float comp2 = Math.min((elapsedTime - openTimer - downTimer - delay) / rotationTimer, 1);
						float comp3 = Math.min((elapsedTime - openTimer - downTimer - delay * 2) / rotationTimer, 1);
						float comp4 = Math.min((elapsedTime - openTimer - downTimer - delay * 3) / rotationTimer, 1);
						if (first)
							shapes2[i].rotateAroundX(angle * comp1, y, even ? -0.5F : 0);
						else if (second && comp2 > 0)
						{
							comp = Math.min((elapsedTime - openTimer - downTimer - delay) / rotationTimer, 1);
							shapes2[i].rotateAroundX(angle * comp1, y, even ? -0.5F : 0);
							shapes2[i].rotateAroundX(angle * comp2, y, even ? 0 : -0.5F);
						}
						else if (third && comp3 > 0)
						{
							shapes2[i].rotateAroundX(angle * comp1, y, even ? -0.5F : 0);
							shapes2[i].rotateAroundX(angle * comp2, y, even ? 0 : -0.5F);
							shapes2[i].rotateAroundX(angle * comp3, y, even ? -0.5F : 0.0F);
						}
						else if (comp4 > 0)
						{
							shapes2[i].rotateAroundX(angle * comp1, y, even ? -0.5F : 0);
							shapes2[i].rotateAroundX(angle * comp2, y, even ? 0 : -0.5F);
							shapes2[i].rotateAroundX(angle * comp3, y, even ? -0.5F : 0.0F);
							shapes2[i].rotateAroundX(angle * comp4, y, even ? 0.0F : -0.5F);
						}
					}
				}

				// drawShape(shapes[0]);
				for (Shape s : shapes)
				{
					s.applyMatrix();
					drawShape(s);
				}

				RenderParameters rp = RenderParameters.setDefault();
				rp.useBlockBrightness = false;
				if (drawShapes2)
				{
					for (Shape s : shapes2)
					{
						if (s != null)
						{
							s.applyMatrix();
							drawShape(s, rp);
						}
					}
				}
			}

			if (elapsedTime > openTimer + downTimer + expandTimer)
			{
				Shape platform = ShapePreset.Cube();
				platform.translate(0, -0.5F + sliceHeight / 2, 0);
				platform.scale(5F, sliceHeight, 5F);
				platform.applyMatrix();
				drawShape(platform);
				
				
				comp = Math.min((elapsedTime - openTimer - downTimer - expandTimer) / archTimer, 1);

				RenderParameters rp = RenderParameters.setDefault();
				rp.renderAllFaces = true;

				

				Shape base = ShapePreset.Cube();
				RenderParameters rpFace = new RenderParameters();
				rpFace.calculateAOColor = false;
				rpFace.calculateBrightness = false;
				rpFace.useBlockBrightness = false;
				rpFace.brightness = 32;
				rpFace.icon = Blocks.diamond_block.getIcon(0, 0);
				base.setParameters(FacePreset.Bottom(), rpFace, true);
				base.shrink(FacePreset.Bottom(), 0.69F);
				base.shrink(FacePreset.Top(), 0.87F);

				int totalArch = 12;
				float angle = -240;
				int offset = (int) ((-angle - 180) / 2);
				int nbArch = (int) (comp * totalArch);
				for (int i = 0; i <= nbArch; i++)
				{
					Shape s = new Shape(base);
					s.pivotZ(90);
					s.translate(0, 0.5F, 0);
					s.rotateAroundZ((angle / (float) (totalArch) * i) + offset);
					s.translate(-2.2F, 0, 0);
					s.scale(0.5F, 1, 0.5F);

					s.applyMatrix();
					drawShape(s, rp);
				}
			}

			if (elapsedTime > openTimer + downTimer + expandTimer + archTimer + 10)
			{
				comp = Math.min((elapsedTime - openTimer - downTimer - expandTimer - archTimer - 10) / fadeTimer, 1);
				alpha = (int) (255 * comp);
				next();
				drawTopFace = true;
			}
		}

		if (blockMetadata == 1 || drawTopFace)
		{
			Shape topFace = new Shape(new Face[] { FacePreset.Top() });
			topFace.translate(0, -0.499F + sliceHeight / 2, 0);
			topFace.scale(5F, sliceHeight, 5F);

			bindTexture(rlPlatform);
			RenderParameters rp = RenderParameters.setDefault();
			rp.useCustomTexture = true;
			rp.alpha = alpha;
			topFace.applyMatrix();
			drawShape(topFace, rp);
		}

	}

	private long getWorldTime()
	{
		return Minecraft.getMinecraft().theWorld.getTotalWorldTime();
	}

	private void renderStargateBlock()
	{
		if (blockMetadata == 0)
			return;

		RenderParameters rpFace = new RenderParameters();
		rpFace.calculateAOColor = false;
		rpFace.calculateBrightness = false;
		rpFace.brightness = Vertex.BRIGHTNESS_MAX;
		rpFace.useBlockBrightness = false;
		rpFace.icon = Blocks.lava.getIcon(0, 0);
		rpFace.colorFactor = 1F;
		

		Shape platform = ShapePreset.Cube();
		platform.setParameters(FacePreset.Top(), rpFace, true);

		platform.translate(0, -0.5F + sliceHeight / 2, 0);
		platform.scale(5F, sliceHeight, 5F);
		platform.applyMatrix();
		drawShape(platform);

		Shape base = ShapePreset.Cube();
		rpFace.icon = Blocks.diamond_block.getIcon(0, 0);
		base.setParameters(FacePreset.Bottom(), rpFace, true);
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		RenderParameters rpArch = RenderParameters.setDefault();
		rpArch.renderAllFaces = true;
		
		int nbArch = 12;
		float angle = -240;
		int offset = (int) ((-angle - 180) / 2);
		for (int i = 0; i <= nbArch; i++)
		{
			Shape s = new Shape(base);
			s.pivotZ(90);
			s.translate(0, 0.5F, 0);
			s.rotateAroundZ((angle / (float) (nbArch) * i) + offset);
			s.translate(-2.2F, 0, 0);
			s.scale(0.5F, 1, 0.5F);

			s.applyMatrix();
			drawShape(s, rpArch);
		}

	}

	private void renderTestBlock()
	{

		RenderParameters rp = RenderParameters.setDefault();
		int color = 255;
		rp.renderAllFaces = true;

		Shape base = ShapePreset.Cube();
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		int nbArch = 12;
		float angle = -240;
		int offset = (int) ((-angle - 180) / 2);
		for (int i = 0; i <= nbArch; i++)
		{
			Shape s = new Shape(base);
			color = (int) (color * 0.9);
			rp.colorMultiplier = (color << 16) + (color << 8) + color;
			s.pivotZ(90);
			s.translate(0, 0.5F, 0);
			s.rotateAroundZ((angle / (float) (nbArch) * i) + offset);
			s.translate(-2.2F, 0, 0);
			s.scale(0.5F, 1, 0.5F);

			s.applyMatrix();
			drawShape(s, rp);
		}

		// Shape s = new Shape(base);
		// s.translate(0, 2, 0);
		// s.rotateAroundZ(15);
		// drawShape(s, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

}
