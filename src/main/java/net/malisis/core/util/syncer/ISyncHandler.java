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

package net.malisis.core.util.syncer;

import net.malisis.core.util.syncer.message.SyncerMessage.Packet;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * {@link ISyncHandler} are used to define handlers for the {@link Syncer}.<br>
 * They're registered with {@link Syncer#registerSyncHandler(ISyncHandler)}.
 *
 * @author Ordinastie
 * @param <T> the generic type
 * @param <S> the generic type
 */
public interface ISyncHandler<T, S extends ISyncableData>
{
	/**
	 * Gets the name of this {@link ISyncHandler}. That name needs to match the {@link Syncable} value for the class it's applied on.
	 *
	 * @return the name
	 */
	public String getName();

	/**
	 * Gets the object to be synced on the receiving side.
	 *
	 * @param ctx the ctx
	 * @param data the data
	 * @return the receiver
	 */
	public T getReceiver(MessageContext ctx, S data);

	/**
	 * Gets the {@link ISyncableData} that holds the extra data to be sent.<br>
	 * On the receiving side, getSyncData() is called with a null caller, as the data is filled later by
	 * {@link ISyncableData#fromBytes(io.netty.buffer.ByteBuf)}
	 *
	 * @param caller the caller
	 * @return the sync data
	 */
	public S getSyncData(T caller);

	/**
	 * Adds a {@link FieldData} to be handled by this {@link ISyncHandler}.
	 *
	 * @param fieldData the field data
	 */
	public void addFieldData(FieldData fieldData);

	/**
	 * Gets the {@link FieldData} for the specified index (called from the the SyncerMessage.Packet).
	 *
	 * @param index the index
	 * @return the field data
	 */
	public FieldData getFieldData(int index);

	/**
	 * Gets the {@link FieldData} from its name.
	 *
	 * @param name the name
	 * @return the field data
	 */
	public FieldData getFieldData(String name);

	/**
	 * Sends the syncing packet.
	 *
	 * @param caller the caller
	 * @param packet the packet
	 */
	public void send(T caller, Packet<T, S> packet);
}
