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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.world.DefaultWorldKeys;
import sponge.database.Methods.TrustAction;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.database.Methods.IsoworldsAction;
import sponge.Main;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;

import org.spongepowered.api.entity.living.player.User;

import sponge.translation.TranslateManager;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class UnTrustCommand implements CommandExecutor {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private final Main instance = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        User target;
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());
        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));
        ServerPlayer pPlayer = ply.get();

        //If the method return true then the command is in lock
        if (!instance.cooldown.isAvailable(pPlayer, Cooldown.TIME)) {
            return CommandResult.success();
        }

        if (!context.hasAny(Parameter.key("player", String.class))) {
            pPlayer.sendMessage(Message.error("Please provide a player username"));
            return CommandResult.success();
        }

        // SELECT WORLD
        if (!IsoworldsAction.isPresent(pPlayer, false)) {
            pPlayer.sendMessage(Message.error(translateManager.translate("IsoworldNotFound")));
            return CommandResult.success();
        }

        String username = context.requireOne(Parameter.key("player", String.class));

        try {
            Optional<User> otrg = Sponge.server().userManager().load(username).get();

            if (!otrg.isPresent()) {
                pPlayer.sendMessage(Message.error(translateManager.translate("InvalidPlayer")));
                return CommandResult.success();
            }
            target = otrg.get();

            if (target.uniqueId().toString().isEmpty()) {
                pPlayer.sendMessage(Message.error(translateManager.translate("InvalidPlayer")));
                return CommandResult.success();
            }
        } catch (NoSuchElementException | IllegalArgumentException i) {
            i.printStackTrace();
            return CommandResult.success();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // CHECK AUTORISATIONS
        if (!TrustAction.isTrusted(target.player().get(), pPlayer.uniqueId().toString())) {
            pPlayer.sendMessage(Message.error(translateManager.translate("NotTrusted")));
            return CommandResult.success();
        }

        // DELETE AUTORISATION
        if (!TrustAction.deleteTrust(pPlayer.uniqueId().toString(), target.player().get())) {
            return CommandResult.success();
        }

        try {
            if (target.isOnline()) {
                ResourceKey worldKey = DefaultWorldKeys.DEFAULT;
                ServerWorld spawnWorld = Sponge.server().worldManager().world(worldKey).get();
                if (target.player().get().world().properties().name().equals(pPlayer.uniqueId().toString() + "-isoworld")) {
                    target.player().get().setLocation(ServerLocation.of(spawnWorld, spawnWorld.properties().spawnPosition()));
                    pPlayer.sendMessage(Message.error(translateManager.translate("NotTrusted")));
                }
            }
        } catch (NoSuchElementException nse) {
            nse.printStackTrace();
        }

        pPlayer.sendMessage(Message.success(translateManager.translate("SuccessUntrust")));
        return CommandResult.success();
    }


    public static Command.Parameterized getCommand() {
        return Command.builder()
                .shortDescription(Component.text("Retire un joueur à la liste de confiance"))
                .permission("Isoworlds.untrust")
                .addParameter(Parameter.string().key("player").build())
                .executor(new UnTrustCommand())
                .build();
    }
}