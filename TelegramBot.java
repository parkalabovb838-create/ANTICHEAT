package com.good.anticheat;

import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBot {

    private final AntiCheatPlugin plugin;
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public TelegramBot(AntiCheatPlugin plugin) { this.plugin = plugin; }

    public void send(Player p, String check, int vl) {
        if (!plugin.getConfig().getBoolean("telegram.enabled", false)) return;

        String token = plugin.getConfig().getString("telegram.bot-token", "");
        String chatId = plugin.getConfig().getString("telegram.chat-id", "");
        if (token.isEmpty() || chatId.isEmpty()) return;

        long cd = 5000L;
        String key = p.getUniqueId() + ":" + check;
        long now = System.currentTimeMillis();
        Long last = cooldowns.get(key);
        if (last != null && now - last < cd) return;
        cooldowns.put(key, now);

        String text = "🚨 ANTICHEAT\n"
                + "Игрок: " + p.getName() + "\n"
                + "Проверка: " + check + "\n"
                + "VL: " + vl + "\n"
                + "Мир: " + p.getWorld().getName() + "\n"
                + "Координаты: " + String.format("%.1f, %.1f, %.1f",
                        p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()) + "\n"
                + "Пинг: " + p.getPing() + "ms";

        String url = "https://api.telegram.org/bot" + token + "/sendMessage";
        String json = "{\"chat_id\":\"" + esc(chatId) + "\",\"text\":\"" + esc(text) + "\"}";

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> post(url, json));
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
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
            plugin.getLogger().warning("Telegram: " + e.getMessage());
        }
    }
}