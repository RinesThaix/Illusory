package sexy.kostya.illusory.world.impl.protocol;

/**
 * Created by k.shandurenko on 23/06/2019
 */
class LABReader {

    private final byte[] data;
    private int offset = 0;

    LABReader(byte[] data) {
        this.data = data;
    }

    short readShort(int bits) {
        short value = 0;
        int read = 0;
        while (bits > 0) {
            int toRead = Math.min(8, bits);
            int b = Byte.toUnsignedInt(readByte(toRead));
            b <<= read;
            value |= b;
            read += toRead;
            bits -= toRead;
        }
        return value;
    }

    int readInt(int bits) {
        int value = 0;
        int read = 0;
        while (bits > 0) {
            int toRead = Math.min(8, bits);
            int b = Byte.toUnsignedInt(readByte(toRead));
            b <<= read;
            value |= b;
            read += toRead;
            bits -= toRead;
        }
        return value;
    }

    byte readByte(int bits) {
        int p = offset / 8, o = offset % 8;
        p = 7 - p % 8 + (p / 8) * 8;
        byte b = (byte) (Byte.toUnsignedInt(data[p]) >>> o);
        int read = 8 - o;
        if (read > bits) {
            short ff = 0xFF;
            ff >>>= (8 - bits);
            b &= ff;
            offset += bits;
            return b;
        }
        offset += read;
        bits -= read;
        if (bits > 0) {
            byte c = readByte(bits);
            b |= (c << read);
        }
        return b;
    }

}