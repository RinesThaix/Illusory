package sexy.kostya.illusory.world.impl;

import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public interface ChunkRewriter {

    XChunkData rewrite(Player player, int x, int z);

    World getWorld();

}
