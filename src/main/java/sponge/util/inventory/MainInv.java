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
package sponge.util.inventory;

import common.IsoChat;
import sponge.Database.Methods.ChargeAction;
import sponge.Database.Methods.PlayTimeAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.Player;
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
import sponge.Main;
import sponge.location.Locations;
import sponge.util.console.Logger;
import sponge.util.inventory.biome.BiomeInv;
import sponge.util.inventory.build.BuildInv;
import sponge.util.inventory.time.TimeInv;
import sponge.util.inventory.trust.TrustInv;
import sponge.util.inventory.warp.WarpInv;
import sponge.util.inventory.weather.WeatherInv;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static common.Msg.msgNode;

public class MainInv {

    public static InventoryMenu menuPrincipal(ServerPlayer pPlayer) {

        ViewableInventory inventory = ViewableInventory.builder().type(ContainerTypes.GENERIC_9X2).completeStructure().carrier(pPlayer).build();
        InventoryMenu menu = inventory.asMenu();
        menu.setReadOnly(true);
        menu.setTitle(Component.text("Isoworlds"));

        // Récupération nombre charge
        Integer charges = null;
        try {
            charges = ChargeAction.getCharge(pPlayer);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Integer playtime = PlayTimeAction.getPlayTime(pPlayer);
        String formatedPlayTime = (playtime > 60) ? playtime / 60 + " H " + playtime % 60 + " m" : playtime + " m";


        // Build Mode
        List<Component> list1 = new ArrayList<>();
        list1.add(Component.text(msgNode.get("InvBiomeLore")));
        ItemStack item1 = ItemStack.builder().itemType(ItemTypes.DIAMOND_PICKAXE).add(Keys.LORE, list1).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvBuild"))
                .color(NamedTextColor.GRAY)).quantity(1).build();
        menu.inventory().set(0, item1);

        // Home
        List<Component> list2 = new ArrayList<>();
        list2.add(Component.text(msgNode.get("InvHomeLore")));
        ItemStack item2 = ItemStack.builder().itemType(ItemTypes.RED_BED).add(Keys.LORE, list2).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvHome"))
                .color(NamedTextColor.BLUE)).quantity(1).build();
        menu.inventory().set(1, item2);

