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

import com.google.common.collect.Iterables;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.Main;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.World;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ListWorldsCommand implements CommandExecutor {

    private final Main plugin = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        ArrayList<ServerWorld> worlds = new ArrayList<>();
        ServerPlayer pPlayer = null;
        CommandCause cause = context.cause();

        Audience audience = cause.audience();
        if (audience instanceof ServerPlayer) {
            pPlayer = (ServerPlayer) audience;
        } else {
            throw new CommandException(Message.error("Your are not a player."));
        }

        for(ServerWorld world : Sponge.server().worldManager().worlds()) {
            if (world.isLoaded()) {
                if (world.properties().displayName().toString().contains("-Isoworld")) {
                    worlds.add(world);
                }
            }
        }

        // Check si Isoworld existe
        if (worlds.isEmpty()) {
            pPlayer.sendMessage(Message.info("Sijania ne repère aucun Isoworld dans le Royaume Isolonice"));
            return CommandResult.success();
        }

        // Construction des textes en fonction des Isoworlds loadés
        TextComponent title = Component.text("[Liste des Isoworlds (cliquables)]").color(NamedTextColor.GOLD);
        pPlayer.sendMessage(title);

        for(ServerWorld w : worlds ) {
            String worldName = w.properties().name();
            UUID uuid = UUID.fromString(worldName.split("-Isoworld")[0]);
            String pname;
            String status;

            if (!Sponge.server().userManager().exists(uuid)) {
                pname = "OFF";
                status = "OFF";
            } else {
                CompletableFuture<Optional<User>> fPlayer = Sponge.server().userManager().load(uuid);
                Optional<User> ply;

                try {
                    ply = fPlayer.get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                status = (ply.isPresent() && ply.get().isOnline()) ? "ON" : "OFF";
                pname = (ply.isPresent() && ply.get().isOnline()) ? ply.get().name() : "OFF";
            }

            int numOfEntities = w.entities().size();
            int loadedChunks = Iterables.size(w.loadedChunks());

            TextComponent name = Component.text(pname + " [" + status +"] | Chunks: " + loadedChunks + " | Entités: " + numOfEntities + " | TPS: ").color(NamedTextColor.GREEN)
                    .append(StatAction.getTPS(Sponge.server().ticksPerSecond()))
                            .clickEvent(ClickEvent.runCommand("/iw teleport " + pPlayer.name() + " " + worldName));
            //! Add hover showText(Component.tex(worldName))


            pPlayer.sendMessage(name);
        }
        return CommandResult.success();
    }

    // Constructeurs
    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Commande pour lister les IsoWorlds"))
                .permission("Isoworlds.list")
                .executor(new ListWorldsCommand())
                .build();
    }
}