package com.good.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    private final AntiCheatPlugin plugin;

    public MovementListener(AntiCheatPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isFlying() || p.getAllowFlight()) return;
        if (e.getTo() == null || e.getFrom() == null) return;

        PlayerData d = plugin.getData(p.getUniqueId());
        long now = System.currentTimeMillis();

        if (plugin.getConfig().getBoolean("detection.fly-enabled", true)) {
            double dy = e.getTo().getY() - e.getFrom().getY();
            if (!p.isOnGround() && dy > 0 && now - d.lastMove < 200) {
                d.flyVl++;
                if (d.flyVl > 3) plugin.getWebhook().send(p, "Fly", d.flyVl, 0);
            }
        }

        if (plugin.getConfig().getBoolean("detection.speed-enabled", true)) {
            double dx = e.getTo().getX() - e.getFrom().getX();
            double dz = e.getTo().getZ() - e.getFrom().getZ();
            double speed = Math.sqrt(dx * dx + dz * dz);
            if (speed > 0.5 && !p.isInsideVehicle()) {
                d.speedVl++;
                if (d.speedVl > 5) plugin.getWebhook().send(p, "Speed", d.speedVl, 0);
            }
        }

        d.lastMove = now;
        d.wasOnGround = p.isOnGround();
    }
}