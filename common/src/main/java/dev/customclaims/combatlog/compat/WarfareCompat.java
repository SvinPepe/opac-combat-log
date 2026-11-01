package dev.customclaims.combatlog.compat;

import java.lang.reflect.Method;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Soft reflection bridge for Open Parties and Claims: Warfare.
 * Delegates to dev.customclaims.api.war.WarfareBridgeHolder when available.
 */
public final class WarfareCompat {
    private static final Logger LOGGER = LoggerFactory.getLogger("OPACCombatLog-WarfareCompat");
    private static volatile Boolean warfareAvailable = null;
    private static volatile Method checkMethod = null;

    private WarfareCompat() {
    }

    public static boolean isContestedWarChunk(ServerLevel level, ChunkPos chunkPos) {
        if (level == null || chunkPos == null) {
            return false;
        }

        if (warfareAvailable == null) {
            initBridge();
        }

        if (Boolean.TRUE.equals(warfareAvailable) && checkMethod != null) {
            try {
                return (boolean) checkMethod.invoke(null, level, chunkPos);
            } catch (Throwable t) {
                return false;
            }
        }
        return false;
    }

    public static boolean isWarfareLoaded() {
        if (warfareAvailable == null) {
            initBridge();
        }
        return Boolean.TRUE.equals(warfareAvailable);
    }

    private static synchronized void initBridge() {
        if (warfareAvailable != null) {
            return;
        }
        try {
            Class<?> holderClass = Class.forName("dev.customclaims.api.war.WarfareBridgeHolder");
            checkMethod = holderClass.getMethod("isContestedWarChunk", ServerLevel.class, ChunkPos.class);
            warfareAvailable = true;
            LOGGER.info("OPAC Warfare integration bridge initialized successfully via dev.customclaims.api.war.WarfareBridgeHolder");
        } catch (Throwable t) {
            warfareAvailable = false;
            LOGGER.debug("OPAC Warfare not detected on classpath: {}", t.getMessage());
        }
    }

    // Testing / mock hook
    public static void setMockBridge(Boolean available, Method method) {
        warfareAvailable = available;
        checkMethod = method;
    }

    public static void resetBridge() {
        warfareAvailable = null;
        checkMethod = null;
    }
}
