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

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.AlphaTransform;
import net.malisis.core.renderer.animation.transformation.BrightnessTransform;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.ColorTransform;
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Scale;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.IAnimationLoader;
import net.malisis.core.util.Silenced;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * This class imports animations from a JSON file.<br>
 * See <a href="https://gist.github.com/Ordinastie/8e554458be39684451fdf7f15cc7a8c5">grammar definition</a> of the JSON.
 *
 * @author Ordinastie
 */
public class AnimationImporter implements IAnimationLoader
{
	/** List of {@link Transform} defined in the JSON. */
	private Map<String, Transform> transforms = Maps.newHashMap();
	/** List of {@link Anim} defined in the JSON. */
	private Multimap<String, Anim> anims = ArrayListMultimap.create();
	/** List of {@link Animation} built from the JSON. */
	private Multimap<String, Animation<Shape>> animations = ArrayListMultimap.create();

	/**
	 * Instantiates a new {@link AnimationImporter}.
	 *
	 * @param resourceLocation the resource location
	 */
	public AnimationImporter(ResourceLocation resourceLocation)
	{
		load(resourceLocation);
	}

	/**
	 * Build and return the {@link Animation Animations}.
	 *
	 * @param shapes the shapes
	 * @return the animations
	 */
	@Override
	public Multimap<String, Animation<Shape>> getAnimations(Map<String, Shape> shapes)
	{
		for (Entry<String, Anim> entry : anims.entries())
		{
			Anim anim = entry.getValue();
			Shape s = shapes.get(anim.group);
			Transformation<?, Shape> t = getTransform(anim.transform);

			if (s == null)
				MalisisCore.log.error("Could not find shape {} in the model. Ignoring this animation.", anim.group);
			if (t == null)
				MalisisCore.log.error("Could not find a tranform with name {} in the JSON. Ignoring this animation.", anim.transform);

			if (s != null && t != null)
			{
				t.reversed(anim.reversed);
				Animation<Shape> a = new Animation<>(s, t);
				a.setRender(false, anim.persist);
				animations.put(entry.getKey(), a);
			}

		}
		return animations;
	}

	/**
	 * Loads and reads the JSON.
	 *
	 * @param resourceLocation the resource location
	 */
	public void load(ResourceLocation resourceLocation)
	{
		IResource res = Silenced.get(() -> Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation));
		if (res == null)
			return;

		GsonBuilder gsonBuilder = new GsonBuilder();
		//we don't want GSON to create a new AnimationImporte but use this current one
		gsonBuilder.registerTypeAdapter(AnimationImporter.class, (InstanceCreator<AnimationImporter>) type -> this);
		//no builtin way to dezerialize multimaps
		gsonBuilder.registerTypeAdapter(Multimap.class, (JsonDeserializer<Multimap<String, Anim>>) this::deserializeAnim);
		Gson gson = gsonBuilder.create();

