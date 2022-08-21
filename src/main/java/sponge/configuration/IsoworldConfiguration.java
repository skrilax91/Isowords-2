package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import sponge.configuration.ModulesConfig;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigSerializable
public class IsoworldConfiguration {

    @Comment("Server name stored in database, feel free. Example: (AgrarianSkies2 : AS2)")
    private String serverId = "DEV";

    @Comment("Locale used in all plugin message, check locales folder for more precision")
    private String localeTag = "en";

    @Comment("Main world name (not folder name), used to teleport players on login/logout and build safe spawn (avoid death)")
    private String mainWorld = "Isolonice";

    @Comment("Default spawn position is 0,60,0")
    private String mainWorldSpawnCoordinate = "0;60;0";

    @Comment("Differents modules, if enabled then adjust parameters if not (disabled) skip them")
    private final ModulesConfig modules = new ModulesConfig();

    @Comment("Generate 1*1 dirt on Isoworlds spawn if the spawn coordinate is empty (Y axis), to avoid death\n"
            + "Breaking this dirt doesn't drop\n"
            + "If Y axis is not empty then it will teleport the player on the highest solid position\n"
            + "Handle lava and water")
    private boolean safeSpawn = true;

    @Comment("Disable player's interaction on main world spawn")
    private boolean spawnProtection = true;

    @Comment("This module will count playtime of players (by simply adding 1 every minutes if player is online)")
    private boolean playTime = false;

    @Comment("Setup sql connection")
    private final MysqlConfig sql = new MysqlConfig();

    public String serverId()
    {
        return this.serverId;
    }

    public void setServerId(String id)
    {
        this.serverId = id;
    }

    public String mainWorld()
    {
        return this.mainWorld;
    }

    public void setMainWorld(String name)
    {
        this.mainWorld = name;
    }

    public boolean playTime()
    {
        return this.playTime;
    }

    public void setPlayTime(boolean value)
    {
        this.playTime = value;
    }

    public String mainWorldSpawnCoordinate()
    {
        return this.mainWorldSpawnCoordinate;
    }

    public void setMainWorldSpawnCoordinate(String name)
    {
        this.mainWorldSpawnCoordinate = name;
    }

    public ModulesConfig modules()
    {
        return this.modules;
    }

    public MysqlConfig getSql() {
        return sql;
    }

    public boolean isSpawnProtection() {
        return spawnProtection;
    }

    public String getLocaleTag() { return localeTag; }

    public void setLocaleTag(String localeTag) { this.localeTag = localeTag; }
}