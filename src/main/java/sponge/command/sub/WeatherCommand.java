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
import sponge.database.Methods.ChargeAction;
import sponge.database.Methods.TrustAction;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.weather.WeatherTypes;
import sponge.Main;
import org.spongepowered.api.command.CommandResult;
import sponge.translation.TranslateManager;
import sponge.util.action.StatAction;
import sponge.util.message.Message;

import java.sql.SQLException;
import java.util.*;

import static java.lang.Integer.parseInt;

public class WeatherCommand implements CommandExecutor {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private final Main instance = Main.instance;

    @Override
    public CommandResult execute(CommandContext context) throws CommandException {
        Optional<ServerPlayer> ply = StatAction.getPlayerFromCause(context.cause());
        int time = 0;
        if (!ply.isPresent())
            throw new CommandException(Message.error("Your are not a player."));
        ServerPlayer pPlayer = ply.get();

        //If the method return true then the command is in lock
        if (!instance.cooldown.isAvailable(pPlayer, Cooldown.METEO)) {
            return CommandResult.success();
        }

        // If got charges
        int charges = 0;
        try {
            charges = ChargeAction.getCharge(pPlayer);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (charges <= 0) {
            pPlayer.sendMessage(sponge.util.message.Message.error(translateManager.translate("ChargeEmpty")));
            return CommandResult.success();
        }

        // Check if actual world is an Isoworld
        if (!pPlayer.world().properties().name().contains("-isoworld")) {
            pPlayer.sendMessage(Message.error(translateManager.translate("NotInAIsoworld")));
            return CommandResult.success();
        }

        // Check if player is trusted
        if (!TrustAction.isTrusted(pPlayer, pPlayer.world().properties().name())) {
            pPlayer.sendMessage(Message.error(translateManager.translate("NotTrusted")));
            return CommandResult.success();
        }

        if (!context.hasAny(Parameter.key("weather", String.class))) {
            pPlayer.sendMessage(Message.success(translateManager.translate("HeaderIsoworld")));
            pPlayer.sendMessage(Message.success(translateManager.translate("SpaceLine")));
            pPlayer.sendMessage(Message.success (translateManager.translate("WeatherTypes")));
            pPlayer.sendMessage(Message.success(translateManager.translate("WeatherTypesDetail")));
            pPlayer.sendMessage(Message.success(translateManager.translate("SpaceLine")));
            return CommandResult.success();

        }

        String weather = context.requireOne(Parameter.key("weather", String.class));
        if (!context.hasAny(Parameter.key("time", String.class))) {
            time = context.requireOne(Parameter.key("time", int.class));
        }

        switch (weather) {
            case "clear":
                pPlayer.world().setWeather(WeatherTypes.CLEAR.get(), Ticks.of(time));
                break;
            case "rain":
                pPlayer.world().setWeather(WeatherTypes.RAIN.get(), Ticks.of(time));
                break;
            case "storm":
                pPlayer.world().setWeather(WeatherTypes.THUNDER.get(), Ticks.of(time));
                break;
            default:
                return CommandResult.success();
        }
        // Message pour tous les joueurs
        for (ServerPlayer p : pPlayer.world().players()) {
            p.sendMessage(Message.success(translateManager.translate("WeatherChangeSuccess") + pPlayer.name()));
        }


        try {
            if (ChargeAction.updateCharge(pPlayer, charges - 1))
                pPlayer.sendMessage(Message.success(translateManager.translate("ChargeUsed")));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        instance.cooldown.addPlayerCooldown(pPlayer, Cooldown.METEO, Cooldown.METEO_DELAY);

        return CommandResult.success();
    }

    public static Command.Parameterized getCommand() {

        return Command.builder()
                .shortDescription(Component.text("Modifier la météo de l'IsoWorld"))
                .permission("Isoworlds.weather")
                .addParameter(Parameter.choices("clear", "rain", "storm").key("weather").build())
                .addParameter(Parameter.bigInteger().optional().key("time").build())
                .executor(new WeatherCommand())
                .build();
    }
}