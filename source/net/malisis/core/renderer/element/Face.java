package net.malisis.core.renderer.element;

import java.util.HashMap;

import net.malisis.core.renderer.preset.FaceData;
import net.minecraft.util.Icon;

public class Face
{
    /** order of vertexes IS important **/
    //@formatter:off

    //regular faces
    public static Face Bottom = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomNorthWest});
    public static Face Top = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopNorthEast});
    public static Face West = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthWest});
    public static Face North = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest});
    public static Face East = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthEast});
    public static Face South = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast});

    //corners
    public static Face NorthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomSouthWest, Vertex.TopSouthWest});
    public static Face NorthEast = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest});
    public static Face SouthWest = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast});
    public static Face SouthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomNorthEast, Vertex.TopNorthEast});
    //slopes
    public static Face TopNorth = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopSouthWest});
    public static Face TopEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthWest});
    public static Face TopSouth = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    public static Face TopWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    //corner slopes
    public static Face TopSouthEast = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomNorthEast, Vertex.TopNorthWest});
    public static Face TopSouthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    public static Face TopNorthWest = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    public static Face TopNorthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomNorthWest, Vertex.TopSouthWest});

    //inverted corner slopes
    public static Face InvTopSouthEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthEast, Vertex.BottomSouthEast, Vertex.TopNorthEast});
    public static Face InvTopSouthWest = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthWest, Vertex.TopSouthEast});
    public static Face InvTopNorthWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthWest, Vertex.BottomNorthWest, Vertex.TopSouthWest});
    public static Face InvTopNorthEast = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthEast, Vertex.TopNorthWest});

    //top triangles
    public static Face TriangleTopSouthWest = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.TopNorthEast, Vertex.TopNorthWest, Vertex.TopNorthWest});
    public static Face TriangleTopSouthEast = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthWest});
    public static Face TriangleTopNorthWest = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopNorthEast, Vertex.TopNorthEast});
    public static Face TriangleTopNorthEast = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.TopSouthWest, Vertex.TopSouthEast, Vertex.TopSouthEast});
    // bottom triangles
    public static Face TriangleBottomSouthEast = new Face(new Vertex[] { Vertex.BottomSouthWest, Vertex.BottomNorthWest, Vertex.BottomNorthEast, Vertex.BottomNorthEast});
    public static Face TriangleBottomSouthWest = new Face(new Vertex[] { Vertex.BottomNorthWest, Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthEast});
    public static Face TriangleBottomNorthWest = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomSouthWest});
    public static Face TriangleBottomNorthEast = new Face(new Vertex[] { Vertex.BottomSouthEast, Vertex.BottomSouthWest, Vertex.BottomNorthWest, Vertex.BottomNorthWest});
    //side triangles
    public static Face TriangleWestTopSouth = new Face(new Vertex[] { Vertex.TopNorthWest, Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.BottomSouthWest});
    public static Face TriangleEastTopSouth = new Face(new Vertex[] { Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.TopNorthEast, Vertex.TopNorthEast});
    public static Face TriangleWestTopNorth = new Face(new Vertex[] { Vertex.BottomNorthWest, Vertex.BottomSouthWest, Vertex.TopSouthWest, Vertex.TopSouthWest});
    public static Face TriangleEastTopNorth = new Face(new Vertex[] { Vertex.TopSouthEast, Vertex.BottomSouthEast, Vertex.BottomNorthEast, Vertex.BottomNorthEast});
    public static Face TriangleNorthTopWest = new Face(new Vertex[] { Vertex.TopNorthEast, Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.BottomNorthWest});
    public static Face TriangleSouthTopWest = new Face(new Vertex[] { Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.TopSouthEast, Vertex.TopSouthEast});
    public static Face TriangleNorthTopEast = new Face(new Vertex[] { Vertex.BottomNorthEast, Vertex.BottomNorthWest, Vertex.TopNorthWest, Vertex.TopNorthWest});
    public static Face TriangleSouthTopEast = new Face(new Vertex[] { Vertex.TopSouthWest, Vertex.BottomSouthWest, Vertex.BottomSouthEast, Vertex.BottomSouthEast});
    //@formatter:on

    static
    {
        FaceData.load();
    }

    private Vertex[] vertexes;
    private RenderParameters params = new RenderParameters();

    public Face(Vertex[] vertexes, RenderParameters params)
    {
        // we need a copy of the vertexes else the modification for one face would impact the others ones
        this.vertexes = new Vertex[vertexes.length];
        for (int i = 0; i < vertexes.length; i++)
            this.vertexes[i] = new Vertex(vertexes[i]);
        this.params = new RenderParameters(params);
    }

    public Face(Vertex[] vertexes)
    {
    	this(vertexes, null);
    }

    public Face(Face face)
    {
        this(face, face.params);
    }

    public Face(Face face, RenderParameters params)
    {
        this(face.vertexes, params);
    }

    public Vertex[] getVertexes()
    {
        return vertexes;
    }

    public Face setParams(RenderParameters params)
    {
        this.params = params;
        return this;
    }

    public RenderParameters getParameters()
    {
    	return params;
    }

    public Face setColor(int color)
    {
        for (Vertex v : vertexes)
            v.setColor(color);
        return this;
    }

    public Face setAlpha(int alpha)
    {
        for (Vertex v : vertexes)
            v.setAlpha(alpha);
        return this;
    }

    public Face setBrightness(int brightness)
    {
        for (Vertex v : vertexes)
            v.setBrightness(brightness);
        return this;
    }

    public Face setTexture(Icon icon)
    {
        return setTexture(icon, params.uvFactor, params.flipU, params.flipV);
    }

    public Face setTexture(Icon icon, float[][] uvFactor)
    {
        return setTexture(icon, uvFactor, params.flipU, params.flipV);
    }

    public Face setTexture(Icon icon, float[][] uvFactor, boolean flippedU, boolean flippedV)
    {
    	if(uvFactor == null)
    		return this;

        float u = icon.getMinU();
        float v = icon.getMinV();
        float U = icon.getMaxU();
        float V = icon.getMaxV();

        for (int i = 0; i < vertexes.length; i++)
        {
            vertexes[i].setUV(interpolate(u, U, uvFactor[i][0], flippedU), interpolate(v, V, uvFactor[i][1], flippedV));
        }

        return this;
    }

    private float interpolate(float min, float max, float factor, boolean flipped)
    {
    	if(flipped)
    	{
    		factor = 1 - factor;
    		float t = max;
    		max = min;
    		min = t;
    	}
        return min + (max - min) * factor;
    }

    public void scale(float f)
    {
        for (Vertex v : vertexes)
            v.factor(f);
    }


    public String name()
    {
    	HashMap<String, Integer> map = new HashMap<String, Integer>();
    	String[] dirs = new String[] { "North", "South", "East", "West", "Top", "Bottom" };
    	for(String dir : dirs)
    	{
    		map.put(dir, 0);
	    	for(Vertex v : vertexes)
	    	{
	    		if(v.name().contains(dir))
	    			map.put(dir, map.get(dir) + 1);
	    	}
	    	if(map.get(dir) == 4)
	    		return dir;
    	}
    	return "";
    }
    public String toString()
    {
        String s = name() +  "[";
        for (Vertex v : vertexes)
            s += v.name() + ", ";
        return s + "]";
    }

}
