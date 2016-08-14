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

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
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

/**
 * @author Ordinastie
 *
 */
public class AnimationImporter implements IAnimationLoader
{
	private Map<String, Transform> transforms = Maps.newHashMap();
	private Multimap<String, Anim> anims = ArrayListMultimap.create();

	private Multimap<String, Animation<Shape>> animations = ArrayListMultimap.create();

	public AnimationImporter(ResourceLocation resourceLocation)
	{
		load(resourceLocation);
	}

	@Override
	public Multimap<String, Animation<Shape>> getAnimations(Map<String, Shape> shapes)
	{
		for (Entry<String, Anim> entry : anims.entries())
		{
			Anim anim = entry.getValue();
			Shape s = shapes.get(anim.group);
			Transformation<?, Shape> t = getTransform(anim.transform);
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

	public void load(ResourceLocation resourceLocation)
	{
		IResource res = Silenced.get(() -> Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation));
		if (res == null)
			return;

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(AnimationImporter.class, (InstanceCreator<AnimationImporter>) type -> this);
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

	public Transformation<?, Shape> getTransform(String name)
	{
		return transforms.get(name).getTransformation(this);
	}

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
		CHAINED
	}

	private static class Transform
	{
		private TransformType type;

		private Object from = null;
		private Object to = null;

		//rotate/alpha
		private float fromA = 0;
		private float toA = 0;
		//rotate
		private String axis = "Y";
		//rotate/scale
		private double[] offset = { 0, 0, 0 };
		//translate/scale
		private double[] fromXYZ = { 0, 0, 0 };
		private double[] toXYZ = { 0, 0, 0 };
		//chained/parallel
		private String[] transforms = {};
		//all
		private int ticks = 0;
		private int delay = 0;
		private int loops = 1;

		private Transformation<?, ?> build(AnimationImporter importer)
		{
			if (type == null)
				return null;

			setFromTo();
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
			}
			return null;
		}

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
			if (from instanceof Number)
				fromA = (float) from;
			if (to instanceof Number)
				toA = (float) to;
		}

		@SuppressWarnings("unchecked")
		public Transformation<?, Shape> getTransformation(AnimationImporter importer)
		{
			return (Transformation<?, Shape>) build(importer);
		}

		public Translation buildTranslation()
		{
			Translation t = new Translation((float) fromXYZ[0], (float) fromXYZ[1], (float) fromXYZ[2], (float) toXYZ[0], (float) toXYZ[1],
					(float) toXYZ[2]);
			t.forTicks(ticks, delay);
			t.loop(loops);
			return t;
		}

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

		public Scale buildScale()
		{
			Scale s = new Scale((float) fromXYZ[0], (float) fromXYZ[1], (float) fromXYZ[2], (float) toXYZ[0], (float) toXYZ[1],
					(float) toXYZ[2]);
			s.offset((float) offset[0], (float) offset[1], (float) offset[2]);
			s.forTicks(ticks, delay);
			s.loop(loops);
			return s;
		}

		public ChainedTransformation buildChained(AnimationImporter importer)
		{
			Transformation<?, ?>[] transformations = Lists.newArrayList(transforms)
															.stream()
															.map(importer::getTransform)
															.toArray(Transformation<?, ?>[]::new);
			ChainedTransformation c = new ChainedTransformation(transformations);
			return c;
		}
	}

	private static class Anim
	{
		private String group;
		private String transform;
		private boolean persist = true;
		private boolean reversed = false;
	}

}
