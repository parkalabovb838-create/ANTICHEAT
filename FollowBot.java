package com.good.anticheat;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FollowBot {

    private final AntiCheatPlugin plugin;
    private final Map<UUID, NPC> bots = new HashMap<>();
    private final Map<UUID, BukkitRunnable> tasks = new HashMap<>();

    public FollowBot(AntiCheatPlugin plugin) {
        this.plugin = plugin;
    }

    public void startFollow(Player target) {
        stopFollow(target);

        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC bot = registry.createNPC(EntityType.PLAYER, "§c[AC] §fBot");
        bot.spawn(target.getLocation().add(2, 1, 0));

        Equipment eq = bot.getOrAddTrait(Equipment.class);
        eq.set(Equipment.EquipmentSlot.HAND, new ItemStack(org.bukkit.Material.DIAMOND_SWORD));
        eq.set(Equipment.EquipmentSlot.HELMET, new ItemStack(org.bukkit.Material.DIAMOND_HELMET));

        bot.getNavigator().getDefaultParameters().speedModifier(2.5f);
        bots.put(target.getUniqueId(), bot);

        BukkitRunnable task = new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!target.isOnline() || !bot.isSpawned()) {
                    stopFollow(target);
                    cancel();
                    return;
                }

                Location playerLoc = target.getLocation();
                Location botLoc = bot.getEntity().getLocation();
                double distance = botLoc.distance(playerLoc);

                if (distance > 6) {
                    Location tp = playerLoc.clone().add(playerLoc.getDirection().multiply(-3));
                    tp.setY(playerLoc.getY() + 1.5);
                    bot.teleport(tp, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                } else if (distance > 4) {
                    Vector dir = playerLoc.toVector().subtract(botLoc.toVector()).normalize();
                    Location newLoc = botLoc.clone().add(dir.multiply(0.3));
                    newLoc.setY(playerLoc.getY() + 1.5);
                    bot.teleport(newLoc, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
                }

                faceTarget(bot, playerLoc);

                ticks++;
                if (ticks % 20 == 0) {
                    logStats(target);
                }
            }
        };

        task.runTaskTimer(plugin, 0L, 2L);
        tasks.put(target.getUniqueId(), task);

        plugin.getLogger().info("[ANTICHEAT] Бот запущен для " + target.getName());
        target.sendMessage("§c[ANTICHEAT] §fЗа тобой следит бот проверки.");
    }

    private void faceTarget(NPC bot, Location target) {
        Location botLoc = bot.getEntity().getLocation();
        Vector dir = target.toVector().subtract(botLoc.toVector());
        Location look = botLoc.clone();
        look.setDirection(dir);
        bot.getEntity().teleport(look, org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    private void logStats(Player p) {
        PlayerData d = plugin.getData(p.getUniqueId());
        int totalVl = d.killauraVl + d.reachVl + d.cpsVl + d.gcdVl
                + d.rotationVl + d.multiAuraVl + d.triggerbotVl
                + d.autoClickerVl + d.aiVl;

        plugin.getFileLogger().log(p.getName(),
                "VL=" + totalVl
                        + " AI=" + String.format("%.3f", d.lastAI)
                        + " pos=" + String.format("%.1f,%.1f,%.1f",
                        p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()));
    }

    public void stopFollow(Player target) {
        NPC bot = bots.remove(target.getUniqueId());
        if (bot != null) {
            if (bot.isSpawned()) bot.despawn();
            bot.destroy();
        }

        BukkitRunnable task = tasks.remove(target.getUniqueId());
        if (task != null) task.cancel();

        if (target.isOnline()) {
            target.sendMessage("§c[ANTICHEAT] §fБот проверки убран.");
        }
    }

    public boolean hasBot(Player p) {
        return bots.containsKey(p.getUniqueId());
    }

    public int count() {
        return bots.size();
    }
}