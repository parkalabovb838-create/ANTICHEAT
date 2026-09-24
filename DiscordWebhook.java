package com.good.anticheat;

import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DiscordWebhook {

    private final AntiCheatPlugin plugin;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public DiscordWebhook(AntiCheatPlugin plugin) { this.plugin = plugin; }

    public void send(Player p, String check, int vl, float aiScore) {
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) return;
        String url = plugin.getConfig().getString("discord.webhook-url", "");
        if (url.isEmpty() || url.contains("ВСТАВЬ")) return;

        int minVl = plugin.getConfig().getInt("discord.min-vl-to-send", 2);
        if (vl < minVl) return;

        long cd = plugin.getConfig().getInt("discord.cooldown-seconds", 5) * 1000L;
        String key = p.getUniqueId() + ":" + check;
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(key);
        if (last != null && now - last < cd) return;
        cooldowns.put(key, now);

        String json = buildJson(p, check, vl, aiScore);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> post(url, json));

        if (plugin.getTelegram() != null) {
            plugin.getTelegram().send(p, check, vl);
        }
    }

    public void sendPingKick(Player p, int ping, int max) {
        if (!plugin.getConfig().getBoolean("discord.enabled", false)) return;
        String url = plugin.getConfig().getString("discord.webhook-url", "");
        if (url.isEmpty() || url.contains("ВСТАВЬ")) return;

        String json = "{"
                + "\"username\":\"ANTICHEAT\","
                + "\"embeds\":[{"
                + "\"title\":\"\\ud83d\\udcf6 Ping Kick\","
                + "\"color\":15158332,"
                + "\"fields\":["
                + "{\"name\":\"Player\",\"value\":\"" + esc(p.getName()) + "\",\"inline\":true},"
                + "{\"name\":\"Ping\",\"value\":\"" + ping + "ms\",\"inline\":true},"
                + "{\"name\":\"Max\",\"value\":\"" + max + "ms\",\"inline\":true}"
                + "],"
                + "\"footer\":{\"text\":\"ANTICHEAT\"}"
                + "}]}";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> post(url, json));
    }

    private String buildJson(Player p, String check, int vl, float ai) {
        return "{"
                + "\"username\":\"ANTICHEAT\","
                + "\"embeds\":[{"
                + "\"title\":\"\\ud83d\\udea8 Detection\","
                + "\"color\":" + (ai > 0.9f ? 15158332 : 16776960) + ","
                + "\"fields\":["
                + f("Player", p.getName())
                + f("Check", check)
                + f("VL", String.valueOf(vl))
                + f("AI Score", String.format("%.3f", ai))
                + f("World", p.getWorld().getName())
                + f("Coords", String.format("%.1f, %.1f, %.1f",
                        p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()))
                + f("Ping", p.getPing() + "ms")
                + "],"
                + "\"footer\":{\"text\":\"ANTICHEAT\"}"
                + "}]}";
    }

    private String f(String n, String v) {
        return "{\"name\":\"" + esc(n) + "\",\"value\":\"" + esc(v) + "\",\"inline\":true},";
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void post(String urlStr, String json) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(urlStr).openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json");
            c.setRequestProperty("User-Agent", "ANTICHEAT/1.0");
            c.setDoOutput(true);
            try (OutputStream os = c.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            c.getResponseCode();
            c.disconnect();
        } catch (Exception e) {
            plugin.getLogger().warning("Webhook: " + e.getMessage());
        }
    }
}