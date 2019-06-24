package sexy.kostya.illusory.event.world;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public abstract class IllusoryWorldCancellableEvent extends IllusoryWorldEvent implements Cancellable {

    private boolean cancelled;

    IllusoryWorldCancellableEvent(Player player) {
        super(player);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
