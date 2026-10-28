package dev.customclaims.combatlog;

import dev.customclaims.combatlog.config.CombatLogConfig;
import dev.customclaims.combatlog.platform.PlatformHelperHolder;
import dev.customclaims.combatlog.service.CombatManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OpacCombatLog {
    public static final String MOD_ID = "opac_combatlog";
    public static final Logger LOGGER = LoggerFactory.getLogger("OPACCombatLog");

    private static CombatLogConfig config;
    private static CombatManager manager;

    private OpacCombatLog() {
    }

    public static void init() {
        if (manager != null) {
            return;
        }

        config = CombatLogConfig.load(PlatformHelperHolder.get().getConfigDirectory());
        manager = new CombatManager(config);

        LOGGER.info("OPAC Combat Log initialized successfully! Combat duration: {}s", config.combatDurationSeconds);
    }

    public static CombatManager getManager() {
        if (manager == null) {
            throw new IllegalStateException("OPAC Combat Log has not been initialized yet!");
        }
        return manager;
    }

    public static CombatLogConfig getConfig() {
        if (config == null) {
            throw new IllegalStateException("OPAC Combat Log has not been initialized yet!");
        }
        return config;
    }
}
