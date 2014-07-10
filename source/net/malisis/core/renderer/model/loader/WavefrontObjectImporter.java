/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.renderer.model.loader;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.model.MalisisModel;
import net.minecraftforge.client.model.obj.GroupObject;
import net.minecraftforge.client.model.obj.WavefrontObject;

/**
 * @author Ordinastie
 * 
 */
public class WavefrontObjectImporter
{
	private MalisisModel model;
	public WavefrontObject wfo;

	public WavefrontObjectImporter(WavefrontObject wfo)
	{
		this.wfo = wfo;
		this.model = new MalisisModel();
	}

	public MalisisModel getModel()
	{
		return model;
	}

	public void load()
	{
		for (GroupObject go : wfo.groupObjects)
		{
			model.addShape(go.name, new Shape(getFaces(go)));
		}
	}

	private Face[] getFaces(GroupObject groupObject)
	{
		Face[] faces = new Face[groupObject.faces.size()];
		int i = 0;
		for (net.minecraftforge.client.model.obj.Face f : groupObject.faces)
		{
			Face face = new Face(getVertexes(f));
			faces[i] = face;
			i++;
		}

		return faces;
	}

	private Vertex[] getVertexes(net.minecraftforge.client.model.obj.Face f)
	{
		Vertex[] vertexes = new Vertex[f.vertices.length];
		for (int i = 0; i < f.vertices.length; i++)
		{
			net.minecraftforge.client.model.obj.Vertex v = f.vertices[i];
			vertexes[i] = new Vertex(v.x, v.y, v.z);
			if (f.textureCoordinates != null && f.textureCoordinates.length > 0)
			{
				net.minecraftforge.client.model.obj.TextureCoordinate uv = f.textureCoordinates[i];
				vertexes[i].setUV(uv.u, uv.v);
			}

		}

		return vertexes;
	}

}
