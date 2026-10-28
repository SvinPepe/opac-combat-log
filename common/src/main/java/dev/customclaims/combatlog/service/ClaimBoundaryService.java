package dev.customclaims.combatlog.service;

import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class ClaimBoundaryService {
    private final Map<UUID, Vec3> lastValidPositions = new ConcurrentHashMap<>();

    public void updateValidPosition(ServerPlayer player) {
        lastValidPositions.put(player.getUUID(), player.position());
    }

    public void removePlayer(UUID playerUuid) {
        lastValidPositions.remove(playerUuid);
    }

    public boolean validateAndEnforceBoundary(ServerPlayer player, CombatTag tag, double targetX, double targetY, double targetZ) {
        if (tag == null || tag.isExpired()) {
            updateValidPosition(player);
            return true;
        }

        ServerLevel level = player.serverLevel();
        ChunkPos targetChunk = new ChunkPos((int) Math.floor(targetX) >> 4, (int) Math.floor(targetZ) >> 4);

        if (OpacCombatHook.canEnterChunk(level, targetChunk, tag)) {
            updateValidPosition(player);
            return true;
        }

        // Breach detected! Rollback to last valid position
        enforceRollback(player, targetX, targetY, targetZ);
        return false;
    }

    private void enforceRollback(ServerPlayer player, double targetX, double targetY, double targetZ) {
        Vec3 lastValid = lastValidPositions.get(player.getUUID());
        if (lastValid == null) {
            lastValid = player.position();
        }

        // Reset movement speed
        player.setDeltaMovement(Vec3.ZERO);

        Entity vehicle = player.getVehicle();
        if (vehicle != null) {
            vehicle.setDeltaMovement(Vec3.ZERO);
            vehicle.teleportTo(lastValid.x, lastValid.y, lastValid.z);
        }

        if (player.connection != null) {
            player.connection.teleport(lastValid.x, lastValid.y, lastValid.z, player.getYRot(), player.getXRot());
        } else {
            player.teleportTo(lastValid.x, lastValid.y, lastValid.z);
        }

        // Apply a small knockback vector away from target chunk
        Vec3 pushback = lastValid.subtract(targetX, targetY, targetZ).normalize().scale(0.3);
        if (pushback.lengthSqr() > 0.001) {
            player.push(pushback.x, 0.1, pushback.z);
            player.hurtMarked = true;
        }

        ServerLevel level = player.serverLevel();
        level.playSound(
                null,
                lastValid.x, lastValid.y, lastValid.z,
                SoundEvents.SHIELD_BLOCK,
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );

        level.sendParticles(
                ParticleTypes.ENCHANTED_HIT,
                lastValid.x, lastValid.y + 1.0, lastValid.z,
                15,
                0.3, 0.5, 0.3,
                0.1
        );

        player.sendSystemMessage(
                Component.literal("⚔ You cannot enter claims while in combat!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                true
        );
    }
}
