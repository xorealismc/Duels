package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.GameModeType;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.object.Party;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PartyManager {
    public static final int MAX_PARTY_SIZE = 6;
    private final Map<UUID, Party> playerPartyMap = new HashMap<>();
    private final Map<UUID, Party> invites = new HashMap<>();
    private final XorealisDuels plugin;

    public void attemptToQueue(Player player, Kit kit, GameModeType mode) {
        Optional<Party> partyOpt = getParty(player);

        if (mode == GameModeType.SOLO) {
            if (partyOpt.isPresent()) {
                player.sendMessage("§cДля поиска одиночной игры, пожалуйста, покиньте пати.");
                player.closeInventory();
                return;
            }
            plugin.getQueueManager().addPlayerToQueue(player, kit, mode);

        } else {
            if (partyOpt.isEmpty()) {
                player.sendMessage("§cДля этого режима требуется пати.");
                player.closeInventory();
                return;
            }
            Party party = partyOpt.get();
            if (!party.isLeader(player)) {
                player.sendMessage("§cТолько лидер пати может начать поиск игры.");
                player.closeInventory();
                return;
            }
            if (party.getSize() != mode.getTeamSize()) {
                player.sendMessage("§cРазмер вашей пати ("+party.getSize()+") не соответствует выбранному режиму ("+mode.getTeamSize()+").");
                player.closeInventory();
                return;
            }
            plugin.getQueueManager().addPlayerToQueue(player, kit, mode);
        }
    }

    public void createParty(Player leader) {
        if (playerPartyMap.containsKey(leader.getUniqueId())) {
            leader.sendMessage("§cВы уже в пати.");
            return;
        }
        Party party = new Party(leader);
        playerPartyMap.put(leader.getUniqueId(), party);
        leader.sendMessage("§aВы создали пати.");
    }

    public Optional<Party> getParty(Player player) {
        if (player == null) return Optional.empty();
        return Optional.ofNullable(playerPartyMap.get(player.getUniqueId()));
    }

    public Optional<Party> getParty(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(playerPartyMap.get(uuid));
    }
    public void invitePlayer(Player leader, Player target) {
        Optional<Party> partyOpt = getParty(leader);
        if (partyOpt.isEmpty()) {
            createParty(leader);
            partyOpt = getParty(leader);
        }
        Party party = partyOpt.get();

        if (leader.equals(target)) {
            leader.sendMessage("§cВы не можете пригласить самого себя.");
            return;
        }

        if (getParty(target).isPresent()) {
            leader.sendMessage("§cИгрок " + target.getName() + " уже состоит в пати.");
            return;
        }
        invites.put(target.getUniqueId(), party);
        party.addInvite(target);

        leader.sendMessage("§aВы пригласили " + target.getName() + " в пати.");

        TextComponent message = new TextComponent("§e----------------------------------\n");
        TextComponent inviteBody = new TextComponent("§a" + leader.getName() + " пригласил вас в пати! ");
        TextComponent acceptButton = new TextComponent("§b[ПРИНЯТЬ]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + leader.getName()));
        inviteBody.addExtra(acceptButton);
        message.addExtra(inviteBody);
        message.addExtra("\n§e----------------------------------");
        target.spigot().sendMessage(message);
    }
    public void promotePlayer(Player currentLeader, Player newLeader) {
        getParty(currentLeader).ifPresent(party -> {
            if (!party.isLeader(currentLeader)) {
                currentLeader.sendMessage("§cВы не лидер этой пати.");
                return;
            }
            if (!party.isMember(newLeader)) {
                currentLeader.sendMessage("§cИгрок " + newLeader.getName() + " не состоит в вашей пати.");
                return;
            }
            if (currentLeader.equals(newLeader)) {
                currentLeader.sendMessage("§cВы уже являетесь лидером.");
                return;
            }

            party.setLeader(newLeader.getUniqueId());
            party.broadcast("§eИгрок " + newLeader.getName() + " был назначен новым лидером пати!");
        });
    }

    public void acceptInvite(Player player, Player leader) {
        Party party = invites.get(player.getUniqueId());

        if (party == null || !party.isLeader(leader)) {
            player.sendMessage(ChatColor.RED + "Приглашение не найдено или истекло");
            return;
        }

        if (!party.hasInvite(player)) {
            player.sendMessage(ChatColor.RED + "Приглашение не найдено или истекло");
            invites.remove(player.getUniqueId());
            return;
        }

        if(party.getSize()>= MAX_PARTY_SIZE){
            player.sendMessage(ChatColor.RED+"Не удалось присоедениться. Пати "+leader.getName()+
                    "уже заполнена");
            leader.sendMessage(ChatColor.RED+"Игрок "+player.getName()+
                    "не смог присоединиться, так как ваша пати заполнена");
            invites.remove(player.getUniqueId());
            party.removeInvite(player);
            return;
        }
        party.removeInvite(player);
        invites.remove(player.getUniqueId());
        party.addMember(player);
        playerPartyMap.put(player.getUniqueId(), party);

        party.broadcast(ChatColor.GREEN+"Игрок "+player.getName()+" присоединился к пати! ("+ChatColor.YELLOW+party.getSize()+"/"+ChatColor.BOLD+ChatColor.WHITE+MAX_PARTY_SIZE+ChatColor.GREEN+")");
    }

    public void disbandParty(Party party) {
        party.broadcast("§cПати была распущена.");
        for (UUID memberUUID : party.getMembers()) {
            playerPartyMap.remove(memberUUID);
            invites.remove(memberUUID);
        }
    }


    public void leaveParty(Player player) {
        getParty(player).ifPresent(party -> {
            if (party.isLeader(player)) {
                disbandParty(party);
            } else {
                party.removeMember(player);
                playerPartyMap.remove(player.getUniqueId());
                player.sendMessage("§eВы покинули пати.");
                party.broadcast("§eИгрок " + player.getName() + " покинул пати.");
            }
        });
    }
    public void kickPlayer(Player leader, Player target) {
        Optional<Party> partyOpt = getParty(leader);

        if(partyOpt.isEmpty() || !partyOpt.get().isLeader(leader)) {
            leader.sendMessage("§cТолько лидер пати может исключать игроков.");
            return;
        }
        Party party = partyOpt.get();

        if(leader.equals(target)) {
            leader.sendMessage(ChatColor.RED+"Вы не можете исключить самого себя. Используйте /party leave");
            return;
        }

        if(!party.isMember(target)) {
            leader.sendMessage(ChatColor.RED+"Игрок "+target.getName()+" не состоит в вашей пати");
            return;
        }
        party.removeMember(target);
        playerPartyMap.remove(target.getUniqueId());

        party.broadcast("§eИгрок " + target.getName() + " был исключен из пати.");
        target.sendMessage("§cВы были исключены из пати.");
    }

}
