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
package sponge.configuration;

import org.spongepowered.configurate.CommentedConfigurationNode;
import sponge.util.ConfigManager;

public class Configuration {

    private static final CommentedConfigurationNode configurationNode = ConfigManager.configurationNode;

    public static String getId() {
        return ((String) configurationNode.node("Isoworlds", "Id").toString());
    }

    public static String getMainWorld() {
        return ((String) configurationNode.node("Isoworlds", "MainWorld").toString());
    }

    public static String getMainWorldSpawnCoordinate() {
        return ((String) configurationNode.node("Isoworlds", "MainWorldSpawnCoordinate").toString());
    }

    public static boolean getAutomaticUnload() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "AutomaticUnload", "Enabled").getBoolean());
    }

    public static Integer getInactivityTime() {
        return ((Integer) configurationNode.node("Isoworlds", "Modules", "AutomaticUnload", "InactivityTime").getInt());
    }

    public static boolean getStorage() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "Storage", "Enabled").getBoolean());
    }

    public static boolean getDimensionAlt() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "DimensionAlt", "Enabled").getBoolean());
    }

    public static boolean getMining() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "DimensionAlt", "Mining").getBoolean());
    }

    public static boolean getExploration() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "DimensionAlt", "Exploration").getBoolean());
    }

    public static boolean getSafePlateform() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "SafePlateform", "Enabled").getBoolean());
    }

    public static boolean getSafeSpawn() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "SafeSpawn", "Enabled").getBoolean());
    }

    public static boolean getSpawnProtection() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "SpawnProtection", "Enabled").getBoolean());
    }

    public static boolean getBorder() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "Border", "Enabled").getBoolean());
    }

    public static Integer getDefaultRadiusSize() {
        return ((Integer) configurationNode.node("Isoworlds", "Modules", "Border", "DefaultRadiusSize").getInt());
    }

    public static Integer getSmallRadiusSize() {
        return ((Integer) configurationNode.node("Isoworlds", "Modules", "Border", "SmallRadiusSize").getInt());
    }

    public static Integer getMediumRadiusSize() {
        return ((Integer) configurationNode.node("Isoworlds", "Modules", "Border", "MediumRadiusSize").getInt());
    }

    public static Integer getLargeRadiusSize() {
        return ((Integer) configurationNode.node("Isoworlds", "Modules", "Border", "LargeRadiusSize").getInt());
    }

    public static boolean getPlayTime() {
        return ((boolean) configurationNode.node("Isoworlds", "Modules", "PlayTime", "Enabled").getBoolean());
    }

}
