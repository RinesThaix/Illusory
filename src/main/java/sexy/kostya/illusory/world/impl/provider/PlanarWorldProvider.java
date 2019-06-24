package sexy.kostya.illusory.world.impl.provider;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.Illusory;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.impl.XChunkData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class PlanarWorldProvider extends SemiRealWorldProvider {

    private final Map<String, Plane> planes = new ConcurrentHashMap<>();
    private final Map<UUID, String> playersToPlanes = new ConcurrentHashMap<>();

    public PlanarWorldProvider(World mirror, World world) {
        super(mirror, world);
    }

    @Override
    public BlockData getBlock(Player p, int x, int y, int z) {
        String planeName = getPlayerPlane(p);
        Preconditions.checkState(planeName != null, "Player %s is not at any plane", p.getName());
        return getBlock(planeName, x, y, z);
    }

    @Override
    public void setBlock(Player p, BlockData blockData) {
        String planeName = getPlayerPlane(p);
        Preconditions.checkState(planeName != null, "Player %s is not at any plane", p.getName());
        setBlock(planeName, blockData);
    }

    @Override
    public void setBlocks(Player p, Collection<BlockData> blockDatas) {
        String planeName = getPlayerPlane(p);
        Preconditions.checkState(planeName != null, "Player %s is not at any plane", p.getName());
        setBlocks(planeName, blockDatas);
    }

    public void createNewPlane(String planeName) {
        Preconditions.checkArgument(!this.planes.containsKey(planeName), "Plane %s already exists", planeName);
        this.planes.put(planeName, new Plane(planeName));
    }

    public boolean doesPlaneExist(String planeName) {
        return this.planes.containsKey(planeName);
    }

    public void changePlayersPlane(Player player, String planeName) {
        if (!player.isOnline()) {
            return;
        }
        Plane plane = this.planes.get(planeName);
        Preconditions.checkArgument(plane != null, "Plane %s does not exist", planeName);
        String previousPlaneName = this.playersToPlanes.get(player.getUniqueId());
        Plane previousPlane = previousPlaneName == null ? null : this.planes.get(previousPlaneName);
        if (player.getWorld() != super.world) {
            if (previousPlane != null) {
                synchronized (previousPlane) {
                    previousPlane.players.remove(player);
                }
            }
            synchronized (plane) {
                plane.players.add(player);
                this.playersToPlanes.put(player.getUniqueId(), planeName);
            }
            return;
        }
        Preconditions.checkState(previousPlane != null, "You can not setup first plane of player whilst he is already in planar world");
        Plane[] planes = new Plane[]{plane, previousPlane};
        Arrays.sort(planes, Comparator.comparing(p -> p.identifier));
        Collection<Long> chunkIDs = new HashSet<>();
        synchronized (planes[0]) {
            synchronized (planes[1]) {
                previousPlane.players.remove(player);
                plane.players.add(player);
                previousPlane.chunks.values().forEach(chunk -> chunkIDs.add(hash(chunk.getX(), chunk.getZ())));
                plane.chunks.values().forEach(chunk -> chunkIDs.add(hash(chunk.getX(), chunk.getZ())));
                this.playersToPlanes.put(player.getUniqueId(), planeName);
            }
        }
        synchronized (plane) {
            Collection<XChunkData> chunks = new HashSet<>();
            chunkIDs.forEach(hash -> {
                int x = (int) (hash >> 32);
                int z = (int) (hash & 0xFFFFFFFF) + Integer.MIN_VALUE;
                chunks.add(plane.chunks.computeIfAbsent(hash, h -> new XChunkData(this, x, z)));
            });
            updateBlocks(Collections.singleton(player), null, chunks);
            Bukkit.getOnlinePlayers().forEach(p2 -> {
                if (player == p2) {
                    return;
                }
                if (plane.players.contains(p2)) {
                    player.showPlayer(Illusory.getInstance(), p2);
                    p2.showPlayer(Illusory.getInstance(), player);
                } else {
                    player.hidePlayer(Illusory.getInstance(), p2);
                    p2.hidePlayer(Illusory.getInstance(), player);
                }
            });
        }
    }

    public void updateAll(Player p) {
        String planeName = getPlayerPlane(p);
        Preconditions.checkState(planeName != null, "Player %s is not at any plane", p.getName());
        Plane plane = planes.get(planeName);
        synchronized (plane) {
            updateBlocks(Collections.singleton(p), null, new HashSet<>(plane.chunks.values()));
        }
    }

    public void removeExistingPlane(String planeName) {
        Plane plane = this.planes.get(planeName);
        Preconditions.checkArgument(plane != null, "Plane %s does not exist", planeName);
        synchronized (plane) {
            Preconditions.checkState(plane.players.isEmpty(), "Plane %s can not be deleted: there are players", planeName);
            this.planes.remove(planeName);
        }
    }

    private BlockData getBlock(String planeName, int x, int y, int z) {
        Plane plane = this.planes.get(planeName);
        Preconditions.checkArgument(plane != null, "Plane %s does not exist", planeName);
        synchronized (plane) {
            XChunkData chunk = plane.chunks.computeIfAbsent(
                    hash(x >> 4, z >> 4),
                    h -> new XChunkData(this, x >> 4, z >> 4)
            );
            return chunk.getBlock(x, y, z);
        }
    }

    public void setBlock(String planeName, BlockData blockData) {
        Plane plane = this.planes.get(planeName);
        Preconditions.checkArgument(plane != null, "Plane %s does not exist", planeName);
        synchronized (plane) {
            int cx = blockData.getX() >> 4, cz = blockData.getZ() >> 4;
            XChunkData chunk = plane.chunks.computeIfAbsent(
                    hash(cx, cz),
                    h -> new XChunkData(this, cx, cz)
            );
            chunk.setBlock(blockData);
            updateBlocks(plane.players, Collections.singleton(blockData), null);
        }
    }

    public void setBlocks(String planeName, Collection<BlockData> blockDatas) {
        Plane plane = this.planes.get(planeName);
        Preconditions.checkArgument(plane != null, "Plane %s does not exist", planeName);
        Long2ObjectMap<Collection<BlockData>> perChunks = new Long2ObjectOpenHashMap<>();
        blockDatas.forEach(block -> {
            int cx = block.getX() >> 4, cz = block.getZ() >> 4;
            perChunks.computeIfAbsent(
                    hash(cx, cz),
                    h -> new HashSet<>()
            ).add(block);
        });
        Set<XChunkData> chunks = new HashSet<>();
        synchronized (plane) {
            perChunks.forEach((hash, blocks) -> {
                XChunkData chunk = plane.chunks.computeIfAbsent(hash, h -> {
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
            updateBlocks(plane.players, blockDatas, chunks);
        }
    }

    public String getPlayerPlane(UUID uuid) {
        return this.playersToPlanes.get(uuid);
    }

    public String getPlayerPlane(Player player) {
        return getPlayerPlane(player.getUniqueId());
    }

    public Collection<Player> getPlanePlayers(String planeName) {
        Plane plane = this.planes.get(planeName);
        if (plane == null) {
            return null;
        }
        synchronized (plane) {
            return Collections.unmodifiableCollection(new HashSet<>(plane.players));
        }
    }

    @Override
    public XChunkData rewrite(Player player, int x, int z) {
        String planeName = getPlayerPlane(player);
        if (planeName == null) {
            return null;
        }
        Plane plane = this.planes.get(planeName);
        if (plane == null) {
            return null;
        }
        synchronized (plane) {
            XChunkData chunk = plane.chunks.get(hash(x, z));
            if (chunk != null) {
                return chunk;
            }
            if (super.mirror == super.world) {
                return null;
            } else {
                chunk = new XChunkData(this, x, z);
                plane.chunks.put(hash(x, z), chunk);
                return chunk;
            }
        }
    }

    private static class Plane {

        private final String identifier;
        private final Collection<Player> players = new HashSet<>();
        private final Long2ObjectMap<XChunkData> chunks = new Long2ObjectOpenHashMap<>();

        public Plane(String identifier) {
            this.identifier = identifier;
        }

    }

}
