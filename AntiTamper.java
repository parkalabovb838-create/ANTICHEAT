package com.good.anticheat;

import java.security.MessageDigest;

public class AntiTamper {

    public static boolean verify(AntiCheatPlugin plugin) {
        if (!plugin.getConfig().getBoolean("anti-tamper.enabled", false)) return true;

        String expected = plugin.getConfig().getString("anti-tamper.expected-hash", "");
        if (expected == null || expected.isEmpty()) return true;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            var loc = AntiTamper.class.getProtectionDomain().getCodeSource().getLocation();
            try (var is = loc.openStream()) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) md.update(buf, 0, n);
            }
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            if (!sb.toString().equals(expected)) {
                plugin.getLogger().severe("Anti-Tamper: jar modified.");
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}