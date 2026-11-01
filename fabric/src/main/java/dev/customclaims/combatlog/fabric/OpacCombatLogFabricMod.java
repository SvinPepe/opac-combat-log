package dev.customclaims.combatlog.fabric;

import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.platform.PlatformHelperHolder;
import dev.customclaims.combatlog.service.CombatInteractionLock;
import dev.customclaims.combatlog.service.PvPDamageResolver;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

public final class OpacCombatLogFabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        PlatformHelperHolder.setPlatformHelper(new FabricPlatformHelper());
        OpacCombatLog.init();

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer victim && amount > 0.0f) {
                Optional<ServerPlayer> attackerOpt = PvPDamageResolver.resolveAttacker(source, victim);
                attackerOpt.ifPresent(attacker -> OpacCombatLog.getManager().tag(attacker, victim));
            }
            return true;
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player) {
                OpacCombatLog.getManager().handlePlayerDeath(player);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            OpacCombatLog.getManager().handlePlayerDisconnect(handler.getPlayer());
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            OpacCombatLog.getManager().tick(server);
        });

        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                if (OpacCombatLog.getConfig().blockContainers && OpacCombatLog.getManager().isInCombat(serverPlayer)) {
                    BlockState state = level.getBlockState(hitResult.getBlockPos());
                    if (CombatInteractionLock.isContainerBlock(state)) {
                        CombatInteractionLock.notifyContainerBlocked(serverPlayer);
                        return InteractionResult.FAIL;
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }
}
