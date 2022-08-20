package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
@ConfigSerializable
public class MysqlConfig {
    private String host = "127.0.0.1";
    private int port = 3306;
    private String database = "Isoworlds2";
    private String username = "DATABASE_USERNAME";
    private String password = "DATABASE_PASSWORD";


    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String database() {
        return database;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }
}
