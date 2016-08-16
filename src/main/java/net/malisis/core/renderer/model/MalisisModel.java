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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.loader.AnimationImporter;
import net.malisis.core.renderer.model.loader.ObjFileImporter;
import net.malisis.core.renderer.model.loader.TextureModelLoader;
import net.malisis.core.util.Timer;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * This class is a holder for multiple shapes.<br>
 * If a {@link ResourceLocation} is provided, the model will be populated using the specified {@link IModelLoader}. If no loaded is giver,
 * it will be determined by the model file extension.
 *
 * @author Ordinastie
 */
public class MalisisModel implements ITransformable.Translate, ITransformable.Rotate, ITransformable.Scale, Iterable<Shape>
{
	/** Shapes building this {@link MalisisModel}. */
	protected Map<String, Shape> shapes = Maps.newHashMap();
	/** Animations for the Shapes in this {@link MalisisModel} */
	protected Multimap<String, Animation<Shape>> animations = ArrayListMultimap.create();

	/**
	 * Instantiates a new empty {@link MalisisModel}.<br>
	 * Allows {@link Shape shapes} to be added manually to the model.
	 */
	public MalisisModel()
	{}

	/**
	 * Instantiates a new {@link MalisisModel} with the specified {@link Shape shapes}.
	 *
	 * @param shapes the shapes
	 */
	public MalisisModel(Shape... shapes)
	{
		addShapes(shapes);
	}

	/**
	 * Instantiates a new {@link MalisisModel} with the specified {@link IModelLoader}.
	 *
	 * @param loader the loader
	 */
	public MalisisModel(IModelLoader loader)
	{
		load(loader);
	}

	public MalisisModel(IModelLoader loader, IAnimationLoader animLoader)
	{
		load(loader);
		load(animLoader);
	}

	/**
	 * Instantiates a new {@link MalisisModel}. The loader will be determined by the model file extension.
	 *
	 * @param resource the {@link ResourceLocation} for the model file
	 */
	public MalisisModel(ResourceLocation resource)
	{
		if (resource == null)
			return;

		IModelLoader loader = null;
		IAnimationLoader animLoader = null;
		if (resource.getResourcePath().endsWith(".obj"))
		{
			loader = new ObjFileImporter(resource);
			animLoader = new AnimationImporter(new ResourceLocation(resource.toString() + ".anim"));
		}
		if (resource.getResourcePath().endsWith(".png"))
			loader = new TextureModelLoader(resource);

		if (loader != null)
			load(loader);
		else
			MalisisCore.log.error("[MalisisModel] No loader determined for {}.", resource.getResourcePath());

		if (animLoader != null)
			load(animLoader);
	}

	/**
	 * Loads this {@link MalisisModel} from the specified {@link IModelLoader}.
	 *
	 * @param loader the loader
	 */
	protected void load(IModelLoader loader)
	{
		if (loader == null)
			return;

		shapes = loader.getShapes();
		storeState();
	}

	/**
	 * Load the animations from the specified {@link IAnimationLoader}.
	 *
	 * @param loader the loader
	 */
	protected void load(IAnimationLoader loader)
	{
		try
		{
			animations = loader.getAnimations(shapes);
		}
		catch (Exception e)
		{
			MalisisCore.log.error("Failed to get the animations for the model :", e);
		}
	}

	/**
	 * Adds the {@link Shape shapes} to this {@link MalisisModel} with default names.
	 *
	 * @param shapes the shapes
	 */
	public void addShapes(Shape... shapes)
	{
		for (Shape shape : shapes)
			addShape(shape);
	}

	/**
	 * Adds a {@link Shape} to this {@link MalisisModel} with a default name.
	 *
	 * @param shape the shape
	 */
	public void addShape(Shape shape)
	{
		addShape("shape_" + (shapes.size() + 1), shape);
	}

	/**
	 * Adds a {@link Shape} to this {@link MalisisModel} with the specified name.
	 *
	 * @param name the name of the shape
	 * @param shape the shape
	 */
	public void addShape(String name, Shape shape)
	{
		if (shape == null)
			return;

		shapes.put(name.toLowerCase(), shape);
	}

	/**
	 * Gets the {@link Shape} with the specified name.
	 *
	 * @param name the name of the shape
	 * @return the shape
	 */
	public Shape getShape(String name)
	{
		return shapes.get(name.toLowerCase());
	}

	/**
	 * Gets the list of {@link Shape} names in this {@link MalisisModel}.
	 *
	 * @return the shape list
	 */
	public Set<String> getShapeNames()
	{
		return shapes.keySet();
	}

