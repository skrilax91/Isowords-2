package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
@ConfigSerializable
public class SafePlateformConfig {
    private boolean enable = true;

    public boolean isEnable() {
        return enable;
    }
}