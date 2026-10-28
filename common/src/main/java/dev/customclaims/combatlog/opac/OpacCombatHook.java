package dev.customclaims.combatlog.opac;

import dev.customclaims.combatlog.model.CombatTag;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.parties.party.api.IServerPartyAPI;

public final class OpacCombatHook {
    private static final Logger LOGGER = LoggerFactory.getLogger("OPACCombatLog");

    public static Optional<UUID> getClaimOwner(ServerLevel level, ChunkPos chunkPos) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return Optional.empty();
        }

        try {
            OpenPACServerAPI api = OpenPACServerAPI.get(server);
            if (api == null || api.getServerClaimsManager() == null) {
                return Optional.empty();
            }

            IPlayerChunkClaimAPI claim = api.getServerClaimsManager().get(level.dimension().location(), chunkPos);
            if (claim == null) {
                return Optional.empty();
            }

            UUID claimPlayerId = claim.getPlayerId();
            var playerInfo = api.getServerClaimsManager().getPlayerInfo(claimPlayerId);
            if (playerInfo != null && playerInfo.isPartyOwned() && api.getPartyManager() != null) {
                IServerPartyAPI party = api.getPartyManager().getPartyByOwner(claimPlayerId);
                if (party == null) {
                    party = api.getPartyManager().getPartyByMember(claimPlayerId);
                }
                if (party != null) {
                    return Optional.of(party.getId());
                }
            }

            return Optional.of(claimPlayerId);
        } catch (Exception e) {
            LOGGER.warn("Error checking claim owner with OPAC: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public static boolean isClaimed(ServerLevel level, ChunkPos chunkPos) {
        return getClaimOwner(level, chunkPos).isPresent();
    }

    public static boolean canEnterChunk(ServerLevel level, ChunkPos targetChunk, CombatTag tag) {
        if (tag == null || tag.isExpired()) {
            return true;
        }

        Optional<UUID> targetOwner = getClaimOwner(level, targetChunk);

        // Case 1: Combat originated in the wilderness
        if (tag.getOriginClaimOwner().isEmpty()) {
            // Cannot enter any claim while in combat
            return targetOwner.isEmpty();
        }

        // Case 2: Combat originated inside a specific claim
        UUID originOwner = tag.getOriginClaimOwner().get();
        if (targetOwner.isEmpty()) {
            // Can step into wilderness
            return true;
        }

        // Can remain in or return to the origin claim, but cannot enter any other claim
        return targetOwner.get().equals(originOwner);
    }
}
