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
import common.Msg;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import sponge.Main;
import sponge.location.Locations;

import org.spongepowered.api.command.CommandResult;
import sponge.util.action.LockAction;
import sponge.util.action.StatAction;
import sponge.util.action.StorageAction;
import sponge.util.message.Message;

public class HomeCommand implements CommandExecutor {

    public static final Main instance = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        String worldname = "";
        ServerPlayer pPlayer = null;
        CommandCause cause = context.cause();

        Audience audience = cause.audience();
        if (audience instanceof ServerPlayer) {
            pPlayer = (ServerPlayer) audience;
        } else {
            throw new CommandException(Message.error("Your are not a player."));
        }
        worldname = (StatAction.PlayerToUUID(pPlayer) + "-Isoworld");

        //If return true then the command is in lock
        if (!instance.cooldown.isAvailable(pPlayer, Cooldown.MAISON)) {
            return CommandResult.success();
        }

        // If return true then lock is enabled for import, else setting it
        if (LockAction.isLocked(pPlayer, "checkTag")) {
            return CommandResult.success();
        }

        // Pull / Push
        // False if processing on Isoworld as @PUSHED state in database
        // True if Isoworld avalable
        if (!StorageAction.checkTag(pPlayer, worldname)) {
            return CommandResult.success();
        }

        // Removing lock
        Main.lock.remove(pPlayer.uniqueId().toString() + ";" + "checkTag");

        // Check if Isoworld exists and load it if need (true)
        if (!sponge.util.action.IsoworldsAction.isPresent(pPlayer, true)) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("IsoworldNotFound")));
            return CommandResult.success();
        }

        // Teleport player
        Locations.teleport(pPlayer, worldname);

        instance.cooldown.addPlayerCooldown(pPlayer, Cooldown.MAISON, Cooldown.MAISON_DELAY);

        return CommandResult.success();
    }

    // Constructeurs
    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Commande pour retourner dans son isoWorld"))
                .permission("Isoworlds.home")
                .executor(new HomeCommand())
                .build();
    }
}