package sexy.kostya.illusory.world.impl.protocol;

import org.bukkit.Material;
import sexy.kostya.illusory.world.api.ChunkData;
import sexy.kostya.illusory.world.api.MaterialData;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by k.shandurenko on 23/06/2019
 */
class ChunkSection {

    private final MaterialData[] blocks;
    private final byte[] lightData;
    private MaterialData[] paletteBlocks;
    private final Set<MaterialData> paletteBlockSet;

    ChunkSection(byte[] MaterialData, byte[] lightData, BlockPalette palette) {
        this.blocks = palette.decode(MaterialData);
        paletteBlocks = palette.getMaterials();
        if (paletteBlocks != null) {
            paletteBlockSet = new HashSet<>();
            paletteBlockSet.addAll(Arrays.asList(paletteBlocks));
        } else {
            paletteBlockSet = null;
        }
        this.lightData = lightData;
    }

    ChunkSection(boolean overworld) {
        MaterialData air = new MaterialData(Material.AIR, (byte) 0);
        blocks = new MaterialData[1 << 12];
        Arrays.fill(blocks, air);
        paletteBlocks = new MaterialData[]{air};
        paletteBlockSet = new HashSet<>();
        paletteBlockSet.add(air);
        lightData = new byte[1 << (overworld ? 12 : 11)];
        Arrays.fill(lightData, (byte) -1);
    }

    void setBlockRelative(MaterialData data, int x, int y, int z) {
        blocks[ChunkData.hash(x, y, z)] = data;
        if (paletteBlockSet != null) {
            if (!paletteBlockSet.contains(data)) {
                paletteBlocks = null;
            }
            paletteBlockSet.add(data);
        }
    }

    MaterialData[] getMaterialData() {
        return blocks;
    }

    MaterialData[] getContainedBlocks() {
        if (paletteBlocks == null) {
            if (paletteBlockSet == null) {
                return null;
            }
            paletteBlocks = paletteBlockSet.toArray(new MaterialData[0]);
        }
        return paletteBlocks;
    }

    byte[] getLightingData() {
        return lightData;
    }

}