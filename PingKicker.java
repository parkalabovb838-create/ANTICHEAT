package com.good.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PingKicker {

    private final AntiCheatPlugin plugin;

    public PingKicker(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfig().getBoolean("ping-kick.enabled", true)) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                int defaultMax = plugin.getConfig().getInt("ping-kick.max-ping", 400);
                int ownerMax = plugin.getConfig().getInt("ping-kick.owner-max-ping", 800);
                String ownerName = plugin.getConfig().getString("server-lock.expected-owner", "");

                for (Player p : Bukkit.getOnlinePlayers()) {
                    int ping = p.getPing();
                    boolean isOwner = p.getName().equalsIgnoreCase(ownerName);
                    int max = isOwner ? ownerMax : defaultMax;

                    if (ping > max) {
                        String msg = "§c[ANTICHEAT] §fКик за пинг: §e" + p.getName()
                                + " §7(" + ping + "ms > " + max + "ms)";
                        broadcast(msg);
                        plugin.getWebhook().sendPingKick(p, ping, max);
                        plugin.getFileLogger().log(p.getName(),
                                "PING_KICK=" + ping + "ms max=" + max + "ms");

                        p.kickPlayer("§c[ANTICHEAT]\n§fТвой пинг: §e" + ping + " ms\n"
                                + "§fМаксимум: §e" + max + " ms\n§fЗайди позже.");
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void broadcast(String msg) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(pl -> pl.hasPermission("anticheat.alerts"))
                .forEach(pl -> pl.sendMessage(msg));
        plugin.getServer().getConsoleSender().sendMessage(msg);
    }
}