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
package common;

import org.spongepowered.api.entity.living.player.Player;
import sponge.database.MysqlHandler;
import sponge.Main;
import sponge.translation.TranslateManager;
import sponge.util.message.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Cooldown implements ICooldown {
    private final Logger logger;
    private final MysqlHandler database;
    private final String servername;
    private final String type;

    private static TranslateManager translateManager = Main.instance.translateManager;

    public Cooldown(MysqlHandler database, String servername, String type, Logger logger) {
        this.database = database;
        this.servername = servername;
        this.type = type;
        this.logger = logger;
    }

    public boolean isSponge() {
        return this.type.equals("sponge");
    }


    /**
     * Sponge method
     */
    public boolean isAvailable(Player pPlayer, String type) {
        Timestamp cooldown = this.getPlayerLastCooldown(pPlayer, type);
        if (cooldown != null) {
            String timerMessage = this.getCooldownTimer(cooldown);
            pPlayer.sendMessage(Message.error(translateManager.translate("CommandCooldown") + timerMessage));
            Main.lock.remove(pPlayer.uniqueId().toString() + ";" + String.class.getName());

            return false;
        }

        return true;
    }


    /**
     * Sponge method
     */
    public Timestamp getPlayerLastCooldown(Player pPlayer, String type) {
        String uuid_p = pPlayer.uniqueId().toString();

        return getPlayerLastCooldown(uuid_p, type);
    }

    /**
     * Return all the occurences for a given player, type (ex: refonte) with date greater than now
     */
    private Timestamp getPlayerLastCooldown(String uuid_p, String type) {
        String query = "SELECT * FROM `players_cooldown` WHERE `uuid_p` = ? AND `cooldown_type` = ? AND `date_time` > ? AND `server_id` = ?";
        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(query);

            // UUID _P
            check.setString(1, uuid_p);

            //Type
            check.setString(2, type);

            //Date
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            check.setTimestamp(3, timestamp);

            // SERVEUR_ID
            check.setString(4, this.servername);

            ResultSet resultSet = check.executeQuery();
            if (resultSet.next()) {
                return resultSet.getTimestamp("date_time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Sponge method
     */
    public void addPlayerCooldown(Player pPlayer, String type, int delay) {
        String uuid_p = pPlayer.uniqueId().toString();
        addPlayerCooldown(uuid_p, type, delay);
    }

    private void addPlayerCooldown(String uuid_p, String type, int delay) {
        String query = "INSERT INTO `players_cooldown` (`uuid_p`, `date_time`, `cooldown_type`, `server_id`) VALUES (?, ?, ?, ?)";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis() + (delay * 1000L));
        try {
            Connection connection = database.getConnection();
            PreparedStatement insert = connection.prepareStatement(query);

            // UUID_P
            insert.setString(1, uuid_p);

            // Date
            insert.setString(2, (timestamp.toString()));

            // Type
            insert.setString(3, type);

            // SERVEUR_ID
            insert.setString(4, this.servername);

            insert.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private String getCooldownTimer(Timestamp timestamp) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        long cooldown = (timestamp.getTime() - now.getTime()) / 1000;
        String timer = "";
        int hours = (int) (cooldown / 3600);
        if (hours > 0) {
            timer += " " + hours + plurializeMessage(hours, " heure");
        }

        int remainder = (int) (cooldown - hours * 3600);
        int mins = remainder / 60;
        if (mins > 0) {
            timer += " " + mins + plurializeMessage(mins, " minute");
        }

        remainder = remainder - mins * 60;
        int secs = remainder;
        if (secs > 0) {
            timer += " " + secs + plurializeMessage(secs, " seconde");
        }

        return timer;
    }

    private String plurializeMessage(int number, String message) {
        return number > 1 || number == 0 ? message + "s" : message;
    }
}
