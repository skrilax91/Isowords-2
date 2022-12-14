/*
 * This file is part of Isoworlds, licensed under the MIT License (MIT).
 *
 * Copyright (c) Edwin Petremann <https://github.com/Isolonice/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sponge.listener;

import common.ManageFiles;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Server;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.living.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.database.Methods.IsoworldsAction;
import sponge.translation.TranslateManager;
import sponge.location.Locations;
import sponge.util.console.Logger;
import sponge.Main;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import sponge.util.message.Message;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Listeners {
    private final Main plugin = Main.instance;
    private static TranslateManager translateManager = Main.instance.translateManager;

    @Listener
    public void onRespawnPlayerEvent(RespawnPlayerEvent event) {
        ServerPlayer p = event.entity();
        String worldname = (p.uniqueId().toString() + "-isoworld");
        ResourceKey worldKey = DefaultWorldKeys.DEFAULT;

        // Teleport to spawn of own Isoworld if is loaded
        if (Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).isPresent() && Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).get().isLoaded()) {
            Sponge.server().scheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> Locations.teleport(p, worldname)).delay(2, TimeUnit.MILLISECONDS).build());
        } else if (Sponge.server().worldManager().world(worldKey).isPresent()) {
            Sponge.server().scheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> Locations.teleport(p, "Isolonice")).delay(2, TimeUnit.MILLISECONDS).build());
        }
    }

    @Listener
    // Anti grief spawn
    public void onDestructSpawn(ChangeBlockEvent.Pre event, @First ServerPlayer player) {

        // ****** MODULES ******
        // Spawn Protection
        if (plugin.getConfig().isSpawnProtection() || player.hasPermission("Isoworlds.bypass.spawn")) {
            return;
        }

        String worldname = player.world().properties().name();

        if (player.world().properties().key() == DefaultWorldKeys.DEFAULT) {
            event.setCancelled(true);
        }

        // If break in chunk of spawn layer 60, remove drop
        if (event.locations().get(0).blockX() == Locations.getAxis(worldname).x() & event.locations().get(0).blockZ() == Locations.getAxis(worldname).z()) {
            if (event.locations().get(0).blockType() == BlockTypes.DIRT.get()) {
                event.setCancelled(true);
                event.locations().get(0).setBlockType(BlockTypes.AIR.get());
            }
        }
        // *********************
    }

    // On t??l??porte tous les joueurs ?? la d??connexion
    @Listener
    public void onStop(StoppingEngineEvent<Server> event) {
        ResourceKey worldKey = DefaultWorldKeys.DEFAULT;
        Collection<ServerPlayer> players = Sponge.server().onlinePlayers();
        ServerWorld spawnWorld = Sponge.server().worldManager().world(worldKey).get();
        ServerLocation spawn = ServerLocation.of(spawnWorld, spawnWorld.properties().spawnPosition());
        ServerLocation top = Locations.getHighestLoc(spawn).orElse(ServerLocation.of(spawn.world(), 0, 61, 0));

        for (Player p : players) {
            p.setLocation(top);
            Logger.info("TP DECO: " + p.name() + " : " + top.world().properties().name().toLowerCase());
        }
    }

    @Listener
    public void onConnect(ServerSideConnectionEvent.Join event) {
        // Welcome message and open menu for those who do not have their own Isoworld
        if (!IsoworldsAction.iwExists(event.player().uniqueId().toString())) {
            Sponge.server().scheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
                event.player().sendMessage(Message.success(translateManager.translate("FirstJoin")));
            }).delay(5, TimeUnit.SECONDS).build());
        }
    }

    @Listener
    public void onLogin(ServerSideConnectionEvent.Login event) {
        ResourceKey worldKey = DefaultWorldKeys.DEFAULT;
        Sponge.server().scheduler().submit(Task.builder()
                .plugin(Main.instance.getContainer())
                .execute(() -> event.setToLocation(ServerLocation.of(worldKey, 0, 60, 0)))
                .delay(1 / 5, TimeUnit.SECONDS)
                .build());
    }

    // Logout event, tp spawn
    @Listener
    public void onLogout(ServerSideConnectionEvent.Disconnect event) {
        ResourceKey worldKey = DefaultWorldKeys.DEFAULT;
        ServerWorld spawnWorld = Sponge.server().worldManager().world(worldKey).get();
        ServerLocation spawn = ServerLocation.of(spawnWorld, spawnWorld.properties().spawnPosition());
        ServerLocation top = Locations.getHighestLoc(spawn).orElse(ServerLocation.of(spawn.world(), 0, 61, 0));
        event.player().setLocation(top);
        Logger.info("Joueur t??l??port?? au spawn");
    }

    // Debug load world
    @Listener
    public void onLoadWorld(LoadWorldEvent event) {
        ServerWorld world = event.world();
        String worldname = world.properties().name();


        // Check si g??n??r?? de fa??on anormale, on log le tout pour sanctionner ;.;
        File file = new File(ManageFiles.getPath() + "/" + worldname + "@PUSHED");

        if (file.exists()) {
            // Anomalie
            Logger.warning("--- Anomalie: LOADING " + worldname + " WORLD, CAUSED BY: " + event.cause().toString() + " ---");
            // Check if players in there
            for (ServerPlayer p : world.players()) {
                p.kick(Component.text("Vous faites quelque chose d'anormal, veuillez avertir le staff"));
                Logger.severe("--- Le joueur " + p.name() + " fait partie de l'anomalie ---");
            }
            // D??chargement et suppression
            Sponge.server().worldManager().deleteWorld(Main.instance.getWorldKey(worldname));
        } else {
            Logger.info("LOADING " + worldname + " WORLD, CAUSED BY: " + event.cause().toString());
        }
    }

    // TP lors du unload d'un monde
    @Listener
    public void onUnloadWorld(UnloadWorldEvent event) {
        ResourceKey worldKey = DefaultWorldKeys.DEFAULT;
        ServerWorld world = event.world();

        ServerWorld spawnWorld = Sponge.server().worldManager().world(worldKey).get();
        ServerLocation spawn = ServerLocation.of(spawnWorld, spawnWorld.properties().spawnPosition());
        ServerLocation top = Locations.getHighestLoc(spawn).orElse(ServerLocation.of(spawn.world(), 0, 61, 0));

        // Kick players
        for (ServerPlayer p : world.players()) {
            p.setLocation(top);
            Logger.info("TP UNLOAD: " + p.name() + " : " + top.world().properties().name().toLowerCase());
        }

        // Check if file exist, to detect mirrors
        File file = new File(ManageFiles.getPath() + "/" + world.properties().name() + "@PUSHED");
        // If exists and contains Isoworld
        if (file.exists() & world.properties().name().contains("-isoworld")) {
            // Anomalie
            Logger.severe("--- Anomalie: UNLOADING " + world.properties().name() + " WORLD, CAUSED BY: " + event.cause().toString() + " ---");
            // Check if players in there
            for (ServerPlayer p : world.world().players()) {
                p.kick(Component.text("Vous faites quelque chose d'anormal, veuillez avertir le staff"));
                Logger.warning("--- Le joueur " + p.name() + " fait partie de l'anomalie ---");
            }
        } else {
            Logger.info("UNLOADING " + world.properties().name() + " WORLD, CAUSED BY: " + event.cause().toString());
        }
    }

    @Listener
    public void onPlayerChangeWorld(ChangeEntityWorldEvent.Pre event, @Getter("entity") ServerPlayer pPlayer) {
        final String CHECK = "SELECT * FROM `autorisations` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        String check_p;
        String check_w;
        String eventworld = event.originalDestinationWorld().properties().name();

        Logger.info("[TRACKING-IW] T??l??poration: " + pPlayer.name() + " [FROM: " + event.originalWorld().properties().name() + "] - [TO: " + eventworld + "] - [CAUSE: "
                + event.cause().toString() + "]");



        // Check if world folder is present
        File checkFolder = new File(ManageFiles.getPath() + eventworld);
        if (!checkFolder.exists() && eventworld.contains("-isoworld")) {
            event.setCancelled(true);
            return;
        }

        if (eventworld.contains("-isoworld")) {
            Sponge.server().worldManager().loadWorld(Main.instance.getWorldKey(eventworld));
            try {
                Connection connection = plugin.database.getConnection();
                PreparedStatement check = connection.prepareStatement(CHECK);
                // UUID_P
                check_p = pPlayer.uniqueId().toString();
                check.setString(1, check_p);
                // UUID_W
                check_w = eventworld;
                check.setString(2, check_w);
                // Requ??te
                check.setString(3, plugin.servername);
                ResultSet rselect = check.executeQuery();
                Logger.info("Monde event: " + eventworld);

                if (pPlayer.hasPermission("Isoworlds.bypass.teleport")) {
                    return;
                }

                // To clean

                if (!rselect.isBeforeFirst() && !Objects.equals(pPlayer.world().properties().name(), eventworld)) {
                    event.setCancelled(true);
                }

            } catch (Exception se) {
                pPlayer.sendMessage(Message.error(translateManager.translate("IsoworldNotFound")));
            }

        }
    }

    @Listener
    public void onInteractSpawn(InteractBlockEvent.Primary.Start event, @First ServerPlayer p) {
        if (event.block() == BlockSnapshot.empty()) {
            return;
        }
        // Allow break if permission
        if (p.hasPermission("Isoworlds.bypass.spawn")) {
            return;
        }

        if (p.world().properties().key() == DefaultWorldKeys.DEFAULT) {
            event.setCancelled(true);
        }
    }

//    @Listener
//    public void onChunkPregen(ChunkPreGenerationEvent.Pre event) {
//        if (event.getTargetWorld().getName().contains("-isoworld")) {
//            event.getChunkPreGenerate().cancel();
//        }
//    }

}