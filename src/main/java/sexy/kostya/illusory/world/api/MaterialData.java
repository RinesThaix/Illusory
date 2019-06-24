package sexy.kostya.illusory.world.api;

import org.bukkit.Material;

import java.util.Objects;

/**
 * Created by k.shandurenko on 23/06/2019
 */
public class MaterialData {

    private final Material type;
    private final int data;

    public MaterialData(Material type, int data) {
        this.type = type;
        this.data = data;
    }

    public Material getType() {
        return type;
    }

    public byte getData() {
        return (byte) data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MaterialData that = (MaterialData) o;
        return data == that.data && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, data);
    }
}