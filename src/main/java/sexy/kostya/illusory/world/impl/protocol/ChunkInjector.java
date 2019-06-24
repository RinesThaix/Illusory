package sexy.kostya.illusory.world.impl.protocol;

import org.bukkit.World;
import sexy.kostya.illusory.world.api.BlockData;
import sexy.kostya.illusory.world.api.MaterialData;
import sexy.kostya.illusory.world.impl.ChunkMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class ChunkInjector {

    public static void inject(World world, ChunkMap map, Map<Integer, Collection<BlockData>> perSections) {
        if (perSections.isEmpty()) {
            return;
        }
        int originalMask = map.c;
        for (int section : perSections.keySet()) {
            map.c |= 1 << section;
        }
        ChunkHolder chunkHolder = splitToChunkSections(map.c, originalMask, map.d, world.getEnvironment() == World.Environment.NORMAL);
        insert(chunkHolder.getSections(), perSections);
        try {
            map.d = toByteArray(chunkHolder);
        } catch (IOException ex) {
            map.c = originalMask;
            ex.printStackTrace();
        }
    }

    private static byte[] toByteArray(ChunkHolder holder) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ChunkSection section : holder.getSections()) {
            if (section != null) {
                writeChunkSectionData(baos, section);
            }
        }
        baos.write(holder.getAdditionalData());
        return baos.toByteArray();
    }

    private static void writeChunkSectionData(ByteArrayOutputStream baos, ChunkSection section) throws IOException {
        MaterialData[] used = section.getContainedBlocks();
        BlockPalette palette;
        if (used == null) {
            palette = BlockPalette.GLOBAL;
        } else {
            palette = BlockPalette.createPalette(used);
        }
        byte bpb = (byte) palette.getBitsPerBlock();
        int paletteLength = palette.getLength();
        int[] paletteInfo;
        if (paletteLength == 0) {
            paletteInfo = new int[0];
        } else {
            paletteInfo = palette.getPaletteData();
        }
        byte[] MaterialData = palette.encode(section.getMaterialData());
        byte[] lightingData = section.getLightingData();
        baos.write(bpb);
        ByteBufferReader.writeVarInt(paletteLength, baos);
        for (int p : paletteInfo) {
            ByteBufferReader.writeVarInt(p, baos);
        }
        ByteBufferReader.writeVarInt(MaterialData.length >> 3, baos);
        baos.write(MaterialData);
        baos.write(lightingData);
    }

    private static void insert(ChunkSection[] sections, Map<Integer, Collection<BlockData>> blocks) {
        blocks.forEach((sectionID, group) -> {
            ChunkSection section = sections[sectionID];
            group.forEach(block -> {
                MaterialData data = new MaterialData(block.getType(), block.getData());
                section.setBlockRelative(
                        data,
                        block.getX() & 15,
                        block.getY() % 16,
                        block.getZ() & 15
                );
            });
        });
    }

    private static ChunkHolder splitToChunkSections(int bitmask, int originalMask, byte[] data, boolean isOverworld) {
        int skylightLength = isOverworld ? (1 << 11) : 0;
        ChunkSection[] sections = new ChunkSection[16];
        ByteBuffer buffer = ByteBuffer.wrap(data);
        ByteBufferReader bbr = new ByteBufferReader(buffer);
        for (int i = 0; i < 16; i++) {
            if ((bitmask & 0x8000 >> (15 - i)) != 0) {
                if ((originalMask & 0x8000 >> (15 - i)) != 0) {
                    short bpb = (short) Byte.toUnsignedInt(buffer.get());
                    int paletteLength = bbr.readVarInt();
                    BlockPalette palette;
                    if (paletteLength != 0 || bpb < 9) {
                        int[] paletteData = new int[paletteLength];
                        for (int j = 0; j < paletteLength; j++) {
                            paletteData[j] = bbr.readVarInt();
                        }
                        palette = BlockPalette.createPalette(paletteData, bpb);
                    } else {
                        palette = BlockPalette.GLOBAL;
                    }
                    int dataLength = bbr.readVarInt();
                    byte[] MaterialData = new byte[dataLength << 3];
                    buffer.get(MaterialData);
                    byte[] lightingData = new byte[(1 << 11) + skylightLength];
                    buffer.get(lightingData);
                    sections[i] = new ChunkSection(MaterialData, lightingData, palette);
                } else {
                    sections[i] = new ChunkSection(isOverworld);
                }
            }
        }
        byte[] additional = new byte[data.length - buffer.position()];
        buffer.get(additional);
        return new ChunkHolder(sections, additional);
    }

}
