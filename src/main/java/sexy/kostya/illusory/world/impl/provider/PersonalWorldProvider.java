package sexy.kostya.illusory.world.impl.provider;

import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.impl.XChunkData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class PersonalWorldProvider extends SemiRealWorldProvider {

    private final Map<UUID, Long2ObjectMap<XChunkData>> subworlds = new ConcurrentHashMap<>();

    public PersonalWorldProvider(World mirror, World world) {
        super(mirror, world);
    }

    @Override
    public BlockData getBlock(Player p, int x, int y, int z) {
        XChunkData chunk = getChunk(p, x >> 4, z >> 4);
        return chunk.getBlock(x, y, z);
    }

    @Override
    public void setBlock(Player p, BlockData blockData) {
        int cx = blockData.getX() >> 4, cz = blockData.getZ() >> 4;
        Long2ObjectMap<XChunkData> chunks = getChunks(p);
        synchronized (chunks) {
            XChunkData chunk = chunks.computeIfAbsent(
                    hash(cx, cz),
                    h -> new XChunkData(this, cx, cz)
            );
            chunk.setBlock(blockData);
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
        Long2ObjectMap<XChunkData> subworld = getChunks(p);
        synchronized (subworlds) {
            perChunks.forEach((hash, blocks) -> {
                XChunkData chunk = subworld.computeIfAbsent(hash, h -> {
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

    public XChunkData getChunk(Player p, int x, int z) {
        Long2ObjectMap<XChunkData> chunks = getChunks(p);
        synchronized (chunks) {
            return chunks.computeIfAbsent(
                    hash(x, z),
                    h -> new XChunkData(this, x, z)
            );
        }
    }

    private Long2ObjectMap<XChunkData> getChunks(Player p) {
        return this.subworlds.computeIfAbsent(p.getUniqueId(), uuid -> new Long2ObjectOpenHashMap<>());
    }

    @Override
    public XChunkData rewrite(Player player, int x, int z) {
        Long2ObjectMap<XChunkData> chunks = getChunks(player);
        synchronized (chunks) {
            XChunkData chunk = chunks.get(hash(x, z));
            if (chunk != null) {
                return chunk;
            }
            if (super.mirror == super.world) {
                return null;
            } else {
                chunk = new XChunkData(this, x, z);
                chunks.put(hash(x, z), chunk);
                return chunk;
            }
        }
    }

}
