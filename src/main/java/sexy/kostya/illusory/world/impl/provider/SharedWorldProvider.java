package sexy.kostya.illusory.world.impl.provider;

import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.impl.XChunkData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class SharedWorldProvider extends SemiRealWorldProvider {

    private final Long2ObjectMap<XChunkData> chunks = new Long2ObjectOpenHashMap<>();

    public SharedWorldProvider(World mirror, World world) {
        super(mirror, world);
    }

    @Override
    public BlockData getBlock(Player p, int x, int y, int z) {
        XChunkData chunk = getChunk(x >> 4, z >> 4);
        return chunk.getBlock(x, y, z);
    }

    @Override
    public void setBlock(Player p, BlockData blockData) {
        int cx = blockData.getX() >> 4, cz = blockData.getZ() >> 4;
        synchronized (this.chunks) {
            XChunkData chunkData = this.chunks.computeIfAbsent(
                    hash(cx, cz),
                    h -> new XChunkData(this, cx, cz)
            );
            chunkData.setBlock(blockData);
            updateBlocks(Collections.singleton(p), Collections.singleton(blockData), null);
        }
    }

    @Override
    public void setBlocks(Player p, Collection<BlockData> blockDatas) {
        Long2ObjectMap<Collection<BlockData>> perChunks = new Long2ObjectOpenHashMap<>();
        blockDatas.forEach(block -> {
            int cx = block.getX() >> 4, cz = block.getZ() >> 4;
            perChunks.computeIfAbsent(
                    hash(cx, cz),
                    h -> new HashSet<>()
            ).add(block);
        });
        Set<XChunkData> chunks = new HashSet<>();
        synchronized (this.chunks) {
            perChunks.forEach((hash, blocks) -> {
                XChunkData chunk = this.chunks.computeIfAbsent(hash, h -> {
                    BlockData block = blocks.iterator().next();
                    return new XChunkData(
                            this,
                            block.getX() >> 4,
                            block.getZ() >> 4
                    );
                });
                blocks.forEach(chunk::setBlock);
                chunks.add(chunk);
            });
        }
        updateBlocks(Collections.singleton(p), blockDatas, chunks);
    }

    public XChunkData getChunk(int x, int z) {
        synchronized (this.chunks) {
            return this.chunks.computeIfAbsent(
                    hash(x, z),
                    h -> new XChunkData(this, x, z)
            );
        }
    }

    @Override
    public XChunkData rewrite(Player player, int x, int z) {
        synchronized (this.chunks) {
            XChunkData chunk = this.chunks.get(hash(x, z));
            if (chunk != null) {
                return chunk;
            }
            if (super.mirror == super.world) {
                return null;
            } else {
                chunk = new XChunkData(this, x, z);
                this.chunks.put(hash(x, z), chunk);
                return chunk;
            }
        }
    }

}
