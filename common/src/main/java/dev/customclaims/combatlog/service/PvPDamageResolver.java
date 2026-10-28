package dev.customclaims.combatlog.service;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;

public final class PvPDamageResolver {

    private PvPDamageResolver() {
    }

    public static Optional<ServerPlayer> resolveAttacker(DamageSource source, ServerPlayer victim) {
        Entity direct = source.getDirectEntity();
        Entity causing = source.getEntity();

        ServerPlayer attacker = null;

        // 1. Direct player attack
        if (causing instanceof ServerPlayer player) {
            attacker = player;
        }
        // 2. Projectiles (Arrows, Tridents, Wind Charges)
        else if (direct instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            attacker = player;
        }
        // 3. Lingering potions / Area Effect Clouds
        else if (direct instanceof AreaEffectCloud cloud && cloud.getOwner() instanceof ServerPlayer player) {
            attacker = player;
        }
        // 4. Thrown potions
        else if (direct instanceof ThrownPotion potion && potion.getOwner() instanceof ServerPlayer player) {
            attacker = player;
        }
        // 5. Tamed pets (Wolves, etc.)
        else if (causing instanceof TamableAnimal pet && pet.getOwner() instanceof ServerPlayer player) {
            attacker = player;
        }

        // Self-damage filter: Player cannot combat-tag themselves
        if (attacker == null || attacker.getUUID().equals(victim.getUUID())) {
            return Optional.empty();
        }

        return Optional.of(attacker);
    }
}
