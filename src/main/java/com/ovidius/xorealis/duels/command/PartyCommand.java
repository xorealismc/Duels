package com.ovidius.xorealis.duels.command;

import com.ovidius.xorealis.duels.XorealisDuels;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Эту команду может выполнять только игрок.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "Используйте: /party create");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                plugin.getPartyManager().createParty(player);
                break;
            case "invite":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /party invite <игрок>");
                    break;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ChatColor.RED + "Игрок не найден");
                    break;
                }
                plugin.getPartyManager().inviteParty(player, target);
                break;
            case "accept":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Использование: /party accept <лидер>");
                    break;
                }
                Player leader = Bukkit.getPlayer(args[1]);
                if (leader == null) {
                    player.sendMessage(ChatColor.RED + "Игрок не найден");
                    break;
                }
                plugin.getPartyManager().acceptInvite(player, leader);
                break;
            case "leave":
                plugin.getPartyManager().leaveParty(player);
                break;
            case "disband":
                plugin.getPartyManager().leaveParty(player);
                break;
            case "kick":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /party kick <игрок>");
                    break;
                }
                Player targetToKick = Bukkit.getPlayer(args[1]);
                if (targetToKick == null) {
                    player.sendMessage("§cИгрок не найден.");
                    break;
                }
                plugin.getPartyManager().kickPlayer(player, targetToKick);
                break;

            case "list":
                plugin.getPartyManager().getParty(player).ifPresentOrElse(
                        party -> {
                            player.sendMessage("§e--- Участники пати ---");
                            party.getOnlineMembers().forEach(member -> {
                                if (party.isLeader(member)) {
                                    player.sendMessage("§a- " + member.getName() + " (Лидер)");
                                } else {
                                    player.sendMessage("§7- " + member.getName());
                                }
                            });
                            player.sendMessage("§e--------------------");
                        },
                        () -> player.sendMessage("§cВы не состоите в пати.")
                );
                break;

            default:
                player.sendMessage("§cНеизвестная подкоманда.");
                break;
        }

        return true;
    }

}
