package dev.customclaims.combatlog.fabric.mixin;

import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import dev.customclaims.combatlog.service.CombatInteractionLock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public class ThrownEnderpearlMixin {
    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true, require = 0)
    private void onHit(HitResult hitResult, CallbackInfo ci) {
        ThrownEnderpearl pearl = (ThrownEnderpearl) (Object) this;
        Entity owner = pearl.getOwner();
        if (!(owner instanceof ServerPlayer player) || OpacCombatLog.getManager().hasBypass(player)) {
            return;
        }
        if (!OpacCombatLog.getConfig().blockEnderPearlsIntoClaims) {
            return;
        }
        CombatTag tag = OpacCombatLog.getManager().getTag(player.getUUID());
        if (tag == null || tag.isExpired()) {
            return;
        }
        ChunkPos targetChunk = new ChunkPos((int) Math.floor(hitResult.getLocation().x) >> 4, (int) Math.floor(hitResult.getLocation().z) >> 4);
        if (!OpacCombatHook.canEnterChunk((ServerLevel) pearl.level(), targetChunk, tag)) {
            ci.cancel();
            pearl.discard();
            CombatInteractionLock.notifyEnderPearlBlocked(player);
        }
    }
}
