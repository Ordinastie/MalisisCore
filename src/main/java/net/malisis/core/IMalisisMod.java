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

package net.malisis.core;

import net.malisis.core.configuration.Settings;

/**
 * Interface to allow {@link MalisisCore} to have some automatic handling for mods.<br>
 * Allows to display the version of the registered, and automatic configuration GUI if {@link Settings} are provided.
 *
 * @author Ordinastie
 */
public interface IMalisisMod
{

	/**
	 * Gets the mod id.
	 *
	 * @return the mod id
	 */
	public String getModId();

	/**
	 * Gets the mod name.
	 *
	 * @return the mod name
	 */
	public String getName();

	/**
	 * Gets the mod version.
	 *
	 * @return the version
	 */
	public String getVersion();

	/**
	 * Gets the configuration settings.
	 *
	 * @return the configuration settings
	 */
	public Settings getSettings();
}
