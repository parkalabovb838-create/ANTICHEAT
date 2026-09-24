package com.good.anticheat;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileLogger {

    private final AntiCheatPlugin plugin;
    private final Path logDir;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public FileLogger(AntiCheatPlugin plugin) {
        this.plugin = plugin;
        this.logDir = Paths.get(plugin.getDataFolder().getAbsolutePath(), "logs");
        try {
            Files.createDirectories(logDir);
        } catch (IOException e) {
            plugin.getLogger().warning("Не удалось создать папку logs.");
        }
    }

    public void log(String playerName, String message) {
        String fileName = "check_" + playerName + ".log";
        Path file = logDir.resolve(fileName);
        String time = timeFormat.format(new Date());
        String line = "[" + time + "] " + message + "\n";

        try (FileWriter fw = new FileWriter(file.toFile(), true)) {
            fw.write(line);
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка записи лога: " + e.getMessage());
        }
    }
}