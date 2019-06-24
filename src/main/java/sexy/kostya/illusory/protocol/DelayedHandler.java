package sexy.kostya.illusory.protocol;

import com.google.common.collect.Sets;
import io.netty.util.Recycler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.Illusory;
import sexy.kostya.illusory.event.world.IBlockBreakEvent;
import sexy.kostya.illusory.event.world.IBlockInteractEvent;
import sexy.kostya.illusory.protocol.wrapper.WrappedBlockPosition;
import sexy.kostya.illusory.protocol.wrapper.WrappedDirection;
import sexy.kostya.illusory.protocol.wrapper.WrappedPlayerDigType;
import sexy.kostya.illusory.world.IllusoryWorlds;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.api.WorldProvider;
import sexy.kostya.illusory.world.impl.ChunkRewriter;
import sexy.kostya.illusory.world.impl.XChunkData;
import sexy.kostya.illusory.world.impl.provider.SemiRealWorldProvider;

import java.util.*;

import static sexy.kostya.illusory.protocol.IllusoryProtocol.*;

/**
 * Created by k.shandurenko on 24/06/2019
 */
@SuppressWarnings({"unchecked", "ConstantConditions"})
public class DelayedHandler {

    private static Recycler<PacketExecution> RECYCLER = new Recycler<PacketExecution>() {
        @Override
        protected PacketExecution newObject(Handle<PacketExecution> handle) {
            return new PacketExecution(handle);
        }
    };

    final static DelayedExecutor CHUNKS_EXECUTOR = new DelayedExecutor() {
        @Override
        public void handle(Player player, Object packet) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(player.getWorld());

            int x = CHUNK_PACKET_X.get(packet);
            int z = CHUNK_PACKET_Z.get(packet);

            XChunkData result = rewriter.rewrite(player, x, z);

            result.getMap();
            Object newPacket = CHUNK_PACKET_EMPTY_CONSTRUCTOR.invoke();
            CHUNK_PACKET_X.set(newPacket, x);
            CHUNK_PACKET_Z.set(newPacket, z);
            CHUNK_PACKET_NBT_LIST.set(newPacket, Collections.emptyList());
            CHUNK_PACKET_GROUND_FLAG.set(newPacket, true);
            Illusory.getInstance().getProtocol().sendPacket(player, newPacket);
        }
    };

    final static DelayedExecutor CHUNKS_TILE_ENTITIES_EXECUTOR = new DelayedExecutor() {
        @Override
        void handle(Player player, Object packets) {
            Illusory.getInstance().getProtocol().sendPackets(player, (Collection) packets);
        }
    };

    final static DelayedExecutor BLOCK_BREAK_EXECUTOR = new DelayedExecutor() {

        private final Set<Material> INSTA_BREAKABLE = Sets.newHashSet(
                Material.LONG_GRASS, Material.CROPS, Material.POTATO, Material.CARROT, Material.SUGAR_CANE,
                Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.RED_ROSE,
                Material.YELLOW_FLOWER, Material.DEAD_BUSH, Material.NETHER_WARTS, Material.DOUBLE_PLANT, Material.TRIPWIRE_HOOK
        );

        @Override
        public void handle(Player player, Object packet) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(player.getWorld());

            Object handle = BLOCK_DIG_POSITION.get(packet);
            WrappedBlockPosition position = WrappedBlockPosition.fromHandle(handle);
            handle = BLOCK_DIG_ACTION_TYPE.get(packet);
            WrappedPlayerDigType actionType = WrappedPlayerDigType.fromHandle(handle);

            @SuppressWarnings("unchecked")
            WorldProvider provider = (WorldProvider) rewriter;

            BlockData block = provider.getBlockSurely(
                    player,
                    position.getX(),
                    position.getY(),
                    position.getZ()
            );

            if (actionType == WrappedPlayerDigType.START_DESTROY_BLOCK && (player.getGameMode() == GameMode.CREATIVE || INSTA_BREAKABLE.contains(block.getType())) ||
                    actionType == WrappedPlayerDigType.STOP_DESTROY_BLOCK) {
                IBlockBreakEvent event = new IBlockBreakEvent(player, block);
                event.call();
                if (event.isCancelled()) {
                    return;
                }
                if (provider instanceof SemiRealWorldProvider) {
                    block.setIsReal(false);
                    ((SemiRealWorldProvider) provider).updateBlock(player, block);
                }
            }
        }
    };

    final static DelayedExecutor BLOCK_INTERACT_EXECUTOR = new DelayedExecutor() {
        @Override
        public void handle(Player player, Object packet) {
            ChunkRewriter rewriter = IllusoryWorlds.getChunkRewriter(player.getWorld());

            Object handle = USE_ITEM_POSITION.get(packet);
            WrappedBlockPosition position = WrappedBlockPosition.fromHandle(handle);
            handle = USE_ITEM_DIRECTION.get(packet);
            WrappedDirection direction = WrappedDirection.fromHandle(handle);
            @SuppressWarnings("unchecked")
            WorldProvider provider = (WorldProvider) rewriter;

            BlockData block = provider.getBlockSurely(
                    player,
                    position.getX(),
                    position.getY(),
                    position.getZ()
            );

            IBlockInteractEvent event = new IBlockInteractEvent(player, block, direction);
            event.call();
            if (provider instanceof SemiRealWorldProvider) {
                block = event.getRelativeBlockData();
                block.setIsReal(false);
                ((SemiRealWorldProvider) provider).updateBlock(player, block);
                player.updateInventory();
            }
        }
    };

    public static void initialize() {
        CHUNKS_EXECUTOR.run();
        CHUNKS_TILE_ENTITIES_EXECUTOR.run();
        BLOCK_BREAK_EXECUTOR.run();
        BLOCK_INTERACT_EXECUTOR.run();
    }

    static abstract class DelayedExecutor {

        private final List<PacketExecution> executions = new ArrayList<>();

        void enqueue(Player player, Object packet) {
            synchronized (this.executions) {
                this.executions.add(RECYCLER.get().fill(player, packet));
            }
        }

        abstract void handle(Player player, Object packet);

        private void tick() {
            synchronized (this.executions) {
                this.executions.forEach(execution -> {
                    try {
                        if (!execution.player.isOnline()) {
                            return;
                        }
                        handle(execution.player, execution.packet);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    } finally {
                        execution.release();
                    }
                });
                this.executions.clear();
            }
        }

        private void run() {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(Illusory.getInstance(), this::tick, 1L, 1L);
        }

    }

    private static class PacketExecution {

        private final Recycler.Handle<PacketExecution> handle;

        private Player player;
        private Object packet;

        PacketExecution(Recycler.Handle<PacketExecution> handle) {
            this.handle = handle;
        }

        PacketExecution fill(Player player, Object packet) {
            this.player = player;
            this.packet = packet;
            return this;
        }

        void release() {
            this.player = null;
            this.packet = null;
            this.handle.recycle(this);
        }

    }

}