	/**
	 * Adds the {@link Animation} to this {@link MalisisModel} with default names.
	 *
	 * @param animations the animations
	 */
	public void addAnimations(@SuppressWarnings("unchecked") Animation<Shape>... animations)
	{
		for (Animation<Shape> anim : animations)
			addAnimation(anim);
	}

	/**
	 * Adds an {@link Animation} to this {@link MalisisModel} with a default name.
	 *
	 * @param animation the animation
	 */
	public void addAnimation(Animation<Shape> animation)
	{
		addAnimation("animation_" + (animations.size() + 1), animation);
	}

	/**
	 * Adds an {@link Animation} to this {@link MalisisModel} with the specified name.
	 *
	 * @param name the name
	 * @param animation the animation
	 */
	public void addAnimation(String name, Animation<Shape> animation)
	{
		animations.put(name, animation);
	}

	/**
	 * Gets the {@link Animation} with the specified name.
	 *
	 * @param name the name
	 * @return the animation
	 */
	public Collection<Animation<Shape>> getAnimation(String name)
	{
		return Objects.firstNonNull(animations.get(name), ImmutableList.<Animation<Shape>> of());
	}

	public Set<String> getAnimatedShapes()
	{
		Set<String> animatedShapes = Sets.newHashSet();
		for (Animation<Shape> anim : animations.values())
		{
			for (Entry<String, Shape> entry : shapes.entrySet())
			{
				if (entry.getValue() == anim.getTransformable())
					animatedShapes.add(entry.getKey());
			}
		}
		return animatedShapes;
	}

	/**
	 * Gets the list of animation names in this {@link MalisisModel}.
	 *
	 * @return the animations list
	 */
	public Set<String> getAnimationsList()
	{
		return animations.keySet();
	}

	/**
	 * Renders all the {@link Shape shapes} of this {@link MalisisModel} using the specified {@link MalisisRenderer}.
	 *
	 * @param renderer the renderer
	 */
	public void render(MalisisRenderer<?> renderer)
	{
		render(renderer, (RenderParameters) null);
	}

	/**
	 * Renders all the {@link Shape shapes} of this {@link MalisisModel} using the specified {@link MalisisRenderer} and
	 * {@link RenderParameters}.
	 *
	 * @param renderer the renderer
	 * @param rp the parameters
	 */
	public void render(MalisisRenderer<?> renderer, RenderParameters rp)
	{
		for (String name : shapes.keySet())
			render(renderer, name, rp);
	}

	/**
	 * Renders a specific {@link Shape} of this {@link MalisisModel} using the specified {@link MalisisRenderer}.
	 *
	 * @param renderer the renderer
	 * @param name the name of the shape
	 */
	public void render(MalisisRenderer<?> renderer, String name)
	{
		render(renderer, name, null);
	}

	/**
	 * Renders a specific {@link Shape} of this {@link MalisisModel} using the specified {@link MalisisRenderer} and
	 * {@link RenderParameters}.
	 *
	 * @param renderer the renderer
	 * @param name the name of the shape
	 * @param rp the paramters
	 */
	public void render(MalisisRenderer<?> renderer, String name, RenderParameters rp)
	{
		Shape shape = getShape(name);
		if (shape != null)
			renderer.drawShape(shape, rp);
	}

	/**
	 * Animates all the animations of this model with the specified name based on the {@link Timer}
	 *
	 * @param name the name
	 * @param timer the timer
	 * @return true if the animation is finished and doesn't have persistance
	 */
	public boolean animate(String name, Timer timer)
	{
		boolean finished = true;
		for (Animation<Shape> anim : getAnimation(name))
			finished &= animate(anim, timer) && !anim.persistance();
		return finished;
	}

	/**
	 * Animates the specific {@link Animation} based on the specified {@link Timer}
	 *
	 * @param animation the animation
	 * @param timer the timer
	 * @return true, if successful
	 */
	public boolean animate(Animation<Shape> animation, Timer timer)
	{
		animation.animate(timer);
		return animation.isFinished();
	}

	/**
	 * Stores the state of this {@link MalisisModel}. Stores the state of all the {@link Shape shapes} contained by this model.
	 */
	public void storeState()
	{
		for (Shape s : this)
			s.storeState();
	}

	/**
	 * Resets the state of this {@link MalisisModel}. Resets the state of all the {@link Shape shapes} contained by this model.
	 */
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
}
