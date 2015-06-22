package net.malisis.core.util.finiteliquid;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.BlockState;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;

public class FiniteLiquidRenderer extends MalisisRenderer
{
	@Override
	protected void initialize()
	{
		shape = new Cube();
		shape.enableMergedVertexes();
	}

	@Override
	public void render()
	{
		if (world == null)
		{
			drawShape(new Cube());
			return;
		}

		initialize();
		shape.resetState();
		rp.renderAllFaces.set(false);

		if (world.getBlock(x, y + 1, z) == block)
		{
			drawShape(new Cube());
			return;
		}

		for (MergedVertex v : shape.getMergedVertexes(shape.getFace("top")))
		{
			int[][] aom = v.getBase().getAoMatrix(ForgeDirection.UP);
			boolean air = false;
			float height = (float) blockMetadata / 16;
			int count = 1;
			for (int i = 0; i < aom.length; i++)
			{
				//				if (i == 1)
				//					continue;
				int oX = aom[i][0];
				int oY = aom[i][1] - 1;
				int oZ = aom[i][2];
				BlockState state = new BlockState(world, x + oX, y + oY, z + oZ);
				if (state.getBlock() == Blocks.air && i != 1)
				{
					//					air = true;
					//					count++;
				}
				else if (state.getBlock() == block)
				{
					height += (float) state.getMetadata() / 16;
					count++;
				}
			}

			v.setY(air ? 0 : height / count);

		}

		drawShape(shape, rp);
	}
}