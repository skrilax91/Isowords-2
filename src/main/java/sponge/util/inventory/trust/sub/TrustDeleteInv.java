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

import sponge.Database.Methods.TrustAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import sponge.util.action.StatAction;
import sponge.util.console.Logger;
import sponge.util.inventory.MainInv;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static common.Msg.msgNode;

public class TrustDeleteInv {

    public static InventoryMenu getInv(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X4).completeStructure().carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds: > " + msgNode.get("TrustRemove")).color(NamedTextColor.BLUE));

        // Menu principal
        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(msgNode.get("MainMenuLore")));

        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.GOLD_BLOCK).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("MainMenu"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(35, item9);

        int i = 0;
        ResultSet trusts = TrustAction.getTrusts(pPlayer.uniqueId().toString());

        try {
            while (trusts.next()) {
                // Récupération du nom du joueur
                String tmp = trusts.getString(1);
                Logger.info("name = " + tmp);
                UUID uuid = UUID.fromString(tmp);
                Logger.info("uuid = " + uuid);
                Optional<User> user = StatAction.getPlayerFromUUID(uuid);

                // Dont show own access
                if (user.get().name().equals(pPlayer.name())) {
                    continue;
                }

                // Construction du lore
                List<Component> list1 = new ArrayList<>();
                list1.add(Component.text(msgNode.get("Player")));

                ItemStack item1 = ItemStack.builder().itemType(ItemTypes.PLAYER_HEAD).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text(user.get().name())
                        .color(NamedTextColor.GOLD)).quantity(1).build();
                menu.inventory().set(i, item1);
                i++;
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
                    MainInv.commandMenu(pPlayer, "iw retirer " + slot.get(Keys.DISPLAY_NAME).get());
                    MainInv.closeOpenMenu(pPlayer, MainInv.menuPrincipal(pPlayer));
                }

                return false;
            }
        });

        return menu;
    }
}
