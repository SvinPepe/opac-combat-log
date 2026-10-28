package dev.customclaims.combatlog.neoforge;

import com.mojang.brigadier.ParseResults;
import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.command.CombatCommandFilter;
import dev.customclaims.combatlog.model.CombatTag;
import dev.customclaims.combatlog.opac.OpacCombatHook;
import dev.customclaims.combatlog.platform.PlatformHelperHolder;
import dev.customclaims.combatlog.service.CombatInteractionLock;
import dev.customclaims.combatlog.service.PvPDamageResolver;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(OpacCombatLog.MOD_ID)
public final class OpacCombatLogNeoForge {

    public OpacCombatLogNeoForge(IEventBus modEventBus) {
        PlatformHelperHolder.setPlatformHelper(new NeoForgePlatformHelper());
        OpacCombatLog.init();

        NeoForge.EVENT_BUS.addListener(this::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(this::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onCommand);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onEntityTravelToDimension);
        NeoForge.EVENT_BUS.addListener(this::onEnderPearlTeleport);
        NeoForge.EVENT_BUS.addListener(this::onChorusFruitTeleport);
    }

    private void onLivingDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        Optional<ServerPlayer> attackerOpt = PvPDamageResolver.resolveAttacker(event.getSource(), victim);
        attackerOpt.ifPresent(attacker -> OpacCombatLog.getManager().tag(attacker, victim));
    }

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OpacCombatLog.getManager().handlePlayerDisconnect(player);
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OpacCombatLog.getManager().handlePlayerDeath(player);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        OpacCombatLog.getManager().tick(event.getServer());
    }

    private void onCommand(CommandEvent event) {
        ParseResults<CommandSourceStack> results = event.getParseResults();
        if (results == null || results.getContext() == null || results.getContext().getSource() == null) {
            return;
        }

        if (!(results.getContext().getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!OpacCombatLog.getManager().isInCombat(player)) {
            return;
        }

        String commandLine = results.getReader().getString();
        if (CombatCommandFilter.isCommandBlocked(commandLine, OpacCombatLog.getConfig().blockedCommands)) {
            event.setCanceled(true);
            CombatCommandFilter.notifyCommandBlocked(player, commandLine);
        }
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!OpacCombatLog.getConfig().blockContainers || !OpacCombatLog.getManager().isInCombat(player)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (CombatInteractionLock.isContainerBlock(state)) {
            event.setCanceled(true);
            CombatInteractionLock.notifyContainerBlocked(player);
        }
    }

    private void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!OpacCombatLog.getConfig().blockPortals || !OpacCombatLog.getManager().isInCombat(player)) {
            return;
        }

        event.setCanceled(true);
        CombatInteractionLock.notifyPortalBlocked(player);
    }

    private void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        ServerPlayer player = event.getPlayer();
        if (player == null || OpacCombatLog.getManager().hasBypass(player)) {
            return;
        }

        if (!OpacCombatLog.getConfig().blockEnderPearlsIntoClaims) {
            return;
        }

        CombatTag tag = OpacCombatLog.getManager().getTag(player.getUUID());
        if (tag == null || tag.isExpired()) {
            return;
        }

        ChunkPos targetChunk = new ChunkPos((int) Math.floor(event.getTargetX()) >> 4, (int) Math.floor(event.getTargetZ()) >> 4);

        if (!OpacCombatHook.canEnterChunk(player.serverLevel(), targetChunk, tag)) {
            event.setCanceled(true);
            CombatInteractionLock.notifyEnderPearlBlocked(player);
        }
    }

    private void onChorusFruitTeleport(EntityTeleportEvent.ChorusFruit event) {
        LivingEntity living = event.getEntityLiving();
        if (!(living instanceof ServerPlayer player) || OpacCombatLog.getManager().hasBypass(player)) {
            return;
        }

        if (!OpacCombatLog.getConfig().blockChorusFruitIntoClaims) {
            return;
        }

        CombatTag tag = OpacCombatLog.getManager().getTag(player.getUUID());
        if (tag == null || tag.isExpired()) {
            return;
        }

        ChunkPos targetChunk = new ChunkPos((int) Math.floor(event.getTargetX()) >> 4, (int) Math.floor(event.getTargetZ()) >> 4);

        if (!OpacCombatHook.canEnterChunk(player.serverLevel(), targetChunk, tag)) {
            event.setCanceled(true);
            CombatInteractionLock.notifyChorusFruitBlocked(player);
        }
    }
}
