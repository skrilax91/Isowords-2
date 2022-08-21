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
package sponge.util.action;

import common.ManageFiles;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldTypes;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.difficulty.Difficulties;
import org.spongepowered.api.world.generation.config.WorldGenerationConfig;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import org.spongepowered.api.world.storage.WorldProperties;
import sponge.Main;
import sponge.location.Locations;
import sponge.util.console.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class DimsAltAction {

    private static final Main plugin = Main.instance;
    private static final DataQuery toId = DataQuery.of("SpongeData", "dimensionId");
    public static void generateDim() {

        String[] dimsSkyblock = new String[]{"MS3", "SF3", "AS2", "PO2", "PO2K"};
        String[] dims;

        // Si contient alors on met pas le minage
        if (!Arrays.asList(dimsSkyblock).contains(plugin.servername)) {

            dims = new String[]{"exploration", "minage"};

            for (String dim : dims) {
                // Path dim

                // Set properties
                setWorldProperties(dim);

                // Set id
                setId(dim);

                // Load world
                Sponge.server().worldManager().loadWorld(plugin.getWorldKey(dim));
            }
        }
    }

    private static void setWorldProperties(String worldname) {
        // Create world properties Isoworlds

        // Check si world properties en place, création else
        CompletableFuture<Optional<ServerWorldProperties>> fwp = Sponge.server().worldManager().loadProperties(plugin.getWorldKey(worldname));
        ServerWorldProperties worldProperties;

        try {

            Optional<ServerWorldProperties> wp = fwp.get();

            if (wp.isPresent()) {
                worldProperties = wp.get();
                Logger.info("WOLRD PROPERTIES: déjà présent");
                //worldProperties.setKeepSpawnLoaded(true);
                worldProperties.setLoadOnStartup(true);
                worldProperties.setPerformsSpawnLogic(false);
                worldProperties.setPvp(false);

                Sponge.server().worldManager().saveProperties(worldProperties);
                Sponge.server().worldManager().saveProperties(worldProperties);
                // Border
                Optional<ServerWorld> world = Sponge.server().worldManager().world(plugin.getWorldKey(worldname));
                world.ifPresent(serverWorld -> serverWorld.setBorder(WorldBorder.builder().center(0, 0).targetDiameter(6000).build()));
                Logger.warning("Border nouveau: " + 6000);
            } else {
                WorldTemplate template = WorldTemplate.builder()
                        .displayName(Component.text(worldname))
                        .worldType(WorldTypes.OVERWORLD)
                        .difficulty(Difficulties.HARD)
                        .gameMode(GameModes.SURVIVAL)
                        .loadOnStartup(true)
                        .performsSpawnLogic(false)
                        .key(plugin.getWorldKey(worldname))
                        .pvp(false)
                        .build();
                sponge.util.console.Logger.info("WOLRD PROPERTIES: non présents, création...");
                Sponge.server().worldManager().loadWorld(template);

                Optional<ServerWorld> world = Sponge.server().worldManager().world(plugin.getWorldKey(worldname));
                world.ifPresent(serverWorld -> serverWorld.setBorder(WorldBorder.builder().center(0, 0).targetDiameter(6000).build()));
                Logger.warning("Border nouveau: " + 6000);
            }
            Logger.info("WorldProperties à jour");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setId(String dim) {
        // TEST
        Path levelSponge = Paths.get(ManageFiles.getPath() + dim + "/level_sponge.dat");
        if (Files.exists(levelSponge)) {
            DataContainer dc;
            boolean gz = false;
            int dimId;

            // Find dat
            try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(levelSponge, StandardOpenOption.READ))) {
                dc = DataFormats.NBT.get().readFrom(gzip);
                gz = true;

                if (dim.equals("minage")) {
                    dimId = 99998;
                } else if (dim.equals("exploration")) {
                    dimId = 99999;
                } else {
                    return;
                }

                dc.set(toId, dimId);

                // define dat
                try (OutputStream os = getOutput(gz, levelSponge)) {
                    DataFormats.NBT.get().writeTo(os, dc);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static OutputStream getOutput(boolean gzip, Path file) throws IOException {
        OutputStream os = Files.newOutputStream(file);
        if (gzip) {
            return new GZIPOutputStream(os, true);
        }

        return os;
    }
}
