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
import net.tridentsdk.entity.Entity;
import net.tridentsdk.server.packet.PacketOut;

import javax.annotation.concurrent.Immutable;

import static net.tridentsdk.server.net.NetData.convertAngle;
import static net.tridentsdk.server.net.NetData.wvint;

@Immutable
public final class PlayOutEntityHeadLook extends PacketOut {

    private final int id;
    private final float yaw;

    public PlayOutEntityHeadLook(Entity entity) {
        super(PlayOutEntityHeadLook.class);
        this.id = entity.getId();
        this.yaw = entity.getPosition().getYaw();
    }

    @Override
    public void write(ByteBuf buf) {
        wvint(buf, this.id);

        buf.writeByte(convertAngle(this.yaw));
    }

}
