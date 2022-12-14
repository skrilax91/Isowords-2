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
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import sponge.Main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PreventLoadingAtStart {

    static Logger logger = Main.instance.getLogger();

    public static void move() {
        String name = "";

        // Isoworlds-SAS
        logger.info("[Isoworlds-SAS]: Stockage des Isoworlds un tag dans le SAS");
        File source = new File(ManageFiles.getPath());
        // Retourne la liste des Isoworld tag
        for (File f : ManageFiles.getOutSAS(source)) {
            name = f.getName();
            ManageFiles.deleteDir(new File(f.getPath() + "/level_sponge.dat"));
            ManageFiles.deleteDir(new File(f.getPath() + "/level_sponge.dat_old"));

            // Gestion des Isoworlds non push, si ne contient pas de tag alors "PUSH-SAS" et on le renomme lors de la sortie
            try {
                ManageFiles.move(new File(source + "/" + f.getName()), new File(ManageFiles.SASPath + "/" + f.getName()));

                // Si le dossier n'est pas TAG et que le dossier de ce m??me nom avec TAG n'existe pas
                if (!f.getName().contains("@PUSHED")) {
                    // Si le Isoworld poss??de pas de @PUSHED dans le dossier de base ou le SAS alors on supprime
                    if ((new File(ManageFiles.SASPath + "/" + f.getName() + "@PUSHED").exists()) || (new File(f.getPath() + f.getName() + "@PUSHED").exists())) {
                        ManageFiles.deleteDir(new File(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-SAS/" + f.getName()));
                        logger.info("[Isoworlds-SAS: Anomalie sur le Isoworld " + f.getName());
                        continue;
                    }
                    // Tag Isoworlds @PUSH if Storage config enabled
                    if (Main.instance.getConfig().modules().storage().isEnable()) {
                        ManageFiles.rename(ManageFiles.getPath() + "Isoworlds-UTILS/Isoworlds-SAS/" + f.getName(), "@PUSH");
                    }
                    logger.info("[Isoworlds-SAS]: Isoworlds " + name + " d??sormais TAG ?? PUSH");
                }
            } catch (IOException e) {
                logger.info("[Isoworlds-SAS]: Echec de stockage > " + name);
            }
        }
    }

    public static void moveBack() {
        Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
            sponge.util.console.Logger.info("[Isoworlds-SAS]: Remise en place des Isoworlds dans le SAS");
            File source = new File(ManageFiles.SASPath);
            File dest = new File(ManageFiles.getPath());
            // Retourne la liste des Isoworld tag
            for (File f : ManageFiles.getOutSAS(source)) {
                // Gestion des Isoworlds non push, si ne contient pas de tag
                try {
                    ManageFiles.move(new File(source + "/" + f.getName()), new File(dest.getPath() + "/" + f.getName()));
                    logger.info("[Isoworlds-SAS]: " + f.getName() + " retir?? du SAS");
                } catch (IOException e) {
                    logger.info("[Isoworlds-SAS]: Echec de destockage > " + f.getName());
                }
            }
        }).delay(1, TimeUnit.SECONDS).build(), "Remet les Isoworlds hors du SAS.");
    }
}
