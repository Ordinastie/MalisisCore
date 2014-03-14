package net.malisis.core.renderer.element;


public class Vertex
{
	public static final int BRIGHTNESS_MAX = 15728640;

    public static final Vertex TopNorthWest = new Vertex(0, 1, 0);
    public static final Vertex TopNorthEast = new Vertex(1, 1, 0);
    public static final Vertex TopSouthWest = new Vertex(0, 1, 1);
    public static final Vertex TopSouthEast = new Vertex(1, 1, 1);
    public static final Vertex BottomNorthWest = new Vertex(0, 0, 0);
    public static final Vertex BottomNorthEast = new Vertex(1, 0, 0);
    public static final Vertex BottomSouthWest = new Vertex(0, 0, 1);
    public static final Vertex BottomSouthEast = new Vertex(1, 0, 1);


    private float x = 0;
    private float y = 0;
    private float z = 0;
    private int brightness = 0;
    private int color = 0xFFFFFF;
    private int alpha = 255;
    private float u = 0.0F;
    private float v = 0.0F;

    public Vertex(float x, float y, float z, int rgba, int brightness, float u, float v)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.color = (rgba >>> 8) & 0xFFFFFF;
        this.alpha = rgba & 0xFF;
        this.brightness = brightness;
        this.u = u;
        this.v = v;
    }
    public Vertex(float x, float y, float z, int rgba, int brightness)
    {
    	this(x, y, z, rgba, brightness, 0, 0);
    }
    public Vertex(float x, float y, float z)
    {
    	this(x, y, z, 0xFFFFFFFF, BRIGHTNESS_MAX, 0, 0);
    }
    public Vertex(Vertex vertex)
    {
        this(vertex.x, vertex.y, vertex.z, vertex.color << 8 | vertex.alpha, vertex.brightness, vertex.u, vertex.v);
    }
    public Vertex(Vertex vertex, int rgba, int brightness)
    {
    	this(vertex.x, vertex.y, vertex.z, rgba, brightness);
    }
    public Vertex(Vertex vertex, int rgba, int brightness, float u, float v)
    {
    	this(vertex.x, vertex.y, vertex.z, rgba, brightness, u, v);
    }

    public float getX()
    {
        return x;
    }
    public int getIntX()
    {
    	return Math.round(x);
    }
    public float getY()
    {
        return y;
    }
    public int getIntY()
    {
    	return Math.round(y);
    }
    public float getZ()
    {
        return z;
    }
    public int getIntZ()
    {
    	return Math.round(z);
    }
    public void limitX(float min, float max)
    {
    	x = Math.max(Math.min(x, max), min);
    }
    public void limitY(float min, float max)
    {
    	y = Math.max(Math.min(y, max), min);
    }
    public void limitZ(float min, float max)
    {
    	z = Math.max(Math.min(z, max), min);
    }
    public void interpolateCoord(double[][] bounds)
    {
    	double fx = bounds[1][0] - bounds[0][0];
    	double fy = bounds[1][1] - bounds[0][1];
    	double fz = bounds[1][2] - bounds[0][2];

    	x = (float) (x * fx + bounds[0][0]);
    	y = (float) (y * fy + bounds[0][1]);
    	z = (float) (z * fz + bounds[0][2]);
    }


    public Vertex factor(float f)
    {
    	x = Math.max(Math.min((x - 0.5F) * f + 0.5F, 1), 0);
    	y = Math.max(Math.min((y - 0.5F) * f + 0.5F, 1), 0);
    	z = Math.max(Math.min((z - 0.5F) * f + 0.5F, 1), 0);
    	return this;
    }
    public Vertex setColor(int color)
    {
        this.color = color;
        return this;
    }
    public int getColor()
    {
        return this.color;
    }
    public Vertex setAlpha(int alpha)
    {
    	this.alpha = alpha;
    	return this;
    }
    public int getAlpha()
    {
    	return this.alpha;
    }
    public Vertex setBrightness(int brightness)
    {
        this.brightness = brightness;
        return this;
    }
    public int getBrightness()
    {
        return this.brightness;
    }

    public void setUV(float u, float v)
    {
        this.u = u;
        this.v = v;
    }
    public float getU()
    {
        return this.u;
    }
    public float getV()
    {
        return this.v;
    }
    public void limitU(float min, float max)
    {
    	u = Math.max(Math.min(u, max), min);
    }
    public void limitV(float min, float max)
    {
    	v = Math.max(Math.min(v, max), min);
    }


    public boolean isCorner()
    {
    	return  (x == 1 || x == 0) && (y == 1 || y == 0) && (z == 1 || z == 0);
    }

    public String name()
    {
    	String s = "";
        if(isCorner())
        	s += (y == 1 ? "Top" : "Bottom") + (z == 1 ? "South" : "North") + (x == 1 ? "East" : "West") + " ";

        return s + "[" + x + ", " + y + ", " + z + "]";
    }
    public String toString()
    {
        return name() + " 0x" + Integer.toHexString(color) + " (a:" + alpha + ", b:" + brightness + ")";
    }
}
