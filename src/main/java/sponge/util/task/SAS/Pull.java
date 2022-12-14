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
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sponge.util.task.SAS;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import sponge.Main;
import sponge.translation.TranslateManager;
import sponge.util.action.StorageAction;
import sponge.util.message.Message;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Pull implements Consumer<ScheduledTask> {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private int check = 60;
    private final Player pPlayer;
    private final File file;
    public Pull(Player pPlayer, File file) {
        this.pPlayer = pPlayer;
        this.file = file;
    }


    @Override
    public void accept(ScheduledTask task) {
        // Message de d??marrage process
        if (check == 60) {
            // Notification au joueur qu'il doit patienter
            pPlayer.sendMessage(Message.info(translateManager.translate("ProcessingPull")));
        }
        check --;
        // Si inf??rieur ?? 1 alors tout le temps s'est ??coul?? sans que le Isoworld soit pr??sent en fichier
        if (check < 1) {
            // Notification au joueur de contacter l'??quipe
            pPlayer.sendMessage(Message.error(translateManager.translate("FailPull")));
            // Suppression du TAG pour permettre l'utilisation de la commande maison et confiance access
            Main.lock.remove(file.getName() + ";" + file.getName());
            Main.lock.remove(pPlayer.uniqueId().toString() + ";" + "checkTag");
            task.cancel();
        // Si le dossier existe, alors on repasse le statut ?? 0 en BDD (pr??sent).
        } else if (file.exists()) {
            // Passage de l'Isoworld en statut pr??sent
            StorageAction.setStatus(file.getName(), 0);
            // Notification au joueur que le Isoworld est disponible
            pPlayer.sendMessage(Message.success(translateManager.translate("SuccessPull")));
            // Suppression du TAG pour permettre l'utilisation de la commande maison et confiance access
            Main.lock.remove(file.getName() + ";" + file.getName());
            Main.lock.remove(pPlayer.uniqueId().toString() + ";" + "checkTag");
            task.cancel();
        }
    }

    public static Task createTask(Player pPlayer, File file)
    {
        return Task.builder()
                .plugin(Main.instance.getContainer())
                .execute(new Pull(pPlayer, file))
                .interval(1, TimeUnit.SECONDS)
                .plugin(Main.instance.getContainer())
                .build();
    }
}

