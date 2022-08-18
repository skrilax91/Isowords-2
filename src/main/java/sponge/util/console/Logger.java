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
package sponge.util.console;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.adventure.Audiences;
import sponge.Main;

public class Logger {
    private static final Main instance = Main.instance;

    private static void sendLog(String s, NamedTextColor color) {
        Audiences.system().sendMessage(
                Component.text("[").color(NamedTextColor.WHITE).append(
                        Component.text("IW").color(NamedTextColor.GOLD).append(
                                Component.text("]").color(NamedTextColor.WHITE).append(
                                        Component.text(s).color(color)
                                )
                        )
                )
        );
    }

    public static void info(String s) {
        sendLog(s, NamedTextColor.GREEN);
    }

    public static void warning(String s) {
        sendLog(s, NamedTextColor.GOLD);
    }

    public static void severe(String s) {
        sendLog(s, NamedTextColor.RED);
    }

    public static void tracking(String s) {
        sendLog(s, NamedTextColor.AQUA);
    }

    public static void tag() {
        warning(" _____          __          __           _      _      ");
        warning("|_   _|         \\ \\        / /          | |    | |     ");
        warning("  | |   ___   ___\\ \\  /\\  / /___   _ __ | |  __| | ___ ");
        warning("  | |  / __| / _ \\\\ \\/  \\/ // _ \\ | '__|| | / _` |/ __|");
        warning(" _| |_ \\__ \\| (_) |\\  /\\  /| (_) || |   | || (_| |\\__ \\");
        warning("|_____||___/ \\___/  \\/  \\/  \\___/ |_|   |_| \\__,_||___/");
    }

}
