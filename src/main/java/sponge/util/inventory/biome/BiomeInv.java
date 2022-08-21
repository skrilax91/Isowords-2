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
package sponge.util.inventory.biome;

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

public class BiomeInv {

    private static TranslateManager translateManager = Main.instance.translateManager;

    // BIOME
    public static InventoryMenu getInv(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X2).completeStructure().plugin(Main.instance.getContainer()).carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds: Biome"));

        // Plaines
        List<Component> list1 = new ArrayList<>();
        list1.add(Component.text(translateManager.translate("BiomePlainLore")));
        list1.add(Component.text(translateManager.translate("BiomePlainLore2")));

        ItemStack item1 = ItemStack.builder().itemType(ItemTypes.GRASS).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomePlain"))
                .color(NamedTextColor.GREEN)).quantity(1).build();
        menu.inventory().set(0, item1);

        // Désert
        List<Component> list2 = new ArrayList<>();
        list2.add(Component.text(translateManager.translate("BiomeDesertLore")));
        list2.add(Component.text(translateManager.translate("BiomeDesertLore2")));

        ItemStack item2 = ItemStack.builder().itemType(ItemTypes.SAND).add(Keys.LORE, list2).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeDesert"))
                .color(NamedTextColor.YELLOW)).quantity(1).build();
        menu.inventory().set(1, item2);

        // Marais
        List<Component> list3 = new ArrayList<>();
        list3.add(Component.text(translateManager.translate("BiomeSwampLore")));
        list3.add(Component.text(translateManager.translate("BiomeSwampLore2")));

        ItemStack item3 = ItemStack.builder().itemType(ItemTypes.CLAY).add(Keys.LORE, list3).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeSwamp"))
                .color(NamedTextColor.GRAY)).quantity(1).build();
        menu.inventory().set(2, item3);

        // Océan
        List<Component> list4 = new ArrayList<>();
        list4.add(Component.text(translateManager.translate("BiomeOceanLore")));

        ItemStack item4 = ItemStack.builder().itemType(ItemTypes.BLUE_WOOL).add(Keys.LORE, list4).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeOcean"))
                .color(NamedTextColor.BLUE)).quantity(1).build();
        menu.inventory().set(3, item4);

        // Champignon
        List<Component> list5 = new ArrayList<>();
        list5.add(Component.text(translateManager.translate("BiomeMushroomLore")));
        list5.add(Component.text(translateManager.translate("BiomeMushroomLore2")));

        ItemStack item5 = ItemStack.builder().itemType(ItemTypes.RED_MUSHROOM).add(Keys.LORE, list5).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeMushroom"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(4, item5);

        // Jungle
        List<Component> list6 = new ArrayList<>();
        list6.add(Component.text(translateManager.translate("BiomeJungleLore")));
        list6.add(Component.text(translateManager.translate("BiomeJungleLore2")));

        ItemStack item6 = ItemStack.builder().itemType(ItemTypes.JUNGLE_SAPLING).add(Keys.LORE, list6).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeJungle"))
                .color(NamedTextColor.DARK_GREEN)).quantity(1).build();
        menu.inventory().set(5, item6);

        // Enfer
        List<Component> list7 = new ArrayList<>();
        list7.add(Component.text(translateManager.translate("BiomeHellLore")));

        ItemStack item7 = ItemStack.builder().itemType(ItemTypes.NETHERRACK).add(Keys.LORE, list7).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeHell"))
                .color(NamedTextColor.DARK_RED)).quantity(1).build();
        menu.inventory().set(6, item7);

        // End
        List<Component> list8 = new ArrayList<>();
        list8.add(Component.text(translateManager.translate("BiomeEndLore")));

        ItemStack item8 = ItemStack.builder().itemType(ItemTypes.END_STONE).add(Keys.LORE, list8).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("BiomeEnd"))
                .color(NamedTextColor.DARK_PURPLE)).quantity(1).build();
        menu.inventory().set(7, item8);

        // Menu principal
        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(translateManager.translate("MainMenuLore")));

        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.GOLD_BLOCK).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text(translateManager.translate("MainMenu"))
                .color(NamedTextColor.RED)).quantity(1).build();
        menu.inventory().set(8, item9);



        menu.registerSlotClick(new SlotClickHandler() {
            @Override
            public boolean handle(Cause cause, Container container, Slot slot, int slotIndex, ClickType<?> clickType) {
                if(clickType != ClickTypes.CLICK_LEFT.get() && clickType != ClickTypes.CLICK_RIGHT.get()) return false;

                switch (slotIndex) {
                    case 0: MainInv.commandMenu(pPlayer, "iw biome plaines");
                        break;
                    case 1: MainInv.commandMenu(pPlayer, "iw biome desert");
                        break;
                    case 2: MainInv.commandMenu(pPlayer, "iw biome marais");
                        break;
                    case 3: MainInv.commandMenu(pPlayer, "iw biome océan");
                        break;
                    case 4: MainInv.commandMenu(pPlayer, "iw biome champignon");
                        break;
                    case 5: MainInv.commandMenu(pPlayer, "iw biome jungle");
                        break;
                    case 6: MainInv.commandMenu(pPlayer, "iw biome enfer");
                        break;
                    case 7: MainInv.commandMenu(pPlayer, "iw biome end");
                        break;
                    case 8: MainInv.closeOpenMenu(pPlayer, MainInv.menuPrincipal(pPlayer));
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
