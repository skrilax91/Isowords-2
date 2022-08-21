package sponge.util;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.plugin.PluginContainer;
import sponge.Main;
import sponge.util.console.Logger;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WorldManager {

    // Create pattern worlds
    public static boolean initPatternWorlds()
    {
        createPattern("normal", WorldTypes.OVERWORLD);
        createPattern("flat", WorldTypes.OVERWORLD);
        createPattern("ocean", WorldTypes.OVERWORLD);
        createPattern("void", WorldTypes.OVERWORLD);

        Logger.info("[PATTERN] All patterns are ready");
        return true;
    }

    private static Boolean createPattern(String type, RegistryReference<WorldType> worldType)
    {
        String worldName = type + "-pattern";
        // Check si world properties en place, création else
        CompletableFuture<Optional<ServerWorldProperties>> fwp = Sponge.server().worldManager().loadProperties(Main.instance.getWorldKey(worldName));

        try {
            Optional<ServerWorldProperties> wp = fwp.get();

            if (wp.isPresent()) {
                Logger.info("[PATTERN] " + worldName + " already exist");
            } else {
                WorldTemplate template = WorldTemplate.builder()
                        .displayName(Component.text(worldName))
                        .worldType(worldType)
                        .difficulty(Difficulties.NORMAL)
                        .gameMode(GameModes.SURVIVAL)
                        .loadOnStartup(true)
                        .performsSpawnLogic(false)
                        .key(Main.instance.getWorldKey(worldName))
                        .pvp(false)
                        .build();
                sponge.util.console.Logger.info("[PATTERN] " + worldName + " don't exist, creation...");
                Sponge.server().worldManager().loadWorld(template);
                Sponge.server().worldManager().unloadWorld(Main.instance.getWorldKey(worldName));
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static Boolean createWorld(String worldName)
    {

        // Check si world properties en place, création else
        CompletableFuture<Optional<ServerWorldProperties>> fwp = Sponge.server().worldManager().loadProperties(Main.instance.getWorldKey(worldName));

        try {
            Optional<ServerWorldProperties> wp = fwp.get();

            if (wp.isPresent()) {
                Logger.info(worldName + " already exist");
            } else {
                WorldTemplate template = WorldTemplate.builder()
                        .displayName(Component.text(worldName))
                        .worldType(WorldTypes.OVERWORLD)
                        .difficulty(Difficulties.NORMAL)
                        .gameMode(GameModes.SURVIVAL)
                        .loadOnStartup(true)
                        .performsSpawnLogic(false)
                        .key(Main.instance.getWorldKey(worldName))
                        .pvp(false)
                        .build();
                Sponge.server().worldManager().loadWorld(template);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
