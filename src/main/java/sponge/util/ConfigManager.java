package sponge.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import sponge.Main;
import sponge.util.console.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class ConfigManager {

    public static CommentedConfigurationNode configurationNode;
    private static HoconConfigurationLoader configurationLoader;

    private static final Path DIRECTORY = Sponge.configManager().pluginConfig(Main.pluginContainer).directory();


    public static void load() {
        try {
            Files.createDirectories(DIRECTORY.resolve("translations"));
            Optional<CommentedConfigurationNode> configs = load("isoworlds.conf");

            if (!configs.isPresent())
                createDefaultConfig();

            Logger.info("Successfully loaded the config.");
        } catch (IOException e) {
            Logger.severe("Error loading the config: " + e.getMessage());
        }
    }

    private static Optional<CommentedConfigurationNode> load(String name) throws IOException {
        Path path = DIRECTORY.resolve(name);
        configurationLoader = HoconConfigurationLoader.builder().path(path).build();
        if (Files.notExists(path))
            return Optional.empty();

        return Optional.ofNullable(configurationLoader.load());
    }

    private static void createDefaultConfig() throws IOException {
        Logger.warning("Fichier de configuration non trouvé, création en cours...");
        Path path = DIRECTORY.resolve("isoworlds.conf");
        Files.createFile(path);
        configurationNode = HoconConfigurationLoader.builder().path(path).build().load();

        configurationNode.node("Isoworlds", "Id").set("DEV").comment("Server name stored in database, feel free. Example: (AgrarianSkies2 : AS2)");
        configurationNode.node("Isoworlds", "MainWorld").set("Isolonice")
                .comment("Main world name (not folder name), used to teleport players on login/logout and build safe spawn (avoid death)");
        configurationNode.node("Isoworlds", "MainWorldSpawnCoordinate").set("0;60;0").
                comment("Default spawn position is 0,60,0");
        configurationNode.node(new Object[]{"Isoworlds", "Modules"}).
                comment("Differents modules, if enabled then adjust parameters if not (disabled) skip them");
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "AutomaticUnload", "Enabled"}).set(true).
                comment("This module will unload every inactive Isoworlds for a given time (check every minutes)\n"
                        + "Once unload, plugin will add @PUSH to worlds forldername\n"
                        + "Then the storage module will push them to the backup storage defined by Isoworlds-SAS (script on github)\n"
                        + "If automatic unload is disabled, storage still works on restarts (push every Isoworlds on backup storage at start)");
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "AutomaticUnload", "InactivityTime"}).set(15);

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Storage", "Enabled"}).set(true).
                comment("This module will handle backup storage (script on github), Isoworlds will be pushed at server start and worlds unload");
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "DimensionAlt", "Enabled"}).set(true).
                comment("This module creates automatically alt dimensions (Mining, Exploration) (Warps access on Isoworlds menu)");

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "DimensionAlt", "Mining"}).set(true);
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "DimensionAlt", "Exploration"}).set(true);

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "SafePlateform", "Enabled"}).set(true).
                comment("Generate a bedrock plateform on nether/end (0,60,0 default if no Y safe position found)\n"
                        + "Clean 3*3 if filled, check at every warp action");

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "SafeSpawn", "Enabled"}).set(true).
                comment("Generate 1*1 dirt on Isoworlds spawn if the spawn coordinate is empty (Y axis), to avoid death\n"
                        + "Breaking this dirt doesn't drop\n"
                        + "If Y axis is not empty then it will teleport the player on the highest solid position\n"
                        + "Handle lava and water");

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "SpawnProtection", "Enabled"}).set(true).
                comment("This module disable player's interaction on main world spawn");

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Border", "Enabled"}).set(true).
                comment("This module define world borders");
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Border", "DefaultRadiusSize"}).set(250);
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Border", "SmallRadiusSize"}).set(500);
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Border", "MediumRadiusSize"}).set(750);
        configurationNode.node(new Object[]{"Isoworlds", "Modules", "Border", "LargeRadiusSize"}).set(1000);

        configurationNode.node(new Object[]{"Isoworlds", "Modules", "PlayTime", "Enabled"}).set(true).
                comment("This module will count playtime of players (by simply adding 1 every minutes if player is online)");

        configurationNode.node(new Object[]{"Isoworlds", "sql"}).
                comment("MySQL server, this configuration is needed as we don't handle sqlite atm");

        configurationNode.node(new Object[]{"Isoworlds", "sql", "host"}).set("IP_ADDRESS");
        configurationNode.node(new Object[]{"Isoworlds", "sql", "port"}).set(3306);
        configurationNode.node(new Object[]{"Isoworlds", "sql", "database"}).set("DATABASE_NAME");
        configurationNode.node(new Object[]{"Isoworlds", "sql", "username"}).set("DATABASE_USERNAME");
        configurationNode.node(new Object[]{"Isoworlds", "sql", "password"}).set("PASSWORD");
        configurationLoader.save(configurationNode);
    }
}
