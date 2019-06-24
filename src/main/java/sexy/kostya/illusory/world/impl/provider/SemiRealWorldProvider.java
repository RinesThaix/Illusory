package sexy.kostya.illusory.world.impl.provider;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.Illusory;
import sexy.kostya.illusory.protocol.TinyProtocol;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.api.WorldProvider;
import sexy.kostya.illusory.world.impl.ChunkRewriter;
import sexy.kostya.illusory.world.impl.TileBlockData;
import sexy.kostya.illusory.world.impl.XChunkData;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static sexy.kostya.illusory.protocol.IllusoryProtocol.*;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public abstract class SemiRealWorldProvider implements WorldProvider, ChunkRewriter {

    protected final World mirror;
    protected final World world;

    public SemiRealWorldProvider(World mirror, World world) {
        this.mirror = mirror;
        this.world = world;
    }

    @Override
    public void setBlock(Player p, int x, int y, int z, Material type, int data) {
        setBlock(p, new BlockData(x, y, z, type, data));
    }

    @Override
    public Chunk getRealChunk(int x, int z) {
        return this.mirror.getChunkAt(x, z);
    }

    @Override
    public BlockData cast(Block block) {
        BlockData data = new BlockData(
                block.getX(),
                block.getY(),
                block.getZ(),
                block.getType(),
                block.getData()
        );
        data.setIsReal(true);
        return data;
    }

    @Override
    public World getMirroredWorld() {
        return this.mirror;
    }

    @Override
    public World getRelativeWorld() {
        return this.world;
    }

    public void updateBlock(Player p, BlockData blockData) {
        XChunkData chunk = rewrite(p, blockData.getX() >> 4, blockData.getZ() >> 4);
        if (chunk == null) {
            chunk = new XChunkData(this, blockData.getX() >> 4, blockData.getZ() >> 4);
            chunk.getMap();
        }
        updateBlock(p, new TileBlockData(blockData, chunk.getTileEntityPacket(
                blockData.getX(),
                blockData.getY(),
                blockData.getZ()
        )));
    }

    void updateBlock(Player p, TileBlockData blockData) {
        updateBlocks(Collections.singleton(p), Collections.singleton(blockData.getBlockData()), null);
        if (blockData.getTileEntityPacket() != null) {
            Illusory.getInstance().getProtocol().sendPacket(p, blockData.getTileEntityPacket());
        }
    }

    void updateBlocks(Collection<Player> players, Collection<BlockData> blocks, Collection<XChunkData> chunks) {
        if (players.isEmpty() || chunks == null && blocks == null) {
            return;
        }
        if (chunks == null || blocks != null && blocks.size() <= 64) {
            multiBlockChange(players, blocks);
        } else {
            refreshChunks(players, chunks);
        }
    }

    private void multiBlockChange(Collection<Player> players, Collection<BlockData> blocks) {
        TinyProtocol protocol = Illusory.getInstance().getProtocol();
        Collection<Object> blockPackets = new HashSet<>();

        blocks.stream()
                .collect(Collectors.groupingBy(block -> hash(block.getX() >> 4, block.getZ() >> 4)))
                .values()
                .forEach(chunk -> {
                    Object packet = BLOCK_PACKET_EMPTY_CONSTRUCTOR.invoke();

                    BlockData first = chunk.iterator().next();
                    Object coords = CHUNK_COORDS_CONSTRUCTOR.invoke(first.getX() >> 4, first.getZ() >> 4);
                    BLOCK_PACKET_COORDS.set(packet, coords);

                    Object changes = Array.newInstance(BLOCK_CHANGE_INFO_CLASS, chunk.size());
                    for (int i = 0; i < chunk.size(); ++i) {
                        BlockData block = chunk.get(i);
                        Object nmsBlock, blockData;
                        if (block.isReal()) {
                            Block original = this.mirror.getBlockAt(
                                    block.getX(),
                                    block.getY(),
                                    block.getZ()
                            );
                            nmsBlock = CRAFT_MAGIC_NUMBERS_GET_BLOCK.invoke(null, original.getType());
                            blockData = BLOCK_FROM_LEGACY_DATA.invoke(nmsBlock, original.getData());
                        } else {
                            nmsBlock = CRAFT_MAGIC_NUMBERS_GET_BLOCK.invoke(null, block.getType());
                            blockData = BLOCK_FROM_LEGACY_DATA.invoke(nmsBlock, block.getData());
                        }
                        short location = (short) (((block.getX() & 15) << 12) | ((block.getZ() & 15) << 8) | block.getY());
                        Object change = BLOCK_CHANGE_INFO_CONSTRUCTOR.invoke(packet, location, blockData);
                        Array.set(changes, i, change);
                    }
                    BLOCK_PACKET_CHANGES.set(packet, changes);

                    blockPackets.add(packet);
                });

        players.forEach(player -> protocol.sendPackets(player, blockPackets));
    }

    private void refreshChunks(Collection<Player> players, Collection<XChunkData> chunks) {
        TinyProtocol protocol = Illusory.getInstance().getProtocol();
        Collection<Object> chunkPackets = new HashSet<>();
        chunks.forEach(XChunkData::getMap);
        chunks.forEach(chunk -> {
            Object packet = CHUNK_PACKET_EMPTY_CONSTRUCTOR.invoke();
            CHUNK_PACKET_X.set(packet, chunk.getX());
            CHUNK_PACKET_Z.set(packet, chunk.getZ());
            CHUNK_PACKET_NBT_LIST.set(packet, Collections.emptyList());
            CHUNK_PACKET_GROUND_FLAG.set(packet, true);
            chunkPackets.add(packet);
        });
        players.forEach(player -> protocol.sendPackets(player, chunkPackets));
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    static long hash(int x, int z) {
        return ((long) x << 32) + (long) z - -2147483648L;
    }

}
