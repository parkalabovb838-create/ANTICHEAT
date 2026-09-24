package com.good.anticheat;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    public UUID uuid;
    public Deque<Float> yawDeltas = new ArrayDeque<>();
    public Deque<Float> pitchDeltas = new ArrayDeque<>();
    public Deque<Float> angles = new ArrayDeque<>();
    public Deque<Long> clickTimes = new ArrayDeque<>();
    public Deque<Double> reaches = new ArrayDeque<>();
    public Set<UUID> recentTargets = new HashSet<>();

    public float lastYaw = 0f;
    public float lastPitch = 0f;
    public UUID lastTarget = null;
    public long lastAttack = 0;
    public long lastMove = 0;
    public boolean wasOnGround = true;

    public int killauraVl = 0;
    public int reachVl = 0;
    public int cpsVl = 0;
    public int aiVl = 0;
    public int velocityVl = 0;
    public int rotationVl = 0;
    public int gcdVl = 0;
    public int multiAuraVl = 0;
    public int autoClickerVl = 0;
    public int triggerbotVl = 0;
    public int flyVl = 0;
    public int speedVl = 0;

    public float lastAI = 0f;
    public LegitimacyChecker.LegitData legit = new LegitimacyChecker.LegitData();

    public void trim() {
        while (yawDeltas.size() > 30) yawDeltas.poll();
        while (pitchDeltas.size() > 30) pitchDeltas.poll();
        while (angles.size() > 30) angles.poll();
        while (clickTimes.size() > 40) clickTimes.poll();
        while (reaches.size() > 30) reaches.poll();
        if (recentTargets.size() > 20) recentTargets.clear();
        legit.trim();
    }

    public void reset() {
        yawDeltas.clear();
        pitchDeltas.clear();
        angles.clear();
        clickTimes.clear();
        reaches.clear();
        recentTargets.clear();
        killauraVl = 0;
        reachVl = 0;
        cpsVl = 0;
        aiVl = 0;
        velocityVl = 0;
        rotationVl = 0;
        gcdVl = 0;
        multiAuraVl = 0;
        autoClickerVl = 0;
        triggerbotVl = 0;
        flyVl = 0;
        speedVl = 0;
        lastAI = 0f;
    }
}