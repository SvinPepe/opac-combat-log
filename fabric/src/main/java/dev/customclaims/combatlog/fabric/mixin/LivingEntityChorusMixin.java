package dev.customclaims.combatlog.fabric.mixin;

import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import dev.customclaims.combatlog.service.CombatInteractionLock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityChorusMixin {
    @Inject(method = "randomTeleport", at = @At("HEAD"), cancellable = true, require = 0)
    private void onRandomTeleport(double targetX, double targetY, double targetZ, boolean broadcastTeleport, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof ServerPlayer player) || OpacCombatLog.getManager().hasBypass(player)) {
            return;
        }
        if (!OpacCombatLog.getConfig().blockChorusFruitIntoClaims) {
            return;
        }
        CombatTag tag = OpacCombatLog.getManager().getTag(player.getUUID());
        if (tag == null || tag.isExpired()) {
            return;
        }
        ChunkPos targetChunk = new ChunkPos((int) Math.floor(targetX) >> 4, (int) Math.floor(targetZ) >> 4);
        if (!OpacCombatHook.canEnterChunk((ServerLevel) player.level(), targetChunk, tag)) {
            cir.setReturnValue(false);
            CombatInteractionLock.notifyChorusFruitBlocked(player);
        }
    }
}
