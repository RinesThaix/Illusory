package sexy.kostya.illusory.event.world;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import sexy.kostya.illusory.world.api.BlockData;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public class IBlockBreakEvent extends IllusoryWorldCancellableEvent {

    private static final HandlerList handlers = new HandlerList();

    private final BlockData blockData;

    public IBlockBreakEvent(Player player, BlockData blockData) {
        super(player);
        this.blockData = blockData;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
