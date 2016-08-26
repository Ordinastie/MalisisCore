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

package net.malisis.core.renderer.component;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.AnimatedRenderer;
import net.malisis.core.renderer.ISortedRenderable.AMC;
import net.malisis.core.renderer.ISortedRenderable.SortedRenderableManager;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.IAnimationLoader;
import net.malisis.core.util.Timer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderWorldLastEvent;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * The {@link AnimatedModelComponent} allows animated parts to be rendered for a {@link MalisisBlock} without the need for a
 * {@link TileEntitySpecialRenderer} nor even a {@link TileEntity}.<br>
 * The model needs to have an animation JSON associated (see {@link IAnimationLoader}).<br>
 * The static parts of the model are rendered as part of the batch when the chunk rerenders. The animated parts are rendered every frame as
 * part of a {@link RenderWorldLastEvent}.<br>
 * Shapes/groups that are referenced by an {@link Animation} when loaded are automatically considered animated. The others are considered
 * static.
 *
 * @author Ordinastie
 */
public class AnimatedModelComponent extends ModelComponent
{
	/** Static {@link Shape Shapes}. */
	private Set<String> staticShapes = Sets.newHashSet();
	/** Animated {@link Shape Shapes}. */
	private Set<String> animatedShapes = Sets.newHashSet();
	/** Callback for the checks to make when rendering for the first time. */
	private IStateCheck stateCheck = null;

	RenderParameters rp = new RenderParameters();

	/**
	 * Instantiates a new {@link AnimatedModelComponent}.
	 *
	 * @param modelName the model name
	 */
	public AnimatedModelComponent(String modelName)
	{
		super(modelName);
		rp.rotateIcon.set(false);

		autoDetectAnimatedGroups();
	}

	/**
	 * Automatically detects the animated groups/{@link Shape shapes} for the model.<br>
	 * Sets all the other shapes to be rendered statically.
	 */
	private void autoDetectAnimatedGroups()
	{
		Set<String> animList = model.getAnimatedShapes();
		animatedShapes = Sets.newHashSet(animList);

		Set<String> staticList = model.getShapeNames().stream().filter(s -> !animList.contains(s)).collect(Collectors.toSet());
		staticShapes = Sets.newHashSet(staticList);
	}

	private Optional<AMC> get(BlockPos pos)
	{
		return SortedRenderableManager.getSortedRenderable(pos);
	}

	private Timer addTimer(BlockPos pos, String animation, Timer timer)
	{
		return get(pos).map(amc -> amc.addTimer(animation, timer)).orElse(null);
	}

	private Timer removeTimer(BlockPos pos, String animation)
	{
		return get(pos).map(amc -> amc.removeTimer(animation)).orElse(null);
	}

	private Timer getTimer(BlockPos pos, String animation)
	{
		return get(pos).map(amc -> amc.getTimer(animation)).orElse(null);
	}

	/**
	 * Register a callback for when a the block is first rendered.<br>
	 * The callback is called when the block is placed in world, or when the chunks gets loaded on the client.
	 *
	 * @param stateCheck the state check
	 */
	public void onFirstRender(IStateCheck stateCheck)
	{
		this.stateCheck = stateCheck;
	}

	/**
	 * Triggers the {@link IStateCheck} callback.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @param state the state
	 */
	public void checkState(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		if (stateCheck != null)
			stateCheck.accept(world, pos, state, this);
	}

	/**
	 * Starts the {@link Timer} for the specified animation.<br>
	 * No effect is the animation is already running.
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @return the timer
	 */
	public Timer start(BlockPos pos, String animation)
	{
		return start(pos, animation, new Timer());
	}

	/**
	 * Start the animation with the specified {@link Timer}.<br>
	 * No effect if the animation is already running.
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @param timer the timer
	 * @return the timer
	 */
	public Timer start(BlockPos pos, String animation, Timer timer)
	{
		if (!isAnimating(pos, animation))
			addTimer(pos, animation, timer);
		return getTimer(pos, animation);
	}

	/**
	 * Starts the {@link Timer} for the specified animation.<br>
	 * Restarts the animation if it is already running.
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @return the timer
	 */
	public Timer forceStart(BlockPos pos, String animation)
	{
		return forceStart(pos, animation, new Timer());
	}

	/**
	 * Start the animation with the specified {@link Timer}.<br>
	 * Restarts the animation if it is already running.
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @param timer the timer
	 * @return the timer
	 */
	public Timer forceStart(BlockPos pos, String animation, Timer timer)
	{
		stop(pos, animation);
		return addTimer(pos, animation, timer);
	}

