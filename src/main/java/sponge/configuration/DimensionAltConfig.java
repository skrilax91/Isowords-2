package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
@ConfigSerializable
public class DimensionAltConfig {
    private boolean enable = false;
    private boolean mining = true;
    private boolean exploration = true;

    public boolean isEnable() {
        return enable;
    }

    public boolean isMining() {
        return mining;
    }

    public boolean isExploration() {
        return exploration;
    }
}
