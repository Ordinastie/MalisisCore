package net.malisis.core.renderer.preset;

import java.util.HashMap;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Vertex;
import net.minecraftforge.common.ForgeDirection;

public class FaceData
{
	// shortcuts
	private static ForgeDirection TOP = ForgeDirection.UP;
	private static ForgeDirection BOTTOM = ForgeDirection.DOWN;
	private static ForgeDirection NORTH = ForgeDirection.NORTH;
	private static ForgeDirection SOUTH = ForgeDirection.SOUTH;
	private static ForgeDirection EAST = ForgeDirection.EAST;
	private static ForgeDirection WEST = ForgeDirection.WEST;
	private static HashMap<String, int[]> aom = new HashMap<String, int[]>();

	public static void load()
	{
		buildDirectionMatrix();
		RenderParameters rp;

		/**
		 * Regular faces
		 */
		// Bottom
		rp = new RenderParameters();
		rp.direction = rp.textureSide = BOTTOM;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Face.Bottom, rp.direction);
		Face.Bottom.setParams(rp);
		// Top
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 1.0F;
		rp.aoMatrix = calculateAoMatrix(Face.Top, rp.direction);
		Face.Top.setParams(rp);
		// West
		rp = new RenderParameters();
		rp.direction = rp.textureSide = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.West, rp.direction);
		Face.West.setParams(rp);
		// North
		rp = new RenderParameters();
		rp.direction = rp.textureSide = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.North, rp.direction);
		Face.North.setParams(rp);
		// East
		rp = new RenderParameters();
		rp.direction = rp.textureSide = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.East, rp.direction);
		Face.East.setParams(rp);
		// South
		rp = new RenderParameters();
		rp.direction = rp.textureSide = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.South.setParams(rp);

		/**
		 * Corners
		 */
		// NorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = NORTH;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopEast"), aom("TopNorthEast"), aom("NorthEast") },
				{ aom("Bottom"), aom("BottomEast"), aom("BottomNorthEast"), aom("NorthEast") },
				{ aom("Bottom"), aom("BottomSouth"), aom("BottomSouthWest"), aom("SouthWest") },
				{ aom("Top"), aom("TopSouth"), aom("TopSouthWest"), aom("SouthWest") } };
		Face.NorthWest.setParams(rp);

		// NorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = EAST;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopSouth"), aom("TopSouthEast"), aom("SouthEast") },
				{ aom("Bottom"), aom("BottomSouth"), aom("BottomSouthEast"), aom("SouthEast") },
				{ aom("Bottom"), aom("BottomWest"), aom("BottomNorthWest"), aom("NorthWest") },
				{ aom("Top"), aom("TopWest"), aom("TopNorthWest"), aom("NorthWest") } };
		Face.NorthEast.setParams(rp);

		// SouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = WEST;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopNorth"), aom("TopNorthWest"), aom("NorthWest") },
				{ aom("Bottom"), aom("BottomNorth"), aom("BottomNorthWest"), aom("NorthWest") },
				{ aom("Bottom"), aom("BottomEast"), aom("BottomSouthEast"), aom("SouthEast") },
				{ aom("Top"), aom("TopEast"), aom("TopSouthEast"), aom("SouthEast") } };
		Face.SouthWest.setParams(rp);

		// SouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = SOUTH;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopWest"), aom("TopSouthWest"), aom("SouthWest") },
				{ aom("Bottom"), aom("BottomWest"), aom("BottomSouthWest"), aom("SouthWest") },
				{ aom("Bottom"), aom("BottomNorth"), aom("BottomNorthEast"), aom("NorthEast") },
				{ aom("Top"), aom("TopNorth"), aom("TopNortEastWest"), aom("NorthEast") } };
		Face.SouthEast.setParams(rp);

		/**
		 * Top Slopes
		 */
		// TopNorth
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.9F;
		rp.aoMatrix = new int[][][] { { aom("TopSouth"), aom("TopSouthEast"), aom("TopEast"), aom("East") },
				{ aom("East"), aom("NorthEast"), aom("BottomNorthEast"), aom("BottomNorth") },
				{ aom("BottomNorth"), aom("BottomNorthWest"), aom("NorthWest"), aom("West") },
				{ aom("West"), aom("TopWest"), aom("TopSouthWest"), aom("TopSouth") } };
		Face.TopNorth.setParams(rp);
		// TopEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopSouthWest"), aom("TopSouth"), aom("South") },
				{ aom("South"), aom("SouthEast"), aom("BottomSouthEast"), aom("BottomEast") },
				{ aom("BottomEast"), aom("BottomNorthEast"), aom("NorthEast"), aom("East") },
				{ aom("North"), aom("TopNorth"), aom("TopNorthWest"), aom("TopWest") } };
		Face.TopEast.setParams(rp);
		// TopSouth
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.9F;
		rp.aoMatrix = new int[][][] { { aom("TopNorth"), aom("TopNorthWest"), aom("TopWest"), aom("West") },
				{ aom("West"), aom("SouthWest"), aom("BottomSouthWest"), aom("BottomSouth") },
				{ aom("BottomSouth"), aom("BottomSouthEast"), aom("SouthEast"), aom("East") },
				{ aom("East"), aom("TopEast"), aom("TopNorthEast"), aom("TopNorth") } };
		Face.TopSouth.setParams(rp);
		// TopWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = new int[][][] { { aom("TopEast"), aom("TopNorthEast"), aom("TopNorth"), aom("North") },
				{ aom("North"), aom("NorthWest"), aom("BottomNorthWest"), aom("BottomWest") },
				{ aom("BottomWest"), aom("BottomSouthWest"), aom("SouthWest"), aom("South") },
				{ aom("South"), aom("TopSouth"), aom("TopSouthEast"), aom("TopEast") } };
		Face.TopWest.setParams(rp);

		/**
		 * Corner slopes
		 */
		// TopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0.5F, 0 }, { 0, 1}, {1, 1}, {0.5F, 0}};
		rp.aoMatrix = new int[][][] { { aom("TopNorthWest"), aom("TopNorth"), aom("TopWest") },
				{ aom("West"), aom("SouthWest"), aom("Bottom"), aom("BottomSouth") },
				{ aom("North"), aom("NorthEast"), aom("Bottom"), aom("BottomEast") },
				{ aom("TopNorthWest"), aom("TopNorth"), aom("TopWest") } };
		Face.TopSouthEast.setParams(rp);

		// TopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0.5F, 0 }, { 0, 1}, {1, 1}, {0.5F, 0}};
		rp.aoMatrix = new int[][][] { { aom("TopNorthEast"), aom("TopNorth"), aom("TopEast") },
				{ aom("North"), aom("NorthWest"), aom("Bottom"), aom("BottomWest") },
				{ aom("East"), aom("SouthEast"), aom("Bottom"), aom("BottomSouth") },
				{ aom("TopNorthEast"), aom("TopNorth"), aom("TopEast") } };
		Face.TopSouthWest.setParams(rp);

		// TopNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0.5F, 0 }, { 0, 1}, {1, 1}, {0.5F, 0}};
		rp.aoMatrix = new int[][][] { { aom("TopSouthEast"), aom("TopSouth"), aom("TopEast") },
				{ aom("West"), aom("NorthWest"), aom("Bottom"), aom("BottomNorth") },
				{ aom("South"), aom("SouthWest"), aom("Bottom"), aom("BottomWest") },
				{ aom("TopSouthEast"), aom("TopSouth"), aom("TopEast") } };
		Face.TopNorthWest.setParams(rp);

		// TopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[4][2];
		rp.uvFactor = new float[][] {{ 0.5F, 0 }, { 0, 1}, {1, 1}, {0.5F, 0}};
		rp.aoMatrix = new int[][][] { { aom("TopSouthWest"), aom("TopSouth"), aom("TopWest") },
				{ aom("South"), aom("SouthEast"), aom("Bottom"), aom("BottomEast") },
				{ aom("West"), aom("NorthWest"), aom("Bottom"), aom("BottomNorth") },
				{ aom("TopSouthWest"), aom("TopSouth"), aom("TopWest") } };
		Face.TopNorthEast.setParams(rp);

		/**
		 * Inverted Corner slopes
		 */
		// InvTopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 }};
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopSouthWest"), aom("TopSouth"), aom("South") },
				{ aom("BottomSouth"), aom("SouthEast"), aom("BottomEast") }, { aom("BottomSouth"), aom("SouthEast"), aom("BottomEast") },
				{ aom("TopNorth"), aom("TopNorthEast"), aom("TopEast"), aom("East") } };
		Face.InvTopSouthEast.setParams(rp);

		// InvTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 }};
		rp.aoMatrix = new int[][][] { { aom("TopNorth"), aom("TopNorthWest"), aom("TopWest"), aom("West") },
				{ aom("BottomSouth"), aom("SouthWest"), aom("BottomWest") }, { aom("BottomSouth"), aom("SouthWest"), aom("BottomWest") },
				{ aom("TopEast"), aom("TopSouthEast"), aom("TopSouth"), aom("South") } };
		Face.InvTopSouthWest.setParams(rp);

		// InvTopNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 }};
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopNorthWest"), aom("East") },
				{ aom("BottomNorth"), aom("NorthWest"), aom("BottomWest") }, { aom("BottomNorth"), aom("NorthWest"), aom("BottomWest") },
				{ aom("TopSouth"), aom("TopSouthWest"), aom("West") } };
		Face.InvTopNorthWest.setParams(rp);

		// InvTopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] {{ 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 }};
		rp.aoMatrix = new int[][][] { { aom("TopSouth"), aom("TopSouthEast"), aom("East") },
				{ aom("BottomWest"), aom("BottomNortWest"), aom("BottomNorth") },
				{ aom("BottomWest"), aom("BottomNortWest"), aom("BottomNorth") }, { aom("TopWest"), aom("TopNorthWest"), aom("North") } };

		Face.InvTopNorthEast.setParams(rp);

		/**
		 * Side Triangles
		 */
		// TriangleWestTopSouth
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleWestTopSouth.setParams(rp);
		// TriangleEastTopSouth
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleEastTopSouth.setParams(rp);
		// TriangleWestTopNorth
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleWestTopNorth.setParams(rp);
		// TriangleEastTopNorth
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleEastTopNorth.setParams(rp);
		// TriangleNorthTopWest
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleNorthTopWest.setParams(rp);
		// TriangleSouthTopWest
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleSouthTopWest.setParams(rp);
		// TriangleNorthTopEast
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleNorthTopEast.setParams(rp);
		// TriangleSouthTopEast
		rp = new RenderParameters();
		rp.textureSide = TOP;
		rp.direction = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(Face.South, rp.direction);
		Face.TriangleSouthTopEast.setParams(rp);

		/**
		 * Top Triangles
		 */
		// TriangleTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleTopSouthWest, rp.direction);
		Face.TriangleTopSouthWest.setParams(rp);
		// TriangleTopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleTopSouthEast, rp.direction);
		Face.TriangleTopSouthEast.setParams(rp);
		// TriangleTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleTopNorthWest, rp.direction);
		Face.TriangleTopNorthWest.setParams(rp);
		// TriangleTopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = TOP;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleTopNorthEast, rp.direction);
		Face.TriangleTopNorthEast.setParams(rp);

		/**
		 * Bottom Triangles
		 */
		// TriangleBottomSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = BOTTOM;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleBottomSouthEast, rp.direction);
		Face.TriangleBottomSouthEast.setParams(rp);
		// TriangleBottomSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = BOTTOM;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleBottomSouthWest, rp.direction);
		Face.TriangleBottomSouthWest.setParams(rp);
		// TriangleBottomNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = BOTTOM;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleBottomNorthWest, rp.direction);
		Face.TriangleBottomNorthWest.setParams(rp);
		// TriangleBottomNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = BOTTOM;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Face.TriangleBottomNorthEast, rp.direction);
		Face.TriangleBottomNorthEast.setParams(rp);

	}

	public static int[] aom(String s)
	{
		int[] a = aom.get(s);
		return a == null ? new int[] { 0, 0, 0 } : a;
	}

	private static void buildDirectionMatrix()
	{
		int[] a = { -1, 0, 1 };
		for (int x : a)
			for (int y : a)
				for (int z : a)
					aom.put(dirToString(x, y, z), new int[] { x, y, z });
	}

	private static String dirToString(int x, int y, int z)
	{
		String s = "";
		s += y == 0 ? "" : y == 1 ? "Top" : "Bottom";
		s += z == 0 ? "" : z == 1 ? "South" : "North";
		s += x == 0 ? "" : x == 1 ? "East" : "West";
		return s;
	}

	/**
	 * Automatically calculate AoMatrix for face. Only works for regular N/S/E/W faces
	 * @param face
	 * @param offset
	 */
	public static int[][][] calculateAoMatrix(Face face, ForgeDirection offset)
	{
		Vertex[] vertexes = face.getVertexes();
		int[][][] aoMatrix = new int[vertexes.length][3][3];

		for (int i = 0; i < vertexes.length; i++)
		{
			aoMatrix[i] = vertexAoMatrix(vertexes[i], offset);
		}

		return aoMatrix;
	}

	private static int[][] vertexAoMatrix(Vertex vertex, ForgeDirection offset)
	{
		int[][] a = new int[3][3];

		if (offset == ForgeDirection.WEST || offset == ForgeDirection.EAST)
		{
			a[0][0] = a[1][0] = a[2][0] = offset.offsetX;
			a[1][1] += vertex.getIntY() * 2 - 1; // 1 if 0, 1 if 1;
			a[2][1] += vertex.getIntY() * 2 - 1; // -1 if 0, 1 if 1;
			a[0][2] += vertex.getIntZ() * 2 - 1; // -1 if 0, 1 if 1;
			a[1][2] += vertex.getIntZ() * 2 - 1; // -1 if 0, 1 if 1;
		}
		else if (offset == ForgeDirection.UP || offset == ForgeDirection.DOWN)
		{
			a[0][1] = a[1][1] = a[2][1] = offset.offsetY;
			a[1][0] += vertex.getIntX() * 2 - 1; // 1 if 0, 1 if 1;
			a[2][0] += vertex.getIntX() * 2 - 1; // -1 if 0, 1 if 1;
			a[0][2] += vertex.getIntZ() * 2 - 1; // -1 if 0, 1 if 1;
			a[1][2] += vertex.getIntZ() * 2 - 1; // -1 if 0, 1 if 1;
		}
		else if (offset == ForgeDirection.NORTH || offset == ForgeDirection.SOUTH)
		{
			a[0][2] = a[1][2] = a[2][2] = offset.offsetZ;
			a[1][0] += vertex.getIntX() * 2 - 1; // 1 if 0, 1 if 1;
			a[2][0] += vertex.getIntX() * 2 - 1; // -1 if 0, 1 if 1;
			a[0][1] += vertex.getIntY() * 2 - 1; // -1 if 0, 1 if 1;
			a[1][1] += vertex.getIntY() * 2 - 1; // -1 if 0, 1 if 1;
		}

		return a;
	}
}
