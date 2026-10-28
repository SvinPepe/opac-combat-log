package dev.customclaims.combatlog;

import dev.customclaims.combatlog.config.CombatLogConfig;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CombatLogConfigTest {

    @Test
    void testConfigDefaultsAndSanitization() {
        CombatLogConfig config = new CombatLogConfig();
        assertTrue(config.combatDurationSeconds > 0);
        assertTrue(config.knockbackStrength > 0);
        assertTrue(config.restoreFlightAfterCombat);
        assertTrue(config.blockChorusFruitIntoClaims);
        assertFalse(config.operatorBypass);
        assertNotNull(config.blockedCommands);

        // Test invalid inputs
        config.combatDurationSeconds = -5;
        config.knockbackStrength = -1.0;
        config.blockedCommands = null;

        config.sanitize();

        assertEquals(15, config.combatDurationSeconds);
        assertEquals(0.5, config.knockbackStrength);
        assertNotNull(config.blockedCommands);
    }

    @Test
    void testConfigLoadOrCreate(@org.junit.jupiter.api.io.TempDir Path tempDir) throws IOException {
        CombatLogConfig loaded = CombatLogConfig.load(tempDir);
        assertNotNull(loaded);
        assertTrue(Files.exists(tempDir.resolve("opac-combatlog.json")));
        assertTrue(loaded.restoreFlightAfterCombat);
        assertTrue(loaded.blockChorusFruitIntoClaims);
        assertFalse(loaded.operatorBypass);
    }
}
