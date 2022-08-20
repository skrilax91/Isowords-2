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
package sponge.Database;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.sql.SqlManager;
import sponge.configuration.MysqlConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MysqlHandler {

    public static MysqlHandler instance = null;
    private final MysqlConfig sqlConfigs;
    private final DataSource dataSource;

    public MysqlHandler(MysqlConfig configs) throws SQLException, ClassNotFoundException {
        instance = this;
        sqlConfigs = configs;
        String jdbcURL = this.createJDBCURL();
        SqlManager sqlManager = Sponge.game().sqlManager();
        dataSource = sqlManager.dataSource(jdbcURL);
        setStructure();
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private String createJDBCURL() throws ClassNotFoundException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return "jdbc:mysql://" + sqlConfigs.host() + ":" + sqlConfigs.port()
                + "/" + sqlConfigs.database()
                + "?user=" + sqlConfigs.username()
                + "&password=" + sqlConfigs.password();
    }

    public void setStructure() {
        String AUTORISATIONS = "CREATE TABLE IF NOT EXISTS autorisations (uuid_p varchar(60) NOT NULL, uuid_w varchar(60) NOT NULL, date_time varchar(30) NOT NULL, server_id varchar(30) NOT NULL, PRIMARY KEY (date_time));";
        String ISOWORLDS = "CREATE TABLE IF NOT EXISTS isoworlds (uuid_p varchar(60) NOT NULL, uuid_w varchar(60) NOT NULL, date_time varchar(30) NOT NULL, server_id varchar(30) NOT NULL, status int(1) NOT NULL, dimension_id int(11) DEFAULT 0, PRIMARY KEY (date_time));";
        String PLAYER_INFO = "CREATE TABLE IF NOT EXISTS players_info (id int(11) NOT NULL AUTO_INCREMENT, uuid_p varchar(255) NOT NULL, charges int(6) NOT NULL DEFAULT 0, playtimes int(4) NOT NULL, PRIMARY KEY (id), UNIQUE KEY id_2 (id), KEY id (id));";
        String PLAYER_COOLDOWN = "CREATE TABLE IF NOT EXISTS players_cooldown (id int(11) NOT NULL AUTO_INCREMENT, UUID_P varchar(60) NOT NULL, date_time timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(), cooldown_type varchar(60) NOT NULL, server_id varchar(60) NOT NULL, PRIMARY KEY (id));";

        try {
            Connection connection = dataSource.getConnection();
            connection.prepareStatement(AUTORISATIONS).execute();
            connection.prepareStatement(ISOWORLDS).execute();
            connection.prepareStatement(PLAYER_INFO).execute();
            connection.prepareStatement(PLAYER_COOLDOWN).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}