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
import common.action.ChargeAction;
import common.action.TrustAction;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryReference;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.biome.Biomes;
import org.spongepowered.api.world.server.ServerLocation;
import sponge.Main;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.util.*;

public class BiomeCommand implements CommandExecutor {

    private static final Map<String, RegistryReference<Biome>> availableBiomes;
    static {
        Map<String, RegistryReference<Biome>> aMap = new HashMap<>();
        // Corriger l'erreur
        aMap.put("plaines", Biomes.PLAINS);
        aMap.put("desert", Biomes.DESERT);
        aMap.put("marais", Biomes.SWAMP);
        aMap.put("océan", Biomes.OCEAN);
        aMap.put("champignon", Biomes.MUSHROOM_FIELDS);
        aMap.put("jungle", Biomes.JUNGLE);
        aMap.put("enfer", Biomes.NETHER_WASTES);
        aMap.put("end", Biomes.THE_END);
        availableBiomes = Collections.unmodifiableMap(aMap);
    }

    private final Main instance = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());
        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));
        ServerPlayer pPlayer = ply.get();

        if (!context.hasAny(Parameter.key("biome", RegistryReference.class))) {
            pPlayer.sendMessage(Message.error("Please provide a biome type"));
            return CommandResult.success();
        }

        Biome arg = (Biome) context.requireOne(Parameter.key("biome", RegistryReference.class)).get();
        //If the method return true then the command is in lock
        if (!instance.cooldown.isAvailable(pPlayer, Cooldown.BIOME)) {
            return CommandResult.success();
        }

        // If got charges
        int charges = ChargeAction.checkCharge(pPlayer);
        if (charges == -1) {
            return CommandResult.success();
        }

        // Check if actual world is an Isoworld
        if (!pPlayer.world().properties().name().contains("-Isoworld")) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("NotInAIsoworld")));
            return CommandResult.success();
        }

        // Check if player is trusted
        if (!TrustAction.isTrusted(pPlayer.uniqueId().toString(), pPlayer.world().properties().name())) {
            pPlayer.sendMessage(Message.error(Msg.msgNode.get("NotTrusted")));
            return CommandResult.success();
        }

        // Setup every blocks of chunk to the clicked biome
        ServerLocation loc = pPlayer.serverLocation();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                pPlayer.world().setBiome(loc.chunkPosition().x() * 16 + x, 0, loc.chunkPosition().z() * 16 + z, arg);
            }
        }

        if (!pPlayer.hasPermission("Isoworlds.unlimited.charges")) {
            ChargeAction.updateCharge(pPlayer.uniqueId().toString(), charges - 1);
            pPlayer.sendMessage(Message.success(Msg.msgNode.get("ChargeUsed")));
        }

        pPlayer.sendMessage(Message.success(Msg.msgNode.get("BiomeChanged")));

        instance.cooldown.addPlayerCooldown(pPlayer, Cooldown.BIOME, Cooldown.BIOME_DELAY);

        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Permet de modifier le biome d'un chunk"))
                .permission("Isoworlds.biome")
                .addParameter(Parameter.choices(RegistryReference.class, availableBiomes).key("biome").build())
                .executor(new BiomeCommand())
                .build();
    }

}