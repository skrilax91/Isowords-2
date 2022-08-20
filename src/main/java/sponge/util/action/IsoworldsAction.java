package sponge.util.action;

import common.ManageFiles;

import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import sponge.Main;
import sponge.location.Locations;
import sponge.util.console.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IsoworldsAction {

    private static final Map<String, Integer> lock = Main.instance.getLock();
    public static final DataQuery toId = DataQuery.of("SpongeData", "dimensionId");

    // Create Isoworld for pPlayer
    public static Boolean setIsoworld(Player pPlayer) {
        String INSERT = "INSERT INTO `isoworlds` (`uuid_p`, `uuid_w`, `date_time`, `server_id`, `status`, `dimension_id`) VALUES (?, ?, ?, ?, ?, ?)";
        String Iuuid_w;
        String Iuuid_p;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            PreparedStatement insert = Main.instance.database.prepare(INSERT);
            // UUID_P
            Iuuid_p = pPlayer.uniqueId().toString();
            insert.setString(1, Iuuid_p);
            // UUID_W
            Iuuid_w = ((pPlayer.uniqueId()) + "-Isoworld");
            insert.setString(2, Iuuid_w);
            // Date
            insert.setString(3, (timestamp.toString()));
            // Serveur_id
            insert.setString(4, Main.instance.servername);
            // STATUS
            insert.setInt(5, 0);
            // DIMENSION_ID
            int id = getNextDimensionId();
            if (id == 0) {
                return false;
            }
            insert.setInt(6, id);
            insert.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    // Create world properties Isoworlds
    public static void setWorldProperties(String worldname, Player pPlayer) {

        // Check si world properties en place, création else
        CompletableFuture<Optional<ServerWorldProperties>> fWp = Sponge.server().worldManager() .loadProperties(ResourceKey.brigadier(worldname));
        ServerWorldProperties worldProperties;

        try {
            // Deal with permission of owner only

            int x = -1;
            String username = worldname.split("-Isoworld")[0];
            Optional<User> user = StatAction.getPlayerFromUUID(UUID.fromString(username));

            // Global
            // Radius border 1000

            for (Map.Entry<String, Integer> entry : Main.instance.getConfig().modules().borderModule().getBorders().entrySet()) {
                if (user.get().hasPermission("Isoworlds.size." + entry.getKey())) {
                    x = entry.getValue() * 2;
                    break;
                }
            }

            if (x == -1)
                x = (Main.instance.getConfig().modules().borderModule().getBorders().entrySet().iterator().next().getValue() * 2);

            Optional<ServerWorldProperties> wp = fWp.get();

            if (wp.isPresent()) {
                worldProperties = wp.get();
                sponge.util.console.Logger.info("WOLRD PROPERTIES: déjà présent");
                //worldProperties.setKeepSpawnLoaded(false);
                worldProperties.setLoadOnStartup(false);
                worldProperties.setPerformsSpawnLogic(false);
                worldProperties.setPvp(true);
                Sponge.server().worldManager().saveProperties(worldProperties);

                // ****** MODULES ******
                // Border
                if (Main.instance.getConfig().modules().borderModule().isEnable()) {
                    Optional<ServerWorld> world = Sponge.server().worldManager().world(ResourceKey.brigadier(worldname));
                    int finalX = x;
                    world.ifPresent(serverWorld -> serverWorld.setBorder(WorldBorder.builder()
                            .center(Locations.getAxis(worldname).x(), Locations.getAxis(worldname).z())
                            .targetDiameter(finalX)
                            .build()));
                    Logger.warning("Border nouveau: " + x);
                }
                // *********************
            } else {
                WorldTemplate template = WorldTemplate.builder()
                        .displayName(Component.text(worldname))
                        .loadOnStartup(false)
                        .performsSpawnLogic(false)
                        .pvp(true)
                        .build();
                sponge.util.console.Logger.info("WOLRD PROPERTIES: non présents, création...");
                Sponge.server().worldManager().loadWorld(template);

                // ****** MODULES ******
                // Border
                if (Main.instance.getConfig().modules().borderModule().isEnable()) {
                    Optional<ServerWorld> world = Sponge.server().worldManager().world(ResourceKey.brigadier(worldname));
                    int finalX = x;
                    world.ifPresent(serverWorld -> serverWorld.setBorder(WorldBorder.builder()
                            .center(Locations.getAxis(worldname).x(), Locations.getAxis(worldname).z())
                            .targetDiameter(finalX)
                            .build()));
                    Logger.warning("Border nouveau: " + x);
                }
                // *********************
            }
            sponge.util.console.Logger.info("WorldProperties à jour");

        } catch (NoSuchElementException ie) {
            ie.printStackTrace();
            lock.remove(pPlayer.uniqueId().toString() + ";" + String.class.getName());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    // Check if Isoworld exists and load it if load true
    public static Boolean isPresent(Player pPlayer, Boolean load) {

        String CHECK = "SELECT * FROM `isoworlds` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        String check_w;
        String check_p;
        try {
            PreparedStatement check = sponge.Main.instance.database.prepare(CHECK);

            // UUID _P
            check_p = pPlayer.uniqueId().toString();
            check.setString(1, check_p);
            // UUID_W
            check_w = (pPlayer.uniqueId() + "-Isoworld");
            check.setString(2, check_w);
            // SERVEUR_ID
            check.setString(3, sponge.Main.instance.servername);
            // Requête
            ResultSet rselect = check.executeQuery();

            if (!rselect.isBeforeFirst()) {
                return false;
            }
            // Chargement si load = true
            setWorldProperties(pPlayer.uniqueId() + "-Isoworld", pPlayer);
            if (!StorageAction.getStatus(pPlayer.uniqueId() + "-Isoworld")) {
                if (load) {

                    // TEST
                    Path levelSponge = Paths.get(ManageFiles.getPath() + pPlayer.uniqueId() + "-Isoworld/" + "level_sponge.dat");
                    if (Files.exists(levelSponge)) {
                        DataContainer dc;

                        // Find dat
                        try (GZIPInputStream gzip = new GZIPInputStream(Files.newInputStream(levelSponge, StandardOpenOption.READ))) {
                            dc = DataFormats.NBT.get().readFrom(gzip);

                            // get all id
                            ArrayList<Integer> allId = IsoworldsAction.getAllDimensionId();

                            // get id
                            int dimId = IsoworldsAction.getDimensionId(pPlayer);

                            // Si non Isoworld ou non défini
                            if (dimId == 0) {
                                for (int i = 1000; i < Integer.MAX_VALUE; i++) {
                                    if (!allId.contains(i)) {
                                        IsoworldsAction.setDimensionId(pPlayer, i);
                                        dimId = i;
                                        break;
                                    }
                                }
                            }

                            dc.set(toId, dimId);

                            // define dat
                            try (OutputStream os = getOutput(true, levelSponge)) {
                                DataFormats.NBT.get().writeTo(os, dc);
                                os.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    Sponge.server().worldManager().loadWorld(ResourceKey.brigadier(pPlayer.uniqueId() + "-Isoworld"));
                }
            }
            return true;

        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
    }

    private static OutputStream getOutput(boolean gzip, Path file) throws IOException {
        OutputStream os = Files.newOutputStream(file);
        if (gzip) {
            return new GZIPOutputStream(os, true);
        }

        return os;
    }

    // Get all Isoworlds dimension id
    public static ArrayList<Integer> getAllDimensionId() {
        String CHECK = "SELECT `dimension_id` FROM `isoworlds` WHERE `server_id` = ? ORDER BY `dimension_id` DESC";
        String check_w;
        ArrayList<Integer> dimList = new ArrayList<Integer>();
        try {
            PreparedStatement check = sponge.Main.instance.database.prepare(CHECK);

            // SERVEUR_ID
            check.setString(1, sponge.Main.instance.servername);
            // Requête
            ResultSet rselect = check.executeQuery();
            while (rselect.next()) {
                dimList.add(rselect.getInt("dimension_id"));
            }
            return dimList;
        } catch (Exception se) {
            se.printStackTrace();
            return dimList;
        }
    }

    // Get all trusted players of pPlayer's Isoworld
    public static Integer getDimensionId(Player pPlayer) {
        String CHECK = "SELECT `dimension_id` FROM `isoworlds` WHERE `uuid_w` = ? AND `server_id` = ?";
        String check_w;
        try {
            PreparedStatement check = sponge.Main.instance.database.prepare(CHECK);

            // UUID _W
            check_w = pPlayer.uniqueId().toString() + "-Isoworld";
            check.setString(1, check_w);
            // SERVEUR_ID
            check.setString(2, sponge.Main.instance.servername);
            // Requête
            ResultSet rselect = check.executeQuery();
            if (rselect.next()) {
                return rselect.getInt(1);
            }
        } catch (Exception se) {
            se.printStackTrace();
            return 0;
        }
        return 0;
    }

    // get next dimensionID
    public static Integer getNextDimensionId() {
        // get all id
        ArrayList<Integer> allId = IsoworldsAction.getAllDimensionId();

        for (int i = 1000; i < Integer.MAX_VALUE; i++) {
            if (!allId.contains(i)) {
                return i;
            }
        }
        return 0;
    }

    // set Isoworld dimension ID
    public static Boolean setDimensionId(org.spongepowered.api.entity.living.player.Player pPlayer, Integer number) {
        String CHECK = "UPDATE `isoworlds` SET `dimension_id` = ? WHERE `uuid_w` = ?";
        try {
            PreparedStatement check = sponge.Main.instance.database.prepare(CHECK);

            // Number
            check.setInt(1, number);
            // UUID_P
            check.setString(2, pPlayer.uniqueId().toString() + "-Isoworld");
            // Requête
            check.executeUpdate();
            return true;
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
    }
}
