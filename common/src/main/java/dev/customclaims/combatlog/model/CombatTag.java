package dev.customclaims.combatlog.model;

import java.util.Optional;
import java.util.UUID;

public final class CombatTag {
    private final UUID opponentUuid;
    private final String opponentName;
    private volatile long expiresAtMillis;
    private final UUID originClaimOwner;

    public CombatTag(UUID opponentUuid, String opponentName, long expiresAtMillis, Optional<UUID> originClaimOwner) {
        this(opponentUuid, opponentName, expiresAtMillis, originClaimOwner != null ? originClaimOwner.orElse(null) : null);
    }

    public CombatTag(UUID opponentUuid, String opponentName, long expiresAtMillis, UUID originClaimOwner) {
        this.opponentUuid = opponentUuid;
        this.opponentName = opponentName;
        this.expiresAtMillis = expiresAtMillis;
        this.originClaimOwner = originClaimOwner;
    }

    public UUID getOpponentUuid() {
        return opponentUuid;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public long getExpiresAtMillis() {
        return expiresAtMillis;
    }

    public void refreshExpiry(long newExpiresAtMillis) {
        this.expiresAtMillis = newExpiresAtMillis;
    }

    public Optional<UUID> getOriginClaimOwner() {
        return Optional.ofNullable(originClaimOwner);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAtMillis;
    }

    public long remainingSeconds() {
        long remaining = (expiresAtMillis - System.currentTimeMillis() + 999L) / 1000L;
        return Math.max(0L, remaining);
    }
}
