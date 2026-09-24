package com.good.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheatPlugin extends JavaPlugin {

    private final Map<UUID, PlayerData> data = new ConcurrentHashMap<>();
    private DiscordWebhook webhook;
    private AIDetector ai;
    private LicenseManager license;
    private FollowBot followBot;
    private FileLogger fileLogger;
    private PingKicker pingKicker;
    private TelegramBot telegram;
    private LegitimacyChecker legitimacyChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        if (!ServerLock.isMyServer(this)) {
            getLogger().severe("=================================");
            getLogger().severe(" Плагин привязан к другому серверу.");
            getLogger().severe("=================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        license = new LicenseManager(this);
        if (!license.validate()) {
            getLogger().severe("Лицензия недействительна.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!AntiTamper.verify(this)) {
            getLogger().severe("Anti-Tamper: jar изменён.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        webhook = new DiscordWebhook(this);
        telegram = new TelegramBot(this);
        fileLogger = new FileLogger(this);
        legitimacyChecker = new LegitimacyChecker(this);
        followBot = new FollowBot(this);
        pingKicker = new PingKicker(this);

        File model = new File(getDataFolder(), getConfig().getString("ai.model-path", "model.onnx"));
        if (model.exists()) {
            ai = new AIDetector(model.toPath());
            if (!ai.isReady()) getLogger().warning("AI model failed to load.");
        } else {
            getLogger().warning("AI model not found, classic checks only.");
        }

        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
        getCommand("ac").setExecutor(new AntiCheatCommand(this));

        pingKicker.start();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            long now = System.currentTimeMillis();
            data.values().removeIf(d -> now - d.lastAttack > 180_000);
        }, 1200L, 1200L);

        getLogger().info("[ANTICHEAT] Плагин загружен. Server lock OK.");
    }

    @Override
    public void onDisable() { data.clear(); }

    public PlayerData getData(UUID u) {
        return data.computeIfAbsent(u, k -> { PlayerData d = new PlayerData(); d.uuid = k; return d; });
    }

    public DiscordWebhook getWebhook() { return webhook; }
    public AIDetector getAI() { return ai; }
    public FollowBot getFollowBot() { return followBot; }
    public FileLogger getFileLogger() { return fileLogger; }
    public TelegramBot getTelegram() { return telegram; }
    public LegitimacyChecker getLegitimacyChecker() { return legitimacyChecker; }
}