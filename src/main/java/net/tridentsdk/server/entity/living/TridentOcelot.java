/*
 * Trident - A Multithreaded Server Alternative
 * Copyright 2014 The TridentSDK Team
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

package net.tridentsdk.server.entity.living;

import net.tridentsdk.Position;
import net.tridentsdk.entity.Entity;
import net.tridentsdk.entity.EntityProperties;
import net.tridentsdk.entity.OcelotType;
import net.tridentsdk.entity.Projectile;
import net.tridentsdk.entity.living.Ocelot;
import net.tridentsdk.entity.living.Player;
import net.tridentsdk.event.entity.EntityDamageEvent;
import net.tridentsdk.server.data.ProtocolMetadata;
import net.tridentsdk.server.entity.TridentLivingEntity;
import net.tridentsdk.server.entity.TridentTameable;

import java.util.UUID;

public class TridentOcelot extends TridentTameable implements Ocelot {
    private volatile OcelotType breed; // consider removing volatile, enums have free-synchronization

    public TridentOcelot(UUID id, Position spawnLocation) {
        super(id, spawnLocation);
        this.breed = OcelotType.WILD;
    }

    @Override
    protected void updateProtocolMeta() {
        protocolMeta.setMeta(18, ProtocolMetadata.MetadataType.BYTE, (byte) breed.asInt());
    }

    @Override
    public OcelotType breed() {
        return breed;
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public boolean isInLove() {
        return false;
    }

    @Override
    public void hide(Entity entity) {
    }

    @Override
    public void show(Entity entity) {
    }

    @Override
    public EntityDamageEvent lastDamageEvent() {
        return null;
    }

    @Override
    public Player lastPlayerDamager() {
        return null;
    }
}
