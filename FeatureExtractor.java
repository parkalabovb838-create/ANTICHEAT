package com.good.anticheat;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Deque;
import java.util.Iterator;

public class FeatureExtractor {

    public static float[] extract(Player p, Entity target, PlayerData d) {
        float[] f = new float[24];
        long now = System.currentTimeMillis();

        float yawDelta = Math.abs(p.getLocation().getYaw() - d.lastYaw);
        float pitchDelta = Math.abs(p.getLocation().getPitch() - d.lastPitch);
        f[0] = yawDelta;
        f[1] = pitchDelta;
        f[2] = avg(d.yawDeltas);
        f[3] = avg(d.pitchDeltas);
        f[4] = gcd(d.yawDeltas);
        f[5] = gcd(d.pitchDeltas);

        Vector look = p.getEyeLocation().getDirection().normalize();
        Vector toT = target.getLocation().toVector()
                .subtract(p.getEyeLocation().toVector()).normalize();
        double angle = Math.toDegrees(Math.acos(Math.max(-1, Math.min(1, look.dot(toT)))));
        f[6] = (float) angle;
        f[7] = avg(d.angles);
        f[8] = (float) stddev(d.angles);

        double reach = p.getEyeLocation().distance(target.getLocation());
        f[9] = (float) reach;
        f[10] = (float) avgD(d.reaches);
        f[11] = (float) stddevD(d.reaches);

        f[12] = cps(d.clickTimes, now, 1000);
        f[13] = cps(d.clickTimes, now, 5000);
        f[14] = (float) stddevL(d.clickTimes);
        f[15] = d.recentTargets.size();
        f[16] = d.lastTarget != null && !d.lastTarget.equals(target.getUniqueId()) ? 1f : 0f;
        f[17] = p.isSprinting() ? 1f : 0f;
        f[18] = p.isSneaking() ? 1f : 0f;
        f[19] = (float) p.getVelocity().length();
        f[20] = (float) Math.abs(p.getLocation().getY() - target.getLocation().getY());
        f[21] = p.getPing() / 100f;
        f[22] = (now - d.lastAttack) / 1000f;
        f[23] = d.killauraVl + d.reachVl + d.cpsVl + d.gcdVl + d.aiVl;

        d.lastYaw = p.getLocation().getYaw();
        d.lastPitch = p.getLocation().getPitch();
        d.yawDeltas.add(yawDelta);
        d.pitchDeltas.add(pitchDelta);
        d.angles.add((float) angle);
        d.reaches.add(reach);
        d.clickTimes.add(now);
        d.lastTarget = target.getUniqueId();
        d.lastAttack = now;
        d.trim();

        return f;
    }

    private static float avg(Deque<Float> q) {
        if (q.isEmpty()) return 0;
        float s = 0; for (float v : q) s += v; return s / q.size();
    }
    private static double avgD(Deque<Double> q) {
        if (q.isEmpty()) return 0;
        double s = 0; for (double v : q) s += v; return s / q.size();
    }
    private static float stddev(Deque<Float> q) {
        if (q.size() < 2) return 0;
        float m = avg(q); float s = 0;
        for (float v : q) s += (v - m) * (v - m);
        return (float) Math.sqrt(s / q.size());
    }
    private static double stddevD(Deque<Double> q) {
        if (q.size() < 2) return 0;
        double m = avgD(q); double s = 0;
        for (double v : q) s += (v - m) * (v - m);
        return Math.sqrt(s / q.size());
    }
    private static float stddevL(Deque<Long> q) {
        if (q.size() < 2) return 0;
        double m = 0; for (long v : q) m += v; m /= q.size();
        double s = 0; for (long v : q) s += (v - m) * (v - m);
        return (float) Math.sqrt(s / q.size());
    }
    private static float cps(Deque<Long> q, long now, long window) {
        int c = 0;
        for (long t : q) if (now - t <= window) c++;
        return c * (1000f / window);
    }
    private static float gcd(Deque<Float> q) {
        if (q.size() < 3) return 0;
        Iterator<Float> it = q.iterator();
        float g = it.next();
        while (it.hasNext()) g = gcd2(g, it.next());
        return g;
    }
    private static float gcd2(float a, float b) {
        a = Math.abs(a); b = Math.abs(b);
        while (b > 0.001f) { float t = b; b = a % b; a = t; }
        return a;
    }
}