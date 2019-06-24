package sexy.kostya.illusory.world.impl.protocol;

/**
 * Created by k.shandurenko on 23/06/2019
 */
class ChunkHolder {

    private final ChunkSection[] sections;
    private final byte[] additionalData; // Biome data?

    ChunkHolder(ChunkSection[] sections, byte[] additionalData) {
        this.sections = sections;
        this.additionalData = additionalData;
    }

    ChunkSection[] getSections() {
        return sections;
    }

    byte[] getAdditionalData() {
        return additionalData;
    }
}