package net.malisis.core.renderer.preset;

import static net.minecraftforge.common.util.ForgeDirection.DOWN;
import static net.minecraftforge.common.util.ForgeDirection.EAST;
import static net.minecraftforge.common.util.ForgeDirection.NORTH;
import static net.minecraftforge.common.util.ForgeDirection.SOUTH;
import static net.minecraftforge.common.util.ForgeDirection.UP;
import static net.minecraftforge.common.util.ForgeDirection.WEST;

import java.util.HashMap;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Vertex;
import net.minecraftforge.common.util.ForgeDirection;

public class FacePreset
{
	//@formatter:off
	//regular faces
    private static Face Bottom = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomNorthWest});
    private static Face Top = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopNorthEast});
    private static Face West = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthWest});
    private static Face North = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest});
    private static Face East = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthEast});
    private static Face South = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast});

    //corners
    private static Face NorthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomSouthWest, Vertex.TopSouthWest});
    private static Face NorthEast = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest});
    private static Face SouthWest = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast});
    private static Face SouthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomNorthEast, Vertex.TopNorthEast});
    //slopes
    private static Face TopNorth = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopSouthWest});
    private static Face TopEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthWest});
    private static Face TopSouth = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    private static Face TopWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    //corner slopes
    private static Face TopSouthEast = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomNorthEast, Vertex.TopNorthWest});
    private static Face TopSouthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    private static Face TopNorthWest = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    private static Face TopNorthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomNorthWest, Vertex.TopSouthWest});

    //inverted corner slopes
    private static Face InvTopSouthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    private static Face InvTopSouthWest = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    private static Face InvTopNorthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomNorthWest, Vertex.TopSouthWest});
    private static Face InvTopNorthEast = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthEast, Vertex.TopNorthWest});

    //top triangles
    private static Face TriangleTopSouthWest = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.TopNorthEast, Vertex.TopNorthWest, Vertex.TopNorthWest});
    private static Face TriangleTopSouthEast = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthWest});
    private static Face TriangleTopNorthWest = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopNorthEast, Vertex.TopNorthEast});
    private static Face TriangleTopNorthEast = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopSouthEast});
    // bottom triangles
    private static Face TriangleBottomSouthEast = new Face(new Vertex[] { Vertex.BottomSouthWest, Vertex.BottomNorthWest, Vertex.BottomNorthEast, Vertex.BottomNorthEast});
    private static Face TriangleBottomSouthWest = new Face(new Vertex[] { Vertex.BottomNorthWest, Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthEast});
    private static Face TriangleBottomNorthWest = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomSouthWest});
    private static Face TriangleBottomNorthEast = new Face(new Vertex[] { Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomNorthWest, Vertex.BottomNorthWest});
    //side triangles
    private static Face TriangleWestTopSouth = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthWest});
    private static Face TriangleEastTopSouth = new Face(new Vertex[] { Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthEast, Vertex.TopNorthEast});
    private static Face TriangleWestTopNorth = new Face(new Vertex[] { Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthWest, Vertex.TopSouthWest});
    private static Face TriangleEastTopNorth = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthEast});
    private static Face TriangleNorthTopWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.BottomNorthWest});
    private static Face TriangleSouthTopWest = new Face(new Vertex[] { Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast, Vertex.TopSouthEast});
    private static Face TriangleNorthTopEast = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest, Vertex.TopNorthWest});
    private static Face TriangleSouthTopEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.BottomSouthEast});
   
    
    public static Face Bottom() { return new Face(Bottom); }
    public static Face Top() { return new Face(Top); }
    public static Face West() { return new Face(West); }
    public static Face North() { return new Face(North); }
    public static Face East() { return new Face(East); }
    public static Face South() { return new Face(South); }

    //corners
    public static Face NorthWest() { return new Face(NorthWest); }
    public static Face NorthEast() { return new Face(NorthEast); }
    public static Face SouthWest() { return new Face(SouthWest); }
    public static Face SouthEast() { return new Face(SouthEast); }
    //slopes
    public static Face TopNorth() { return new Face(TopNorth); }
    public static Face TopEast() { return new Face(TopEast); }
    public static Face TopSouth() { return new Face(TopSouth); }
    public static Face TopWest() { return new Face(TopWest); }
    //corner slopes
    public static Face TopSouthEast() { return new Face(TopSouthEast); }
    public static Face TopSouthWest() { return new Face(TopSouthWest); }
    public static Face TopNorthWest() { return new Face(TopNorthWest); }
    public static Face TopNorthEast() { return new Face(TopNorthEast); }

    //inverted corner slopes
    public static Face InvTopSouthEast() { return new Face(InvTopSouthEast); }
    public static Face InvTopSouthWest() { return new Face(InvTopSouthWest); }
    public static Face InvTopNorthWest() { return new Face(InvTopNorthWest); }
    public static Face InvTopNorthEast() { return new Face(InvTopNorthEast); }

    //top triangles
    public static Face TriangleTopSouthWest() { return new Face(TriangleTopSouthWest); }
    public static Face TriangleTopSouthEast() { return new Face(TriangleTopSouthEast); }
    public static Face TriangleTopNorthWest() { return new Face(TriangleTopNorthWest); }
    public static Face TriangleTopNorthEast() { return new Face(TriangleTopNorthEast); }
    // bottom triangles
    public static Face TriangleBottomSouthEast() { return new Face(TriangleBottomSouthEast); }
    public static Face TriangleBottomSouthWest() { return new Face(TriangleBottomSouthWest); }
    public static Face TriangleBottomNorthWest() { return new Face(TriangleBottomNorthWest); }
    public static Face TriangleBottomNorthEast() { return new Face(TriangleBottomNorthEast); }
    //side triangles
    public static Face TriangleWestTopSouth() { return new Face(TriangleWestTopSouth); }
    public static Face TriangleEastTopSouth() { return new Face(TriangleEastTopSouth); }
    public static Face TriangleWestTopNorth() { return new Face(TriangleWestTopNorth); }
    public static Face TriangleEastTopNorth() { return new Face(TriangleEastTopNorth); }
    public static Face TriangleNorthTopWest() { return new Face(TriangleNorthTopWest); }
    public static Face TriangleSouthTopWest() { return new Face(TriangleSouthTopWest); }
    public static Face TriangleNorthTopEast() { return new Face(TriangleNorthTopEast); }
    public static Face TriangleSouthTopEast() { return new Face(TriangleSouthTopEast); }
    //@formatter:on    

	private static HashMap<String, int[]> aom = new HashMap<String, int[]>();

	static
	{
		buildDirectionMatrix();
		RenderParameters rp;

		/**
		 * Regular faces
		 */
		// Bottom
		rp = new RenderParameters();
		rp.direction = rp.textureSide = DOWN;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(Bottom, rp.direction);
		Bottom.setParameters(rp);
		// Top
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 1.0F;
		rp.aoMatrix = calculateAoMatrix(Top, rp.direction);
		Top.setParameters(rp);
		// West
		rp = new RenderParameters();
		rp.direction = rp.textureSide = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(West, rp.direction);
		West.setParameters(rp);
		// North
		rp = new RenderParameters();
		rp.direction = rp.textureSide = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(North, rp.direction);
		North.setParameters(rp);
		// East
		rp = new RenderParameters();
		rp.direction = rp.textureSide = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(East, rp.direction);
		East.setParameters(rp);
		// South
		rp = new RenderParameters();
		rp.direction = rp.textureSide = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		South.setParameters(rp);

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
		NorthWest.setParameters(rp);

		// NorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = EAST;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopSouth"), aom("TopSouthEast"), aom("SouthEast") },
				{ aom("Bottom"), aom("BottomSouth"), aom("BottomSouthEast"), aom("SouthEast") },
				{ aom("Bottom"), aom("BottomWest"), aom("BottomNorthWest"), aom("NorthWest") },
				{ aom("Top"), aom("TopWest"), aom("TopNorthWest"), aom("NorthWest") } };
		NorthEast.setParameters(rp);

		// SouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = WEST;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopNorth"), aom("TopNorthWest"), aom("NorthWest") },
				{ aom("Bottom"), aom("BottomNorth"), aom("BottomNorthWest"), aom("NorthWest") },
				{ aom("Bottom"), aom("BottomEast"), aom("BottomSouthEast"), aom("SouthEast") },
				{ aom("Top"), aom("TopEast"), aom("TopSouthEast"), aom("SouthEast") } };
		SouthWest.setParameters(rp);

		// SouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = SOUTH;
		rp.colorFactor = 0.7F;
		rp.aoMatrix = new int[][][] { { aom("Top"), aom("TopWest"), aom("TopSouthWest"), aom("SouthWest") },
				{ aom("Bottom"), aom("BottomWest"), aom("BottomSouthWest"), aom("SouthWest") },
				{ aom("Bottom"), aom("BottomNorth"), aom("BottomNorthEast"), aom("NorthEast") },
				{ aom("Top"), aom("TopNorth"), aom("TopNortEastWest"), aom("NorthEast") } };
		SouthEast.setParameters(rp);

		/**
		 * Top Slopes
		 */
		// TopNorth
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.9F;
		rp.aoMatrix = new int[][][] { { aom("TopSouth"), aom("TopSouthEast"), aom("TopEast"), aom("East") },
				{ aom("East"), aom("NorthEast"), aom("BottomNorthEast"), aom("BottomNorth") },
				{ aom("BottomNorth"), aom("BottomNorthWest"), aom("NorthWest"), aom("West") },
				{ aom("West"), aom("TopWest"), aom("TopSouthWest"), aom("TopSouth") } };
		TopNorth.setParameters(rp);
		// TopEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopSouthWest"), aom("TopSouth"), aom("South") },
				{ aom("South"), aom("SouthEast"), aom("BottomSouthEast"), aom("BottomEast") },
				{ aom("BottomEast"), aom("BottomNorthEast"), aom("NorthEast"), aom("East") },
				{ aom("North"), aom("TopNorth"), aom("TopNorthWest"), aom("TopWest") } };
		TopEast.setParameters(rp);
		// TopSouth
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.9F;
		rp.aoMatrix = new int[][][] { { aom("TopNorth"), aom("TopNorthWest"), aom("TopWest"), aom("West") },
				{ aom("West"), aom("SouthWest"), aom("BottomSouthWest"), aom("BottomSouth") },
				{ aom("BottomSouth"), aom("BottomSouthEast"), aom("SouthEast"), aom("East") },
				{ aom("East"), aom("TopEast"), aom("TopNorthEast"), aom("TopNorth") } };
		TopSouth.setParameters(rp);
		// TopWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = new int[][][] { { aom("TopEast"), aom("TopNorthEast"), aom("TopNorth"), aom("North") },
				{ aom("North"), aom("NorthWest"), aom("BottomNorthWest"), aom("BottomWest") },
				{ aom("BottomWest"), aom("BottomSouthWest"), aom("SouthWest"), aom("South") },
				{ aom("South"), aom("TopSouth"), aom("TopSouthEast"), aom("TopEast") } };
		TopWest.setParameters(rp);

		/**
		 * Corner slopes
		 */
		// TopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0.5F, 0 }, { 0, 1 }, { 1, 1 }, { 0.5F, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopNorthWest"), aom("TopNorth"), aom("TopWest") },
				{ aom("West"), aom("SouthWest"), aom("Bottom"), aom("BottomSouth") },
				{ aom("North"), aom("NorthEast"), aom("Bottom"), aom("BottomEast") },
				{ aom("TopNorthWest"), aom("TopNorth"), aom("TopWest") } };
		TopSouthEast.setParameters(rp);

		// TopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0.5F, 0 }, { 0, 1 }, { 1, 1 }, { 0.5F, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopNorthEast"), aom("TopNorth"), aom("TopEast") },
				{ aom("North"), aom("NorthWest"), aom("Bottom"), aom("BottomWest") },
				{ aom("East"), aom("SouthEast"), aom("Bottom"), aom("BottomSouth") },
				{ aom("TopNorthEast"), aom("TopNorth"), aom("TopEast") } };
		TopSouthWest.setParameters(rp);

		// TopNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0.5F, 0 }, { 0, 1 }, { 1, 1 }, { 0.5F, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopSouthEast"), aom("TopSouth"), aom("TopEast") },
				{ aom("West"), aom("NorthWest"), aom("Bottom"), aom("BottomNorth") },
				{ aom("South"), aom("SouthWest"), aom("Bottom"), aom("BottomWest") },
				{ aom("TopSouthEast"), aom("TopSouth"), aom("TopEast") } };
		TopNorthWest.setParameters(rp);

		// TopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[4][2];
		rp.uvFactor = new float[][] { { 0.5F, 0 }, { 0, 1 }, { 1, 1 }, { 0.5F, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopSouthWest"), aom("TopSouth"), aom("TopWest") },
				{ aom("South"), aom("SouthEast"), aom("Bottom"), aom("BottomEast") },
				{ aom("West"), aom("NorthWest"), aom("Bottom"), aom("BottomNorth") },
				{ aom("TopSouthWest"), aom("TopSouth"), aom("TopWest") } };
		TopNorthEast.setParameters(rp);

		/**
		 * Inverted Corner slopes
		 */
		// InvTopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopSouthWest"), aom("TopSouth"), aom("South") },
				{ aom("BottomSouth"), aom("SouthEast"), aom("BottomEast") }, { aom("BottomSouth"), aom("SouthEast"), aom("BottomEast") },
				{ aom("TopNorth"), aom("TopNorthEast"), aom("TopEast"), aom("East") } };
		InvTopSouthEast.setParameters(rp);

		// InvTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopNorth"), aom("TopNorthWest"), aom("TopWest"), aom("West") },
				{ aom("BottomSouth"), aom("SouthWest"), aom("BottomWest") }, { aom("BottomSouth"), aom("SouthWest"), aom("BottomWest") },
				{ aom("TopEast"), aom("TopSouthEast"), aom("TopSouth"), aom("South") } };
		InvTopSouthWest.setParameters(rp);

		// InvTopNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopWest"), aom("TopNorthWest"), aom("East") },
				{ aom("BottomNorth"), aom("NorthWest"), aom("BottomWest") }, { aom("BottomNorth"), aom("NorthWest"), aom("BottomWest") },
				{ aom("TopSouth"), aom("TopSouthWest"), aom("West") } };
		InvTopNorthWest.setParameters(rp);

		// InvTopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.colorFactor = 0.8F;
		rp.uvFactor = new float[][] { { 0, 0 }, { 0.5F, 1 }, { 0.5F, 1 }, { 1, 0 } };
		rp.aoMatrix = new int[][][] { { aom("TopSouth"), aom("TopSouthEast"), aom("East") },
				{ aom("BottomWest"), aom("BottomNortWest"), aom("BottomNorth") },
				{ aom("BottomWest"), aom("BottomNortWest"), aom("BottomNorth") }, { aom("TopWest"), aom("TopNorthWest"), aom("North") } };

		InvTopNorthEast.setParameters(rp);

		/**
		 * Side Triangles
		 */
		// TriangleWestTopSouth
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleWestTopSouth.setParameters(rp);
		// TriangleEastTopSouth
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleEastTopSouth.setParameters(rp);
		// TriangleWestTopNorth
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = WEST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleWestTopNorth.setParameters(rp);
		// TriangleEastTopNorth
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = EAST;
		rp.colorFactor = 0.6F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleEastTopNorth.setParameters(rp);
		// TriangleNorthTopWest
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleNorthTopWest.setParameters(rp);
		// TriangleSouthTopWest
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleSouthTopWest.setParameters(rp);
		// TriangleNorthTopEast
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = NORTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleNorthTopEast.setParameters(rp);
		// TriangleSouthTopEast
		rp = new RenderParameters();
		rp.textureSide = UP;
		rp.direction = SOUTH;
		rp.colorFactor = 0.8F;
		rp.aoMatrix = calculateAoMatrix(South, rp.direction);
		TriangleSouthTopEast.setParameters(rp);

		/**
		 * Top Triangles
		 */
		// TriangleTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.aoMatrix = calculateAoMatrix(TriangleTopSouthWest, rp.direction);
		TriangleTopSouthWest.setParameters(rp);
		// TriangleTopSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.aoMatrix = calculateAoMatrix(TriangleTopSouthEast, rp.direction);
		TriangleTopSouthEast.setParameters(rp);
		// TriangleTopSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.aoMatrix = calculateAoMatrix(TriangleTopNorthWest, rp.direction);
		TriangleTopNorthWest.setParameters(rp);
		// TriangleTopNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = UP;
		rp.aoMatrix = calculateAoMatrix(TriangleTopNorthEast, rp.direction);
		TriangleTopNorthEast.setParameters(rp);

		/**
		 * Bottom Triangles
		 */
		// TriangleBottomSouthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = DOWN;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(TriangleBottomSouthEast, rp.direction);
		TriangleBottomSouthEast.setParameters(rp);
		// TriangleBottomSouthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = DOWN;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(TriangleBottomSouthWest, rp.direction);
		TriangleBottomSouthWest.setParameters(rp);
		// TriangleBottomNorthWest
		rp = new RenderParameters();
		rp.direction = rp.textureSide = DOWN;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(TriangleBottomNorthWest, rp.direction);
		TriangleBottomNorthWest.setParameters(rp);
		// TriangleBottomNorthEast
		rp = new RenderParameters();
		rp.direction = rp.textureSide = DOWN;
		rp.colorFactor = 0.5F;
		rp.aoMatrix = calculateAoMatrix(TriangleBottomNorthEast, rp.direction);
		TriangleBottomNorthEast.setParameters(rp);

	}

	/**
	 * Shortcut to get the aom mapping for a named position
	 * 
	 * @param s
	 * @return
	 */
	public static int[] aom(String s)
	{
		int[] a = aom.get(s);
		return a == null ? new int[] { 0, 0, 0 } : a;
	}

	/**
	 * Build a mapping between name and position (ie TopSouthWest => 0,1,1) for
	 * all 27 possibilities
	 */
	private static void buildDirectionMatrix()
	{
		int[] a = { -1, 0, 1 };
		for (int x : a)
			for (int y : a)
				for (int z : a)
					aom.put(dirToString(x, y, z), new int[] { x, y, z });
	}

	/**
	 * Get a name for a specific position
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	private static String dirToString(int x, int y, int z)
	{
		String s = "";
		s += y == 0 ? "" : y == 1 ? "Top" : "Bottom";
		s += z == 0 ? "" : z == 1 ? "South" : "North";
		s += x == 0 ? "" : x == 1 ? "East" : "West";
		return s != "" ? s : "Center";
	}

	/**
	 * Automatically calculate AoMatrix for face. Only works for regular
	 * N/S/E/W/T/B faces
	 * 
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

	/**
	 * Calculate AoMatrix for a vertex based on the vertex position and the face
	 * it belongs. Only works for regular N/S/E/W/T/B faces
	 * 
	 * @param vertex
	 * @param offset
	 * @return
	 */
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

	public static Face fromDirection(ForgeDirection dir)
	{
		switch (dir)
		{
			case DOWN:
				return FacePreset.Bottom();
			case UP:
				return FacePreset.Top();
			case NORTH:
				return FacePreset.North();
			case SOUTH:
				return FacePreset.South();
			case WEST:
				return FacePreset.West();
			case EAST:
				return FacePreset.East();
			default:
				return null;
		}
	}
}
