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

package net.malisis.core.network;

import java.util.List;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.malisis.core.inventory.message.OpenInventoryMessage;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;

import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.ASMDataTable.ASMData;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * {@link MalisisNetwork} is a wrapper around {@link SimpleNetworkWrapper} in order to ease the handling of discriminators.<br>
 * Each mod should instantiate a {@code MalisisNetwork} instance when constructed, and {@link IMessageHandler} should be annotated with
 * {@link MalisisMessage} and register their packets inside their own public paramless constructors.<br>
 * <br>
 * Example : {@link OpenInventoryMessage}.
 *
 *
 * @author Ordinastie
 */
public class MalisisNetwork extends SimpleNetworkWrapper
{
	/** The global discriminator for each packet. */
	private static int discriminator = 0;

	/**
	 * Instantiates a new {@link MalisisNetwork}.
	 *
	 * @param channelName the channel name
	 */
	public MalisisNetwork(String channelName)
	{
		super(channelName);
	}

	/**
	 * Instantiates a new {@link MalisisNetwork}
	 *
	 * @param mod the mod
	 */
	public MalisisNetwork(IMalisisMod mod)
	{
		this(mod.getModId());
	}

	/**
	 * Register a message with the next discriminator available.
	 *
	 * @param <REQ> the generic type
	 * @param <REPLY> the generic type
	 * @param messageHandler the message handler
	 * @param requestMessageType the request message type
	 * @param side the side
	 */
	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, Side side)
	{
		super.registerMessage(messageHandler, requestMessageType, discriminator++, side);
	}

	/**
	 * Register a message with the next discriminator available.
	 *
	 * @param <REQ> the generic type
	 * @param <REPLY> the generic type
	 * @param messageHandler the message handler
	 * @param requestMessageType the request message type
	 * @param side the side
	 */
	public <REQ extends IMessage, REPLY extends IMessage> void registerMessage(IMessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType, Side side)
	{
		super.registerMessage(messageHandler, requestMessageType, discriminator++, side);
	}

	/**
	 * Gets the next discriminator available.
	 *
	 * @return the next discriminator
	 */
	public int getNextDiscriminator()
	{
		return discriminator++;
	}

	/**
	 * Instantiates every {@link IMessageHandler} annotated with {@link MalisisMessage}.<br>
	 *
	 * @param asmDataTable the asm data table
	 */
	public static void createMessages(ASMDataTable asmDataTable)
	{
		List<ASMData> classes = Ordering.natural().onResultOf(new Function<ASMData, String>()
		{
			@Override
			public String apply(ASMData data)
			{
				return data.getClassName();
			}
		}).sortedCopy(asmDataTable.getAll(MalisisMessage.class.getName()));

		for (ASMData data : classes)
		{
			try
			{
				Class clazz = Class.forName(data.getClassName());
				if (IMessageHandler.class.isAssignableFrom(clazz))
					clazz.newInstance();
				else
					MalisisCore.log.error("@MalisisMessage found on {} that does not implement IMessageHandler", data.getClassName());
			}
			catch (Exception e)
			{
				MalisisCore.log.error("Could not create {} message.", data.getClassName(), e);
			}
		}

	}
}
