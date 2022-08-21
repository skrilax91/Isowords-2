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
package sponge.command.sub;

import common.Cooldown;
import common.ManageFiles;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.database.Methods.IsoworldsAction;
import sponge.Main;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import sponge.translation.TranslateManager;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.io.File;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ReforgeCommand implements CommandExecutor {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private final Main plugin = Main.instance;
    final static Map<String, Timestamp> confirm = new HashMap<>();

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String fullpath = "";
        String worldname = "";
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());

        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));

        ServerPlayer pPlayer = ply.get();

        if (!plugin.cooldown.isAvailable(pPlayer, Cooldown.REFONTE)) {
            return CommandResult.success();
        }

        // Check is Isoworld exists in database
        if (!sponge.database.Methods.IsoworldsAction.isPresent(pPlayer, false)) {
            pPlayer.sendMessage(Message.error(translateManager.translate("IsoworldNotFound")));
            return CommandResult.success();
        }

        // Confirmation message (2 times cmd)
        if (!(confirm.containsKey(pPlayer.uniqueId().toString()))) {
            pPlayer.sendMessage(Message.error(translateManager.translate("Confirm")));
            confirm.put(pPlayer.uniqueId().toString(), timestamp);
            return CommandResult.success();
        } else {
            long millis = timestamp.getTime() - (confirm.get(pPlayer.uniqueId().toString()).getTime());
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            if (minutes >= 1) {
                confirm.remove(pPlayer.uniqueId().toString());
                pPlayer.sendMessage(Message.error(translateManager.translate("Confirm")));
                return CommandResult.success();
            }
        }

        confirm.remove(pPlayer.uniqueId().toString());

        worldname = (pPlayer.uniqueId().toString() + "-isoworld");
        ServerWorld spawnWorld = Sponge.server().worldManager().world(Main.instance.getWorldKey("Isolonice")).get();
        File destDir = new File(ManageFiles.getPath() + "/Isoworlds-REFONTE/" + worldname);
        destDir.mkdir();

        if (!Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).isPresent()) {
            pPlayer.sendMessage(Message.error(translateManager.translate("IsoworldNotFound")));
            return CommandResult.success();
        }
        if (Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).get().isLoaded()) {
            Collection<ServerPlayer> colPlayers = Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).get().players();

            for (ServerPlayer player : colPlayers) {
                player.setLocation(ServerLocation.of(spawnWorld, spawnWorld.properties().spawnPosition()));
                pPlayer.sendMessage(Message.error(translateManager.translate("ReforgeKick")));
            }
            Sponge.server().worldManager().unloadWorld(Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).get());
        }

        try {
            if (!IsoworldsAction.deleteIsoworld(pPlayer.uniqueId().toString())) {
                pPlayer.sendMessage(Message.error(translateManager.translate("FailReforgeIsoworld")));
                return CommandResult.success();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        // Deleting isoworld
        Sponge.server().worldManager().deleteWorld(Main.instance.getWorldKey(worldname));

        pPlayer.sendMessage(Message.success(translateManager.translate("SuccesReforge")));

        plugin.cooldown.addPlayerCooldown(pPlayer, Cooldown.REFONTE, Cooldown.REFONTE_DELAY);

        // Open menu to player
        Sponge.server().commandManager().process(pPlayer, "iw");

        return CommandResult.success();
    }


    // Constructeurs
    public static Command.Parameterized getCommand() {
        return Command.builder()
                .shortDescription(Component.text("Commande de refonte des isoWorlds"))
                .permission("Isoworlds.reforge")
                .executor(new ReforgeCommand())
                .build();
    }
}
