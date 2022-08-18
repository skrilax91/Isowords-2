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
package sponge.util.inventory.build.sub;

import common.action.IsoworldsAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import sponge.util.console.Logger;
import sponge.util.inventory.MainInv;
import sponge.util.inventory.build.BuildInv;

import java.util.ArrayList;
import java.util.List;

import static common.Msg.msgNode;
import static sponge.Main.instance;

public class CreateInv {

    public static InventoryMenu getInv(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X1).completeStructure().carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds: " + msgNode.get("InvBuild")).color(NamedTextColor.BLUE));

        // Minage
        List<Component> list1 = new ArrayList<>();
        list1.add(Component.text(msgNode.get("BuildNormalLore")));

        ItemStack item1 = ItemStack.builder().itemType(ItemTypes.WHITE_WOOL).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("BuildNormal"))
                .color(NamedTextColor.WHITE)).quantity(1).build();
        menu.inventory().set(0, item1);

        // Exploration
        List<Component> list2 = new ArrayList<>();
        list2.add(Component.text(msgNode.get("BuildVoidLore")));

        ItemStack item2 = ItemStack.builder().itemType(ItemTypes.FILLED_MAP).add(Keys.LORE, list2).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("BuildVoid"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(1, item2);

        // End
        List<Component> list3 = new ArrayList<>();
        list3.add(Component.text(msgNode.get("BuildOceanLore")));

        ItemStack item3 = ItemStack.builder().itemType(ItemTypes.ENDER_PEARL).add(Keys.LORE, list3).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("BuildOcean"))
                .color(NamedTextColor.BLUE)).quantity(1).build();
        menu.inventory().set(2, item3);

        // Nether
        List<Component> list4 = new ArrayList<>();
        list4.add(Component.text(msgNode.get("BuildFlatLore")));

        ItemStack item4 = ItemStack.builder().itemType(ItemTypes.NETHER_STAR).add(Keys.LORE, list4).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("BuildFlat"))
                .color(NamedTextColor.GREEN)).quantity(1).build();
        menu.inventory().set(3, item4);

        // Menu principal
        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(msgNode.get("MainMenuLore")));

        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.GOLD_BLOCK).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvBuild"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(8, item9);

        menu.registerSlotClick(new SlotClickHandler() {
            @Override
            public boolean handle(Cause cause, Container container, Slot slot, int slotIndex, ClickType<?> clickType) {
                if(clickType != ClickTypes.CLICK_LEFT.get() && clickType != ClickTypes.CLICK_RIGHT.get()) return false;

                switch (slotIndex) {
                    case 0: MainInv.commandMenu(pPlayer, "iw c n");
                        break;
                    case 1: MainInv.commandMenu(pPlayer, "iw c v");
                        break;
                    case 2: MainInv.commandMenu(pPlayer, "iw c o");
                        break;
                    case 3: MainInv.commandMenu(pPlayer, "iw c f");
                        break;
                    case 8: MainInv.closeOpenMenu(pPlayer, BuildInv.getInv(pPlayer));
                        break;

                    default:
                        return false;
                }

                if (slotIndex != 8)
                    MainInv.closeMenu(pPlayer);

                return false;
            }
        });

        return menu;
    }
}
