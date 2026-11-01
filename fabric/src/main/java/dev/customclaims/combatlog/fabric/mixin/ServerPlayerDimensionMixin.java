package dev.customclaims.combatlog.fabric.mixin;

import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.service.CombatInteractionLock;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
//? if <1.21 {
/*import net.minecraft.server.level.ServerLevel;
*///?} else {
import net.minecraft.world.level.portal.DimensionTransition;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class ServerPlayerDimensionMixin {
    //? if <1.21 {
    /*@Inject(method = "changeDimension(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true, require = 0)
    private void onChangeDimension(ServerLevel destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (OpacCombatLog.getConfig().blockPortals && OpacCombatLog.getManager().isInCombat(player)) {
            cir.setReturnValue(null);
            CombatInteractionLock.notifyPortalBlocked(player);
        }
    }
    *///?} else {
    @Inject(method = "changeDimension(Lnet/minecraft/world/level/portal/DimensionTransition;)Lnet/minecraft/world/entity/Entity;", at = @At("HEAD"), cancellable = true, require = 0)
    private void onChangeDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (OpacCombatLog.getConfig().blockPortals && OpacCombatLog.getManager().isInCombat(player)) {
            cir.setReturnValue(null);
            CombatInteractionLock.notifyPortalBlocked(player);
        }
    }
    //?}
}
