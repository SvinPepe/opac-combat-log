package dev.customclaims.combatlog.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CombatLogConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("OPACCombatLog");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public int combatDurationSeconds = 15;
    public boolean untagOnEnemyDeath = true;
    public boolean disableFlight = true;
    public boolean blockContainers = true;
    public boolean blockPortals = true;
    public boolean blockEnderPearlsIntoClaims = true;
    public boolean broadcastCombatLog = true;
    public boolean actionbarNotification = true;
    public double knockbackStrength = 0.5;
    public List<String> blockedCommands = new ArrayList<>(List.of(
            "spawn", "home", "sethome", "tp", "tpa", "tpaccept", "tpahere",
            "warp", "back", "rtp", "openpac", "opac"
    ));

    public static CombatLogConfig load(Path configDir) {
        Path configFile = configDir.resolve("opac-combatlog.json");
        CombatLogConfig config = new CombatLogConfig();

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                CombatLogConfig loaded = GSON.fromJson(reader, CombatLogConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load opac-combatlog.json config, falling back to defaults: {}", e.getMessage());
            }
        } else {
            try {
                Files.createDirectories(configDir);
                try (Writer writer = Files.newBufferedWriter(configFile)) {
                    GSON.toJson(config, writer);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to write default opac-combatlog.json config: {}", e.getMessage());
            }
        }

        return config;
    }
}
