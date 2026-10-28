package dev.customclaims.combatlog.service;

import dev.customclaims.combatlog.config.CombatLogConfig;
import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ChunkPos;

public final class CombatManager {
    private final CombatLogConfig config;
    private final Map<UUID, CombatTag> activeTags = new ConcurrentHashMap<>();
    private final ClaimBoundaryService claimBoundaryService = new ClaimBoundaryService();
    private long tickCounter = 0L;

    public CombatManager(CombatLogConfig config) {
        this.config = config;
    }

    public CombatLogConfig getConfig() {
        return config;
    }

    public ClaimBoundaryService getClaimBoundaryService() {
        return claimBoundaryService;
    }

    public boolean isInCombat(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        CombatTag tag = getTag(player.getUUID());
        return tag != null && !tag.isExpired();
    }

    public CombatTag getTag(UUID playerUuid) {
        CombatTag tag = activeTags.get(playerUuid);
        if (tag != null && tag.isExpired()) {
            untag(playerUuid);
            return null;
        }
        return tag;
    }

    public void tag(ServerPlayer player1, ServerPlayer player2) {
        if (player1 == null || player2 == null || player1.getUUID().equals(player2.getUUID())) {
            return;
        }

        long now = System.currentTimeMillis();
        long expiry = now + (config.combatDurationSeconds * 1000L);

        ServerLevel level = player1.serverLevel();
        ChunkPos combatChunk = player1.chunkPosition();
        Optional<UUID> originClaim = OpacCombatHook.getClaimOwner(level, combatChunk);

        tagPlayer(player1, player2, expiry, originClaim);
        tagPlayer(player2, player1, expiry, originClaim);
    }

    private void tagPlayer(ServerPlayer player, ServerPlayer opponent, long expiry, Optional<UUID> originClaim) {
        UUID uuid = player.getUUID();
        CombatTag existing = activeTags.get(uuid);

        if (existing == null || existing.isExpired()) {
            activeTags.put(uuid, new CombatTag(opponent.getUUID(), opponent.getScoreboardName(), expiry, originClaim));
            sendActionbar(player, Component.literal("⚔ In Combat with " + opponent.getScoreboardName() + "! (" + config.combatDurationSeconds + "s)")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        } else {
            existing.refreshExpiry(expiry);
        }

        claimBoundaryService.updateValidPosition(player);

        if (config.disableFlight) {
            enforceNoFlight(player);
        }
    }

    public void untag(UUID playerUuid) {
        activeTags.remove(playerUuid);
        claimBoundaryService.removePlayer(playerUuid);
    }

    public void enforceNoFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }

        boolean updated = false;
        if (player.getAbilities().flying) {
            player.getAbilities().flying = false;
            updated = true;
        }
        if (player.getAbilities().mayfly) {
            player.getAbilities().mayfly = false;
            updated = true;
        }

        if (updated) {
            player.onUpdateAbilities();
        }
    }

    public void handlePlayerDeath(ServerPlayer deceased) {
        UUID deceasedUuid = deceased.getUUID();
        untag(deceasedUuid);

        if (config.untagOnEnemyDeath) {
            // If the opponent is only fighting this deceased player, untag the opponent immediately
            activeTags.forEach((uuid, tag) -> {
                if (tag.getOpponentUuid().equals(deceasedUuid)) {
                    ServerPlayer opponent = deceased.server.getPlayerList().getPlayer(uuid);
                    if (opponent != null) {
                        sendActionbar(opponent, Component.literal("⚔ Opponent defeated! You are no longer in combat.")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                    }
                    untag(uuid);
                }
            });
        }
    }

    public void handlePlayerDisconnect(ServerPlayer player) {
        UUID uuid = player.getUUID();
        CombatTag tag = activeTags.get(uuid);

        if (tag == null || tag.isExpired()) {
            untag(uuid);
            return;
        }

        MinecraftServer server = player.server;
        ServerPlayer opponent = server.getPlayerList().getPlayer(tag.getOpponentUuid());

        if (config.broadcastCombatLog) {
            String oppName = opponent != null ? opponent.getScoreboardName() : tag.getOpponentName();
            Component message = Component.literal("⚔ " + player.getScoreboardName() + " logged out during combat with " + oppName + " and was eliminated!")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            server.getPlayerList().broadcastSystemMessage(message, false);
        }

        // Deal lethal damage credited to attacker
        DamageSource killSource = opponent != null
                ? player.damageSources().playerAttack(opponent)
                : player.damageSources().genericKill();

        player.hurt(killSource, Float.MAX_VALUE);
        player.kill();

        untag(uuid);
    }

    public void tick(MinecraftServer server) {
        tickCounter++;
        boolean isSecondTick = (tickCounter % 20L == 0L);

        activeTags.forEach((uuid, tag) -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                untag(uuid);
                return;
            }

            if (tag.isExpired()) {
                sendActionbar(player, Component.literal("⚔ You are no longer in combat.")
                        .withStyle(ChatFormatting.GREEN));
                untag(uuid);
                return;
            }

            if (config.disableFlight) {
                enforceNoFlight(player);
            }

            // Boundary validation
            claimBoundaryService.validateAndEnforceBoundary(player, tag, player.getX(), player.getY(), player.getZ());

            // Actionbar timer update
            if (isSecondTick && config.actionbarNotification) {
                long sec = tag.remainingSeconds();
                Component timerMsg = Component.literal("⚔ In Combat: ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(sec + "s").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(" (Do not log out!)").withStyle(ChatFormatting.GRAY));
                sendActionbar(player, timerMsg);
            }
        });
    }

    private void sendActionbar(ServerPlayer player, Component component) {
        player.sendSystemMessage(component, true);
    }
}
