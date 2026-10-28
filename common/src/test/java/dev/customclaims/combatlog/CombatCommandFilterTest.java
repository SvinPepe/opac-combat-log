package dev.customclaims.combatlog;

import dev.customclaims.combatlog.command.CombatCommandFilter;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatCommandFilterTest {

    private final List<String> blocked = List.of(
            "spawn", "home", "tp", "tpa", "warp", "back", "rtp", "openpac", "opac"
    );

    @Test
    void testExtractRootCommand() {
        assertEquals("spawn", CombatCommandFilter.extractRootCommand("/spawn"));
        assertEquals("tp", CombatCommandFilter.extractRootCommand("/tp"));
        assertEquals("tp", CombatCommandFilter.extractRootCommand("/minecraft:tp"));
        assertEquals("spawn", CombatCommandFilter.extractRootCommand("/essentials:spawn player"));
        assertEquals("home", CombatCommandFilter.extractRootCommand("home myhome"));
        assertEquals("opac", CombatCommandFilter.extractRootCommand("/openpartiesandclaims:opac claims"));
    }

    @Test
    void testIsCommandBlocked() {
        assertTrue(CombatCommandFilter.isCommandBlocked("/spawn", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("/SPAWN", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("/minecraft:spawn", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("/tp Steve Alex", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("tp Steve Alex", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("/openpac claims", blocked));
        assertTrue(CombatCommandFilter.isCommandBlocked("/opac claims", blocked));

        assertFalse(CombatCommandFilter.isCommandBlocked("/msg Steve Hello", blocked));
        assertFalse(CombatCommandFilter.isCommandBlocked("/help", blocked));
        assertFalse(CombatCommandFilter.isCommandBlocked("/list", blocked));
    }
}
