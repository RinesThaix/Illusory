package sexy.kostya.illusory.world;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import sexy.kostya.illusory.world.api.WorldProvider;
import sexy.kostya.illusory.world.impl.ChunkRewriter;
import sexy.kostya.illusory.world.impl.provider.PersonalWorldProvider;
import sexy.kostya.illusory.world.impl.provider.PlanarWorldProvider;
import sexy.kostya.illusory.world.impl.provider.RealWorldProvider;
import sexy.kostya.illusory.world.impl.provider.SharedWorldProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class IllusoryWorlds {

    private final static Map<World, WorldProvider> PROVIDERS = new ConcurrentHashMap<>();

    public static RealWorldProvider getOrCreateRealWorldProvider(World world) {
        WorldProvider provider = PROVIDERS.get(world);
        if (provider != null) {
            Preconditions.checkState(provider instanceof RealWorldProvider, "Provider for world %s is not RealWorldProvider", world.getName());
            return (RealWorldProvider) provider;
        }
        RealWorldProvider created = new RealWorldProvider(world);
        PROVIDERS.put(world, created);
        return created;
    }

    public static SharedWorldProvider getOrCreateSharedWorldProvider(World world, World mirror) {
        WorldProvider provider = PROVIDERS.get(world);
        if (provider != null) {
            Preconditions.checkState(provider instanceof SharedWorldProvider, "Provider for world %s is not SharedWorldProvider", world.getName());
            return (SharedWorldProvider) provider;
        }
        SharedWorldProvider created = new SharedWorldProvider(mirror, world);
        PROVIDERS.put(world, created);
        return created;
    }

    public static PersonalWorldProvider getOrCreatePersonalWorldProvider(World world, World mirror) {
        WorldProvider provider = PROVIDERS.get(world);
        if (provider != null) {
            Preconditions.checkState(provider instanceof PersonalWorldProvider, "Provider for world %s is not PersonalWorldProvider", world.getName());
            return (PersonalWorldProvider) provider;
        }
        PersonalWorldProvider created = new PersonalWorldProvider(mirror, world);
        PROVIDERS.put(world, created);
        return created;
    }

    public static PlanarWorldProvider getOrCreatePlanarWorldProvider(World world, World mirror) {
        WorldProvider provider = PROVIDERS.get(world);
        if (provider != null) {
            Preconditions.checkState(provider instanceof PlanarWorldProvider, "Provider for world %s is not PlanarWorldProvider", world.getName());
            return (PlanarWorldProvider) provider;
        }
        PlanarWorldProvider created = new PlanarWorldProvider(mirror, world);
        PROVIDERS.put(world, created);
        return created;
    }

    public static <T extends WorldProvider> T getWorldProvider(World world) {
        return (T) PROVIDERS.get(world);
    }

    public static ChunkRewriter getChunkRewriter(World world) {
        WorldProvider provider = PROVIDERS.get(world);
        if (provider == null || !(provider instanceof ChunkRewriter)) {
            return null;
        }
        return (ChunkRewriter) provider;
    }

    public static World createEmptyWorld(String worldName) {
        return Bukkit.createWorld(new WorldCreator(worldName).type(WorldType.FLAT).generatorSettings("2;0;1;"));
    }

}
