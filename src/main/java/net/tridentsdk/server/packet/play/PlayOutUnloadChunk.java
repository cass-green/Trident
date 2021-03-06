/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2017 The TridentSDK Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tridentsdk.server.packet.play;

import io.netty.buffer.ByteBuf;
import net.tridentsdk.server.packet.PacketOut;

import javax.annotation.concurrent.Immutable;

/**
 * Sent to players to have the client unload the chunk at
 * the given coordinates on the client side.
 */
@Immutable
public class PlayOutUnloadChunk extends PacketOut {
    /**
     * The chunk x
     */
    private final int x;
    /**
     * The chunk z
     */
    private final int z;

    public PlayOutUnloadChunk(int x, int z) {
        super(PlayOutUnloadChunk.class);
        this.x = x;
        this.z = z;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.z);
    }
}