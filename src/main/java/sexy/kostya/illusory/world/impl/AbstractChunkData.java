package sexy.kostya.illusory.world.impl;

import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.api.ChunkData;
import sexy.kostya.illusory.world.impl.protocol.ChunkInjector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public abstract class AbstractChunkData extends ChunkData {

    private ChunkMap cachedMap;

    protected Int2ObjectMap<Object> tileEntitiesPackets;

    public AbstractChunkData(int x, int z) {
        super(x, z);
    }

    @Override
    public synchronized void setBlock(BlockData blockData) {
        super.setBlock(blockData);
        this.cachedMap = null;
    }

    public Collection<Object> getTileEntitiesPackets() {
        return this.tileEntitiesPackets.values();
    }

    public Object getTileEntityPacket(int x, int y, int z) {
        return this.tileEntitiesPackets.get(hash(x, y, z));
    }

    public synchronized ChunkMap getMap() {
        if (this.cachedMap != null) {
            return this.cachedMap;
        }
        return this.cachedMap = generateMap();
    }

    public synchronized ChunkMap getCachedMap() {
        return this.cachedMap;
    }

    abstract void initializeTileEntitiesPackets();

    private ChunkMap generateMap() {
        ChunkMap map = getOriginalMap();
        ChunkInjector.inject(getRelativeWorld(), map, sortBlocksPerSections());
        return map;
    }

    protected abstract ChunkMap getOriginalMap();

    protected abstract World getRelativeWorld();

    private Map<Integer, Collection<BlockData>> sortBlocksPerSections() {
        Int2ObjectMap<Collection<BlockData>> result = new Int2ObjectOpenHashMap<>();
        super.blocks.values().forEach(block -> result.computeIfAbsent(block.getY() >> 4, y -> new HashSet<>()).add(block));
        return result;
    }

}
