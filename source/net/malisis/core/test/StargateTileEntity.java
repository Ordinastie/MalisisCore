package net.malisis.core.test;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class StargateTileEntity extends TileEntity
{
	public long placedTimer;

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord - 2, yCoord, zCoord -2, xCoord + 3, yCoord + 2, zCoord + 3);
	}
}
