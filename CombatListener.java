package com.good.anticheat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatListener implements Listener {

    private final AntiCheatPlugin plugin;
    private final ClassicChecks classic;

    public CombatListener(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.classic = new ClassicChecks(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;

        Entity target = e.getEntity();
        PlayerData d = plugin.getData(p.getUniqueId());

        int cps = countCps(d.clickTimes);
        float gcd = gcd(d.yawDeltas);
        float angle = d.angles.peekLast() != null ? d.angles.peekLast() : 0f;

        plugin.getLegitimacyChecker().update(d, gcd, cps, angle);

        boolean isLegit = plugin.getLegitimacyChecker().isLegit(d);
        boolean isCheater = plugin.getLegitimacyChecker().isCheater(d);

        if (isLegit && !isCheater) return;

        String flag = classic.check(p, target, d, isCheater);

        float ai = 0f;
        if (plugin.getAI() != null && plugin.getAI().isReady()
                && plugin.getConfig().getBoolean("ai.enabled", true)) {
            float[] features = FeatureExtractor.extract(p, target, d);
            ai = plugin.getAI().predict(features);
            d.lastAI = ai;
            float threshold = (float) plugin.getConfig().getDouble("ai.threshold", 0.80);
            if (ai > threshold) {
                d.aiVl++;
                if (flag == null) flag = "AI-KillAura";
            }
        }

        if (flag == null) return;

        int vl = d.killauraVl + d.cpsVl + d.reachVl + d.gcdVl + d.aiVl
                + d.rotationVl + d.multiAuraVl + d.triggerbotVl + d.autoClickerVl;

        broadcast(p, flag, vl, ai);
        plugin.getWebhook().send(p, flag, vl, ai);
        plugin.getFileLogger().log(p.getName(),
                "FLAG=" + flag + " VL=" + vl + " AI=" + String.format("%.3f", ai)
                        + " LEGIT=" + d.legit.legitScore);

        int followAt = plugin.getConfig().getInt("auto-follow.at-vl", 10);
        if (followAt > 0 && vl >= followAt && !plugin.getFollowBot().hasBot(p)) {
            plugin.getFollowBot().startFollow(p);
            broadcastRaw("§c[ANTICHEAT] §fЗа §e" + p.getName() + " §fвылетел бот проверки.");
        }

        int banAt = plugin.getConfig().getInt("punishment.ban-at-vl", 0);
        if (banAt > 0 && vl >= banAt) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    plugin.getServer().dispatchCommand(
                            plugin.getServer().getConsoleSender(),
                            "ban " + p.getName() + " Cheating"));
        }

        int kickAt = plugin.getConfig().getInt("punishment.kick-at-vl", 0);
        if (kickAt > 0 && vl >= kickAt) {
            plugin.getServer().getScheduler().runTask(plugin, () ->
                    p.kickPlayer("§c[ANTICHEAT]\n§fПричина: " + flag));
        }
    }

    private void broadcast(Player p, String check, int vl, float ai) {
        String msg = "§c[ANTICHEAT] §f" + p.getName()
                + " §7- §e" + check
                + " §7(VL:§c" + vl + "§7 | AI:§c"
                + String.format("%.3f", ai) + "§7)";

        plugin.getServer().getOnlinePlayers().stream()
                .filter(pl -> pl.hasPermission("anticheat.alerts"))
                .forEach(pl -> pl.sendMessage(msg));

        plugin.getServer().getConsoleSender().sendMessage(msg);

        if (plugin.getConfig().getBoolean("alerts.sound", true)) {
            plugin.getServer().getOnlinePlayers().stream()
                    .filter(pl -> pl.hasPermission("anticheat.alerts"))
                    .forEach(pl -> pl.playSound(pl.getLocation(),
                            org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f));
        }
    }

    private void broadcastRaw(String msg) {
        plugin.getServer().getOnlinePlayers().stream()
                .filter(pl -> pl.hasPermission("anticheat.alerts"))
                .forEach(pl -> pl.sendMessage(msg));
        plugin.getServer().getConsoleSender().sendMessage(msg);
    }

    private int countCps(java.util.Deque<Long> q) {
        long now = System.currentTimeMillis();
        int c = 0;
        for (long t : q) if (now - t <= 1000) c++;
        return c;
    }

    private float gcd(java.util.Deque<Float> q) {
        if (q.size() < 3) return 0;
        var it = q.iterator();
        float g = it.next();
        while (it.hasNext()) g = gcd2(g, it.next());
        return g;
    }

    private float gcd2(float a, float b) {
        a = Math.abs(a); b = Math.abs(b);
        while (b > 0.001f) { float t = b; b = a % b; a = t; }
        return a;
    }
}