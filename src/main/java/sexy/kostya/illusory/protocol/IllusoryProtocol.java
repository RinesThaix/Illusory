package sexy.kostya.illusory.protocol;

import io.netty.channel.Channel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.Illusory;
import sexy.kostya.illusory.Reflection;
import sexy.kostya.illusory.world.IllusoryWorlds;
import sexy.kostya.illusory.world.impl.ChunkMap;
import sexy.kostya.illusory.world.impl.ChunkRewriter;
import sexy.kostya.illusory.world.impl.XChunkData;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class IllusoryProtocol extends TinyProtocol {

    private final static String CHUNK_PACKET_CLASS_NAME = "{nms}.PacketPlayOutMapChunk";
    private final static String BLOCK_PACKET_CLASS_NAME = "{nms}.PacketPlayOutMultiBlockChange";
    private final static Class<?> CHUNK_PACKET_CLASS = Reflection.getClass(CHUNK_PACKET_CLASS_NAME);
    private final static Class<?> BLOCK_PACKET_CLASS = Reflection.getClass(BLOCK_PACKET_CLASS_NAME);
    private final static Class<?> CHUNK_COORDS_CLASS = Reflection.getClass("{nms}.ChunkCoordIntPair");
    public final static Class<?> BLOCK_CHANGE_INFO_CLASS = Reflection.getClass("{nms}.PacketPlayOutMultiBlockChange$MultiBlockChangeInfo");
    private final static Class<?> CRAFT_MAGIC_NUMBERS_CLASS = Reflection.getClass("{obc}.util.CraftMagicNumbers");
    private final static Class<?> BLOCK_CLASS = Reflection.getClass("{nms}.Block");
    private final static Class<?> IBLOCK_DATA_CLASS = Reflection.getClass("{nms}.IBlockData");
    private final static Class<?> BLOCK_DIG_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayInBlockDig");
    private final static Class<?> USE_ITEM_PACKET_CLASS = Reflection.getClass("{nms}.PacketPlayInUseItem");
    public final static Reflection.ConstructorInvoker CHUNK_PACKET_CONSTRUCTOR = Reflection.getConstructor(
            CHUNK_PACKET_CLASS_NAME,
            Reflection.getClass("{nms}.Chunk"), int.class
    );
    public final static Reflection.ConstructorInvoker CHUNK_PACKET_EMPTY_CONSTRUCTOR = Reflection.getConstructor(
            CHUNK_PACKET_CLASS_NAME
    );
    public final static Reflection.ConstructorInvoker BLOCK_PACKET_EMPTY_CONSTRUCTOR = Reflection.getConstructor(
            BLOCK_PACKET_CLASS_NAME
    );
    public final static Reflection.ConstructorInvoker CHUNK_COORDS_CONSTRUCTOR = Reflection.getConstructor(
            CHUNK_COORDS_CLASS,
            int.class, int.class
    );
    public final static Reflection.ConstructorInvoker BLOCK_CHANGE_INFO_CONSTRUCTOR = Reflection.getConstructor(
            BLOCK_CHANGE_INFO_CLASS,
            Reflection.getClass("{nms}.PacketPlayOutMultiBlockChange"), short.class, IBLOCK_DATA_CLASS
    );
    public final static Reflection.MethodInvoker CHUNK_TO_HANDLE = Reflection.getMethod(
            "{obc}.CraftChunk",
            "getHandle"
    );
    public final static Reflection.MethodInvoker CHUNK_GET_TILE_ENTITIES = Reflection.getMethod(
            "{nms}.Chunk",
            "getTileEntities"
    );
    public final static Reflection.MethodInvoker TILE_ENTITY_GET_PACKET = Reflection.getMethod(
            "{nms}.TileEntity",
            "getUpdatePacket"
    );
    public final static Reflection.MethodInvoker TILE_ENTITY_GET_POSITION = Reflection.getMethod(
            "{nms}.TileEntity",
            "getPosition"
    );
    public final static Reflection.MethodInvoker CRAFT_MAGIC_NUMBERS_GET_BLOCK = Reflection.getMethod(
            CRAFT_MAGIC_NUMBERS_CLASS,
            "getBlock",
            Material.class
    );
    public final static Reflection.MethodInvoker BLOCK_FROM_LEGACY_DATA = Reflection.getMethod(
            BLOCK_CLASS,
            "fromLegacyData",
            int.class
    );
    public final static Reflection.FieldAccessor<Integer> CHUNK_PACKET_X = Reflection.getField(
            CHUNK_PACKET_CLASS,
            int.class,
            0
    );
    public final static Reflection.FieldAccessor<Integer> CHUNK_PACKET_Z = Reflection.getField(
            CHUNK_PACKET_CLASS,
            int.class,
            1
    );
    public final static Reflection.FieldAccessor<Integer> CHUNK_PACKET_MASK = Reflection.getField(
            CHUNK_PACKET_CLASS,
            int.class,
            2
    );
    public final static Reflection.FieldAccessor<byte[]> CHUNK_PACKET_DATA = Reflection.getField(
            CHUNK_PACKET_CLASS,
            byte[].class,
            0
    );
    public final static Reflection.FieldAccessor<List> CHUNK_PACKET_NBT_LIST = Reflection.getField(
            CHUNK_PACKET_CLASS,
            List.class,
            0
    );
    public final static Reflection.FieldAccessor<Boolean> CHUNK_PACKET_GROUND_FLAG = Reflection.getField(
            CHUNK_PACKET_CLASS,
            boolean.class,
            0
    );
    public final static Reflection.FieldAccessor<?> BLOCK_PACKET_COORDS = Reflection.getField(
            BLOCK_PACKET_CLASS,
            CHUNK_COORDS_CLASS,
            0
    );
    public final static Reflection.FieldAccessor<?> BLOCK_PACKET_CHANGES = Reflection.getField(
            BLOCK_PACKET_CLASS,
            Array.newInstance(BLOCK_CHANGE_INFO_CLASS, 0).getClass(),
            0
    );
    final static Reflection.FieldAccessor<?> BLOCK_DIG_POSITION = Reflection.getField(
            BLOCK_DIG_PACKET_CLASS,
            Reflection.getClass("{nms}.BlockPosition"),
            0
    );
    final static Reflection.FieldAccessor<?> BLOCK_DIG_ACTION_TYPE = Reflection.getField(
            BLOCK_DIG_PACKET_CLASS,
            Reflection.getClass("{nms}.PacketPlayInBlockDig$EnumPlayerDigType"),
            0
    );
    final static Reflection.FieldAccessor<?> USE_ITEM_POSITION = Reflection.getField(
            USE_ITEM_PACKET_CLASS,
            Reflection.getClass("{nms}.BlockPosition"),
            0
    );
    final static Reflection.FieldAccessor<?> USE_ITEM_DIRECTION = Reflection.getField(
            USE_ITEM_PACKET_CLASS,
            Reflection.getClass("{nms}.EnumDirection"),
            0
    );

    public IllusoryProtocol(Illusory plugin) {
        super(plugin);
    }

    @Override
    public Object onPacketOutAsync(Player receiver, Channel channel, Object packet) {
        if (packet.getClass() == CHUNK_PACKET_CLASS) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(receiver.getWorld());
            if (rewriter != null) {
                int x = CHUNK_PACKET_X.get(packet);
                int z = CHUNK_PACKET_Z.get(packet);
                XChunkData result = rewriter.rewrite(receiver, x, z);
                if (result != null) {
                    ChunkMap map = result.getCachedMap();
                    if (map != null) {
                        CHUNK_PACKET_MASK.set(packet, map.c);
                        CHUNK_PACKET_DATA.set(packet, map.d);
                        Collection<Object> tileEntitiesPackets = result.getTileEntitiesPackets();
                        if (!tileEntitiesPackets.isEmpty()) {
                            DelayedHandler.CHUNKS_TILE_ENTITIES_EXECUTOR.enqueue(receiver, tileEntitiesPackets);
                        }
                    } else {
                        DelayedHandler.CHUNKS_EXECUTOR.enqueue(receiver, packet);
                        return null;
                    }
                }
            }
        }
        return packet;
    }

    @Override
    public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
        if (packet.getClass() == BLOCK_DIG_PACKET_CLASS) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(sender.getWorld());
            if (rewriter != null) {
                DelayedHandler.BLOCK_BREAK_EXECUTOR.enqueue(sender, packet);
                return null;
            }
        } else if (packet.getClass() == USE_ITEM_PACKET_CLASS) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(sender.getWorld());
            if (rewriter != null) {
                DelayedHandler.BLOCK_INTERACT_EXECUTOR.enqueue(sender, packet);
                return null;
            }
        }
        return packet;
    }

}
