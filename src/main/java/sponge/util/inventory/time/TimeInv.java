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
package sponge.util.inventory.time;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.menu.ClickType;
import org.spongepowered.api.item.inventory.menu.ClickTypes;
import org.spongepowered.api.item.inventory.menu.InventoryMenu;
import org.spongepowered.api.item.inventory.menu.handler.SlotClickHandler;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import sponge.Main;
import sponge.translation.TranslateManager;
import sponge.util.inventory.MainInv;

import java.util.ArrayList;
import java.util.List;

public class TimeInv {
    private static TranslateManager translateManager = Main.instance.translateManager;

    public static InventoryMenu getInv(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X1).completeStructure().plugin(Main.instance.getContainer()).carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds: " + translateManager.translate("InvTime")).color(NamedTextColor.BLUE));

        // Day
        List<Component> list1 = new ArrayList<>();
        list1.add(Component.text(translateManager.translate("TimeDayLore")));

        ItemStack item1 = ItemStack.builder().itemType(ItemTypes.WHITE_WOOL).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text("Jour")
                .color(NamedTextColor.YELLOW)).quantity(1).build();
        menu.inventory().set(0, item1);

        // Night
        List<Component> list2 = new ArrayList<>();
        list2.add(Component.text(translateManager.translate("TimeNightLore")));

        ItemStack item2 = ItemStack.builder().itemType(ItemTypes.BLACK_WOOL).add(Keys.LORE, list2).add(Keys.DISPLAY_NAME, Component.text("Nuit")
                .color(NamedTextColor.BLUE)).quantity(1).build();
        menu.inventory().set(1, item2);

        // Menu principal
        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(translateManager.translate("MainMenuLore")));

        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.GOLD_BLOCK).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("MainMenu"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(8, item9);

        menu.registerSlotClick(new SlotClickHandler() {
            @Override
            public boolean handle(Cause cause, Container container, Slot slot, int slotIndex, ClickType<?> clickType) {
                if (clickType != ClickTypes.CLICK_LEFT.get() && clickType != ClickTypes.CLICK_RIGHT.get()) return false;

                switch (slotIndex) {
                    case 0:
                        MainInv.commandMenu(pPlayer, "iw temps jour");
                        break;
                    case 1:
                        MainInv.commandMenu(pPlayer, "iw temps nuit");
                        break;
                    case 8:
                        MainInv.closeOpenMenu(pPlayer, MainInv.menuPrincipal(pPlayer));
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
