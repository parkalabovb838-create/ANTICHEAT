package com.good.anticheat;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class AntiCheatCommand implements CommandExecutor {

    private final AntiCheatPlugin plugin;

    public AntiCheatCommand(AntiCheatPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
        if (a.length == 0) {
            s.sendMessage("§c/ac reload|info|debug|webhook");
            return true;
        }

        switch (a[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                s.sendMessage("§a[ANTICHEAT] Конфиг перезагружен.");
                plugin.getLogger().info("Config reloaded by " + s.getName());
            }
            case "webhook" -> {
                if (s instanceof Player p) plugin.getWebhook().send(p, "Test", 99, 0.99f);
                s.sendMessage("§a[ANTICHEAT] Тестовый алерт отправлен.");
            }
            case "info" -> {
                s.sendMessage("§e[ANTICHEAT] Статус:");
                s.sendMessage("§eServer lock: §a" + (ServerLock.isMyServer(plugin) ? "OK" : "FAIL"));
                s.sendMessage("§eAI: §a" + (plugin.getAI() != null && plugin.getAI().isReady() ? "loaded" : "off"));
                s.sendMessage("§eMOTD: §f" + plugin.getServer().getMotd());
                s.sendMessage("§eHost: §f" + plugin.getServer().getIp());
                s.sendMessage("§ePort: §f" + plugin.getServer().getPort());
                s.sendMessage("§eFollow-ботов: §f" + plugin.getFollowBot().count());
            }
            case "debug" -> {
                if (a.length < 2) { s.sendMessage("§c/ac debug <ник>"); break; }
                Player p = plugin.getServer().getPlayer(a[1]);
                if (p == null) { s.sendMessage("§cИгрок не найден."); break; }
                PlayerData d = plugin.getData(p.getUniqueId());
                s.sendMessage("§e[ANTICHEAT] Debug: §f" + p.getName());
                s.sendMessage("§eKillAura VL: §c" + d.killauraVl);
                s.sendMessage("§eReach VL: §c" + d.reachVl);
                s.sendMessage("§eCPS VL: §c" + d.cpsVl);
                s.sendMessage("§eGCD VL: §c" + d.gcdVl);
                s.sendMessage("§eRotation VL: §c" + d.rotationVl);
                s.sendMessage("§eMultiAura VL: §c" + d.multiAuraVl);
                s.sendMessage("§eTriggerbot VL: §c" + d.triggerbotVl);
                s.sendMessage("§eAutoClicker VL: §c" + d.autoClickerVl);
                s.sendMessage("§eAI VL: §c" + d.aiVl);
                s.sendMessage("§eLast AI score: §c" + String.format("%.3f", d.lastAI));
                s.sendMessage("§eLegit score: §c" + d.legit.legitScore);
            }
            default -> s.sendMessage("§cНеизвестная команда.");
        }
        return true;
    }
}