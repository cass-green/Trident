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

package net.tridentsdk.server.world;

import com.google.common.collect.Lists;
import net.tridentsdk.Coordinates;
import net.tridentsdk.base.Tile;
import net.tridentsdk.meta.nbt.*;
import net.tridentsdk.server.data.ChunkMetaBuilder;
import net.tridentsdk.server.packets.play.out.PacketPlayOutMapChunkBulk;
import net.tridentsdk.util.TridentLogger;
import net.tridentsdk.world.Chunk;
import net.tridentsdk.world.ChunkLocation;
import net.tridentsdk.world.ChunkSnapshot;
import net.tridentsdk.world.Dimension;

import java.util.List;

public class TridentChunk implements Chunk {
    private final TridentWorld world;
    private final ChunkLocation location;
    private volatile int lastFileAccess;

    private volatile long lastModified;
    private volatile long inhabitedTime;
    private volatile byte lightPopulated;
    private volatile byte terrainPopulated;

    private volatile ChunkSection[] sections;

    public TridentChunk(TridentWorld world, int x, int z) {
        this(world, ChunkLocation.create(x, z));
    }

    public TridentChunk(TridentWorld world, ChunkLocation coord) {
        this.world = world;
        this.location = coord;
        this.lastFileAccess = 0;
    }

    protected int getLastFileAccess() {
        return this.lastFileAccess;
    }

    protected void setLastFileAccess(int last) {
        this.lastFileAccess = last;
    }

    @Override
    public void generate() {
    }

    @Override
    public int getX() {
        return location.getX();
    }

    @Override
    public int getZ() {
        return location.getZ();
    }

    @Override
    public ChunkLocation location() {
        return this.location;
    }

    @Override
    public TridentWorld world() {
        return this.world;
    }

    @Override
    public Tile tileAt(int relX, int y, int relZ) {
        int index = WorldUtils.getBlockArrayIndex(relX, y, relZ);

        return new TridentTile(Coordinates.create(this.world, relX + this.getX() * 16, y, relZ + this.getZ() * 16)
                               //TODO
                , null, (byte) 0);
    }

    @Override
    public ChunkSnapshot snapshot() {
        List<CompoundTag> sections = Lists.newArrayList();
        for (ChunkSection section : this.sections) {
            sections.add(NBTSerializer.serialize(section));
        }
        return new TridentChunkSnapshot(world, location, sections, lastFileAccess, lastModified, inhabitedTime,
                                        lightPopulated, terrainPopulated);
    }

    public PacketPlayOutMapChunkBulk asPacket() {
        PacketPlayOutMapChunkBulk chunkBulk = new PacketPlayOutMapChunkBulk();

        int bitmask = (1 << sections.length) - 1;
        int count = sections.length;
        int size = 0;
        int sectionSize = ChunkSection.LENGTH * 5 / 2;

        if (world.dimension() == Dimension.OVERWORLD) sectionSize += ChunkSection.LENGTH / 2;

        size += count * sectionSize + 256;

        byte[] data = new byte[size];
        int pos = 0;

        for (ChunkSection section : sections) {
            for (byte b : section.getTypes()) {
                data[pos++] = (byte) (b & 0xff);
                data[pos++] = (byte) (b >> 8);
            }
        }

        for (ChunkSection section : sections) {
            System.arraycopy(section.blockLight, 0, data, pos, section.blockLight.length);
            pos += section.blockLight.length;
        }

        for (int i = 0; i < 256; i += 1) {
            data[pos++] = 0;
        }

        if (pos != size) {
            TridentLogger.error(new IllegalStateException("Wrote " + pos + " when expected " + size + " bytes"));
        }

        chunkBulk.set("meta", new ChunkMetaBuilder().bitmap((short) bitmask).location(location));
        chunkBulk.set("data", data);
        chunkBulk.set("lightSent", true);
        chunkBulk.set("columnCount", sections.length);

        return chunkBulk;
    }

    public void load(CompoundTag root) {
        CompoundTag tag = root.getTagAs("Level");
        LongTag lastModifed = tag.getTagAs("LastUpdate");
        ByteTag lightPopulated = (tag.containsTag("LightPopulated")) ? (ByteTag) tag.getTagAs(
                "LightPopulated") : new ByteTag("LightPopulated").setValue((byte) 0);
        ByteTag terrainPopulated = tag.getTagAs("TerrainPopulated");

        LongTag inhabitedTime = tag.getTagAs("InhabitedTime");
        IntArrayTag biomes = tag.getTagAs("HeightMap");

        ListTag sections = tag.getTagAs("Sections");
        ListTag entities = tag.getTagAs("Entities");
        ListTag tileEntities = tag.getTagAs("TileEntities");
        ListTag tileTicks = (tag.containsTag("TileTicks")) ? (ListTag) tag.getTag("TileTicks") : new ListTag(
                "TileTicks", TagType.COMPOUND);
        List<NBTTag> sectionsList = sections.listTags();

        this.sections = new ChunkSection[sectionsList.size()];
        ChunkSection[] copy = this.sections;

        /* Load sections */
        for (int i = 0; i < sectionsList.size(); i += 1) {
            NBTTag t = sections.getTag(i);

            if (t instanceof CompoundTag) {
                CompoundTag ct = (CompoundTag) t;

                this.sections[i] = NBTSerializer.deserialize(ChunkSection.class, ct);
                this.sections[i].loadBlocks(world());
            }
        }

        for (NBTTag t : entities.listTags()) {
            //TridentEntity entity = EntityBuilder.create().build(TridentEntity.class);

            //entity.load((CompoundTag) t);
            //world.entities().add(entity);
        }

        /* Load extras */
        this.lightPopulated = lightPopulated.getValue(); // Unknown use
        this.terrainPopulated = terrainPopulated.getValue(); // if chunk was populated with special things (ores,
        // trees, etc.), if 1 regenerate
        this.lastModified = lastModifed.getValue(); // Tick when the chunk was last saved
        this.inhabitedTime = inhabitedTime.getValue(); // Cumulative number of ticks player have been in the chunk
    }

    public CompoundTag asNbt() {
        return null;
    }
}
