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

import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.configurate.CommentedConfigurationNode;

import org.spongepowered.api.Server;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.command.Command;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;


import sponge.Database.MysqlHandler;
import sponge.command.Commands;
import sponge.configuration.IsoworldConfiguration;
import sponge.listener.ChatListeners;
import sponge.listener.Listeners;
import sponge.util.action.DimsAltAction;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;
import sponge.util.task.SAS.PreventLoadingAtStart;
import sponge.util.task.SAS.Push;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.sql.SQLException;
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
    public MysqlHandler database;

    @DefaultConfig(sharedRoot = false)
    private final HoconConfigurationLoader ConfigLoader;
    private CommentedConfigurationNode rootNode;
    private IsoworldConfiguration config = null;

    @Inject
    public Main(org.apache.logging.log4j.Logger logger, Game game, final PluginContainer container) {
        this.logger = logger;
        this.commonLogger = new common.Logger("sponge");
        this.game = game;
        this.container = container;
        this.ConfigLoader = HoconConfigurationLoader.builder().path(FileSystems.getDefault().getPath("config/isoworlds2/main.conf")).build();
        instance = this;
    }

    @Listener
    public void onRegisterCommands(RegisterCommandEvent<Command.Parameterized> event)
    {
        event.register(this.container, Commands.getCommand(), "iw", "Isoworld", "Isoworlds");
    }

    @Listener
    public void onConstruct(final ConstructPluginEvent event) throws SQLException, ClassNotFoundException {

        logger.info(Logger.pluginTag);
        logger.info("[IW] Chargement de la version Sponge: " + container.metadata().version() + " Auteur: " + container.metadata().contributors().get(0).name() + " Site: " + container.metadata().links().homepage());
        logger.info("[IW] Chargement des fichiers de configuration...");

        try {
            rootNode = ConfigLoader.load();
            this.config = rootNode.get(IsoworldConfiguration.class);
            rootNode.set(IsoworldConfiguration.class, this.config);
        } catch (IOException e) {
            System.err.println("[IW] An error occurred while loading this configuration: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
        }

        try {
            ConfigLoader.save(rootNode);
        } catch (final ConfigurateException e) {
            Logger.severe("[IW] Unable to save your messages configuration! Sorry! " + e.getMessage());
            System.exit(1);
        }
        logger.info("[IW] Fichiers chargés !");

        // Log configs
        logger.info("[IW][CONFIG] id: " + this.config.serverId());
        logger.info("[IW][CONFIG] main_worldname: " + this.config.mainWorld());
        logger.info("[IW][CONFIG] main_world_spawn_coordinate: " + this.config.mainWorldSpawnCoordinate());
        logger.info("[IW][CONFIG] inactivity_before_world_unload: " + this.config.modules().automaticUnload().inactivityTime());

        logger.info("[IW] Enregistrement des events...");
        registerEvents();
        logger.info("[IW] Connexion à la base de donnée " + config.getSql().database());
        this.database = new MysqlHandler(config.getSql());
        logger.info("[IW] Isoworlds connecté avec succès à la base de données !");
    }



    @Listener
    public void onGameInit(StartingEngineEvent<Server> event) {
        //ConfigManager.load();

        // Create needed dirs
        Logger.info("Initialisation des répertoires...");
        ManageFiles.initIsoworldsDirs();

        Logger.info("Lecture de la configuration...");
        this.initServerName();

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

        // Init manager
        Manager.instance = Main.instance;
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
        if (config.modules().storage().isEnable()) {
            // Start push action (unload task with tag)
            Push.PushProcess(config.modules().automaticUnload().inactivityTime());
            // Set global status 1
            StorageAction.setGlobalStatus();
        }

        // PlayTime
        /*if (config.playTime()) {
            // Start playtime task
            PlayTime.IncreasePlayTime();
        }*/

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
        if (config.modules().dimensionAlt().isEnable()) {
            // Gen alt dim
            DimsAltAction.generateDim();
        }
        // *********************
    }

    private void registerEvents() {
        Sponge.eventManager().registerListeners(container, new Listeners());
        Sponge.eventManager().registerListeners(container, new ChatListeners());
    }

    public Game getGame() {
        return game;
    }

    public org.apache.logging.log4j.Logger getLogger() {
        return logger;
    }

    private void initServerName() {
        try {
            this.servername = config.serverId();
        } catch (NullPointerException npe) {
            npe.printStackTrace();
        }
    }

    @Override
    public MysqlHandler getMysql() {
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

    public IsoworldConfiguration getConfig() {
        return config;
    }

    public PluginContainer getContainer() {
        return this.container;
    }
}