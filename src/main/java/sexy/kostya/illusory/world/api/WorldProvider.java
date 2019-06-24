package sexy.kostya.illusory.world.api;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public interface WorldProvider {

    BlockData getBlock(Player p, int x, int y, int z);

    default BlockData getBlockSurely(Player p, int x, int y, int z) {
        BlockData block = getBlock(p, x, y, z);
        if (block == null) {
            block = cast(getMirroredWorld().getBlockAt(x, y, z));
        }
        return block;
    }

    void setBlock(Player p, BlockData blockData);

    void setBlock(Player p, int x, int y, int z, Material type, int data);

    default void setBlock(Player p, int x, int y, int z, Material type) {
        setBlock(p, x, y, z, type);
    }

    void setBlocks(Player p, Collection<BlockData> blockDatas);

    Chunk getRealChunk(int x, int z);

    BlockData cast(Block block);

    World getMirroredWorld();

    World getRelativeWorld();

}
