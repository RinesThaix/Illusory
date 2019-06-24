package sexy.kostya.illusory.world.api;

import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public abstract class ChunkData {

    protected final int x, z;
    protected Int2ObjectMap<BlockData> blocks = new Int2ObjectOpenHashMap<>();

    public ChunkData(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getBlocksAmount() {
        return this.blocks.size();
    }

    public synchronized BlockData getBlock(int x, int y, int z) {
        return this.blocks.get(hash(x, y, z));
    }

    public synchronized void setBlock(BlockData blockData) {
        if (blockData.isReal()) {
            this.blocks.remove(blockData.hashCode());
        } else {
            this.blocks.put(blockData.hashCode(), blockData);
        }
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public int hashCode() {
        return hash(this.x, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChunkData)) {
            return false;
        }
        ChunkData that = (ChunkData) o;
        return this.x == that.x && this.z == that.z;
    }

    public static int hash(int x, int y, int z) {
        return x + (z << 4) + (y << 8);
    }

    static int hash(int x, int z) {
        return (int) (((long) x << 32) + (long) z - -2147483648L);
    }

}
