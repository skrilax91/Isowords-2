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
package sponge.command;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.exception.CommandException;
import net.kyori.adventure.text.Component;
import sponge.Main;

import sponge.command.sub.*;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import sponge.util.inventory.MainInv;

public class Commands implements CommandExecutor {

    private final Main plugin = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        ServerPlayer pPlayer = null;
        CommandCause cause = context.cause();

        Audience audience = cause.audience();
        if (audience instanceof ServerPlayer) {
            pPlayer = (ServerPlayer) audience;
        } else {
            throw new CommandException(Component.text("Your are not a player.", NamedTextColor.RED));
        }

        pPlayer.openInventory(MainInv.menuPrincipal(pPlayer).inventory());
        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {
        return Command.builder()
                .shortDescription(Component.text("Commande Isoworlds, permet de créer/refondre/lister"))
                .permission("Isoworlds.default")
                .addChild(CreateCommand.getCommand(), "creation", "créer", "creer", "create", "c")
                .addChild(ListWorldsCommand.getCommand(), "lister", "liste", "list", "l")
                .addChild(ReforgeCommand.getCommand(), "refonte", "refondre", "r")
                .addChild(HomeCommand.getCommand(), "maison", "home", "h")
                .addChild(WarpCommand.getCommand(), "warp", "w")
                .addChild(BiomeCommand.getCommand(), "biome", "b")
                .addChild(TrustCommand.getCommand(), "confiance", "trust", "a")
                .addChild(UnTrustCommand.getCommand(), "retirer", "supprimer", "untrust", "remove")
                .addChild(WeatherCommand.getCommand(), "meteo", "weather", "m", "météo")
                .addChild(TimeCommand.getCommand(), "time", "temps", "t", "cycle")
                .executor(new Commands())
                .build();
    }
}
