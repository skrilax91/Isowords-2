package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
@ConfigSerializable
public class ModulesConfig {
    @Comment("This module will unload every inactive Isoworlds for a given time (check every minutes)\n"
            + "Once unload, plugin will add @PUSH to worlds forldername\n"
            + "Then the storage module will push them to the backup storage defined by Isoworlds-SAS (script on github)\n"
            + "If automatic unload is disabled, storage still works on restarts (push every Isoworlds on backup storage at start)")
    private final AutomaticUnloadConfig automaticUnload = new AutomaticUnloadConfig();

    @Comment("This module will handle backup storage (script on github), Isoworlds will be pushed at server start and worlds unload")
    private final StorageConfig storage = new StorageConfig();

    @Comment("This module creates automatically alt dimensions (Mining, Exploration) (Warps access on Isoworlds menu)")
    private final DimensionAltConfig dimensionAlt = new DimensionAltConfig();

    @Comment("Generate a bedrock plateform on nether/end (0,60,0 default if no Y safe position found)\n"
            + "Clean 3*3 if filled, check at every warp action")
    private final SafePlateformConfig safePlateform = new SafePlateformConfig();

    @Comment("This module define world borders")
    private final BorderConfig borderModule = new BorderConfig();

    public AutomaticUnloadConfig automaticUnload() { return this.automaticUnload; }

    public StorageConfig storage() { return storage; }

    public DimensionAltConfig dimensionAlt() { return dimensionAlt; }

    public BorderConfig borderModule() { return borderModule; }
}
