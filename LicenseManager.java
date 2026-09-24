package com.good.anticheat;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LicenseManager {

    private final AntiCheatPlugin plugin;
    private boolean valid = false;

    public LicenseManager(AntiCheatPlugin plugin) { this.plugin = plugin; }

    public boolean validate() {
        if (!plugin.getConfig().getBoolean("license.enabled", false)) {
            valid = true;
            return true;
        }

        String key = plugin.getConfig().getString("license.key", "");
        if (key.isEmpty()) return false;

        try {
            String api = plugin.getConfig().getString("license.api-url", "");
            String ip = Bukkit.getIp();
            if (ip == null || ip.isEmpty()) ip = "127.0.0.1";
            URL url = new URL(api + "?key=" + key + "&ip=" + ip);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setConnectTimeout(5000);
            c.setReadTimeout(5000);
            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();
            valid = sb.toString().contains("\"valid\":true");
            return valid;
        } catch (Exception e) {
            plugin.getLogger().severe("License failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isValid() { return valid; }
}