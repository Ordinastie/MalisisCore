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

package net.malisis.core.renderer.element.face;

import static net.minecraft.util.EnumFacing.*;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.vertex.BottomNorthEast;
import net.malisis.core.renderer.element.vertex.BottomSouthWest;
import net.malisis.core.renderer.element.vertex.TopNorthWest;

/**
 * @author Ordinastie
 *
 */
public class TopSouthEastFace extends Face
{
	public TopSouthEastFace()
	{
		super(new TopNorthWest(), new BottomSouthWest(), new BottomNorthEast(), new TopNorthWest());

		params.direction.set(UP);
		params.textureSide.set(UP);
		params.colorFactor.set(0.8F);
		//		params.aoMatrix.set(new int[][][] { { FacePresets.aom("TopNorthWest"), FacePresets.aom("TopNorth"), FacePresets.aom("TopWest") },
		//				{ FacePresets.aom("West"), FacePresets.aom("SouthWest"), FacePresets.aom("Bottom"), FacePresets.aom("BottomSouth") },
		//				{ FacePresets.aom("North"), FacePresets.aom("NorthEast"), FacePresets.aom("Bottom"), FacePresets.aom("BottomEast") },
		//				{ FacePresets.aom("TopNorthWest"), FacePresets.aom("TopNorth"), FacePresets.aom("TopWest") } });
		setStandardUV();
	}
}
