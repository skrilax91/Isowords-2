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
package sponge.database.Methods;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import sponge.database.MysqlHandler;
import sponge.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class TrustAction {

    private static final MysqlHandler database = Main.instance.getMysql();

    /**
     * <p> Get all isoworlds autorised for a player
     *
     * @param ply The player
     * @return {@link ResultSet} of all isoworld
     */
    public static ResultSet getAccess(ServerPlayer ply) {
        String CHECK = "SELECT `uuid_w` FROM `autorisations` WHERE `uuid_p` = ? AND `server_id` = ?";
        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);
            check.setString(1, ply.uniqueId().toString());
            // Server id
            check.setString(2, Main.instance.servername);
            // Request
            ResultSet res = check.executeQuery();
            if (res.isBeforeFirst())
                return res;
        } catch (Exception se) {
            se.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * <p> Get all Trusted player of an isoword
     *
     * @param worldName The world name (can be without -isoworld)
     * @return {@link ResultSet} of all player
     */
    public static ResultSet getTrusts(String worldName) {
        String CHECK = "SELECT `uuid_p` FROM `autorisations` WHERE `uuid_w` = ? AND `server_id` = ?";
        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // World name
            if (!worldName.contains("-isoworld"))
                worldName = (worldName + "-isoworld");


            check.setString(1, worldName);
            check.setString(2, Main.instance.servername);

            // Request
            ResultSet res = check.executeQuery();
            if (res.isBeforeFirst())
                return res;
        } catch (Exception se) {
            se.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * <p> add trusted player to isoworld
     *
     * @param ply The player
     * @return whether the function was successful or not
     */
    public static Boolean setTrust(ServerPlayer ply) {
        return setTrust(ply.uniqueId().toString() + "-isoworld", ply);
    }

    /**
     * <p> add trusted player to isoworld
     *
     * @param worldName The world name (can be without -isoworld)
     * @param ply The player
     * @return whether the function was successful or not
     */
    public static Boolean setTrust(String worldName, ServerPlayer ply) {
        String INSERT = "INSERT INTO `autorisations` (`uuid_p`, `uuid_w`, `date_time`, `server_id`) VALUES (?, ?, ?, ?)";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        try {
            Connection connection = database.getConnection();

            PreparedStatement insert = connection.prepareStatement(INSERT);
            // Player uuid
            insert.setString(1, ply.uniqueId().toString());
            // World name
            if (!worldName.contains("-isoworld"))
                worldName = (worldName + "-isoworld");

            insert.setString(2, worldName);
            // Date
            insert.setString(3, (timestamp.toString()));
            // Serveur_id
            insert.setString(4, Main.instance.servername);
            insert.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Delete player from trusted list on specific IsoWorld
     *
     * @param worldName The name of the IsoWorld (can be without -isoworld)
     * @param ply The player to remove
     * @return {@link Boolean}
     */
    public static Boolean deleteTrust(String worldName, ServerPlayer ply) {
        String DELETE_AUTORISATIONS = "DELETE FROM `autorisations` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        try {
            Connection connection = database.getConnection();
            PreparedStatement delete_autorisations = connection.prepareStatement(DELETE_AUTORISATIONS);

            // World name
            if (!worldName.contains("-isoworld"))
                worldName += "-isoworld";

            // delete autorisation
            delete_autorisations.setString(1, ply.uniqueId().toString());
            delete_autorisations.setString(2, worldName);
            delete_autorisations.setString(3, Main.instance.servername);

            // execute
            delete_autorisations.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Check if a player is trusted on specific IsoWorld
     *
     * @param ply The player to check
     * @param worldName The name of the IsoWorld (can be without -isoworld)
     * @return {@link Boolean}
     */
    public static Boolean isTrusted(ServerPlayer ply, String worldName) {
        String CHECK = "SELECT * FROM `autorisations` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // Player uuid
            check.setString(1, ply.uniqueId().toString());
            // World name
            if (!worldName.contains("-isoworld"))
                worldName += "-isoworld";

            check.setString(2, worldName);
            check.setString(3, Main.instance.servername);
            // Request
            ResultSet res = check.executeQuery();
            if (res.isBeforeFirst())
                return true;
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
        return false;
    }
}