package com.good.anticheat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;

public class ClassicChecks {

    private final AntiCheatPlugin plugin;

    public ClassicChecks(AntiCheatPlugin plugin) { this.plugin = plugin; }

    public String check(Player p, Entity target, PlayerData d, boolean isCheater) {
        long now = System.currentTimeMillis();

        int cpsMax = isCheater ? plugin.getConfig().getInt("detection.cheater-cps-max", 8)
                : plugin.getConfig().getInt("detection.cps-max", 14);
        double angleMax = isCheater ? plugin.getConfig().getDouble("detection.cheater-angle-max", 8.0)
                : plugin.getConfig().getDouble("detection.angle-max", 12.0);
        double reachMax = isCheater ? plugin.getConfig().getDouble("detection.cheater-reach-max", 3.2)
                : plugin.getConfig().getDouble("detection.reach-max", 3.8);
        float gcdMin = isCheater ? (float) plugin.getConfig().getDouble("detection.cheater-gcd-min", 0.10)
                : (float) plugin.getConfig().getDouble("detection.gcd-min", 0.05);

        if (plugin.getConfig().getBoolean("detection.cps-enabled", true)) {
            int cps = countCps(d.clickTimes, now);
            if (cps > cpsMax) { d.cpsVl++; return "CPS (" + cps + ")"; }
        }

        if (plugin.getConfig().getBoolean("detection.angle-enabled", true)) {
            Vector look = p.getEyeLocation().getDirection().normalize();
            Vector toT = target.getLocation().toVector()
                    .subtract(p.getEyeLocation().toVector()).normalize();
            double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.dot(toT)))));
            if (angle > angleMax && angle < 90) {
                d.killauraVl++;
                return "Angle (" + String.format("%.1f", angle) + "°)";
            }
        }

        if (plugin.getConfig().getBoolean("detection.reach-enabled", true)) {
            double dist = p.getEyeLocation().distance(target.getLocation());
            if (dist > reachMax) {
                d.reachVl++;
                return "Reach (" + String.format("%.2f", dist) + ")";
            }
        }

        if (plugin.getConfig().getBoolean("detection.gcd-enabled", true)) {
            float gcd = gcd(d.yawDeltas);
            if (gcd > 0 && gcd < gcdMin && d.yawDeltas.size() > 10) {
                d.gcdVl++;
                return "GCD (" + String.format("%.4f", gcd) + ")";
            }
        }

        if (plugin.getConfig().getBoolean("detection.rotation-enabled", true)) {
            float delta = Math.abs(p.getLocation().getYaw() - d.lastYaw);
            float max = (float) plugin.getConfig().getDouble("detection.rotation-max-delta", 180.0);
            if (delta > max) {
                d.rotationVl++;
                return "Rotation (" + String.format("%.1f", delta) + "°)";
            }
        }

        if (plugin.getConfig().getBoolean("detection.snap-enabled", true)) {
            float yawDelta = Math.abs(p.getLocation().getYaw() - d.lastYaw);
            if (yawDelta > 30 && d.angles.peekLast() != null && d.angles.peekLast() < 1.0f) {
                d.rotationVl++;
                return "Snap (yaw " + String.format("%.1f", yawDelta) + "°)";
            }
        }

        if (plugin.getConfig().getBoolean("detection.multi-aura-enabled", true)) {
            d.recentTargets.add(target.getUniqueId());
            int max = plugin.getConfig().getInt("detection.multi-aura-max-targets", 2);
            if (d.recentTargets.size() > max && now - d.lastAttack < 500) {
                d.multiAuraVl++;
                return "MultiAura (" + d.recentTargets.size() + ")";
            }
        }

        if (plugin.getConfig().getBoolean("detection.triggerbot-enabled", true)) {
            long delay = now - d.lastAttack;
            long min = plugin.getConfig().getInt("detection.triggerbot-min-delay", 40);
            if (delay < min && d.lastAttack > 0) {
                d.triggerbotVl++;
                return "Triggerbot (" + delay + "ms)";
            }
        }

        if (plugin.getConfig().getBoolean("detection.autoclicker-enabled", true)) {
            double variance = clickVariance(d.clickTimes, now);
            double maxCps = plugin.getConfig().getInt("detection.autoclicker-max-cps", 12);
            if (countCps(d.clickTimes, now) > maxCps && variance < 5.0) {
                d.autoClickerVl++;
                return "AutoClicker";
            }
        }

        return null;
    }

    private int countCps(Deque<Long> q, long now) {
        int c = 0;
        for (long t : q) if (now - t <= 1000) c++;
        return c;
    }

    private double clickVariance(Deque<Long> q, long now) {
        Deque<Long> recent = new ArrayDeque<>();
        for (long t : q) if (now - t <= 5000) recent.add(t);
        if (recent.size() < 5) return 100;
        double m = 0;
        for (long t : recent) m += t;
        m /= recent.size();
        double s = 0;
        for (long t : recent) s += (t - m) * (t - m);
        return Math.sqrt(s / recent.size());
    }

    private float gcd(Deque<Float> q) {
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