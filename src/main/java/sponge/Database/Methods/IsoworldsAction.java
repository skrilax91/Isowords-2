package sponge.Database.Methods;

import common.ManageFiles;

import common.Manager;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.border.WorldBorder;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.server.WorldTemplate;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;
import sponge.Database.MysqlHandler;
import sponge.Main;
import sponge.location.Locations;
import sponge.util.action.StatAction;
import sponge.util.action.StorageAction;
import sponge.util.console.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class IsoworldsAction {

    private static final MysqlHandler database = Manager.getInstance().getMysql();
    private static final Map<String, Integer> lock = Main.instance.getLock();
    public static final DataQuery toId = DataQuery.of("SpongeData", "dimensionId");

    /**
     * <p> Delete an isoworld
     *
     * @param playeruuid The world uuid without -Isoworld
     * @return whether the function was successful or not
     * @exception SQLException if a database access error occurs
     */
    public static Boolean deleteIsoworld(String playeruuid) throws SQLException {
        String DELETE_AUTORISATIONS = "DELETE FROM `autorisations` WHERE `uuid_w` = ? AND `server_id` = ?";
        String DELETE_ISOWORLD = "DELETE FROM `isoworlds` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        Connection connection = database.getConnection();


        try {
            PreparedStatement delete_autorisations = connection.prepareStatement(DELETE_AUTORISATIONS);
            PreparedStatement delete_isoworld = connection.prepareStatement(DELETE_ISOWORLD);

            // delete autorisations
            delete_autorisations.setString(1, playeruuid);
            delete_autorisations.setString(2, Main.instance.servername);

            // delete Isoworld
            delete_isoworld.setString(1, playeruuid);
            delete_isoworld.setString(2, (playeruuid + "-Isoworld"));
            delete_isoworld.setString(3, Main.instance.servername);

            // execute
            delete_autorisations.executeUpdate();
            delete_isoworld.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            connection.close();
            return false;
        }
        connection.close();
        return true;
    }

    /**
     * <p> Check if an isoworld exist in database
     *
     * @param playeruuid The world uuid without -Isoworld
     * @return whether the function was successful or not
     * @exception SQLException if a database access error occurs
     */
    public static Boolean iwExists(String playeruuid) {
        String CHECK = "SELECT * FROM `isoworlds` WHERE `uuid_p` = ? AND `uuid_w` = ? AND `server_id` = ?";
        Connection connection = null;
        try {
            connection = database.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            PreparedStatement check = connection.prepareStatement(CHECK);
            check.setString(1, playeruuid);
            check.setString(2, playeruuid + "-Isoworld");
            check.setString(3, Main.instance.servername);
            // Request
            ResultSet res = check.executeQuery();
            if (res.isBeforeFirst()) {
                connection.close();
                return true;
            }
            connection.close();
        } catch (Exception se) {
            se.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * <p> Add a player isoworld in database
     *
     * @param pPlayer the player
     * @return whether the function was successful or not
     * @exception SQLException if a database access error occurs
     */
    public static Boolean addIsoworld(Player pPlayer) throws SQLException {
        String INSERT = "INSERT INTO `isoworlds` (`uuid_p`, `uuid_w`, `date_time`, `server_id`, `status`, `dimension_id`) VALUES (?, ?, ?, ?, ?, ?)";
        Connection connection = database.getConnection();

        try {
            PreparedStatement insert = connection.prepareStatement(INSERT);
            // UUID_P
            insert.setString(1, pPlayer.uniqueId().toString());
            // UUID_W
            insert.setString(2, ((pPlayer.uniqueId()) + "-Isoworld"));
            // Date
            insert.setString(3, (new Timestamp(System.currentTimeMillis())).toString());
            // Serveur_id
            insert.setString(4, Main.instance.servername);
            // STATUS
            insert.setInt(5, 0);
            // DIMENSION_ID
            int id = getNextDimensionId();
            if (id == 0) {
                connection.close();
                return false;
            }
            insert.setInt(6, id);
            insert.executeUpdate();
            connection.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        connection.close();
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

    /**
     * <p> Check if isoworld exist and load it if load is true
     *
     * @param pPlayer the player
     * @param load load the world
     * @return if world is present
     */
    public static Boolean isPresent(ServerPlayer pPlayer, Boolean load) {
        if (!iwExists(pPlayer.uniqueId().toString()))
            return false;

        if (!load)
            return true;


        setWorldProperties(pPlayer.uniqueId() + "-Isoworld", pPlayer);
        if (!StorageAction.getStatus(pPlayer.uniqueId() + "-Isoworld")) {
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
                        dimId = getNextDimensionId();
                        IsoworldsAction.setDimensionId(pPlayer, dimId);
                    }

                    dc.set(toId, dimId);

                    // define dat
                    try (OutputStream os = getOutput(true, levelSponge)) {
                        DataFormats.NBT.get().writeTo(os, dc);
                        os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }

            Sponge.server().worldManager().loadWorld(ResourceKey.brigadier(pPlayer.uniqueId() + "-Isoworld"));
        }
        return true;
    }

    private static OutputStream getOutput(boolean gzip, Path file) throws IOException {
        OutputStream os = Files.newOutputStream(file);

        return (gzip) ? new GZIPOutputStream(os, true) : os;
    }

    /**
     * <p> Get all isoworld dimensions id
     *
     * @return An {@link ArrayList<Integer> } with all ids
     * @exception SQLException if a database access error occurs
     */
    public static ArrayList<Integer> getAllDimensionId() throws SQLException {
        String CHECK = "SELECT `dimension_id` FROM `isoworlds` WHERE `server_id` = ? ORDER BY `dimension_id` DESC";
        ArrayList<Integer> dimList = new ArrayList<>();
        Connection connection = database.getConnection();

        try {
            PreparedStatement check = connection.prepareStatement(CHECK);

            // SERVEUR_ID
            check.setString(1, Main.instance.servername);
            // Requête
            ResultSet res = check.executeQuery();
            while (res.next())
                dimList.add(res.getInt("dimension_id"));
        } catch (Exception se) {
            se.printStackTrace();
        }
        connection.close();
        return dimList;
    }

    /**
     * <p> Get id of an isoworld dimension
     *
     * @param pPlayer The owner of the world
     * @return The isoworld id. Return 0 if no isoworld was found
     */
    public static Integer getDimensionId(Player pPlayer) {
        String CHECK = "SELECT `dimension_id` FROM `isoworlds` WHERE `uuid_w` = ? AND `server_id` = ?";

        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            check.setString(1, pPlayer.uniqueId().toString() + "-Isoworld");
            // SERVEUR_ID
            check.setString(2, Main.instance.servername);
            // Requête
            ResultSet res = check.executeQuery();
            if (res.next())
                return res.getInt(1);

        } catch (Exception se) {
            se.printStackTrace();
            return 0;
        }
        return 0;
    }

    /**
     * <p> Get the next available dimension id for isoworld
     *
     * @return an {@link Integer} of the id. Return O if no id are available
     */
    public static Integer getNextDimensionId() {
        // get all id
        ArrayList<Integer> allId = null;
        try {
            allId = IsoworldsAction.getAllDimensionId();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (int i = 1000; i < Integer.MAX_VALUE; i++) {
            if (!allId.contains(i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * <p> Set an isoworld dimension od
     *
     * @param pPlayer The owner of the world
     * @param id The id to set
     * @return whether the function was successful or not
     */
    public static Boolean setDimensionId(ServerPlayer pPlayer, int id) {
        String CHECK = "UPDATE `isoworlds` SET `dimension_id` = ? WHERE `uuid_w` = ?";

        try {
            Connection connection = database.getConnection();
            PreparedStatement check = connection.prepareStatement(CHECK);

            // Number
            check.setInt(1, id);
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
