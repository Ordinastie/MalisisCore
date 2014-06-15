package net.malisis.core.demo.stargate;

import java.util.ArrayList;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.Rotation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class StargateRenderer extends BaseRenderer
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

	public StargateRenderer()
	{
		// make sure the renderer is register after pre init, otherwise you may
		// not be able to retrieve block icons yet
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
			// move west first, then down
			ar.translate(-0.5F * i, 0, 0).forTicks(t).translate(0F, -sliceHeight * i, 0F).forTicks(t, ot).animate("openW" + i, sW);

			// opening toward east
			Shape sE = new Shape(sW).translate(0.5F, 0, 0);
			// move east first, then down
			ar.translate(0.5F * i, 0, 0).forTicks(t).translate(0F, -sliceHeight * i, 0F).forTicks(t, ot).animate("openE" + i, sE);
		}
	}

	private void createUnrollingAnimation()
	{
		ar.globalDelay(openTimer);
		RenderParameters rp = new RenderParameters();
		rp.useBlockBrightness.set(false);

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
		rpFace.calculateAOColor.set(false);
		rpFace.calculateBrightness.set(false);
		rpFace.useBlockBrightness.set(false);
		rpFace.brightness.set(32);
		rpFace.icon.set(Blocks.diamond_block.getIcon(0, 0));

		// create the shape
		Shape base = ShapePreset.Cube();
		base.setParameters(FacePreset.Bottom(), rpFace, true);
		base.translate(0, 3, 0);
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		int totalArch = 13;
		float angle = 10;
		int at = archTimer / totalArch;
		// at = 10;

		RenderParameters rp = new RenderParameters();
		rp.renderAllFaces.set(true);

		for (int i = 0; i < totalArch; i++)
		{
			float archAngle = 130 - (angle * i + angle / 2);
			int delay = (totalArch - i) * at;

			Shape sW = new Shape(base);
			sW.rotate(130, 0, 0, 1, 0, -2.2F, 0);

			Shape sE = new Shape(base);
			sE.rotate(-130, 0, 0, 1, 0, -2.2F, 0);

			// rotation then scaling of the western blocks of the arch
			ar.rotate(-archAngle, 0, 0, 1, 0, -2.2F, 0).forTicks(at, delay).scaleFrom(0.5F, 0.3F, 0.2F).scaleTo(0.5F, 0.5F, 0.3F)
					.forTicks(at / 2, delay + at).animate("archW" + i, sW, rp);
			// rotation then scaling of the eastern blocks of the arch
			ar.rotate(archAngle, 0, 0, 1, 0, -2.2F, 0).forTicks(at, delay).scaleFrom(0.5F, 0.3F, 0.2F).scaleTo(0.5F, 0.5F, 0.3F)
					.forTicks(at, delay + at).animate("archE" + i, sE, rp);
		}
	}

	private void createFloatingAnimation()
	{
		ar.globalDelay(openTimer + rotationTimer + archTimer);

		RenderParameters rp = new RenderParameters();
		rp.icon.set(Blocks.gold_block.getIcon(0, 0));
		rp.useBlockBrightness.set(false);
		rp.brightness.set(Vertex.BRIGHTNESS_MAX);
		rp.alpha.set(175);

		Shape s = ShapePreset.Cube();
		s.scale(0.2F);
		s.applyMatrix();
		s.translate(-1.0F, 1.5F, 0);

		ar.rotate(360, 0, 1, 0, 1.0F, 0, 0).forTicks(50).loop(-1).translate(0, 1, 0).forTicks(20).loop(-1, 0, 20).sinusoidal()
				.translate(0, -1, 0).forTicks(20).loop(-1, 20, 0).sinusoidal().rotate(360, 1, 0, 0).forTicks(50).loop(-1)
				.animate("floating", s, rp);
	}

	// #end Animations

	@Override
	public void render()
	{
		if (typeRender == TYPE_TESR_WORLD)
			renderStargateTileEntity();
		else if (typeRender == TYPE_WORLD)
			renderStargateBlock();
		else if (typeRender == TYPE_ISBRH_INVENTORY)
		{
			RenderParameters rp = new RenderParameters();
			rp.colorMultiplier.set(0x6666AA);
			drawShape(ShapePreset.Cube());
		}
	}

	private void renderStargateTileEntity()
	{
		StargateTileEntity te = (StargateTileEntity) tileEntity;

		int alpha = 255;
		boolean drawTopFace = false;
		ar.setStartTime(te.placedTimer);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);

		if (blockMetadata == 0)
		{
			// 1st non moving cube at the bottom
			drawShape(ShapePreset.Cube().setBounds(0, 0, 0, 1F, sliceHeight, 1F));
			// render all animations but not the "floating" one
			ar.renderAllBut("floating");

			// manually calculate the alpha of the top texture
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
			// next() needs to be called to trigger a draw before we bind another texture
			// otherwise, all the blocks would use that new texture
			next();

			Shape topFace = new Shape(new Face[] { FacePreset.Top() });
			// move the platform a bit higher than the block to avoid z-fighting
			topFace.translate(0, -0.499F + sliceHeight / 2, 0);
			topFace.scale(5F, sliceHeight, 5F);

			bindTexture(rlPlatform);
			RenderParameters rp = new RenderParameters();
			rp.useCustomTexture.set(true);
			rp.alpha.set(alpha);
			drawShape(topFace, rp);
		}

	}

	private void renderStargateBlock()
	{
		if (blockMetadata != 1)
			return;

		RenderParameters rpFace = new RenderParameters();
		rpFace.calculateAOColor.set(false);
		rpFace.calculateBrightness.set(false);
		rpFace.brightness.set(Vertex.BRIGHTNESS_MAX);
		rpFace.useBlockBrightness.set(false);
		rpFace.icon.set(Blocks.lava.getIcon(0, 0));
		rpFace.colorFactor.set(1F);

		Shape platform = ShapePreset.Cube();
		platform.setParameters(FacePreset.Top(), rpFace, true);

		platform.translate(0, -0.5F + sliceHeight / 2, 0);
		platform.scale(5F, sliceHeight, 5F);
		drawShape(platform);

		Shape base = ShapePreset.Cube();
		rpFace.icon.set(Blocks.diamond_block.getIcon(0, 0));
		base.setParameters(FacePreset.Bottom(), rpFace, true);
		base.translate(0, 3, 0);
		base.shrink(FacePreset.Bottom(), 0.69F);
		base.shrink(FacePreset.Top(), 0.87F);

		int totalArch = 13;
		float angle = 10;

		RenderParameters rp = new RenderParameters();
		rp.renderAllFaces.set(true);

		for (int i = 0; i < totalArch; i++)
		{
			float archAngle = angle * i + angle / 2;

			Shape s1 = new Shape(base);
			s1.rotate(-archAngle, 0, 0, 1, 0, -2.2F, 0);
			s1.scale(0.5F, 0.5F, 0.3F);

			Shape s2 = new Shape(base);
			s2.rotate(archAngle, 0, 0, 1, 0, -2.2F, 0);
			s2.scale(0.5F, 0.5F, 0.3F);

			drawShape(s1, rp);
			drawShape(s2, rp);
		}

	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}

}
