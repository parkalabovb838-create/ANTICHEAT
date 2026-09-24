package com.good.anticheat;

import java.util.ArrayDeque;
import java.util.Deque;

public class LegitimacyChecker {

    private final AntiCheatPlugin plugin;

    public static class LegitData {
        public Deque<Float> gcdHistory = new ArrayDeque<>();
        public Deque<Integer> cpsHistory = new ArrayDeque<>();
        public Deque<Float> angleHistory = new ArrayDeque<>();
        public long sessionStart = 0;
        public double avgCps = 0;
        public double avgAngle = 0;
        public double avgGcd = 0;
        public boolean isLegit = false;
        public int legitScore = 0;

        public void trim() {
            while (gcdHistory.size() > 50) gcdHistory.poll();
            while (cpsHistory.size() > 50) cpsHistory.poll();
            while (angleHistory.size() > 50) angleHistory.poll();
        }
    }

    public LegitimacyChecker(AntiCheatPlugin plugin) { this.plugin = plugin; }

    public void update(PlayerData d, float gcd, int cps, float angle) {
        if (d.legit.sessionStart == 0) d.legit.sessionStart = System.currentTimeMillis();

        d.legit.gcdHistory.add(gcd);
        d.legit.cpsHistory.add(cps);
        d.legit.angleHistory.add(angle);
        d.legit.trim();

        if (d.legit.gcdHistory.size() >= 20) {
            d.legit.avgGcd = avg(d.legit.gcdHistory);
            d.legit.avgCps = avgInt(d.legit.cpsHistory);
            d.legit.avgAngle = avg(d.legit.angleHistory);

            int score = 0;
            if (d.legit.avgGcd > 0.1) score += 40;
            if (stddevInt(d.legit.cpsHistory) > 2.0) score += 30;
            if (d.legit.avgAngle > 1.5) score += 30;

            d.legit.legitScore = score;
            d.legit.isLegit = score >= 60;
        }
    }

    public boolean isLegit(PlayerData d) {
        return d.legit.isLegit && d.legit.legitScore >= 60;
    }

    public boolean isCheater(PlayerData d) {
        int totalVl = d.killauraVl + d.cpsVl + d.gcdVl + d.aiVl
                + d.rotationVl + d.multiAuraVl + d.triggerbotVl + d.autoClickerVl;
        return totalVl >= 5;
    }

    private float avg(Deque<Float> q) {
        if (q.isEmpty()) return 0;
        float s = 0; for (float v : q) s += v; return s / q.size();
    }
    private double avgInt(Deque<Integer> q) {
        if (q.isEmpty()) return 0;
        double s = 0; for (int v : q) s += v; return s / q.size();
    }
    private double stddevInt(Deque<Integer> q) {
        if (q.size() < 2) return 0;
        double m = avgInt(q); double s = 0;
        for (int v : q) s += (v - m) * (v - m);
        return Math.sqrt(s / q.size());
    }
}