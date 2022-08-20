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
package sponge.util.task.SAS;

import common.ManageFiles;
import common.Msg;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.Main;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Push implements Runnable {
    static Map<String, Integer> worlds = new HashMap<>();

    private static int time = 0;

    public static void PushProcess(Integer time) {
        Push.time = time;

        // Isoworlds task unload
        Sponge.asyncScheduler().submit(
                Task.builder()
                        .execute(new Push())
                        .interval(1, TimeUnit.MINUTES)
                        .build(),
                "Analyse des Isoworlds vides...");
    }

    @Override
    public void run() {
        // Démarrage de la procédure, on log tout les élements du map à chaque fois
        Logger.warning("Démarrage de l'analayse des Isoworlds vides pour déchargement...");

        if (worlds.isEmpty()) {
            Logger.info("Isoworlds inactifs à l'analyse précédente: Aucun");
        } else {
            Logger.info("Isoworlds inactifs à l'analyse précédente:");
            for (Map.Entry<String, Integer> entry : worlds.entrySet()) {
                Logger.info("- " + entry);
            }
        }

        // Boucle de tous les mondes
        for (ServerWorld world : Sponge.server().worldManager().worlds()) {
            // Si le monde est chargé et contient Isoworld
            if (world.isLoaded() & world.properties().name().contains("-Isoworld")) {

                // Si le nombre de joueurs == 0
                if (world.players().isEmpty()) {
                    // Si le monde n'est pas présent dans le tableau
                    if (worlds.get(world.properties().name()) == null) {
                        worlds.put(world.properties().name(), 1);
                        Logger.warning(world.properties().name() + " vient d'être ajouté à l'analyse");
                    } else {
                        // Sinon on incrémente
                        worlds.put(world.properties().name(), worlds.get(world.properties().name()) + 1);
                    }

                    // Si le nombre est supérieur ou = à X on unload
                    if (worlds.get(world.properties().name()) >= time) {
                        Logger.info("La valeur de: " + world.properties().name() + " est de " + time + " , déchargement...");
                        // Procédure de déchargement //

                        // Sauvegarde du monde et déchargement
                            /*try {
                                Sponge.server().worldManager().world(ResourceKey.brigadier(world.properties().name())).get().save();
                            } catch (IOException e) {
                                e.printStackTrace();
                                continue;
                            }*/

                        // Save & unload AUTO CORRECT
                        try {
                            world.save();
                            // If is mirrored then unload + remove then return
                            if (StorageAction.isMirrored(world)) {
                                Logger.severe("--- Anomalie détectée, unload interrompu et suppression de l'anomalie: " + world.properties().name() + " ---");
                                Sponge.server().worldManager().deleteWorld(ResourceKey.brigadier(world.properties().name()));
                                Logger.severe("--- Anomalie: Corrigée, suppression effectuée avec succès de l'Isoworld: " + world.properties().name() + " ---");
                                continue;
                            } else if (!Sponge.server().worldManager().unloadWorld(world).get()) {
                                Logger.severe("--- Echec du déchargement de l'Isoworld: " + world.properties().name() + " ---");
                                continue;
                            }
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }


                        // Suppression dans le tableau
                        worlds.remove(world.properties().name());

                        // Vérification du statut du monde, si il est push ou non
                        if (StorageAction.getStatus(world.properties().name())) {
                            continue;
                        }


                        Logger.info("debug 1");
                        File check = new File(ManageFiles.getPath() + world.properties().name());
                        // Si le dossier existe alors on met le statut à 1 (push)
                        if (check.exists()) {
                            Logger.info("debug 2");
                            StorageAction.setStatus(world.properties().name(), 1);

                            // Suppression ID
                            ManageFiles.deleteDir(new File(ManageFiles.getPath() + "/" + world.properties().name() + "/level_sponge.dat"));
                            ManageFiles.deleteDir(new File(ManageFiles.getPath() + "/" + world.properties().name() + "/level_sponge.dat_old"));
                            ManageFiles.deleteDir(new File(ManageFiles.getPath() + "/" + world.properties().name() + "/session.lock"));
                            ManageFiles.deleteDir(new File(ManageFiles.getPath() + "/" + world.properties().name() + "/forcedchunks.dat"));

                            // Tag du dossier en push
                            ManageFiles.rename(ManageFiles.getPath() + world.properties().name(), "@PUSH");
                            Logger.info("- " + world.properties().name() + " : PUSH avec succès");

                            // Suppression du monde
                            try {
                                if (Sponge.server().worldManager().deleteWorld(ResourceKey.brigadier(world.properties().name())).get()) {
                                    Logger.info("- " + world.properties().name() + " : Isoworld supprimé avec succès !");
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Si le nombre de joueur est supérieur à 0, purge le tableau du Isoworld
                } else if (worlds.get(world.properties().name()) != null) {
                    worlds.remove(world.properties().name());
                    Logger.warning(world.properties().name() + " de nouveau actif, supprimé de l'analyse");
                }
            }
        }

        if (worlds.isEmpty()) {
            Logger.info("Aucun Isoworld n'est à " + time + " minutes d'inactivité...");
            Logger.warning("Fin de l'analyse");
        } else {
            Logger.info("Les Isoworlds vides depuis " + time + " minutes viennent d'être déchargés");
            Logger.warning("Fin de l'analyse");
        }
    }
}
