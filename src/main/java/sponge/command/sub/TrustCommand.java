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
import sponge.Database.Methods.TrustAction;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import sponge.Database.Methods.IsoworldsAction;
import sponge.Main;

import org.spongepowered.api.command.CommandResult;

import sponge.Translation.TranslateManager;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.util.*;

public class TrustCommand implements CommandExecutor {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private final Main plugin = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        UUID uuidcible;
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());
        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));
        ServerPlayer pPlayer = ply.get();

        //If the method return true then the command is in lock
        if (!plugin.cooldown.isAvailable(pPlayer, Cooldown.CONFIANCE)) {
            return CommandResult.success();
        }

        if (!context.hasAny(Parameter.key("player", ServerPlayer.class))) {
            pPlayer.sendMessage(Message.error("Please provide a player username"));
            return CommandResult.success();
        }

        // Check if world exists
        if (!IsoworldsAction.isPresent(pPlayer, false)) {
            pPlayer.sendMessage(Message.error(translateManager.translate("IsoworldNotFound")));
            return CommandResult.success();
        }

        ServerPlayer target = context.requireOne(Parameter.key("player", ServerPlayer.class));

        try {

            uuidcible = target.uniqueId();

            if (uuidcible.toString().isEmpty()) {
                pPlayer.sendMessage(Message.error(translateManager.translate("InvalidPlayer")));
                return CommandResult.success();
            }
        } catch (NoSuchElementException | IllegalArgumentException i) {
            i.printStackTrace();
            return CommandResult.success();
        }

        // CHECK AUTORISATIONS
        if (TrustAction.isTrusted(target, pPlayer.uniqueId().toString())) {
            pPlayer.sendMessage(Message.error(translateManager.translate("AlreadyTrusted")));
            return CommandResult.success();
        }

        // INSERT
        if (!TrustAction.setTrust(pPlayer.uniqueId().toString(), target)) {
            Main.lock.remove(pPlayer.uniqueId().toString() + ";" + "confiance");
            return CommandResult.success();
        }

        pPlayer.sendMessage(Message.success(translateManager.translate("SuccessTrust")));

        plugin.cooldown.addPlayerCooldown(pPlayer, Cooldown.CONFIANCE, Cooldown.CONFIANCE_DELAY);
        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {
        return Command.builder()
                .shortDescription(Component.text("Ajoute un joueur à la liste de confiance"))
                .permission("Isoworlds.trust")
                .addParameter(Parameter.player().key("player").build())
                .executor(new TrustCommand())
                .build();
    }
}