package sexy.kostya.illusory.protocol.wrapper;

import sexy.kostya.illusory.Reflection;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public class WrappedBlockPosition {

    private final static Class<?> BLOCK_POSITION_CLASS = Reflection.getClass("{nms}.BlockPosition");
    private final static Reflection.MethodInvoker GET_X = Reflection.getMethod(BLOCK_POSITION_CLASS, "getX");
    private final static Reflection.MethodInvoker GET_Y = Reflection.getMethod(BLOCK_POSITION_CLASS, "getY");
    private final static Reflection.MethodInvoker GET_Z = Reflection.getMethod(BLOCK_POSITION_CLASS, "getZ");

    public static WrappedBlockPosition fromHandle(Object handle) {
        return new WrappedBlockPosition(handle);
    }

    private final Object handle;

    public WrappedBlockPosition(Object handle) {
        this.handle = handle;
    }

    public int getX() {
        return (int) GET_X.invoke(this.handle);
    }

    public int getY() {
        return (int) GET_Y.invoke(this.handle);
    }

    public int getZ() {
        return (int) GET_Z.invoke(this.handle);
    }

}
