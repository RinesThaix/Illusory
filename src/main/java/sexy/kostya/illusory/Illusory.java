package sexy.kostya.illusory;

import org.bukkit.plugin.java.JavaPlugin;
import sexy.kostya.illusory.protocol.DelayedHandler;
import sexy.kostya.illusory.protocol.IllusoryProtocol;
import sexy.kostya.illusory.protocol.TinyProtocol;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class Illusory extends JavaPlugin {

    private static Illusory INSTANCE;

    private TinyProtocol protocol;

    public static Illusory getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        this.protocol = new IllusoryProtocol(this);
        DelayedHandler.initialize();
    }

    @Override
    public void onDisable() {

    }

    public TinyProtocol getProtocol() {
        return protocol;
    }
}
