package dev.customclaims.combatlog.service;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EnderChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CombatInteractionLock {

    private CombatInteractionLock() {
    }

    public static boolean isContainerBlock(BlockState state) {
        Block block = state.getBlock();
        return block instanceof ChestBlock
                || block instanceof EnderChestBlock
                || block instanceof BarrelBlock
                || block instanceof ShulkerBoxBlock;
    }

    public static void notifyContainerBlocked(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("⚔ You cannot open containers while in combat!")
                        .withStyle(ChatFormatting.RED),
                true
        );
    }

    public static void notifyPortalBlocked(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("⚔ You cannot use portals while in combat!")
                        .withStyle(ChatFormatting.RED),
                true
        );
    }

    public static void notifyEnderPearlBlocked(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("⚔ You cannot pearl into a claim while in combat!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                true
        );
    }

    public static void notifyChorusFruitBlocked(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal("⚔ You cannot teleport into a claim while in combat!")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                true
        );
    }
}
