package sexy.kostya.illusory.world.impl;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import sexy.kostya.illusory.protocol.IllusoryProtocol;
import sexy.kostya.illusory.protocol.wrapper.WrappedBlockPosition;
import sexy.kostya.illusory.world.api.WorldProvider;

import java.lang.ref.WeakReference;
import java.util.Map;

import static sexy.kostya.illusory.protocol.IllusoryProtocol.*;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class XChunkData extends AbstractChunkData {

    private final WeakReference<WorldProvider> world;

    public XChunkData(WorldProvider world, int x, int z) {
        super(x, z);
        this.world = new WeakReference<>(world);
    }

    @Override
    public synchronized ChunkMap getMap() {
        initializeTileEntitiesPackets();
        return super.getMap();
    }

    @Override
    void initializeTileEntitiesPackets() {
        if (super.tileEntitiesPackets != null) {
            return;
        }
        Chunk chunk = this.world.get().getRealChunk(super.x, super.z);
        Object handle = CHUNK_TO_HANDLE.invoke(chunk);
        Map tileEntities = (Map) CHUNK_GET_TILE_ENTITIES.invoke(handle);
        super.tileEntitiesPackets = new Int2ObjectOpenHashMap<>();
        tileEntities.values().forEach(te -> {
            Object packet = TILE_ENTITY_GET_PACKET.invoke(te);
            Object positionHandle = TILE_ENTITY_GET_POSITION.invoke(te);
            WrappedBlockPosition position = WrappedBlockPosition.fromHandle(positionHandle);
            if (packet != null) {
                super.tileEntitiesPackets.put(
                        hash(
                                position.getX(),
                                position.getY(),
                                position.getZ()
                        ),
                        packet
                );
            }
        });
    }

    @Override
    protected ChunkMap getOriginalMap() {
        Chunk chunk = this.world.get().getRealChunk(super.x, super.z);

        Object packet = IllusoryProtocol.CHUNK_PACKET_CONSTRUCTOR.invoke(
                IllusoryProtocol.CHUNK_TO_HANDLE.invoke(chunk),
                65535
        );

        return new ChunkMap(
                IllusoryProtocol.CHUNK_PACKET_MASK.get(packet),
                IllusoryProtocol.CHUNK_PACKET_DATA.get(packet)
        );
    }

    @Override
    protected World getRelativeWorld() {
        return this.world.get().getRelativeWorld();
    }

}
