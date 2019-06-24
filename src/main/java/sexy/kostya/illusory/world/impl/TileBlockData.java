package sexy.kostya.illusory.world.impl;

import sexy.kostya.illusory.world.api.BlockData;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public class TileBlockData {

    private final BlockData blockData;
    private Object tileEntityPacket;

    public TileBlockData(BlockData blockData, Object tileEntityPacket) {
        this.blockData = blockData;
        this.tileEntityPacket = tileEntityPacket;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public Object getTileEntityPacket() {
        return tileEntityPacket;
    }

}
