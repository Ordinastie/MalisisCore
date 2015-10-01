package net.malisis.core.util.finiteliquid;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.MBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class FiniteLiquidRenderer extends MalisisRenderer
{
	private Shape shape;
	private RenderParameters rp;
	private FiniteLiquid block;

	@Override
	protected void initialize()
	{
		shape = new Cube();
		shape.enableMergedVertexes();

		rp = new RenderParameters();
		rp.calculateAOColor.set(false);
	}

	@Override
	public void render()
	{
		rp.calculateAOColor.set(false);
		block = (FiniteLiquid) super.block;
		shape.resetState();
		if (world == null)
		{
			drawShape(shape);
			return;
		}

		initialize();
		shape.resetState();
		rp.renderAllFaces.set(false);

		//draw full cube if same liquid is above
		if (hasLiquidAbove(pos))
		{
			drawShape(shape);
			return;
		}

		for (MergedVertex v : shape.getMergedVertexes(shape.getFace("top")))
		{
			//cheat: we get the same coords that we use to calculate AO
			int[][] aom = v.getBase().getAoMatrix(EnumFacing.UP);
			boolean full = false;
			float height = block.getAmount(new MBlockState(blockState)) / 16F;
			int count = 1;

			for (int i = 0; i < aom.length; i++)
			{
				int oX = aom[i][0];
				int oY = aom[i][1] - 1;
				int oZ = aom[i][2];

				MBlockState state = new MBlockState(world, pos.add(oX, oY, oZ));
				if (state.getBlock() == block)
				{
					if (hasLiquidAbove(state.getPos()))
					{
						full = true;
						break;
					}

					height += block.getAmount(state) / 16F;
					count++;
				}
			}

			v.setY(full ? 1 : height / count);

		}

		drawShape(shape, rp);
	}

	private boolean hasLiquidAbove(BlockPos pos)
	{
		return world.getBlockState(pos.up()).getBlock() == block;
	}
}