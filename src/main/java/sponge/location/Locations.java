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
package sponge.location;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MatterType;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import sponge.Main;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import sponge.util.console.Logger;

import java.util.Optional;

public class Locations {

    private static final Main plugin = Main.instance;

    public static Optional<ServerLocation> getHighestLoc(ServerLocation loc) {
        Optional<Integer> y = getHighestY(loc.world(), loc.x(), loc.z(), loc.blockY());
        return y.map(integer -> ServerLocation.of(loc.world(), loc.x(), integer + 1, loc.z()));
    }

    private static boolean isPassable(ServerWorld w, Double x, int y, Double z) {
        Optional<MatterType> prop = ServerLocation.of(w, x, y, z).block().get(Keys.MATTER_TYPE);
        return prop.get() == MatterTypes.SOLID.get();
    }

    private static Optional<Integer> getHighestY(ServerWorld w, Double x, Double z, int baseY) {
        // If y 0 then auto, else we start from defined value
        int y = baseY;
        if (baseY == 0) {
            y = w.max().y();
        }
        while (isPassable(w, x, y, z)) {
            y = y - 1;
            if (y <= 0) {
                return Optional.empty();
            }
        }
        return Optional.of(y);
    }

    public static boolean teleport(ServerPlayer player, String worldname) {

        ServerLocation maxy;
        Optional<ServerWorld> finalWorld = plugin.getGame().server().worldManager().world(ResourceKey.brigadier(worldname));

        if (finalWorld.isPresent()) {
            try {

                ServerLocation spawn = ServerLocation.of(finalWorld.get(), finalWorld.get().properties().spawnPosition());
                // Actual spawn location

                // Set to 61 for official dimensions
                ServerLocation destination = ServerLocation.of(spawn.world(), getAxis(worldname).x(), 61, getAxis(worldname).z());

                // If dimensions if not autobuilt, return the same name so it can build Isoworlds safe zone
                if (!worldname.equals("DIM1") && !worldname.equals("DIM-1")) {

                    // Get max location, if Y axis is 0 then it will find from max, else find from the one set to lower
                    maxy = Locations.getHighestLoc(ServerLocation.of(spawn.world(), getAxis(worldname)))
                            .orElse(ServerLocation.of(spawn.world(), getAxis(worldname).x(), 61, getAxis(worldname).z()));

                    destination = ServerLocation.of(spawn.world(), getAxis(worldname).x(), maxy.blockY(), getAxis(worldname).z());

                    // Set dirt if liquid or air
                    if (destination.add(0, -1, 0).block().get(Keys.MATTER_TYPE).get() != MatterTypes.SOLID.get()) {
                        // Build safe zone
                        destination.add(0, -1, 0).setBlockType(BlockTypes.DIRT.get());
                    }
                }

                // Téléportation du joueur
                if (player.setLocation(destination)) {
                    Logger.info("Le joueur a bien été téléporté !");
                } else {
                    Logger.info("Le joueur n'a pas pu être téléporté !");
                    return false;
                }

            } catch (NullPointerException npe) {
                npe.printStackTrace();
            }

        }
        return true;
    }

    private static void buildSafeSpawn(String worldname) {

        Sponge.server().worldManager().loadWorld(ResourceKey.brigadier(worldname));

        // Clear zone
        for (int x = -2; x < 2; x++) {
            for (int y = 60; y < 65; y++) {
                for (int z = -2; z < 2; z++) {
                    if (Sponge.server().worldManager().world(ResourceKey.brigadier(worldname)).get().block(x, y, x).type() != BlockTypes.BEDROCK.get()) {
                        Sponge.server().worldManager().world(ResourceKey.brigadier(worldname)).get().setBlock(x, y, z, BlockState.builder().blockType(BlockTypes.AIR).build());
                    }
                }
            }
        }

        // Build safe zone
        for (int x = -2; x < 2; x++) {
            for (int z = -2; z < 2; z++) {
                Sponge.server().worldManager().world(ResourceKey.brigadier(worldname)).get().setBlock(x, 60, z, BlockState.builder().blockType(BlockTypes.BEDROCK).build());
            }
        }

        // Set sign
        //Sponge.getServer().getWorld(worldname).get().setBlockType(-2, 61, -2, BlockTypes.TORCH, Cause.source(Sponge.getPluginManager().fromInstance(plugin).get()).build());

    }

    public static Vector3d getAxis(String worldname) {
        return (new Vector3d(0.500, 0.0, 0.500));
    }

    // Get name, null if not official
    public static String getOfficialDimSpawn(String worldname) {

        // Define dimension name
        if (worldname.equals("end")) {
            worldname = "DIM1";
            buildSafeSpawn(worldname);
            return worldname;
        } else if (worldname.equals("nether")) {
            // Teleport to nether
            worldname = "DIM-1";
            buildSafeSpawn(worldname);
            return worldname;
        }
        return worldname;
    }
}
