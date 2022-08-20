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
package sponge.Database.Methods;

import common.Manager;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import sponge.Database.MysqlHandler;
import sponge.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayTimeAction {

    private static final MysqlHandler database = Main.instance.getMysql();

    /**
     * <p> Add 1 playtime of a player
     *
     * @param ply The player
     * @return whether the function was successful or not
     */
    public static Boolean updatePlayTime(ServerPlayer ply) {
        String CHECK = "UPDATE `players_info` SET `playtimes` = `playtimes` + 1 WHERE `uuid_p` = ?";
        try {
            Connection connection = database.getConnection();

            PreparedStatement check = connection.prepareStatement(CHECK);
            // Player uuid
            check.setString(1, ply.uniqueId().toString());
            // Request
            check.executeUpdate();
            return true;
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
    }

    /**
     * <p> Get playtime of a player
     *
     * @param ply The player
     * @return the play time of the player
     */
    public static Integer getPlayTime(ServerPlayer ply) {
        String CHECK = "SELECT `playtimes` FROM `players_info` WHERE `uuid_p` = ?";

        try {
            Connection connection = database.getConnection();

            PreparedStatement check = connection.prepareStatement(CHECK);
            check.setString(1, ply.uniqueId().toString());
            // Reqest
            ResultSet res = check.executeQuery();
            if (res.next()) {
                return res.getInt(1);
            }
        } catch (Exception se) {
            se.printStackTrace();
            return null;
        }
        return 0;
    }
}