		try (Reader reader = new InputStreamReader(res.getInputStream(), "UTF-8"))
		{
			JsonReader jsonReader = new JsonReader(reader);
			jsonReader.setLenient(true);
			gson.fromJson(jsonReader, AnimationImporter.class);
		}
		catch (Exception e)
		{
			MalisisCore.log.error("Failed to read {}", resourceLocation, e);
		}
	}

	/**
	 * Gets the {@link Transformation} from its name.
	 *
	 * @param name the name
	 * @return the transform
	 */
	public Transformation<?, Shape> getTransform(String name)
	{
		Transform t = transforms.get(name);
		if (t == null)
			return null;
		return t.getTransformation(name, this);
	}

	/**
	 * Deserialize "anims" multimap.
	 *
	 * @param json the json
	 * @param typeOfT the type of t
	 * @param context the context
	 * @return the multimap
	 * @throws JsonParseException the json parse exception
	 */
	public Multimap<String, Anim> deserializeAnim(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
	{
		Multimap<String, Anim> anims = ArrayListMultimap.create();
		JsonObject obj = json.getAsJsonObject();

		TypeToken<ArrayList<Anim>> token = new TypeToken<ArrayList<Anim>>()
		{
		};
		for (Entry<String, JsonElement> entry : obj.entrySet())
			anims.putAll(entry.getKey(), context.deserialize(entry.getValue(), token.getType()));

		return anims;
	}

	private static enum TransformType
	{
		@SerializedName("translation")
		TRANSLATION,
		@SerializedName("rotation")
		ROTATION,
		@SerializedName("scale")
		SCALE,
		@SerializedName("chained")
		CHAINED,
		@SerializedName("parallel")
		PARALLEL,
		@SerializedName("color")
		COLOR,
		@SerializedName("alpha")
		ALPHA,
		@SerializedName("brightness")
		BRIGHTNESS,
	}

	private static class Transform
	{
		private String name;
		private TransformType type;
		private Object from = null;
		private Object to = null;
		//rotatation/alpha/brightness
		private float fromA = 0;
		private float toA = 0;
		//rotation
		private String axis = "Y";
		//rotation/scale
		private double[] offset = { 0, 0, 0 };
		//translation/scale
		private double[] fromXYZ = { 0, 0, 0 };
		private double[] toXYZ = { 0, 0, 0 };
		//chained/parallel
		private String[] transforms = {};
		//all
		private int ticks = 0;
		private int delay = 0;
		private int loops = 1;

		/**
		 * Builds a new {@link Transformation} from this {@link Transform}
		 *
		 * @param importer the importer
		 * @return the transformation
		 */
		private Transformation<?, ?> build(AnimationImporter importer)
		{
			if (type == null)
			{
				MalisisCore.log.error("No type specified for {}. Ignoring transformation.", name);
				return null;
			}

			setFromTo();
			if (!checkValues())
				return null;

			switch (type)
			{
				case TRANSLATION:
					return buildTranslation();
				case ROTATION:
					return buildRotation();
				case SCALE:
					return buildScale();
				case CHAINED:
					return buildChained(importer);
				case PARALLEL:
					return buildParallel(importer);
				case COLOR:
					return buildColor();
				case ALPHA:
					return buildAlpha();
				case BRIGHTNESS:
					return buildBrightness();
			}
			return null;
		}

		/**
		 * Sets from and to based on type.<br>
		 * Both float and array are possible for "from" and "to" depending on the type
		 */
		@SuppressWarnings("unchecked")
		private void setFromTo()
		{
			if (from instanceof List)
			{
				List<Double> f = (List<Double>) from;
				fromXYZ = new double[] { f.get(0), f.get(1), f.get(2) };
			}
			if (to instanceof List)
			{
				List<Double> t = (List<Double>) to;
				toXYZ = new double[] { t.get(0), t.get(1), t.get(2) };
			}
			if (from instanceof Double)
				fromA = (float) ((double) from);
			if (to instanceof Double)
				toA = (float) ((double) to);
		}

		/**
		 * Check if supplied values are valid.
		 *
		 * @return true, if values are valid
		 */
		private boolean checkValues()
		{
			boolean sameValues = false;
			switch (type)
			{
				case TRANSLATION:
				case SCALE:
				case COLOR:
					sameValues = fromXYZ[0] == toXYZ[0] && fromXYZ[1] == toXYZ[1] && fromXYZ[2] == toXYZ[2];
					break;
				case ROTATION:
				case ALPHA:
				case BRIGHTNESS:
					sameValues = fromA == toA;
					break;
				case PARALLEL:
				case CHAINED:
					if (ArrayUtils.isEmpty(transforms))
					{
						MalisisCore.log.error("No transforms specified for {}. Ignoring transformation.", name);
						return false;
					}
					return true;
			}

			if (sameValues)
				MalisisCore.log.error("From and To are the same for {}. Ignoring transformation.", name);
			return !sameValues;
		}

		/**
		 * Gets a new {@link Transformation} from this {@link Transform}
		 *
		 * @param importer the importer
		 * @return the transformation
		 */
		@SuppressWarnings("unchecked")
		public Transformation<?, Shape> getTransformation(String name, AnimationImporter importer)
		{
			this.name = name; //sets the name for better error reporting
			return (Transformation<?, Shape>) build(importer);
		}

		/**
		 * Builds the {@link Translation}.
		 *
		 * @return the translation
		 */
		public Translation buildTranslation()
		{
			Translation t = new Translation((float) fromXYZ[0], (float) fromXYZ[1], (float) fromXYZ[2], (float) toXYZ[0], (float) toXYZ[1],
					(float) toXYZ[2]);
			t.forTicks(ticks, delay);
			t.loop(loops);
			return t;
		}

		/**
		 * Builds the {@link Rotation}.
		 *
		 * @return the rotation
		 */
		public Rotation buildRotation()
		{
			Rotation r = new Rotation(fromA, toA);
			axis = axis.toLowerCase();
			r.aroundAxis(axis.equals("x") ? 1 : 0, axis.equals("y") ? 1 : 0, axis.equals("z") ? 1 : 0);
			r.offset((float) offset[0], (float) offset[1], (float) offset[2]);
			r.forTicks(ticks, delay);
			r.loop(loops);
			return r;
		}

		/**
		 * Builds the {@link Scale}.
		 *
		 * @return the scale
		 */
		public Scale buildScale()
		{
			Scale s = new Scale((float) fromXYZ[0], (float) fromXYZ[1], (float) fromXYZ[2], (float) toXYZ[0], (float) toXYZ[1],
					(float) toXYZ[2]);
			s.offset((float) offset[0], (float) offset[1], (float) offset[2]);
			s.forTicks(ticks, delay);
			s.loop(loops);
			return s;
		}

		/**
		 * Builds the {@link ChainedTransformation}.
		 *
		 * @param importer the importer
		 * @return the chained transformation
		 */
		public ChainedTransformation buildChained(AnimationImporter importer)
		{
			List<Transformation<?, Shape>> transfos = Lists.newArrayList();
			for (String name : transforms)
			{
				Transformation<?, Shape> t = importer.getTransform(name);
				if (t != null)
					transfos.add(t);
				else
					MalisisCore.log.error("Could not find a tranform with name {} in the JSON. Can't add it to chained transformation.",
							name);
			}

			if (transfos.size() == 0)
			{
				MalisisCore.log.error("No valid transformation found for chained transformation {}. Ignoring transformation.", name);
				return null;
			}

			return new ChainedTransformation(transfos.toArray(new Transformation[0]));
		}

		/**
		 * Builds the {@link ParallelTransformation}.
		 *
		 * @param importer the importer
		 * @return the parallel transformation
		 */
		public ParallelTransformation buildParallel(AnimationImporter importer)
		{
			List<Transformation<?, Shape>> transfos = Lists.newArrayList();
			for (String name : transforms)
			{
				Transformation<?, Shape> t = importer.getTransform(name);
				if (t != null)
					transfos.add(t);
				else
					MalisisCore.log.error("Could not find a tranform with name {} in the JSON. Can't add it to parallel transformation.",
							name);
			}

			if (transfos.size() == 0)
			{
				MalisisCore.log.error("No valid transformation found for parallel transformation {}. Ignoring transformation.", name);
				return null;
			}

			return new ParallelTransformation(transfos.toArray(new Transformation[0]));
		}

		/**
		 * Builds the {@link ColorTransform}.
		 *
		 * @return the color transform
		 */
		public ColorTransform buildColor()
		{
			//make sure default is white
			if (from == null)
				fromXYZ = new double[] { 1, 1, 1 };
			if (to == null)
				toXYZ = new double[] { 1, 1, 1 };

			ColorTransform ct = new ColorTransform(getColor(fromXYZ), getColor(toXYZ));
			ct.forTicks(ticks, delay);
			ct.loop(loops);
			return ct;
		}

		/**
		 * Builds the {@link AlphaTransform}.
		 *
		 * @return the alpha transform
		 */
		public AlphaTransform buildAlpha()
		{
			AlphaTransform at = new AlphaTransform((int) (fromA * 255), (int) (toA * 255));
			at.forTicks(ticks, delay);
			at.loop(loops);
			return at;
		}

		/**
		 * Builds the {@link BrightnessTransform}.
		 *
		 * @return the brightness transform
		 */
		public BrightnessTransform buildBrightness()
		{
			BrightnessTransform bt = new BrightnessTransform((int) (fromA * 14), (int) (toA * 14));
			bt.forTicks(ticks, delay);
			bt.loop(loops);
			return bt;
		}

		/**
		 * Gets the color form the array.
		 *
		 * @param xyz the xyz
		 * @return the color
		 */
		private int getColor(double[] xyz)
		{
			int r = (int) (MathHelper.clamp(xyz[0], 0, 1) * 255);
			int g = (int) (MathHelper.clamp(xyz[0], 0, 1) * 255);
			int b = (int) (MathHelper.clamp(xyz[0], 0, 1) * 255);

			return (r << 16) | (g << 8) | b;
		}
	}

	private static class Anim
	{
		/** Group/shape name the {@link Transformation} is applied to. */
		private String group;
		/** The {@link Transform} name. */
		private String transform;
		/** Whether the transformation should still be applied after the animation is finished. */
		private boolean persist = true;
		/** Whether the transform should be animated backwards. */
		private boolean reversed = false;
	}

}
