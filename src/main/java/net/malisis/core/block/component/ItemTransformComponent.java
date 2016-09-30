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

package net.malisis.core.block.component;

import javax.vecmath.Matrix4f;

import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.Item;

/**
 * The {@link ItemTransformComponent} allows the {@link IComponentProvider} to specify the transforms for the associated {@link Item}.
 *
 * @author Ordinastie
 */
public class ItemTransformComponent implements IComponent
{
	protected Matrix4f thirdPersonLeftHand;
	protected Matrix4f thirdPersonRightHand;
	protected Matrix4f firstPersonLeftHand;
	protected Matrix4f firstPersonRightHand;
	protected Matrix4f head;
	protected Matrix4f gui;
	protected Matrix4f ground;
	protected Matrix4f fixed;

	public ItemTransformComponent()
	{}

	@Override
	public boolean isClientComponent()
	{
		return true;
	}

	/**
	 * Sets the transforms to use for the {@link Item} third person.
	 *
	 * @param left the left
	 * @param right the right
	 * @return the item transform component
	 */
	public ItemTransformComponent thirdPerson(Matrix4f left, Matrix4f right)
	{
		this.thirdPersonLeftHand = left;
		this.thirdPersonRightHand = right;
		return this;
	}

	/**
	 * Sets the transforms to use for the {@link Item} in first person.
	 *
	 * @param left the left
	 * @param right the right
	 * @return the item transform component
	 */
	public ItemTransformComponent firstPerson(Matrix4f left, Matrix4f right)
	{
		this.firstPersonLeftHand = left;
		this.firstPersonRightHand = right;
		return this;
	}

	/**
	 * Sets the transform to use for the {@link Item} when equipped in the head slot.
	 *
	 * @param head the head
	 * @return the item transform component
	 */
	public ItemTransformComponent head(Matrix4f head)
	{
		this.head = head;
		return this;
	}

	/**
	 * Sets the transform to use for the {@link Item} when displayed in GUI slots.
	 *
	 * @param gui the gui
	 * @return the item transform component
	 */
	public ItemTransformComponent gui(Matrix4f gui)
	{
		this.gui = gui;
		return this;
	}

	/**
	 * Sets the transform to use for the {@link Item} on the ground.
	 *
	 * @param ground the ground
	 * @return the item transform component
	 */
	public ItemTransformComponent ground(Matrix4f ground)
	{
		this.ground = ground;
		return this;
	}

	/**
	 * Sets the transform to use for the {@link Item} in frames.
	 *
	 * @param fixed the fixed
	 * @return the item transform component
	 */
	public ItemTransformComponent fixed(Matrix4f fixed)
	{
		this.fixed = fixed;
		return this;
	}

	/**
	 * Gets the {@link Matrix4f transformation} for the specified {@link Item} and {@link TransformType}.
	 *
	 * @param item the item
	 * @param transformType the transform type
	 * @return the transform
	 */
	public Matrix4f getTransform(Item item, TransformType transformType)
	{
		switch (transformType)
		{
			case THIRD_PERSON_LEFT_HAND:
				return thirdPersonLeftHand;
			case THIRD_PERSON_RIGHT_HAND:
				return thirdPersonRightHand;
			case FIRST_PERSON_LEFT_HAND:
				return firstPersonLeftHand;
			case FIRST_PERSON_RIGHT_HAND:
				return firstPersonRightHand;
			case HEAD:
				return head;
			case GUI:
				return gui;
			case GROUND:
				return ground;
			case FIXED:
				return fixed;
			default:
				return null;
		}
	}
}