        // Trust Menu
        List<Component> list3 = new ArrayList<>();
        list3.add(Component.text(msgNode.get("InvTrustLore")));
        ItemStack item3 = ItemStack.builder().itemType(ItemTypes.PLAYER_HEAD).add(Keys.LORE, list3).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvTrust"))
                .color(NamedTextColor.GREEN)).quantity(1).build();
        menu.inventory().set(2, item3);

        // Biome Menu
        List<Component> list4 = new ArrayList<>();
        list4.add(Component.text(msgNode.get("InvBiomeLore")));
        ItemStack item4 = ItemStack.builder().itemType(ItemTypes.OAK_LEAVES).add(Keys.LORE, list4).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvBiome"))
                .color(NamedTextColor.GOLD)).quantity(1).build();
        menu.inventory().set(3, item4);


        // Time Menu
        List<Component> list5 = new ArrayList<>();
        list5.add(Component.text(msgNode.get("InvTimeLore")));
        ItemStack item5 = ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.LORE, list5).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvTime"))
                .color(NamedTextColor.LIGHT_PURPLE)).quantity(1).build();
        menu.inventory().set(4, item5);

        // Weather Menu
        List<Component> list6 = new ArrayList<>();
        list6.add(Component.text(msgNode.get("InvWeatherLore")));
        list6.add(Component.text(msgNode.get("InvWeatherLore2")));
        ItemStack item6 = ItemStack.builder().itemType(ItemTypes.CHORUS_PLANT).add(Keys.LORE, list6).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvWeather"))
                .color(NamedTextColor.YELLOW)).quantity(1).build();
        menu.inventory().set(5, item6);

        // Warp Menu
        List<Component> list7 = new ArrayList<>();
        list7.add(Component.text(msgNode.get("InvWarpLore")));
        ItemStack item7 = ItemStack.builder().itemType(ItemTypes.COMPASS).add(Keys.LORE, list7).add(Keys.DISPLAY_NAME, Component.text(msgNode.get("InvWarp"))
                .color(NamedTextColor.DARK_GREEN)).quantity(1).build();
        menu.inventory().set(6, item7);

        // Charge Info
        List<Component> list9 = new ArrayList<>();
        list9.add(Component.text(msgNode.get("InvStatChargeLore")).color(NamedTextColor.YELLOW).append(Component.text(charges + " disponible(s)").color(NamedTextColor.GREEN)));
        ItemStack item9 = ItemStack.builder().itemType(ItemTypes.LEVER).add(Keys.LORE, list9).add(Keys.DISPLAY_NAME, Component.text("InvStat")
                .color(NamedTextColor.AQUA)).quantity(1).build();
        menu.inventory().set(8, item9);

        // Isochat Info
        List<Component> list10 = new ArrayList<>();
        list10.add(Component.text(msgNode.get("InvIsochatLore")));
        list10.add(Component.text(msgNode.get("InvIsochatLore2")));
        list10.add((IsoChat.isActivated(pPlayer.uniqueId()) ? Component.text(msgNode.get("InvIsochatEnabled")).color(NamedTextColor.GREEN) : Component.text(msgNode.get("InvIsochatDisabled")).
                color(NamedTextColor.RED)));
        ItemStack item10 = ItemStack.builder().itemType(ItemTypes.OAK_SIGN).add(Keys.LORE, list10).add(Keys.DISPLAY_NAME, Component.text("InvIsochat")
                .color(NamedTextColor.WHITE)).quantity(1).build();
        menu.inventory().set(9, item10);

        // STAFF
        //if (pPlayer.hasPermission("isworlds.menu.activation")) {
        //    menu.query(SlotPos.of(6, 0)).set(item7);
        //}
        //if (pPlayer.hasPermission("Isoworlds.menu.teleportation")) {
        //    menu.query(SlotPos.of(7, 0)).set(item8);
        //}
        //menu.query(SlotPos.of(7, 0)).set(item8);

        menu.registerSlotClick(new SlotClickHandler() {
            @Override
            public boolean handle(Cause cause, Container container, Slot slot, int slotIndex, ClickType<?> clickType) {
                if(clickType != ClickTypes.CLICK_LEFT.get() && clickType != ClickTypes.CLICK_RIGHT.get()) return false;

                switch (slotIndex) {
                    case 0: closeOpenMenu(pPlayer, BuildInv.getInv(pPlayer));
                        break;
                    case 1: MainInv.commandMenu(pPlayer, "iw h");
                        MainInv.closeMenu(pPlayer);
                        break;
                    case 2: closeOpenMenu(pPlayer, TrustInv.getInv(pPlayer));
                        break;
                    case 3: closeOpenMenu(pPlayer, BiomeInv.getInv(pPlayer));
                        break;
                    case 4: closeOpenMenu(pPlayer, TimeInv.getInv(pPlayer));
                        break;
                    case 5: closeOpenMenu(pPlayer, WeatherInv.getInv(pPlayer));
                        break;
                    case 6: closeOpenMenu(pPlayer, WarpInv.getInv(pPlayer));
                        break;
                    case 9: IsoChat.toggle(pPlayer.uniqueId());
                        closeOpenMenu(pPlayer, menuPrincipal(pPlayer));
                        break;

                    default:
                        return false;
                }

                return false;
            }
        });

        return menu;
    }

    public static void closeOpenMenu(ServerPlayer pPlayer, InventoryMenu inv) {
        Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
            pPlayer.closeInventory();
            pPlayer.openInventory(inv.inventory());
        }).delay(10, TimeUnit.MILLISECONDS).build(), "Ferme l'inventaire d'un joueur et en ouvre un autre.");
    }

    public static void closeMenu(ServerPlayer pPlayer) {
        Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
            pPlayer.closeInventory();
            pPlayer.openInventory(menuPrincipal(pPlayer).inventory());
        }).delay(10, TimeUnit.MILLISECONDS).build(), "Ferme l'inventaire d'un joueur.");
    }

    public static void commandMenu(ServerPlayer pPlayer, String cmd) {
        Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
            try {
                Sponge.server().commandManager().process(pPlayer, cmd);
            } catch (CommandException e) {
                throw new RuntimeException(e);
            }
        }).delay(10, TimeUnit.MILLISECONDS).build(), "Execute une commande pour le joueur.");
    }

    public static void teleportMenu(Player pPlayer, String cmd) {
        Sponge.asyncScheduler().submit(Task.builder().plugin(Main.instance.getContainer()).execute(() -> {
            Logger.info("TP CMD: " + cmd);
            Locations.teleport(pPlayer, cmd);
        }).delay(10, TimeUnit.MILLISECONDS).build(), "Téléporte le joueur dans un Isoworld.");
    }
}
