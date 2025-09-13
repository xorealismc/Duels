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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Only players can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        boolean wasInQueue = plugin.getQueueManager().removePlayerFromAllQueues(player.getUniqueId());

        if(!wasInQueue){
            player.sendMessage(ChatColor.RED + "Вы успешно покинули очередь.");
        }else {
            player.sendMessage(ChatColor.RED+"Вы не находитесь в очереди.");
        }
        return true;
    }

}
