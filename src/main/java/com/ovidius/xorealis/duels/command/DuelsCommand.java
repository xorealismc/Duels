package com.ovidius.xorealis.duels.command;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.ArenaState;
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
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "XorealisDuels v1.0 by Ovidius. Иcпользуйте /duels reload для перезагрузки");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
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
            if (!sender.hasPermission("xorealis.duels.forcestop")) {
                sender.sendMessage(ChatColor.RED + "У вас нет прав!");
                return true;
            }
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
            if (!sender.hasPermission("xorealis.duels.forcestart")) {
                sender.sendMessage("§cУ вас нет прав.");
                return true;
            }
            if (args.length < 5) {
                sender.sendMessage("§cИспользование: /duels forcestart <игрок1> <игрок2> <кит> <арена>");
                return true;
            }

            Player p1 = Bukkit.getPlayer(args[1]);
            if (p1 == null) {
                sender.sendMessage("§cОшибка: Игрок '" + args[1] + "' не найден на сервере.");
                return true;
            }

            Player p2 = Bukkit.getPlayer(args[2]);
            if (p2 == null) {
                sender.sendMessage("§cОшибка: Игрок '" + args[2] + "' не найден на сервере.");
                return true;
            }

            Optional<Kit> kitOpt = plugin.getKitManager().getKitTemplate(args[3]);
            if (kitOpt.isEmpty()) {
                sender.sendMessage("§cОшибка: Кит с ID '" + args[3] + "' не найден в kits.yml.");
                return true;
            }

            Optional<Arena> arenaOpt = plugin.getArenaManager().getAllArenas().stream()
                    .filter(a -> a.getId().equalsIgnoreCase(args[4]))
                    .findFirst();
            if (arenaOpt.isEmpty()) {
                sender.sendMessage("§cОшибка: Арена с ID '" + args[4] + "' не найдена или не загружена.");
                return true;
            }

            if (arenaOpt.get().getState() != ArenaState.AVAILABLE) {
                sender.sendMessage("§cОшибка: Арена '" + arenaOpt.get().getId() + "' в данный момент занята.");
                return true;
            }

            if (plugin.getDuelManager().isPlayerInDuel(p1) || plugin.getDuelManager().isPlayerInDuel(p2)) {
                sender.sendMessage("§cОшибка: Один из игроков уже находится в дуэли.");
                return true;
            }
            plugin.getDuelManager().startDuel(p1, p2, arenaOpt.get(), kitOpt.get());
            sender.sendMessage("§aПринудительный запуск дуэли между " + p1.getName() + " и " + p2.getName() + "...");
            return true;
        }

        sender.sendMessage("§cНеизвестная команда. Доступно: reload, forcestop, forcestart");
        return true;
    }
}
