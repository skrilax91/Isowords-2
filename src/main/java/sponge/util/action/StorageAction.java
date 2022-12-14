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
package sponge.util.action;

import common.ManageFiles;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.Main;
import sponge.util.console.Logger;
import sponge.util.task.SAS.Pull;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;

import static sponge.util.action.LockAction.checkLockFormat;

public class StorageAction {

    public static final Main plugin = Main.instance;

    // Check tag of pPlayer Isoworld (@PUSH, @PUSHED, @PULL, @PULLED, @PUSHED@PULL, @PUSHED@PULLED)
    public static Boolean checkTag(Player pPlayer, String worldname) {

        // Cr??ation des chemins pour v??rification
        File file = new File(ManageFiles.getPath() + worldname);
        File file2 = new File(ManageFiles.getPath() + worldname + "@PUSHED");

        // Si vrai alors en ??tat @PUSHED en bdd
        if (StorageAction.getStatus(worldname)) {
            Logger.info("Isoworld: " + worldname + " EN ETAT @PUSHED");

            // !! Gestion des anomalies !!
            // Si le dossier sans tag et avec tag existe, alors il a ??t?? acc??d?? par un moyen tier et on supprime le non TAG
            if (file.exists()) {
                if (file2.exists()) {
                    Logger.warning(" --- Anomalie (@PUSHED: Dossier Isoworld et Isoworld tag tous deux pr??sents pour: " + worldname + " ---");
                    // D??chargement au cas ou
                    if (Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).isPresent()) {
                        Sponge.server().worldManager().unloadWorld(Sponge.server().worldManager().world(Main.instance.getWorldKey(worldname)).get());
                        Logger.warning(" --- Anomalie (@PUSHED: D??chargement du Isoworld anormalement charg??: " + worldname + " ---");
                    }
                    // Suppression du dossier
                    ManageFiles.deleteDir(file);
                }
            }

            // !! D??marrage de la proc??dure de r??cup??ration !!
            // Si dossier @PUSHED alors on le met en @PULL pour lancer la proc??dure de r??cup??ration
            if (file2.exists()) {
                Logger.info("PASSAGE EN @PULL: " + worldname);
                ManageFiles.rename(ManageFiles.getPath() + worldname + "@PUSHED", "@PULL");
                Logger.info("PASSAGE EN @PULL OK: " + worldname);

                // V??rification pour savoir si le dossier existe pour permettre au joueur d'utiliser la cmd maison
                // On retourne faux pour dire que le monde n'est pas disponible
                // Le passage du statut ?? NON @PUSHED se fait dans la task
                Sponge.asyncScheduler().submit(Pull.createTask(pPlayer, file), pPlayer.uniqueId().toString());

            } else {
                // Gestion du cas ou le dossier Isoworld ne serait pas pr??sent alors qu'il est @PUSHED en bdd
                Logger.warning(" --- Anomalie (@PUSHED): Dossier Isoworld tag n'existe pas: " + worldname + " ---");
            }

            // Retourner faux pour indiquer que le dossier n'existe pas, il doit ??tre en proc??dure
            return false;

        } else if (!StorageAction.getStatus(worldname)) {
            Logger.info("Isoworld DISPONIBLE: " + worldname + " - ETAT NON @PUSHED");

            // V??rification si le dossier @PUSHED n'existe pas, on le supprime dans ce cas, anomalie
            if (file2.exists()) {
                ManageFiles.deleteDir(file2);
                Logger.info(": " + worldname);
                Logger.warning(" --- Anomalie (NON @PUSHED): Dossier Isoworld et Isoworld tag tous deux pr??sents pour: " + worldname + " ---");
            }

            // Isoworld disponible, retour vrai
            return true;
        } else {
            // Si ni @PUSHED ni NON @PUSHED en BDD alors on retourne faux car il doit y avoir un gros probl??me :)
            Logger.warning(" --- Anomalie (NI @PUSHED NI NON @PUSHE): Isoworld: " + worldname + " ---");
            return false;
        }
    }

    // Check status of a Isoworld, if is Pushed return true, else return false
    public static Boolean getStatus(String world) {
        String CHECK = "SELECT STATUS FROM `isoworlds` WHERE `uuid_w` = ? AND `server_id` = ?";
        String check_w;
        try {
            Connection connection = plugin.database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // UUID_W
            check_w = world;
            check.setString(1, check_w);
            // SERVEUR_ID
            check.setString(2, plugin.servername);
            // Requ??te
            ResultSet rselect = check.executeQuery();
            Logger.info(check.toString());
            Logger.info("Debug 8");
            if (rselect.next()) {
                Logger.info(rselect.toString());
                Logger.info("Debug 9");
                if (rselect.getInt(1) == 1) {
                    Logger.info("Debug 10");
                    return true;
                } else {
                    return false;
                }

            }
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
        return false;
    }

    // Set global status
    public static Boolean setGlobalStatus() {
        String CHECK = "UPDATE `isoworlds` SET `status` = 1 WHERE `server_id` = ?";
        try {
            Connection connection = plugin.database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // SERVEUR_ID
            check.setString(1, plugin.servername);
            // Requ??te
            check.executeUpdate();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // Set status of Isoworld (1 for Pushed, 0 for Present)
    // It returns true if pushed, false si envoy?? ou ?? envoyer
    public static void setStatus(String world, Integer status) {
        String CHECK = "UPDATE `isoworlds` SET `status` = ? WHERE `uuid_w` = ? AND `server_id` = ?";
        String check_w;
        try {
            Connection connection = plugin.database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // STATUS
            check.setInt(1, status);
            // UUID_W
            check_w = (world);
            check.setString(2, check_w);
            // SERVEUR_ID
            check.setString(3, plugin.servername);
            // Requ??te
            Logger.info("Debug 3: " + check);
            check.executeUpdate();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    // V??rifie si le lock est pr??sent et renvoi vrai, sinon d??fini le lock et renvoi false
    public static Boolean iwInProcess(Player pPlayer, String worldname) {
        // Si le lock est set, alors on renvoie false avec un message de sorte ?? stopper la commande et informer le jouer
        if (checkLockFormat(worldname, worldname)) {
            return true;
        } else {
            // On set lock
            Main.lock.put(worldname + ";" + worldname, 1);
            return false;
        }
    }

    public static boolean isMirrored(ServerWorld world) {
        return isMirrored(world.properties().name());
    }

    public static boolean isMirrored(String worldname) {
        // Check if file exist, to detect mirrors
        File file = new File(ManageFiles.getPath() + "/" + worldname + "@PUSHED");
        File file2 = new File(ManageFiles.getPath() + "/" + worldname);
        // If exists and contains Isoworld
        return (file.exists() & file2.exists() & worldname.contains("-isoworld"));
    }
}
