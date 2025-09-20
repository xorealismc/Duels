package com.ovidius.xorealis.duels.command;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.manager.PartyManager;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PartyCommand implements CommandExecutor, TabCompleter {

    private final XorealisDuels plugin;
    private final List<String> subCommands = List.of("create", "invite", "accept", "leave", "kick", "disband", "list", "promote");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;
        PartyManager partyManager = plugin.getPartyManager();

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("help")) {
            sendHelpMessage(player);
            return true;
        }

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
                plugin.getPartyManager().invitePlayer(player, target);
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
                partyManager.getParty(player).ifPresentOrElse(
                        party -> {
                            if (party.isLeader(player)) {
                                partyManager.disbandParty(party);
                            } else {
                                player.sendMessage("§cТолько лидер может распустить пати.");
                            }
                        },
                        () -> player.sendMessage("§cВы не состоите в пати.")
                );
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
            case "promote":
            case "leader":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /party promote <игрок>");
                    break;
                }
                Player newLeader = Bukkit.getPlayer(args[1]);
                if (newLeader == null) {
                    player.sendMessage("§cИгрок не найден.");
                    break;
                }
                plugin.getPartyManager().promotePlayer(player, newLeader);
                break;


            case "list":
                partyManager.getParty(player).ifPresentOrElse(
                        party -> {
                            player.sendMessage("§e--- Участники пати (" + party.getSize() + ") ---");
                            party.getOnlineMembers().forEach(member -> {
                                if (party.isLeader(member)) {
                                    player.sendMessage("§a- " + member.getName() + " (Лидер)");
                                } else {
                                    player.sendMessage("§7- " + member.getName());
                                }
                            });
                            player.sendMessage("§e----------------------");
                        },
                        () -> player.sendMessage("§cВы не состоите в пати.")
                );
                break;

            default:
                player.sendMessage("§cНеизвестная подкоманда. Используйте /party help для списка комманд");
                break;
        }

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("invite") || subCommand.equals("kick") || subCommand.equals("promote")) {

                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§8§m                                                                                ");
        player.sendMessage("§6§lXorealis Duels - Команды Пати");
        player.sendMessage("");
        player.sendMessage("§e/party create §7- Создать свою пати.");
        player.sendMessage("§e/party invite <игрок> §7- Пригласить игрока в пати.");
        player.sendMessage("§e/party accept <игрок> §7- Принять приглашение от игрока.");
        player.sendMessage("§e/party leave §7- Покинуть текущую пати.");
        player.sendMessage("§e/party kick <игрок> §7- (Лидер) Исключить игрока из пати.");
        player.sendMessage("§e/party disband §7- (Лидер) Распустить свою пати.");
        player.sendMessage("§e/party list §7- Показать список участников пати.");
        player.sendMessage("§8§m                                                                                ");
    }

}
