package com.ovidius.xorealis.duels.command;


import com.ovidius.xorealis.duels.XorealisDuels;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class LeaveQueueCommand implements CommandExecutor {

    private final XorealisDuels plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by a player.");
            return true;
        }
        Player player = (Player) sender;

        boolean wasInQueue = plugin.getQueueManager().removeAllQueues(player.getUniqueId());

        if (wasInQueue) {
            player.sendMessage("§aYou have successfully left the queue.");
            plugin.getPartyManager().getParty(player).ifPresent(party ->
                    party.broadcast("§e" + player.getName() + " has left the queue.")
            );
        } else {
            player.sendMessage("§cYou are not in a queue.");
        }

        return true;
    }

}
