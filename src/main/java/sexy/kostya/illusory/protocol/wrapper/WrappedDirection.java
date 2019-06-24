package sexy.kostya.illusory.protocol.wrapper;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public enum WrappedDirection {
    DOWN(0, -1, 0),
    UP(0, 1, 0),
    NORTH(0, 0, -1),
    SOUTH(0, 0, 1),
    WEST(-1, 0, 0),
    EAST(1, 0, 0);

    private final int dx, dy, dz;

    private final static WrappedDirection[] VALUES = values();

    public static WrappedDirection fromHandle(Object handle) {
        return VALUES[((Enum) handle).ordinal()];
    }

    WrappedDirection(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

}