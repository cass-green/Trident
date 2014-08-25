/*
 * Copyright (C) 2014 The TridentSDK Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tridentsdk.server.netty.protocol;

import net.tridentsdk.server.netty.packet.PacketType;

/**
 * Interface level access to the communications protocol between client and server
 *
 * @author The TridentSDK Team
 */
public interface TridentProtocol {
    /**
     * Gets the packet from the enum constant list of available packets
     *
     * @param id the packet ID that identifies the type for the packet
     * @return the packet type that holds the ID
     */
    PacketType getPacket(int id);

    // TODO from AgentTroll: WTF is this for?? Read from enum instead
}
