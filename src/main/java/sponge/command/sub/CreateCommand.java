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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import sponge.Database.Methods.IsoworldsAction;
import sponge.util.message.Message;
import common.Msg;
import sponge.Database.Methods.TrustAction;
import sponge.location.Locations;
import common.ManageFiles;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;

import java.io.*;
import java.sql.SQLException;

public class CreateCommand implements CommandExecutor {

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        String fullPath = "";
        String worldName = "";
        ServerPlayer pPlayer = null;
        CommandCause cause = context.cause();

        Audience audience = cause.audience();
        if (audience instanceof ServerPlayer) {
            pPlayer = (ServerPlayer) audience;
        } else {
            throw new CommandException(Message.error("Your are not a player."));
        }

        // Check if Isoworld exists in database
        if (IsoworldsAction.isPresent(pPlayer, false)) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("IsoworldAlreadyExists")));
            return CommandResult.success();
        }

        // Create message
        pPlayer.sendMessage(Message.success(Msg.msgNode.get("CreatingIsoworld")));

        fullPath = (ManageFiles.getPath() + pPlayer.uniqueId() + "-Isoworld");
        worldName = (pPlayer.user().profile().uuid() + "-Isoworld");

        // Check properties exists
        if (Sponge.server().worldManager().worldExists(ResourceKey.brigadier(worldName))) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("IsoworldAlreadyExists")));
            return CommandResult.success();
        }

        // Check arg lenght en send patern types message
        if (!context.hasAny(Parameter.key("type", char.class))) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("HeaderIsoworld")));
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("SpaceLine")));
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("PaternTypes")));
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("PaternTypesDetail")));
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("SpaceLine")));
            return CommandResult.success();
        }

        char result = context.requireOne(Parameter.key("type", char.class));
        File sourceFile;
        switch (result) {
            case 'n':
                sourceFile = new File(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-PATERN/");
                break;
            case 'v':
                sourceFile = new File(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-PATERN-V/");
                break;
            case 'o':
                sourceFile = new File(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-PATERN-O/");
                break;
            case 'f':
                sourceFile = new File(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-PATERN-F/");
                break;
            default:
                return CommandResult.success();
        }

        File destFile = new File(fullPath);

        try {
            ManageFiles.copyFileOrFolder(sourceFile, destFile);
        } catch (IOException ie) {
            ie.printStackTrace();
            return CommandResult.success();
        }

        //  Create world properties
        IsoworldsAction.setWorldProperties(worldName, pPlayer);

        try {
            if (IsoworldsAction.addIsoworld(pPlayer)) {
                if (TrustAction.setTrust(pPlayer)) {
                    // Loading
                    Sponge.game().server().worldManager().loadWorld(ResourceKey.brigadier(worldName));

                    pPlayer.sendMessage(Message.success(Msg.msgNode.get("IsoworldsuccessCreate")));

                    // Teleport
                    Locations.teleport(pPlayer, worldName);

                    // Welcome title (only sponge)
                    pPlayer.showTitle(Title.title(Component.text(Msg.msgNode.get("Welcome1") + pPlayer.name()), Component.text(Msg.msgNode.get("Welcome2"))));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Permer de créer un isoworld"))
                .permission("Isoworlds.create")
                .addParameter(Parameter.choices("n", "v", "o", "f").key("type").build())
                .executor(new CreateCommand())
                .build();
    }
}