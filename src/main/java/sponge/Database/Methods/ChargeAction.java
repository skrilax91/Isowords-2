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
import common.Msg;
import sponge.Database.MysqlHandler;
import org.bukkit.entity.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChargeAction {

    private static final MysqlHandler database = Manager.getInstance().getMysql();

    /**
     * <p> Get number of charge a player have
     *
     * @param pPlayer The {@link ServerPlayer} to check
     * @return  The number of charge
     * @exception SQLException if a database access error occurs
     */
    public static Integer getCharge(ServerPlayer pPlayer) throws SQLException {
        if (pPlayer.hasPermission("Isoworlds.unlimited.charges"))
            return Integer.MAX_VALUE;

        String CHECK = "SELECT `charges` FROM `players_info` WHERE `uuid_p` = ?";
        Connection connection = database.getConnection();

        try {
            PreparedStatement check = connection.prepareStatement(CHECK);

            // Player uuid
            check.setString(1, pPlayer.uniqueId().toString());
            // Request

            ResultSet res = check.executeQuery();
            if (res.isBeforeFirst())
                return res.getInt(1);

            initCharges(pPlayer);
            return 0;
        } catch (Exception se) {
            se.printStackTrace();
            return 0;
        }
    }


    /**
     * <p> Set the current number of charge for a player
     *
     * @param pPlayer The {@link ServerPlayer} to check
     * @param charges The number of charge to set
     * @return whether the function was successful or not
     * @exception SQLException if a database access error occurs
     */
    public static Boolean updateCharge(ServerPlayer pPlayer, Integer charges) throws SQLException {
        if (pPlayer.hasPermission("Isoworlds.unlimited.charges"))
            return true;

        String CHECK = "UPDATE `players_info` SET `charges` = ? WHERE `UUID_P` = ?";
        Connection connection = database.getConnection();
        try {
            PreparedStatement check = connection.prepareStatement(CHECK);
            // Player uuid
            check.setString(2, pPlayer.uniqueId().toString());
            // Amout
            check.setInt(1, charges);
            // Request
            check.executeUpdate();
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * <p> Initialize charge system for a player
     *
     * @param pPlayer The {@link ServerPlayer} to check
     * @return whether the function was successful or not
     * @exception SQLException if a database access error occurs
     */
    public static Boolean initCharges(ServerPlayer pPlayer) throws SQLException {
        String INSERT = "INSERT INTO `players_info` (`uuid_p`, `charges`, `playtimes`) VALUES (?, 0, 0)";
        Connection connection = database.getConnection();

        try {
            PreparedStatement insert = connection.prepareStatement(INSERT);
            // Player uuid
            insert.setString(1, pPlayer.uniqueId().toString());

            insert.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}