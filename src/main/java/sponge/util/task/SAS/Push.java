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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.server.ServerWorld;
import sponge.Main;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
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
        Sponge.server().scheduler().submit(
                Task.builder()
                        .execute(new Push())
                        .interval(1, TimeUnit.MINUTES)
                        .plugin(Main.instance.getContainer())
                        .build(),
                "Analyse des Isoworlds vides...");
    }

    @Override
    public void run() {
        // D??marrage de la proc??dure, on log tout les ??lements du map ?? chaque fois
        Logger.warning("D??marrage de l'analayse des Isoworlds vides pour d??chargement...");

        if (worlds.isEmpty()) {
            Logger.info("Isoworlds inactifs ?? l'analyse pr??c??dente: Aucun");
        } else {
            Logger.info("Isoworlds inactifs ?? l'analyse pr??c??dente:");
            for (Map.Entry<String, Integer> entry : worlds.entrySet()) {
                Logger.info("- " + entry);
            }
        }

        final Collection<ServerWorld> serverWorlds = Sponge.server().worldManager().worlds();

        // Boucle de tous les mondes
        for (ServerWorld world : serverWorlds) {
            // Si le monde est charg?? et contient Isoworld
            if (world.isLoaded() & world.properties().name().contains("-isoworld")) {

                // Si le nombre de joueurs == 0
                if (world.players().isEmpty()) {
                    // Si le monde n'est pas pr??sent dans le tableau
                    if (worlds.get(world.properties().name()) == null) {
                        worlds.put(world.properties().name(), 1);
                        Logger.warning(world.properties().name() + " vient d'??tre ajout?? ?? l'analyse");
                    } else {
                        // Sinon on incr??mente
                        worlds.put(world.properties().name(), worlds.get(world.properties().name()) + 1);
                    }

                    // Si le nombre est sup??rieur ou = ?? X on unload
                    if (worlds.get(world.properties().name()) >= time) {
                        Logger.info("La valeur de: " + world.properties().name() + " est de " + time + " , d??chargement...");
                        // Proc??dure de d??chargement //

                        // Save & unload AUTO CORRECT
                        try {
                            Logger.info("Sauvegarde de l'isoword...");
                            if (!world.save()) {
                                Logger.info("Sauvegarde ??chou??e...");
                                continue;
                            }

                            // If is mirrored then unload + remove then return
                            if (StorageAction.isMirrored(world)) {
                                Logger.severe("--- Anomalie d??tect??e, unload interrompu et suppression de l'anomalie: " + world.properties().name() + " ---");
                                Sponge.server().worldManager().deleteWorld(world.key()).get();
                                Logger.severe("--- Anomalie: Corrig??e, suppression effectu??e avec succ??s de l'Isoworld: " + world.properties().name() + " ---");
                                continue;
                            }

                            if (!Sponge.server().worldManager().unloadWorld(world).get()) {
                                Logger.severe("--- Echec du d??chargement de l'Isoworld: " + world.properties().name() + " ---");
                                continue;
                            }
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }


                        // Suppression dans le tableau
                        worlds.remove(world.properties().name());

                        // V??rification du statut du monde, si il est push ou non
                        if (StorageAction.getStatus(world.properties().name())) {
                            continue;
                        }


                        Logger.info("debug 1");
                        File check = new File(ManageFiles.getPath() + world.properties().name());
                        // Si le dossier existe alors on met le statut ?? 1 (push)
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
                            Logger.info("- " + world.properties().name() + " : PUSH avec succ??s");

                            // Suppression du monde
                            try {
                                if (Sponge.server().worldManager().deleteWorld(Main.instance.getWorldKey(world.properties().name())).get()) {
                                    Logger.info("- " + world.properties().name() + " : Isoworld supprim?? avec succ??s !");
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    // Si le nombre de joueur est sup??rieur ?? 0, purge le tableau du Isoworld
                } else if (worlds.get(world.properties().name()) != null) {
                    worlds.remove(world.properties().name());
                    Logger.warning(world.properties().name() + " de nouveau actif, supprim?? de l'analyse");
                }
            }
        }

        if (worlds.isEmpty()) {
            Logger.info("Aucun Isoworld n'est ?? " + time + " minutes d'inactivit??...");
            Logger.warning("Fin de l'analyse");
        } else {
            Logger.info("Les Isoworlds vides depuis " + time + " minutes viennent d'??tre d??charg??s");
            Logger.warning("Fin de l'analyse");
        }
    }
}
