package dev.customclaims.combatlog.forge;

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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(OpacCombatLog.MOD_ID)
public final class OpacCombatLogForgeMod {

    public OpacCombatLogForgeMod() {
        PlatformHelperHolder.setPlatformHelper(new ForgePlatformHelper());
        OpacCombatLog.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) {
            return;
        }

        Optional<ServerPlayer> attackerOpt = PvPDamageResolver.resolveAttacker(event.getSource(), victim);
        attackerOpt.ifPresent(attacker -> OpacCombatLog.getManager().tag(attacker, victim));
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OpacCombatLog.getManager().handlePlayerDisconnect(player);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            OpacCombatLog.getManager().handlePlayerDeath(player);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            OpacCombatLog.getManager().tick(event.getServer());
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
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

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
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

    @SubscribeEvent
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!OpacCombatLog.getConfig().blockPortals || !OpacCombatLog.getManager().isInCombat(player)) {
            return;
        }

        event.setCanceled(true);
        CombatInteractionLock.notifyPortalBlocked(player);
    }

    @SubscribeEvent
    public void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
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

        if (!OpacCombatHook.canEnterChunk((ServerLevel) player.level(), targetChunk, tag)) {
            event.setCanceled(true);
            CombatInteractionLock.notifyEnderPearlBlocked(player);
        }
    }

    @SubscribeEvent
    public void onChorusFruitTeleport(EntityTeleportEvent.ChorusFruit event) {
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

        if (!OpacCombatHook.canEnterChunk((ServerLevel) player.level(), targetChunk, tag)) {
            event.setCanceled(true);
            CombatInteractionLock.notifyChorusFruitBlocked(player);
        }
    }
}
