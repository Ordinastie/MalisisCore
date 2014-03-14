package net.malisis.core.renderer.element;


public class Shape
{
    public static Shape Cube = new Shape( new Face[] { Face.North, Face.South, Face.East, Face.West, Face.Top, Face.Bottom });
    public static Shape CubeSides = new Shape( new Face[] { Face.North, Face.South, Face.East, Face.West } );

    //corners
    public static Shape NorthWest = new Shape(new Face[] { Face.NorthWest, Face.TriangleTopNorthWest, Face.TriangleBottomNorthWest });
    public static Shape NorthEast = new Shape(new Face[] { Face.NorthEast, Face.TriangleTopNorthEast, Face.TriangleBottomNorthEast });
    public static Shape SouthWest = new Shape(new Face[] { Face.SouthWest, Face.TriangleTopSouthWest, Face.TriangleBottomSouthWest });
    public static Shape SouthEast = new Shape(new Face[] { Face.SouthEast, Face.TriangleTopSouthEast, Face.TriangleBottomSouthEast });

    //slopes
    public static Shape TopNorth = new Shape(new Face[] { Face.TopNorth, Face.TriangleEastTopNorth, Face.TriangleWestTopNorth });
    public static Shape TopSouth = new Shape(new Face[] { Face.TopSouth, Face.TriangleEastTopSouth, Face.TriangleWestTopSouth });
    public static Shape TopWest = new Shape(new Face[] { Face.TopWest, Face.TriangleNorthTopWest, Face.TriangleSouthTopWest });
    public static Shape TopEast = new Shape(new Face[] { Face.TopEast, Face.TriangleNorthTopEast, Face.TriangleSouthTopEast });

    //corner slopes
    public static Shape TopNorthWest = new Shape(new Face[] { Face.TopNorthWest });
    public static Shape TopNorthEast = new Shape(new Face[] { Face.TopNorthEast });
    public static Shape TopSouthEast = new Shape(new Face[] { Face.TopSouthEast });
    public static Shape TopSouthWest = new Shape(new Face[] { Face.TopSouthWest });

    //inverted corner slopes
    public static Shape InvTopNorthWest = new Shape(new Face[] { Face.InvTopNorthWest, Face.TriangleTopNorthWest });
    public static Shape InvTopNorthEast = new Shape(new Face[] { Face.InvTopNorthEast, Face.TriangleTopNorthEast });
    public static Shape InvTopSouthWest = new Shape(new Face[] { Face.InvTopSouthWest, Face.TriangleTopSouthWest });
    public static Shape InvTopSouthEast = new Shape(new Face[] { Face.InvTopSouthEast, Face.TriangleTopSouthEast });


    private Face[] faces;
    private RenderParameters params;

    public Shape(Face[] faces, RenderParameters params)
    {
        // we need a copy of the faces else the modification for one shape would impact the others ones
        this.faces = new Face[faces.length];
        for (int i = 0; i < faces.length; i++)
            this.faces[i] = new Face(faces[i]);
        this.params = new RenderParameters(params);
    }

    public Shape(Face[] faces)
    {
        this(faces, null);
    }
    
    public Shape(Shape s)
    {
    	this(s.faces, s.params);
    }
    public Shape(Shape s, RenderParameters params)
    {
    	this(s.faces, params);
    }

    public Face[] getFaces()
    {
    	return faces;
    }
    public RenderParameters getParameters()
    {
    	return params;
    }

}
