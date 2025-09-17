package com.ovidius.xorealis.duels.manager;

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

    private final Map<UUID, Party> playerPartyMap = new HashMap<>();

    private final Map<UUID, Party> invites = new HashMap<>();

    public void createParty(Player leader) {
        if (playerPartyMap.containsKey(leader.getUniqueId())) {
            leader.sendMessage(ChatColor.RED + "Вы уже находитесь в пати! Пожалуйста покиньте её");
            return;
        }
        Party party = new Party(leader);
        playerPartyMap.put(leader.getUniqueId(), party);
        leader.sendMessage("Вы создали пати!");
    }

    public Optional<Party> getParty(Player player) {
        return Optional.ofNullable(playerPartyMap.get(player.getUniqueId()));
    }

    public void inviteParty(Player leader, Player target) {
        Optional<Party> partyOpt = getParty(leader);

        if (partyOpt.isEmpty()) {
            leader.sendMessage(ChatColor.RED + "Только лидер пати может приглашать игроков");
            return;
        }
        Party party = partyOpt.get();

        if (leader.equals(target)) {
            leader.sendMessage(ChatColor.RED + "Вы не можете пригласить самого себя");
            return;
        }

        if (getParty(target).isPresent()) {
            leader.sendMessage(ChatColor.RED + "Игрок " + target.getName() + " вы уже состоите в пати");
            return;
        }
        invites.put(target.getUniqueId(), party);
        party.addInvite(target);

        leader.sendMessage(ChatColor.RED + "Вы пригласили " + target.getName() + " в пати");

        TextComponent message = new TextComponent(ChatColor.YELLOW + "----------------------------------\\n");
        TextComponent inviteBody = new TextComponent(ChatColor.GREEN + "" + leader.getName() + " пригласил вас в пати! ");
        TextComponent acceptButton = new TextComponent(ChatColor.AQUA + "[ПРИНЯТЬ]");
        acceptButton.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + leader.getName()));
        inviteBody.addExtra(acceptButton);
        message.addExtra(inviteBody);
        message.addExtra("\n" + ChatColor.YELLOW + "----------------------------------");

        target.spigot().sendMessage(message);
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

        party.removeInvite(player);
        invites.remove(player.getUniqueId());
        party.addMember(player);
        playerPartyMap.put(player.getUniqueId(), party);

        party.broadcast(ChatColor.RED + "Игрок " + player.getName() + " присоединился к пати!");
    }

    public void disbandParty(Party party) {
        party.broadcast("§cПати была распущена лидером.");
        for (UUID memberUUID : party.getMembers()) {
            playerPartyMap.remove(memberUUID);
            invites.remove(memberUUID);
        }
    }

    public void leaveParty(Player player) {
        Optional<Party> partyOpt = getParty(player);
        if (partyOpt.isEmpty()) {
            player.sendMessage("§cВы не состоите в пати.");
            return;
        }
        Party party = partyOpt.get();
        if (party.isLeader(player)) {
            disbandParty(party);
        } else {
            party.removeMember(player);
            playerPartyMap.remove(player.getUniqueId());
            player.sendMessage("§eВы покинули пати.");
            party.broadcast("§eИгрок " + player.getName() + " покинул пати.");
        }
    }
    public void kickPlayer(Player leader, Player target) {
        Optional<Party> partyOpt = getParty(leader);

        if(partyOpt.isEmpty()||!partyOpt.get().isLeader(target)) {
            leader.sendMessage(ChatColor.RED+"Только лидер пати может исключать игроков");
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
