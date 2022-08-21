package sponge.Translation;

import common.ManageFiles;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import sponge.Main;
import sponge.Translation.Default.DefaultTranslation;
import sponge.Translation.Default.FrenchTranslation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslateManager {

    public final TranslateManager instance;
    public static Map<String, String> msgNode = new HashMap<>();

    public TranslateManager()
    {
        this.instance = this;


        // Loading defaults translation files
        createConf(FrenchTranslation.class, "fr");
        createConf(DefaultTranslation.class, "en");

        String localeConfig = Main.instance.getConfig().getLocaleTag();
        File lang = new File("config/isoworlds2/translations/" + localeConfig + ".conf");

        if (!lang.exists()) {
            Main.instance.getLogger().error("[IW] Unable to find translation file! Sorry! ");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(lang.toURI()), Charset.defaultCharset());

            for (String line : lines) {
                if (!line.equals("") && !line.equals(" ")) {

                    String value = line.split("=")[1].replaceAll("\"", "");
                    msgNode.put(line.split("=")[0], value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Main.instance.getLogger().info("[IW] Translation [" + localeConfig + "] loaded successfully");
    }

    public String translate(String key)
    {
        return msgNode.getOrDefault(key, key);
    }

    private <T extends DefaultTranslation> void createConf(Class<T> type, String file)
    {
        if (!new File("config/isoworlds2/translations/" + file + ".conf").exists())
            Main.instance.getLogger().info("[IW] Translation file [" + file + "] does not exist, creating...");

        T translationNode = null;
        CommentedConfigurationNode rootNode = null;
        HoconConfigurationLoader translateLoader = HoconConfigurationLoader.builder().path(FileSystems.getDefault().getPath("config/isoworlds2/translations/" + file + ".conf")).build();

        try {
            rootNode = translateLoader.load();
            translationNode = rootNode.get(type);
            rootNode.set(type, translationNode);
        } catch (IOException e) {
            Main.instance.getLogger().error("[IW] An error occurred while creating " + file + " configuration file: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
            System.exit(1);
        }

        try {
            translateLoader.save(rootNode);
        } catch (final ConfigurateException e) {
            Main.instance.getLogger().error("[IW] Unable to create " + file + " translation file! Sorry! " + e.getMessage());
            System.exit(1);
        }
        Main.instance.getLogger().info("[IW] Translation [" + file + "] created successfully");
    }



}
