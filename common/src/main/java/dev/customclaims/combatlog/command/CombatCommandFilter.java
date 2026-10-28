package dev.customclaims.combatlog.command;

import java.util.Collection;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CombatCommandFilter {

    private CombatCommandFilter() {
    }

    public static String extractRootCommand(String commandLine) {
        if (commandLine == null || commandLine.isBlank()) {
            return "";
        }

        String trimmed = commandLine.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }

        String[] parts = trimmed.split("\\s+", 2);
        String root = parts[0].toLowerCase(Locale.ROOT);

        if (root.contains(":")) {
            root = root.substring(root.indexOf(':') + 1);
        }

        return root;
    }

    public static boolean isCommandBlocked(String commandLine, Collection<String> blockedList) {
        String root = extractRootCommand(commandLine);
        if (root.isEmpty() || blockedList == null) {
            return false;
        }

        for (String blocked : blockedList) {
            if (root.equalsIgnoreCase(blocked)) {
                return true;
            }
        }

        return false;
    }

    public static void notifyCommandBlocked(ServerPlayer player, String commandLine) {
        String root = extractRootCommand(commandLine);
        player.sendSystemMessage(Component.literal("⚔ You cannot use /" + root + " while in combat!")
                .withStyle(ChatFormatting.RED));
    }
}
