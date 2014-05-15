package net.malisis.core.demo.test;

import java.util.ArrayList;

import net.malisis.core.demo.stargate.StargateBlock;
import net.malisis.core.demo.stargate.StargateTileEntity;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.Rotation;
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

	int deployTimer = StargateBlock.deployTimer;
	int openTimer = (int) (deployTimer * 0.30F);
	int rotationTimer = (int) (deployTimer * 0.30F);
	int archTimer = (int) (deployTimer * 0.50F);
	int fadeTimer = (int) (deployTimer * 0.20F);

	AnimationRenderer ar = new AnimationRenderer(this);

	public TestRenderer()
	{
		//make sure the renderer is register after pre init, otherwise you may
		//not be able to retrieve block icons yet
		createOpeningAnimation();
		createUnrollingAnimation();
		createArchAnimation();
		createFloatingAnimation();
	}

	// #region Animations 
	private void createOpeningAnimation()
	{
		int ot = openTimer / slices;
		for (int i = 1; i < slices; i++)
		{
			int t = ot * i;
			// opening towards west
			Shape sW = ShapePreset.Cube().setBounds(0, 0, 0, 0.5F, sliceHeight, 1F);
			sW.translate(0, sliceHeight * i, 0F);
			//move west first, then down
			ar.translate(-0.5F * i, 0, 0).forTicks(t).translate(0F, -sliceHeight * i, 0F).forTicks(t, ot).animate("openW" + i, sW);

			// opening toward east
			Shape sE = new Shape(sW).translate(0.5F, 0, 0);
			//move east first, then down
			ar.translate(0.5F * i, 0, 0).forTicks(t).translate(0F, -sliceHeight * i, 0F).forTicks(t, ot).animate("openE" + i, sE);
		}
	}

	private void createUnrollingAnimation()
	{
		ar.globalDelay(openTimer);
		RenderParameters rp = new RenderParameters();
		rp.useBlockBrightness = false;

		int rt = rotationTimer * 2 / 5;
		int delay = rt / 2;
		float y = -0.5F + sliceHeight / 2;

		ArrayList<Animation> north = new ArrayList<>();
		ArrayList<Animation> south = new ArrayList<>();

		for (int row = 0; row < 4; row++)
		{
			// create the shapes
			Shape[] shapesNorth = new Shape[slices];
			Shape[] shapesSouth = new Shape[slices];
			for (int i = 0; i < slices; i++)
			{
				Shape sN = ShapePreset.Cube().setBounds(0, 0, 0, 1, sliceHeight, 0.5F);
				sN.translate(-2 + i, 0, 0);
				shapesNorth[i] = sN;

				Shape sS = new Shape(sN);
				sS.translate(0, 0, 0.5F);
				shapesSouth[i] = sS;
			}

			// create the animations
			north.add(new Rotation(-180, 1F, 0, 0, 0, y, -0.5F).forTicks(rt, 0));
			south.add(new Rotation(180, 1F, 0, 0, 0, y, 0).forTicks(rt, 0));
			if (row > 0)
			{
				north.add(new Rotation(-180, 1F, 0, 0, 0, y, 0).forTicks(rt, delay));
				south.add(new Rotation(180, 1F, 0, 0, 0, y, -0.5F).forTicks(rt, delay));
			}
			if (row > 1)
			{
				north.add(new Rotation(-180, 1F, 0, 0, 0, y, -0.5F).forTicks(rt, delay * 2));
				south.add(new Rotation(180, 1F, 0, 0, 0, y, 0).forTicks(rt, delay * 2));
			}
			if (row > 2)
			{
				north.add(new Rotation(-180, 1F, 0, 0, 0, y, 0).forTicks(rt, delay * 3));
				south.add(new Rotation(180, 1F, 0, 0, 0, y, -0.5F).forTicks(rt, delay * 3));
			}

			// link the shapes the the animations
			for (Animation anim : north)
				ar.add(anim);
			ar.animate("unrollingN" + row, shapesNorth, rp);

			for (Animation anim : south)
				ar.add(anim);
			ar.animate("unrollingS" + row, shapesSouth, rp);

			north.clear();
			south.clear();
		}
	}

	private void createArchAnimation()
	{
		ar.globalDelay(openTimer /* + rotationTimer */);

		// override rendering parameters for bottom face
		RenderParameters rpFace = new RenderParameters();
		rpFace.calculateAOColor = false;
		rpFace.calculateBrightness = false;
		rpFace.useBlockBrightness = false;
		rpFace.brightness = 32;
		rpFace.icon = Blocks.diamond_block.getIcon(0, 0);

		// create the shape
		Shape base = ShapePreset.Cube();
		base.setParameters(FacePreset.Bottom(), rpFace, true);
		base.translate(0, 3.0F, 0);
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		int totalArch = 13;
		float angle = 10;
		int at = archTimer / totalArch;
		// at = 10;

		RenderParameters rp = new RenderParameters();
		rp.renderAllFaces = true;

		for (int i = 0; i < totalArch; i++)
		{
			float archAngle = 130 - (angle * i + angle / 2);
			int delay = (totalArch - i) * at;

			Shape sW = new Shape(base);
			sW.rotate(130, 0, 0, 1, 0, -2.2F, 0);

			Shape sE = new Shape(base);
			sE.rotate(-130, 0, 0, 1, 0, -2.2F, 0);

			ar.rotate(-archAngle, 0, 0, 1, 0, -2.2F, 0).forTicks(at, delay).scaleFrom(0.5F, 0.3F, 0.2F).scaleTo(0.5F, 0.5F, 0.3F)
					.forTicks(at / 2, delay + at).animate("archW" + i, sW, rp);

			ar.rotate(archAngle, 0, 0, 1, 0, -2.2F, 0).forTicks(at, delay).scaleFrom(0.5F, 0.3F, 0.2F).scaleTo(0.5F, 0.5F, 0.3F)
					.forTicks(at, delay + at).animate("archE" + i, sE, rp);
		}
	}

	private void createFloatingAnimation()
	{
		ar.globalDelay(openTimer + rotationTimer + archTimer);

		RenderParameters rp = new RenderParameters();
		rp.icon = Blocks.gold_block.getIcon(0, 0);
		rp.useBlockBrightness = false;
		rp.brightness = Vertex.BRIGHTNESS_MAX;
		rp.alpha = 175;

		Shape s = ShapePreset.Cube();
		s.scale(0.2F);
		s.applyMatrix();
		s.translate(-1.0F, 1.5F, 0);

		ar.rotate(360, 0, 1, 0, 1.0F, 0, 0).forTicks(50).loop(-1).translate(0, 1, 0).forTicks(20).loop(-1, 0, 20).sinusoidal()
				.translate(0, -1, 0).forTicks(20).loop(-1, 20, 0).sinusoidal().rotate(360, 1, 0, 0).forTicks(50).loop(-1)
				.animate("floating", s, rp);
	}
	//#end Animations
	
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
		StargateTileEntity te = (StargateTileEntity) tileEntity;

		int alpha = 255;
		boolean drawTopFace = false;
		ar.setTime(te.placedTimer);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
		
		if (blockMetadata == 0)
		{
			// 1st non moving cube at the bottom
			drawShape(ShapePreset.Cube().setBounds(0, 0, 0, 1F, sliceHeight, 1F));
			//render all animations but not the "floating" one 
			ar.renderAllBut("floating");

			//manually calculate the alpha of the top texture
			float comp = Math.min((ar.getElapsedTime() - deployTimer + fadeTimer) / fadeTimer, 1);
			if (comp > 0)
			{
				alpha = (int) (255 * comp);
				drawTopFace = true;
			}
		}

		if (blockMetadata == 1 || drawTopFace)
		{
			if (blockMetadata == 1)
				ar.render("floating");
			//next() needs to be called to trigger a draw before we bind another texture
			//otherwise, all the blocks would use that new texture
			next();

			Shape topFace = new Shape(new Face[] { FacePreset.Top() });
			//move the platform a bit higher than the block to avoid z-fighting
			topFace.translate(0, -0.499F + sliceHeight / 2, 0);
			topFace.scale(5F, sliceHeight, 5F);

			bindTexture(rlPlatform);
			RenderParameters rp = RenderParameters.setDefault();
			rp.useCustomTexture = true;
			rp.alpha = alpha;
			drawShape(topFace, rp);
		}

	}

	@SuppressWarnings("unused")
	private void renderStargateTileEntity2()
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
					drawShape(s);

				RenderParameters rp = RenderParameters.setDefault();
				rp.useBlockBrightness = false;
				if (drawShapes2)
				{
					for (Shape s : shapes2)
					{
						if (s != null)
							drawShape(s, rp);
					}
				}
			}

			if (elapsedTime > openTimer + downTimer + expandTimer)
			{
				Shape platform = ShapePreset.Cube();
				platform.translate(0, -0.5F + sliceHeight / 2, 0);
				platform.scale(5F, sliceHeight, 5F);
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
			drawShape(topFace, rp);
		}

	}

	private long getWorldTime()
	{
		return Minecraft.getMinecraft().theWorld.getTotalWorldTime();
	}

	private void renderStargateBlock()
	{
		if (blockMetadata != 1)
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
		drawShape(platform);

		Shape base = ShapePreset.Cube();
		rpFace.icon = Blocks.diamond_block.getIcon(0, 0);
		base.setParameters(FacePreset.Bottom(), rpFace, true);
		base.translate(0, 1.0F, 0);
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		int totalArch = 13;
		float angle = 10;

		RenderParameters rp = new RenderParameters();
		rp.renderAllFaces = true;

		for (int i = 0; i < totalArch; i++)
		{
			float archAngle = angle * i + angle / 2;

			Shape s1 = new Shape(base);
			s1.rotateAroundZ(archAngle);
			s1.translate(0, 2.2F, 0);
			s1.scale(0.5F, 0.5F, 0.3F);

			Shape s2 = new Shape(base);
			s2.rotateAroundZ(-archAngle);
			s2.translate(0, 2.2F, 0);
			s2.scale(0.5F, 0.5F, 0.3F);

			drawShape(s1, rp);
			drawShape(s2, rp);
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

		int totalArch = 12;
		float angle = -240;
		int offset = (int) ((-angle - 180) / 2);
		for (int i = 0; i <= totalArch; i++)
		{
			Shape s = new Shape(base);
			color = (int) (color * 0.9);
			rp.colorMultiplier = (color << 16) + (color << 8) + color;
			s.pivotZ(90);
			s.translate(0, 1.0F, 0);
			s.rotateAroundZ((angle / (float) (totalArch) * i) + offset);
			s.translate(-2.2F, 0, 0);
			s.scale(0.5F, 1, 0.5F);

			drawShape(s, rp);
		}
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

}
