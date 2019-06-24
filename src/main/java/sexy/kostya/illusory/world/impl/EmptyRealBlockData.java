package sexy.kostya.illusory.world.impl;

import sexy.kostya.illusory.world.api.BlockData;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class EmptyRealBlockData extends BlockData {

    public EmptyRealBlockData(int x, int y, int z) {
        super(x, y, z, null, 0);
        setIsReal(true);
    }

}
