package sexy.kostya.illusory.world.impl.protocol;

/**
 * Created by k.shandurenko on 23/06/2019
 */
class LABWriter {

    private final byte[] data;
    private int offset = 0;

    LABWriter(byte[] data) {
        this.data = data;
    }

    void writeInt(int i, int bits) {
        while (bits > 0) {
            int toWrite = Math.min(8, bits);
            bits -= toWrite;
            short ff = 0xFF;
            if (toWrite == 8) {
                writeByte((byte) (i & ff), toWrite);
            } else {
                ff >>>= 8 - toWrite;
                writeByte((byte) (i & ff), toWrite);
            }
            i >>>= 8;
        }
    }

    void writeByte(byte b, int bits) {
        if (bits == 0) {
            return;
        }
        int p = offset / 8, o = offset % 8;
        p = 7 - p % 8 + (p / 8) * 8;
        int space = 8 - o;
        if (space >= bits) {
            short ff = 0xFF;
            ff >>>= (8 - bits);
            data[p] |= (b & ff) << o;
            offset += bits;
        } else {
            offset += space;
            bits -= space;
            short ff = 0xFF;
            ff >>>= o;
            data[p] |= (b & ff) << o;
            b >>>= space;
            writeByte(b, bits);
        }
    }
}