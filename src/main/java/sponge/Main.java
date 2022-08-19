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
package sponge;

import com.google.inject.Inject;
import common.*;

import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import org.spongepowered.api.Server;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.command.Command;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;


import sponge.command.Commands;
import sponge.configuration.Configuration;
import sponge.listener.ChatListeners;
import sponge.listener.Listeners;
import sponge.util.ConfigManager;
import sponge.util.action.DimsAltAction;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;
import sponge.util.task.PlayerStatistic.PlayTime;
import sponge.util.task.SAS.PreventLoadingAtStart;
import sponge.util.task.SAS.Push;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Plugin("isoworlds2")
public class Main implements IMain {
    public static Main instance;
    public final common.Logger commonLogger;
    private final org.apache.logging.log4j.Logger logger;
    private final Game game;
    private final PluginContainer container;
    public String servername;
    public static Map<String, Integer> lock = new HashMap<>();
    public Cooldown cooldown;
    public Mysql database;

    @Inject
    public static PluginContainer pluginContainer;

    @Inject
    public Main(org.apache.logging.log4j.Logger logger, Game game, final PluginContainer container) {
        this.logger = logger;
        this.commonLogger = new common.Logger("sponge");
        this.game = game;
        this.container = container;
        instance = this;
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event)
    {
        event.register(this.container, Commands.getCommand(), "iw", "Isoworld", "Isoworlds");
    }



    @Listener
    public void onGameInit(StartingEngineEvent<Server> event) {
        Logger.tag();
        PluginContainer pdf = Sponge.pluginManager().plugin("isoworlds2").get();
        Logger.info("Chargement de la version Sponge: " + pdf.metadata().version() + " Auteur: " + pdf.metadata().contributors() + " Site: " + pdf.metadata().links().homepage());
        Logger.info("Chargement des fichiers de configuration...");
        ConfigManager.load();

        registerEvents();

        // Create needed dirs
        ManageFiles.initIsoworldsDirs();
        Logger.info("Lecture de la configuration...");
        this.initServerName();
        Logger.info("Connexion à la base de données...");
        if (!this.initMySQL()) {
            Logger.severe("Une erreur c'est produite lors de la tentative de connexion !");
            return;
        }
        Logger.info("Isoworlds connecté avec succès à la base de données !");

        // Copy lang.yml if not in config folder
        // thx @ryantheleac for intellij module path
        /*try {
            final Path localePath = Paths.get(configuration.getParent());
            final Asset asset = this.pluginContainer.getAsset("lang.yml").orElse(null);
            if (!new File(localePath.toString() + "/lang.yml").exists()) {
                if (asset != null) {
                    asset.copyToDirectory(localePath);
                }
            }
        } catch (IOException io) {
            io.printStackTrace();
        }*/

        this.cooldown = new Cooldown(this.database, this.servername, "sponge", this.commonLogger);

        // Log configs
        Logger.info("[CONFIG] id: " + Configuration.getId());
        Logger.info("[CONFIG] main_worldname: " + Configuration.getMainWorld());
        Logger.info("[CONFIG] main_world_spawn_coordinate: " + Configuration.getMainWorldSpawnCoordinate());
        Logger.info("[CONFIG] inactivity_before_world_unload: " + Configuration.getInactivityTime());

        // Init manager
        Manager.instance = Main.instance;

        // Set structure if needed
        this.database.setStructure();
    }

    @Listener
    public void onPostInit(StartedEngineEvent<Server> event) {
        // ****** MODULES ******

        // IsoWorlds-SAS move iw to folder sas
        PreventLoadingAtStart.move();
        // Reset auto atl dim process
        ResetAutoDims.reset("sponge");
        // *********************

        // Storage
        if (Configuration.getStorage()) {
            // Start push action (unload task with tag)
            Push.PushProcess(Configuration.getInactivityTime());
            // Set global status 1
            StorageAction.setGlobalStatus();
        }

        // PlayTime
        if (Configuration.getPlayTime()) {
            // Start playtime task
            PlayTime.IncreasePlayTime();
        }

        // *********************

        // Loading messages
        Msg.keys();
    }

    @Listener
    public void onGameStarted(LoadedGameEvent event) {
        // Move iw from Isoworlds-SAS to main world folder
        PreventLoadingAtStart.moveBack();

        // ****** MODULES ******

        // DimensionAlt
        if (Configuration.getDimensionAlt()) {
            // Gen alt dim
            DimsAltAction.generateDim();
        }
        // *********************
    }

    private void registerEvents() {
        Sponge.eventManager().registerListeners(pluginContainer, new Listeners());
        Sponge.eventManager().registerListeners(pluginContainer, new ChatListeners());
    }

    public Game getGame() {
        return game;
    }

    public org.apache.logging.log4j.Logger getLogger() {
        return logger;
    }

    private boolean initMySQL() {
        if (this.database == null) {
            this.database = new Mysql(
                    (String) ConfigManager.configurationNode.node("Isoworlds", "sql", "host").toString(),
                    (Integer) ConfigManager.configurationNode.node("Isoworlds", "sql", "port").getInt(),
                    (String) ConfigManager.configurationNode.node("Isoworlds", "sql", "database").toString(),
                    (String) ConfigManager.configurationNode.node("Isoworlds", "sql", "username").toString(),
                    (String) ConfigManager.configurationNode.node("Isoworlds", "sql", "password").toString(),
                    true
            );

            try {
                this.database.connect();
            } catch (Exception ex) {
                Logger.info("Une erreur est survenue lors de la connexion à la base de données: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
        }

        return true;
    }

    private void initServerName() {
        try {
            this.servername = ConfigManager.configurationNode.node("Isoworlds2", "Id").toString();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public Mysql getMysql() {
        return this.database;
    }

    @Override
    public String getServername() {
        return this.servername;
    }

    @Override
    public Map<String, Integer> getLock() {
        return lock;
    }

    public CommentedConfigurationNode getConfig() {
        return ConfigManager.configurationNode;
    }
}