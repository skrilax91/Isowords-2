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
package sponge.util.inventory.trust.sub;

import common.Cooldown;
import sponge.Database.Methods.TrustAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.scheduler.Task;
import sponge.Database.Methods.IsoworldsAction;
import sponge.Main;
import sponge.Translation.TranslateManager;
import sponge.location.Locations;
import sponge.util.action.StatAction;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;
import sponge.util.inventory.MainInv;

import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TrustAccessInv {
    private static TranslateManager translateManager = Main.instance.translateManager;

    private static final Main plugin = Main.instance;

    public static InventoryMenu getInv(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X4).completeStructure().plugin(Main.instance.getContainer()).carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds: > " + translateManager.translate("TrustAccess")).color(NamedTextColor.BLUE));

        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(translateManager.translate("MainMenuLore")));

        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.GOLD_BLOCK).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("MainMenu"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(35, item9);

        ResultSet trusts = TrustAction.getAccess(pPlayer);

        try {
            for (int i = 0; Objects.requireNonNull(trusts).next(); ++i) {

                // Récupération uuid
                String[] tmp = trusts.getString(1).split("-Isoworld");
                UUID uuid = UUID.fromString(tmp[0]);
                Optional<User> user = StatAction.getPlayerFromUUID(uuid);

                // Dont show own access
                if (!user.isPresent() || user.get().name().equals(pPlayer.name())) {
                    continue;
                }

                // Construction du lore
                List<Component> list1 = new ArrayList<>();
                list1.add(Component.text(user.get().uniqueId().toString()));

                ItemStack item1 = ItemStack.builder().itemType(ItemTypes.PLAYER_HEAD).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("TrustAccessLore2") + ": " + user.get().name())
                        .color(NamedTextColor.GOLD)).quantity(1).build();
                menu.inventory().set(i, item1);

                // Construction des skin itemstack
                /*SkullData data = Sponge.getGame().getDataManager().getManipulatorBuilder(SkullData.class).get().create();
                data.set(Keys.SKULL_TYPE, SkullTypes.PLAYER);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        menu.registerSlotClick(new SlotClickHandler() {
            @Override
            public boolean handle(Cause cause, Container container, Slot slot, int slotIndex, ClickType<?> clickType) {
                if(clickType != ClickTypes.CLICK_LEFT.get() && clickType != ClickTypes.CLICK_RIGHT.get()) return false;

                if (slotIndex == 35) {
                    MainInv.closeOpenMenu(pPlayer, MainInv.menuPrincipal(pPlayer));
                    return false;
                }

                if (slot.totalQuantity() != 0) {
                    String uuid = slot.get(Keys.LORE).get().get(0).toString().split("-Isoworld")[0];
                    Logger.info("NAME " + uuid);
                    Optional<User> user = StatAction.getPlayerFromUUID(UUID.fromString(uuid));
                    String worldname = uuid + "-Isoworld";

                    // Si la méthode renvoi vrai alors on return car le lock est défini pour l'import, sinon elle le set auto
                    if (StorageAction.iwInProcess(pPlayer, worldname))
                        return false;

                    // Pull du Isoworld
                    Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
                        // Si monde présent en dossier ?
                        // Removing iwInProcess in task
                        if (StorageAction.checkTag(pPlayer, worldname)) {
                            // Chargement du Isoworld + tp
                            IsoworldsAction.setWorldProperties(worldname, pPlayer);
                            Sponge.server().worldManager().loadWorld(ResourceKey.brigadier(worldname));
                            Locations.teleport(pPlayer, worldname);
                            plugin.cooldown.addPlayerCooldown(pPlayer, Cooldown.CONFIANCE, Cooldown.CONFIANCE_DELAY);
                        }

                        // Supprime le lock (worldname, worldname uniquement pour les access confiance)
                        Main.lock.remove(worldname + ";" + worldname);

                    }).delay(1, TimeUnit.SECONDS).build(), "Pull du Isoworld.");

                    MainInv.closeOpenMenu(pPlayer, MainInv.menuPrincipal(pPlayer));

                }

                return false;
            }
        });

        return menu;
    }
}
