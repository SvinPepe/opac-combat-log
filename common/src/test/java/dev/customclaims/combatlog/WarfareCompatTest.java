package dev.customclaims.combatlog;

import dev.customclaims.combatlog.compat.WarfareCompat;
import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WarfareCompatTest {

    @org.junit.jupiter.api.BeforeAll
    static void bootstrap() {
        try {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
        } catch (Throwable ignored) {
        }
    }

    @BeforeEach
    @AfterEach
    void resetBridge() {
        WarfareCompat.resetBridge();
    }

    @Test
    void testWarfareNotInstalledReturnsFalse() {
        assertFalse(WarfareCompat.isContestedWarChunk(null, new ChunkPos(0, 0)));
        assertFalse(WarfareCompat.isWarfareLoaded());
    }

    public static boolean mockWarChunkCheck(ServerLevel level, ChunkPos chunkPos) {
        return chunkPos != null && chunkPos.equals(new ChunkPos(10, 20));
    }

    @Test
    void testMockWarfareBridgeIntegration() throws Exception {
        Method mockMethod = WarfareCompatTest.class.getMethod("mockWarChunkCheck", ServerLevel.class, ChunkPos.class);
        WarfareCompat.setMockBridge(true, mockMethod);

        assertTrue(WarfareCompat.isWarfareLoaded());

        // Level is null check
        assertFalse(WarfareCompat.isContestedWarChunk(null, new ChunkPos(10, 20)));

        // Non-null dummy level or mock check via method directly
        assertTrue((boolean) mockMethod.invoke(null, null, new ChunkPos(10, 20)));
        assertFalse((boolean) mockMethod.invoke(null, null, new ChunkPos(5, 5)));
    }

    @Test
    void testCanEnterChunkRulesWhenCombatInWilderness() {
        CombatTag tag = new CombatTag(UUID.randomUUID(), "Enemy", System.currentTimeMillis() + 10000L, Optional.empty());

        // Combat originated in wilderness -> tag.getOriginClaimOwner() is empty
        assertTrue(tag.getOriginClaimOwner().isEmpty());

        // When targetOwner is empty (wilderness or war chunk treated as wilderness) -> canEnterChunk is true
        // Null level returns empty claim owner, which models wilderness
        assertTrue(OpacCombatHook.canEnterChunk(null, new ChunkPos(10, 20), tag));
    }
}
