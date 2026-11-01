package dev.customclaims.combatlog.fabric.mixin;

import com.mojang.brigadier.ParseResults;
import dev.customclaims.combatlog.OpacCombatLog;
import dev.customclaims.combatlog.command.CombatCommandFilter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(method = "performCommand", at = @At("HEAD"), cancellable = true, require = 0)
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfoReturnable<Integer> cir) {
        if (parseResults == null || parseResults.getContext() == null || parseResults.getContext().getSource() == null) {
            return;
        }
        if (!(parseResults.getContext().getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!OpacCombatLog.getManager().isInCombat(player)) {
            return;
        }
        if (CombatCommandFilter.isCommandBlocked(command, OpacCombatLog.getConfig().blockedCommands)) {
            cir.setReturnValue(0);
            CombatCommandFilter.notifyCommandBlocked(player, command);
        }
    }
}
