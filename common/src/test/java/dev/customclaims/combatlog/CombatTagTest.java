package dev.customclaims.combatlog;

import dev.customclaims.combatlog.model.CombatTag;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombatTagTest {

    @Test
    void testCombatTagLifecycle() throws InterruptedException {
        UUID opponent = UUID.randomUUID();
        String oppName = "TestEnemy";
        long now = System.currentTimeMillis();
        long duration = 200L; // 200 ms
        CombatTag tag = new CombatTag(opponent, oppName, now + duration, Optional.empty());

        assertFalse(tag.isExpired());
        assertEquals(opponent, tag.getOpponentUuid());
        assertEquals(oppName, tag.getOpponentName());
        assertTrue(tag.getOriginClaimOwner().isEmpty());
        assertTrue(tag.remainingSeconds() <= 1L);

        Thread.sleep(250L);
        assertTrue(tag.isExpired());
        assertEquals(0L, tag.remainingSeconds());

        // Refresh expiry
        tag.refreshExpiry(System.currentTimeMillis() + 5000L);
        assertFalse(tag.isExpired());
        assertTrue(tag.remainingSeconds() >= 4L);
    }

    @Test
    void testCombatTagOriginClaim() {
        UUID opponent = UUID.randomUUID();
        UUID claimOwner = UUID.randomUUID();
        CombatTag tag = new CombatTag(opponent, "Enemy", System.currentTimeMillis() + 10000L, Optional.of(claimOwner));

        assertTrue(tag.getOriginClaimOwner().isPresent());
        assertEquals(claimOwner, tag.getOriginClaimOwner().get());
    }
}
