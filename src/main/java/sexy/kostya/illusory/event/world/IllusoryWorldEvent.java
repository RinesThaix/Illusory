package sexy.kostya.illusory.event.world;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import sexy.kostya.illusory.world.IllusoryWorlds;
import sexy.kostya.illusory.world.api.WorldProvider;

/**
 * Created by k.shandurenko on 24/06/2019
 */
public abstract class IllusoryWorldEvent extends Event {

    private final Player player;

    IllusoryWorldEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void call() {
        Bukkit.getPluginManager().callEvent(this);
    }

    public WorldProvider getWorldProvider() {
        return IllusoryWorlds.getWorldProvider(this.player.getWorld());
    }

}
