package sexy.kostya.illusory.world.impl.protocol;

import org.bukkit.Material;
import sexy.kostya.illusory.world.api.MaterialData;

import java.util.HashMap;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public abstract class BlockPalette {

    @SuppressWarnings("StaticInitializerReferencesSubClass")
    static final BlockPalette GLOBAL = GlobalBlockPalette.instance();

    public abstract MaterialData[] decode(byte[] data);

    public abstract MaterialData[] getMaterials();

    public abstract int getBitsPerBlock();

    public abstract int getLength();

    public abstract int[] getPaletteData();

    public abstract byte[] encode(MaterialData[] data);

    static BlockPalette createPalette(int[] data, int bitsPerBlock) {
        return new EncodedBlockPalette(data, bitsPerBlock);
    }

    static BlockPalette createPalette(MaterialData[] data) {
        int bitsPerBlock = Math.max(32 - Integer.numberOfLeadingZeros(data.length - 1), 4);
        if (bitsPerBlock < 9) {
            return new EncodedBlockPalette(data, bitsPerBlock);
        } else {
            return GLOBAL;
        }
    }

    private static class GlobalBlockPalette extends BlockPalette {

        @Override
        public MaterialData[] decode(byte[] data) {
            LABReader reader = new LABReader(data);
            MaterialData[] bdata = new MaterialData[1 << 12];
            for (int i = 0; i < bdata.length; i++) {
                byte damage = reader.readByte(4);
                int id = reader.readInt(9);
                bdata[i] = new MaterialData(Material.getMaterial(id), damage);
            }
            return bdata;
        }

        static GlobalBlockPalette instance() {
            return new GlobalBlockPalette();
        }

        @Override
        public MaterialData[] getMaterials() {
            return null;
        }

        @Override
        public int getBitsPerBlock() {
            return 13;
        }

        @Override
        public byte[] encode(MaterialData[] data) {
            byte[] array = new byte[6656];
            LABWriter writer = new LABWriter(array);
            for (MaterialData block : data) {
                writer.writeByte(block.getData(), 4);
                writer.writeInt(block.getType().getId(), 9);
            }
            return array;
        }

        @Override
        public int getLength() {
            return 0;
        }

        @Override
        public int[] getPaletteData() {
            return new int[0];
        }
    }

    private static class EncodedBlockPalette extends BlockPalette {

        private final MaterialData[] lookupTable;
        private final int bitsPerBlock;

        EncodedBlockPalette(int[] data, int bitsPerBlock) {
            this.bitsPerBlock = bitsPerBlock;
            this.lookupTable = createLookupTable(data);
        }

        EncodedBlockPalette(MaterialData[] lookupTable, int bitsPerBlock) {
            this.lookupTable = lookupTable;
            this.bitsPerBlock = bitsPerBlock;
        }

        private MaterialData[] createLookupTable(int[] data) {
            MaterialData[] lookupTable = new MaterialData[data.length];
            for (int i = 0; i < data.length; i++) {
                byte damage = (byte) (data[i] & 0xF);
                int id = data[i] >> 4;
                lookupTable[i] = new MaterialData(Material.getMaterial(id), damage);
            }
            return lookupTable;
        }

        @Override
        public MaterialData[] decode(byte[] data) {
            LABReader reader = new LABReader(data);
            MaterialData[] array = new MaterialData[4096];
            for (int i = 0; i < array.length; i++) {
                array[i] = lookupTable[reader.readShort(bitsPerBlock)];
            }
            return array;
        }

        @Override
        public MaterialData[] getMaterials() {
            return lookupTable;
        }

        @Override
        public int getBitsPerBlock() {
            return bitsPerBlock;
        }

        @Override
        public byte[] encode(MaterialData[] data) {
            byte[] array = new byte[512 * bitsPerBlock];
            LABWriter writer = new LABWriter(array);
            HashMap<MaterialData, Integer> lookup = new HashMap<>();
            for (int i = 0; i < lookupTable.length; i++) {
                lookup.put(lookupTable[i], i);
            }
            for (MaterialData block : data) {
                try {
                    writer.writeInt(lookup.get(block), bitsPerBlock);
                } catch (NullPointerException e) {
                    System.out.println("Error encoding data: " + block.getType());
                    throw e;
                }
            }
            return array;
        }

        @Override
        public int[] getPaletteData() {
            int[] data = new int[lookupTable.length];
            for (int i = 0; i < data.length; i++) {
                data[i] = lookupTable[i].getData() & 0xF;
                data[i] |= lookupTable[i].getType().getId() << 4;
            }
            return data;
        }

        @Override
        public int getLength() {
            return lookupTable.length;
        }

    }
}