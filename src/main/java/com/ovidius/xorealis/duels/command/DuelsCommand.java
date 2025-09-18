package com.ovidius.xorealis.duels.command;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.object.Arena;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
        if (args[0].equalsIgnoreCase("forcestop")) {
            if (!sender.hasPermission("xorealis.duels.forcestop")) { /*...*/ }
            if (args.length < 2) {
                sender.sendMessage("§cИспользование: /duels forcestop <игрок>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !plugin.getDuelManager().isPlayerInDuel(target)) {
                sender.sendMessage("§cИгрок не в дуэли или не найден.");
                return true;
            }

            plugin.getDuelManager().forceStopDuel(target, sender.getName());
            sender.sendMessage("§aДуэль для игрока " + target.getName() + " была остановлена.");
            return true;
        }

        if (args[0].equalsIgnoreCase("forcestart")) {
            if (!sender.hasPermission("xorealis.duels.forcestart")) { /*...*/ }
            if (args.length < 5) {
                sender.sendMessage("§cИспользование: /duels forcestart <игрок1> <игрок2> <кит> <арена>");
                return true;
            }

            Player p1 = Bukkit.getPlayer(args[1]);
            Player p2 = Bukkit.getPlayer(args[2]);
            Optional<Kit> kitOpt = plugin.getKitManager().getKitTemplate(args[3]);
            Optional<Arena> arenaOpt = plugin.getArenaManager().getAllArenas().stream().filter(a -> a.getId().equalsIgnoreCase(args[4])).findFirst();

            if(p1 == null || p2 == null || kitOpt.isEmpty() || arenaOpt.isEmpty()){
                sender.sendMessage("§cОшибка: Один из игроков, кит или арена не найдены.");
                return true;
            }

            plugin.getDuelManager().startDuel(p1, p2, arenaOpt.get(), kitOpt.get());
            sender.sendMessage("§aПринудительный запуск дуэли...");
            return true;
        }
        sender.sendMessage(ChatColor.RED+"Неизвестная подкоманда. Используйте /duels reload.");
        return true;
    }

}
