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

package net.malisis.core.renderer.model;

import java.util.HashMap;
import java.util.Iterator;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.loader.ObjFileImporter;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.WavefrontObject;

/**
 * @author Ordinastie
 *
 */
public class MalisisModel implements ITransformable.Translate, ITransformable.Rotate, ITransformable.Scale, Iterable<Shape>
{
	protected String fileName;
	protected WavefrontObject wfo;

	protected HashMap<String, Shape> shapes = new HashMap<>();

	public MalisisModel()
	{

	}

	public void addShapes(Shape... shapes)
	{
		for (Shape shape : shapes)
			addShape(shape);
	}

	public void addShape(Shape shape)
	{
		addShape("Shape_" + (shapes.size() + 1), shape);
	}

	public void addShape(String name, Shape shape)
	{
		shapes.put(name.toLowerCase(), shape);
	}

	public Shape getShape(String name)
	{
		return shapes.get(name.toLowerCase());
	}

	public void render(BaseRenderer renderer)
	{
		render(renderer, (RenderParameters) null);
	}

	public void render(BaseRenderer renderer, RenderParameters rp)
	{
		for (String name : shapes.keySet())
			render(renderer, name, rp);
	}

	public void render(BaseRenderer renderer, String name)
	{
		render(renderer, name, null);
	}

	public void render(BaseRenderer renderer, String name, RenderParameters rp)
	{
		Shape shape = shapes.get(name);
		if (shape != null)
			renderer.drawShape(shape, rp);
	}

	public void storeState()
	{
		for (Shape s : this)
			s.storeState();
	}

	public void resetState()
	{
		for (Shape s : this)
			s.resetState();
	}

	@Override
	public void translate(float x, float y, float z)
	{
		for (Shape s : this)
			s.translate(x, y, z);
	}

	@Override
	public void rotate(float angle, float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		for (Shape s : this)
			s.rotate(angle, x, y, z, offsetX, offsetY, offsetZ);
	}

	@Override
	public void scale(float x, float y, float z, float offsetX, float offsetY, float offsetZ)
	{
		for (Shape s : this)
			s.scale(x, y, z, offsetX, offsetY, offsetZ);
	}

	@Override
	public Iterator<Shape> iterator()
	{
		return shapes.values().iterator();
	}

	public static MalisisModel load(ResourceLocation resource)
	{
		ObjFileImporter importer = new ObjFileImporter(resource);
		MalisisModel model = importer.getModel();
		model.storeState();

		return model;
	}

}
