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
public class PartyCommand implements CommandExecutor {

    private final XorealisDuels plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "Эту команду может выполнять только игрок.");
            return true;
        }
        Player player = (Player) sender;

        if(args.length == 0){
            player.sendMessage(ChatColor.YELLOW+"Используйте: /party create");
            return true;
        }
        if(args[0].equalsIgnoreCase("create")){
            plugin.getPartyManager().getParty(player);
            return true;
        }
        player.sendMessage(ChatColor.RED+"Неизвестная подкоманда");
        return true;
    }

}
