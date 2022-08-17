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
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import sponge.Main;
import sponge.location.Locations;
import sponge.util.action.StatAction;
import sponge.util.console.Logger;
import sponge.util.message.Message;

import java.util.List;
import java.util.Optional;

public class WarpCommand implements CommandExecutor {

    private final Main instance = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());

        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));

        ServerPlayer pPlayer = ply.get();

        if (!context.hasAny(Parameter.key("warp", String.class))) {
            pPlayer.sendMessage(Message.error("Please provide a warp"));
            return CommandResult.success();
        }

        //If the method return true then the command is in lock
        if (!instance.cooldown.isAvailable(pPlayer, Cooldown.WARP)) {
            return CommandResult.success();
        }

        String warp = context.requireOne(Parameter.key("warp", String.class));
        if (warp.equals("minage") || warp.equals("exploration") || warp.equals("end") || warp.equals("nether")) {
            // Téléportation du joueur
            Locations.teleport(pPlayer, Locations.getOfficialDimSpawn(warp));
        } else {
            return CommandResult.success();
        }

        instance.cooldown.addPlayerCooldown(pPlayer, Cooldown.WARP, Cooldown.WARP_DELAY);

        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Se téléporter sur des points définis"))
                .permission("Isoworlds.warp")
                .addParameter(Parameter.builder(String.class).key("warp").build())
                .executor(new WarpCommand())
                .build();
    }

}