	/**
	 * Stops the animation.
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @return the timer
	 */
	public Timer stop(BlockPos pos, String animation)
	{
		return removeTimer(pos, animation);
	}

	/**
	 * Links two animations together.<br>
	 * The <i>start</i> animation will replace the <i>stop</i> one. The {@link Timer} for <i>start</i> will depend on the time spent by the
	 * <i>stop</i> animation.<br>
	 * The purpose is to have seamless transition between two reversed animations. (Ex: opening and closing of a door.)
	 *
	 * @param pos the pos
	 * @param stop the stop
	 * @param start the start
	 * @return the timer
	 */
	public Timer link(BlockPos pos, String stop, String start)
	{
		Timer t = stop(pos, stop);
		//no stop anim, start from the beginning
		if (t == null)
			return start(pos, start);

		Animation<Shape> anim = Iterables.getFirst(model.getAnimation(stop), null);
		//no stop anim, start from beginning
		if (anim == null)
			return start(pos, start);

		long duration = anim.getTransformation().getDuration();
		//stop already done, start from the beginning
		if (duration <= t.elapsedTime())
			return start(pos, start);

		t.setRelativeStart(t.elapsedTime() - duration);
		addTimer(pos, start, t);
		return t;
	}

	/**
	 * Checks the specified animation is already running. (Note that this method will return true if the animation is finished but is set to
	 * persist.)
	 *
	 * @param pos the pos
	 * @param animation the animation
	 * @return true, if successful
	 */
	public boolean isAnimating(BlockPos pos, String animation)
	{
		return getTimer(pos, animation) != null;
	}

	@Override
	public void render(Block block, MalisisRenderer<TileEntity> renderer)
	{
		//TODO: remove when debug is done
		if (renderer.getRenderType() == RenderType.BLOCK && !MalisisCore.isObfEnv)
		{
			loadModel();
			autoDetectAnimatedGroups();
		}

		RenderParameters rp = new RenderParameters();
		rp.rotateIcon.set(false);

		//no groups defined, render whole model static
		if (staticShapes.isEmpty() && animatedShapes.isEmpty())
		{
			super.render(block, renderer);
			return;
		}

		model.resetState();

		if (renderer.getRenderType() == RenderType.BLOCK)
			model.rotate(DirectionalComponent.getDirection(renderer.getBlockState()));

		staticShapes.forEach(name -> model.render(renderer, name));
		if (renderer.getRenderType() == RenderType.ITEM)
			animatedShapes.forEach(name -> model.render(renderer, name, rp));
	}

	/**
	 * Renders the animated {@link Shape Shapes} for this {@link AnimatedModelComponent}.
	 *
	 * @param block the block
	 * @param renderer the renderer
	 */
	public void renderAnimated(Block block, AMC amc, AnimatedRenderer renderer)
	{
		//no shapes to animated
		if (animatedShapes.size() == 0)
			return;

		model.resetState();
		//only animate for ANIMATED (not ITEM)
		if (renderer.getRenderType() == RenderType.ANIMATED)
		{
			model.rotate(DirectionalComponent.getDirection(renderer.getBlockState()));

			//need copy to prevent CME
			Map<String, Timer> timers = amc.getTimers();
			for (Entry<String, Timer> entry : timers.entrySet())
			{
				//animation is done and doesn't persist
				if (model.animate(entry.getKey(), entry.getValue()))
					timers.remove(renderer.getPos(), entry.getKey());
			}
		}

		//render the shapes
		renderer.enableBlending();
		animatedShapes.forEach(name -> model.render(renderer, name, rp));
	}

	/**
	 * Gets the {@link AnimatedModelComponent} associated to the {@link IComponentProvider}.
	 *
	 * @param block the block
	 * @return the animated model component
	 */
	public static AnimatedModelComponent get(IComponentProvider block)
	{
		return IComponent.getComponent(AnimatedModelComponent.class, block);
	}

	/**
	 * The IStateCheck interface is the callback used for {@link AnimatedModelComponent#onFirstRender(IStateCheck)}.<br>
	 * (See {@link AnimatedModelComponent#checkState(IBlockAccess, BlockPos, IBlockState)})
	 */
	public interface IStateCheck
	{
		public void accept(IBlockAccess world, BlockPos pos, IBlockState state, AnimatedModelComponent amc);
	}
}
