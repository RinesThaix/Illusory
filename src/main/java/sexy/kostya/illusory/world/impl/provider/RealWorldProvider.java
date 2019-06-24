package sexy.kostya.illusory.world.impl.provider;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.api.WorldProvider;

import java.util.Collection;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class RealWorldProvider implements WorldProvider {

    private final World world;

    public RealWorldProvider(World world) {
        this.world = world;
    }

    @Override
    public BlockData getBlock(Player p, int x, int y, int z) {
        Block block = this.world.getBlockAt(x, y, z);
        BlockData data = new BlockData(x, y, z, block.getType(), block.getData());
        data.setIsReal(true);
        return data;
    }

    @Override
    public void setBlock(Player p, BlockData blockData) {
        Block block = this.world.getBlockAt(
                blockData.getX(),
                blockData.getY(),
                blockData.getZ()
        );
        block.setType(blockData.getType());
        block.setData(blockData.getData());
    }

    @Override
    public void setBlock(Player p, int x, int y, int z, Material type, int data) {
        setBlock(p, new BlockData(x, y, z, type, data));
    }

    @Override
    public void setBlocks(Player p, Collection<BlockData> blockDatas) {
        blockDatas.forEach(bd -> setBlock(p, bd));
    }

    @Override
    public Chunk getRealChunk(int x, int z) {
        return this.world.getChunkAt(x, z);
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
        return this.world;
    }

    @Override
    public World getRelativeWorld() {
        return this.world;
    }

}
