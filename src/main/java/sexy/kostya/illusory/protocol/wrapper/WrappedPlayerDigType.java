package sexy.kostya.illusory.protocol.wrapper;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public enum WrappedPlayerDigType {
    START_DESTROY_BLOCK,
    ABORT_DESTROY_BLOCK,
    STOP_DESTROY_BLOCK,
    DROP_ALL_ITEMS,
    DROP_ITEM,
    RELEASE_USE_ITEM,
    SWAP_HELD_ITEMS;

    private final static WrappedPlayerDigType[] VALUES = values();

    public static WrappedPlayerDigType fromHandle(Object handle) {
        return VALUES[((Enum) handle).ordinal()];
    }

}
