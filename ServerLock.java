package com.good.anticheat;

import org.bukkit.Bukkit;

public class ServerLock {

    public static boolean isMyServer(AntiCheatPlugin plugin) {
        if (!plugin.getConfig().getBoolean("server-lock.enabled", true)) return true;

        String expectedMotd = plugin.getConfig().getString("server-lock.expected-motd", "");
        String actualMotd = Bukkit.getMotd();
        if (expectedMotd != null && !expectedMotd.isEmpty() && !expectedMotd.equals(actualMotd)) {
            plugin.getLogger().severe("MOTD mismatch.");
            return false;
        }

        String expectedHost = plugin.getConfig().getString("server-lock.expected-host", "");
        String actualHost = Bukkit.getIp();
        if (actualHost == null || actualHost.isEmpty()) actualHost = "localhost";
        if (expectedHost != null && !expectedHost.isEmpty()
                && !actualHost.contains(expectedHost.split("\\.")[0])) {
            plugin.getLogger().severe("Host mismatch: " + actualHost);
            return false;
        }

        int expectedPort = plugin.getConfig().getInt("server-lock.expected-port", 25565);
        int actualPort = Bukkit.getPort();
        if (expectedPort != 0 && expectedPort != actualPort) {
            plugin.getLogger().severe("Port mismatch: " + actualPort);
            return false;
        }

        String expectedOwner = plugin.getConfig().getString("server-lock.expected-owner", "");
        if (expectedOwner != null && !expectedOwner.isEmpty()) {
            var owner = Bukkit.getOfflinePlayer(expectedOwner);
            if (!owner.hasPlayedBefore() && !owner.isOnline()) {
                plugin.getLogger().severe("Owner not found.");
                return false;
            }
        }

        return true;
    }
}