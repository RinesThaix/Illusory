package sexy.kostya.illusory.world.impl.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by k.shandurenko on 23/06/2019
 */
class ByteBufferReader {

    private final ByteBuffer buffer;

    ByteBufferReader(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    int readVarInt() {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buffer.get();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    static void writeVarInt(int value, ByteArrayOutputStream baos) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            baos.write(temp);
        } while (value != 0);
    }
}