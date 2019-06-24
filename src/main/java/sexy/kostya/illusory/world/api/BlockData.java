package sexy.kostya.illusory.world.api;

import org.bukkit.Material;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class BlockData extends MaterialData {

    private int x, y, z;

    private boolean real;

    public BlockData(int x, int y, int z, Material type, int data) {
        super(type, data);
        this.x = x;
        this.y = y;
        this.z = z;
        this.real = false;
    }

    public BlockData(int x, int y, int z, Material type) {
        this(x, y, z, type, 0);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public boolean isReal() {
        return real;
    }

    public void setIsReal(boolean real) {
        this.real = real;
    }

    @Override
    public int hashCode() {
        return ChunkData.hash(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockData)) {
            return false;
        }
        BlockData that = (BlockData) o;
        return this.x == that.x && this.y == that.y && this.z == that.z;
    }

    @Override
    public String toString() {
        return "BlockData{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", type=" + getType().name() +
                ", data=" + getData() +
                ", real=" + real +
                '}';
    }
}
