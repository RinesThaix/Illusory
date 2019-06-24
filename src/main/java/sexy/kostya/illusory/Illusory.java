package sexy.kostya.illusory;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import sexy.kostya.illusory.protocol.DelayedHandler;
import sexy.kostya.illusory.protocol.IllusoryProtocol;
import sexy.kostya.illusory.protocol.TinyProtocol;
import sexy.kostya.illusory.world.IllusoryWorlds;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.impl.EmptyRealBlockData;
import sexy.kostya.illusory.world.impl.provider.PlanarWorldProvider;

import java.util.Collection;
import java.util.HashSet;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class Illusory extends JavaPlugin {

    private static Illusory INSTANCE;

    private TinyProtocol protocol;

    public static Illusory getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.protocol = new IllusoryProtocol(this);
        DelayedHandler.initialize();
        Bukkit.getPluginManager().registerEvents(new Listener() {

            @EventHandler
            public void onChat(AsyncPlayerChatEvent e) {
                if (e.getMessage().equals("create")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        World created = IllusoryWorlds.createEmptyWorld("test_world");
                        PlanarWorldProvider world = IllusoryWorlds.getOrCreatePlanarWorldProvider(created, e.getPlayer().getWorld());
                        world.createNewPlane("test");
                        world.changePlayersPlane(e.getPlayer(), "test");
                        Location initial = e.getPlayer().getLocation();
                        Location location = new Location(
                                created,
                                initial.getX(),
                                initial.getY(),
                                initial.getZ()
                        );
                        e.getPlayer().teleport(location);
                    });
                } else if (e.getMessage().equals("switch")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (!world.doesPlaneExist("test2")) {
                            world.createNewPlane("test2");
                        }
                        if (world.getPlayerPlane(e.getPlayer()).equals("test")) {
                            world.changePlayersPlane(e.getPlayer(), "test2");
                        } else {
                            world.changePlayersPlane(e.getPlayer(), "test");
                        }
                    });
                } else if (e.getMessage().equals("single")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (world == null) {
                            return;
                        }
                        Location initial = e.getPlayer().getLocation();
                        world.setBlock(e.getPlayer(), new BlockData(
                                initial.getBlockX(),
                                initial.getBlockY() + 2,
                                initial.getBlockZ(),
                                Material.GOLD_BLOCK
                        ));
                    });
                } else if (e.getMessage().equals("multiblock")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (world == null) {
                            return;
                        }
                        Location initial = e.getPlayer().getLocation();
                        Collection<BlockData> blocks = new HashSet<>();
                        for (int x = initial.getBlockX() - 2; x <= initial.getBlockX() + 2; x++) {
                            for (int z = initial.getBlockZ() - 2; z <= initial.getBlockZ() + 2; z++) {
                                if (Math.abs(x - initial.getBlockX()) <= 1 && Math.abs(z - initial.getBlockZ()) <= 1) {
                                    continue;
                                }
                                for (int y = initial.getBlockY(); y <= initial.getBlockY() + 2; y++) {
                                    blocks.add(new BlockData(
                                            x,
                                            y,
                                            z,
                                            Material.DIAMOND_BLOCK
                                    ));
                                }
                            }
                        }
                        world.setBlocks(e.getPlayer(), blocks);
                    });
                } else if (e.getMessage().equals("alot")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (world == null) {
                            return;
                        }
                        Location initial = e.getPlayer().getLocation();
                        Collection<BlockData> blocks = new HashSet<>();
                        for (int x = initial.getBlockX() - 50; x <= initial.getBlockX() + 50; x++) {
                            for (int z = initial.getBlockZ() - 50; z <= initial.getBlockZ() + 50; z++) {
                                if (Math.abs(x - initial.getBlockX()) <= 1 && Math.abs(z - initial.getBlockZ()) <= 1) {
                                    continue;
                                }
                                for (int y = initial.getBlockY(); y <= initial.getBlockY() + 2; y++) {
                                    blocks.add(new BlockData(
                                            x,
                                            y,
                                            z,
                                            Material.WOOL,
                                            15
                                    ));
                                }
                            }
                        }
                        world.setBlocks(e.getPlayer(), blocks);
                    });
                } else if (e.getMessage().equals("wipe")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (world == null) {
                            return;
                        }
                        Location initial = e.getPlayer().getLocation();
                        Collection<BlockData> blocks = new HashSet<>();
                        for (int x = initial.getBlockX() - 50; x <= initial.getBlockX() + 50; x++) {
                            for (int z = initial.getBlockZ() - 50; z <= initial.getBlockZ() + 50; z++) {
                                if (Math.abs(x - initial.getBlockX()) <= 1 && Math.abs(z - initial.getBlockZ()) <= 1) {
                                    continue;
                                }
                                for (int y = initial.getBlockY(); y <= initial.getBlockY() + 2; y++) {
                                    blocks.add(new EmptyRealBlockData(x, y, z));
                                }
                            }
                        }
                        world.setBlocks(e.getPlayer(), blocks);
                    });
                } else if (e.getMessage().equals("update")) {
                    e.setCancelled(true);
                    Bukkit.getScheduler().scheduleSyncDelayedTask(Illusory.this, () -> {
                        if (!e.getPlayer().isOnline()) {
                            return;
                        }
                        PlanarWorldProvider world = IllusoryWorlds.getWorldProvider(e.getPlayer().getWorld());
                        if (world == null) {
                            return;
                        }
                        world.updateAll(e.getPlayer());
                    });
                }
            }

        }, this);
    }

    @Override
    public void onDisable() {

    }

    public TinyProtocol getProtocol() {
        return protocol;
    }
}
