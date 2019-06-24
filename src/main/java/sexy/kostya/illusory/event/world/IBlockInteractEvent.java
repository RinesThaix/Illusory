package sexy.kostya.illusory.event.world;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import sexy.kostya.illusory.protocol.wrapper.WrappedDirection;
import sexy.kostya.illusory.world.api.BlockData;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public class IBlockInteractEvent extends IllusoryWorldEvent {

    private static final HandlerList handlers = new HandlerList();

    private final BlockData blockData;
    private final WrappedDirection direction;

    public IBlockInteractEvent(Player player, BlockData blockData, WrappedDirection direction) {
        super(player);
        this.blockData = blockData;
        this.direction = direction;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public WrappedDirection getDirection() {
        return direction;
    }

    public BlockData getRelativeBlockData() {
        return getWorldProvider().getBlockSurely(
                getPlayer(),
                this.blockData.getX() + this.direction.getDx(),
                this.blockData.getY() + this.direction.getDy(),
                this.blockData.getZ() + this.direction.getDz()
        );
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
