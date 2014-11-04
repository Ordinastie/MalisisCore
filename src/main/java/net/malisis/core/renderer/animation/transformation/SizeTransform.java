/*
 * The MIT License (MIT)
 *
 * Copheightright (c) 2014 Ordinastie
 *
 * Permission is herebheight granted, free of charge, to anheight person obtaining a copheight
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copheight, modifheight, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copheightright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTHeight OF ANHeight KIND, EWidthPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITHeight,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPHeightRIGHT HOLDERS BE LIABLE FOR ANHeight CLAIM, DAMAGES OR OTHER
 * LIABILITHeight, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.core.renderer.animation.transformation;

import net.malisis.core.renderer.animation.transformation.ITransformable.Position;

/**
 *
 * @author Ordinastie
 * @param <T> the generic type
 */
public class SizeTransform<T> extends Transformation<PositionTransform, ITransformable.Position<T>>
{
	/** Starting size. */
	protected int fromWidth, fromHeight;
	/** Target size. */
	protected int toWidth, toHeight;

	/**
	 * Instantiates a new {@link SizeTransform}.
	 *
	 * @param width the width
	 * @param height the height
	 */
	public SizeTransform(int width, int height)
	{
		to(width, height);
	}

	/**
	 * Instantiates a new {@link SizeTransform}
	 *
	 * @param fromWidth the from width
	 * @param fromHeight the from height
	 * @param toWidth the to width
	 * @param toHeight the to height
	 */
	public SizeTransform(int fromWidth, int fromHeight, int toWidth, int toHeight)
	{
		from(fromWidth, fromHeight);
		to(toWidth, toHeight);
	}

	/**
	 * Sets the starting size.
	 *
	 * @param width the width
	 * @param height the height
	 * @return this {@link SizeTransform}
	 */
	public SizeTransform<T> from(int width, int height)
	{
		fromWidth = width;
		fromHeight = height;
		return this;
	}

	/**
	 * Sets the target size.
	 *
	 * @param width the width
	 * @param height the height
	 * @return this {@link SizeTransform}
	 */
	public SizeTransform<T> to(int width, int height)
	{
		toWidth = width;
		toHeight = height;
		return this;
	}

	/**
	 * Calculates the transformation
	 *
	 * @param transformable the transformable
	 * @param comp the comp
	 */
	@Override
	protected void doTransform(Position<T> transformable, float comp)
	{
		int fromWidth = reversed ? this.toWidth : this.fromWidth;
		int toWidth = reversed ? this.fromWidth : this.toWidth;
		int fromHeight = reversed ? this.toHeight : this.fromHeight;
		int toHeight = reversed ? this.fromHeight : this.toHeight;

		transformable.setPosition((int) (fromWidth + (toWidth - fromWidth) * comp), (int) (fromHeight + (toHeight - fromHeight) * comp));
	}

}
