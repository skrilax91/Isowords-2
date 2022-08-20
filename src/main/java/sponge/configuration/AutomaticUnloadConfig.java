package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
@ConfigSerializable
public class AutomaticUnloadConfig {
    private boolean enable = false;
    private int inactivityTime = 15;

    public boolean enabled() { return this.enable; }
    public int inactivityTime() { return this.inactivityTime; }
}
