package com.ovidius.xorealis.duels.command;

import com.ovidius.xorealis.duels.XorealisDuels;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class DuelsCommand implements CommandExecutor {

    private final XorealisDuels plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if(args.length == 0 ){
            sender.sendMessage(ChatColor.GREEN + "XorealisDuels v1.0 by Ovidius. Иcпользуйте /duels reload для перезагрузки");
            return true;
        }
        if(args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("xorealis.duels.reload")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой комманды!");
                return true;
            }
            plugin.getKitManager().loadKits();
            plugin.getArenaManager().loadArenas();
            sender.sendMessage(ChatColor.GREEN + "Конфигурация плагина XorealisDuels была успешно перезагружена!");
            return true;
        }
        sender.sendMessage(ChatColor.RED+"Неизвестная подкоманда. Используйте /duels reload.");
        return true;
    }

}
