package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.GameModeType;
import com.ovidius.xorealis.duels.object.Kit;
import com.ovidius.xorealis.duels.object.Party;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class QueueManager {
    private final XorealisDuels plugin;
    private final Map<String, Queue<UUID>> soloQueuePool = new ConcurrentHashMap<>();
    private final Map<String, Queue<UUID>> duoQueuePool = new ConcurrentHashMap<>();
    private final Map<String, Queue<UUID>> trioQueuePool = new ConcurrentHashMap<>();

    public void addPlayerToQueue(Player player, Kit kit, GameModeType mode) {
        player.closeInventory();
        Optional<Party> partyOpt = plugin.getPartyManager().getParty(player);

        if (mode == GameModeType.SOLO) {
            if (partyOpt.isPresent() && partyOpt.get().getSize() == 2) {
                handlePrivatePartyDuel(player, partyOpt.get(), kit);
            } else {
                handleSoloPublicQueue(player, kit);
            }
            return;
        }

        List<Player> playersToQueue = new ArrayList<>();

        if (partyOpt.isPresent()) {
            Party party = partyOpt.get();
            if (!party.isLeader(player)) {
                player.sendMessage("§cТолько лидер пати может начать поиск игры.");
                return;
            }
            playersToQueue.addAll(party.getOnlineMembers().stream().limit(mode.getTeamSize()).collect(Collectors.toList()));
        } else {
            playersToQueue.add(player);
        }

        handleTeamPublicQueue(playersToQueue, kit, mode);
    }
    private void handleSoloPublicQueue(Player player, Kit kit) {
        UUID uuid = player.getUniqueId();
        removeAllQueues(uuid);
        Queue<UUID> queue = getQueuePoolForMode(GameModeType.SOLO).computeIfAbsent(kit.getId(), k -> new LinkedBlockingQueue<>());

        if (queue.contains(uuid)) {
            player.sendMessage("§eВы уже в этой очереди.");
        } else {
            queue.add(uuid);
            player.sendMessage("§aВы встали в очередь 1 на 1.");
            checkForMatches(kit, GameModeType.SOLO);
        }
    }

    private void handlePrivatePartyDuel(Player player, Party party, Kit kit) {
        if (!party.isLeader(player)) {
            player.sendMessage("§cТолько лидер пати может начать приватную дуэль.");
            return;
        }
        if (party.getSize() != 2) {
            player.sendMessage("§cДля приватной дуэли в пати должно быть ровно 2 игрока.");
            return;
        }


        Player opponent = party.getOnlineMembers().stream().filter(p -> !p.equals(player)).findFirst().orElse(null);
        if (opponent == null) {
            player.sendMessage("§cВаш напарник не в сети.");
            return;
        }

        plugin.getArenaManager().findAvailableArena(1).ifPresentOrElse(
                arena -> {
                    party.broadcast("§aЛидер начал приватную дуэль!");
                    plugin.getDuelManager().startDuel(player, opponent, arena, kit);
                },
                () -> party.broadcast("§cНе нашлось свободной арены для дуэли!")
        );
    }
    private void handleTeamPublicQueue(Collection<Player> players, Kit kit, GameModeType mode){
        Map<String, Queue<UUID>> queuePool = getQueuePoolForMode(mode);
        Queue<UUID> queue = queuePool.computeIfAbsent(kit.getId(), k -> new LinkedBlockingQueue<>());

        players.forEach(p -> {
            removeAllQueues(p.getUniqueId());
            if(!queue.contains(p.getUniqueId())) {
                queue.add(p.getUniqueId());
            }
        });

        String teamComp = players.stream().map(Player::getName).collect(Collectors.joining(", "));
        players.forEach(p -> p.sendMessage("§aВаша группа ("+teamComp+") встала в очередь "+mode.name()+"."));

        checkForMatches(kit, mode);
    }

    private void handleSoloQueue(Player player, Kit kit) {
        UUID uuid = player.getUniqueId();
        removeAllQueues(uuid);
        Queue<UUID> queue = soloQueuePool.computeIfAbsent(kit.getId(), k -> new LinkedBlockingQueue<>());

        if (!queue.contains(uuid)) {
            queue.add(uuid);
            player.sendMessage("§aВы встали в очередь 1 на 1.");
            checkForMatches(kit, GameModeType.SOLO);
        } else {
            player.sendMessage("§eВы уже находитесь в этой очереди.");
        }
    }

    private void handleTeamQueue(Collection<Player> players, Kit kit, GameModeType mode){
        Map<String, Queue<UUID>> queuePool = getQueuePoolForMode(mode);
        Queue<UUID> queue = queuePool.computeIfAbsent(kit.getId(), k -> new LinkedBlockingQueue<>());

        players.forEach(p -> {
            removeAllQueues(p.getUniqueId());
            if(!queue.contains(p.getUniqueId())) {
                queue.add(p.getUniqueId());
            }
        });

        String teamComp = players.stream().map(Player::getName).collect(Collectors.joining(", "));
        players.forEach(p -> p.sendMessage("§aВаша группа ("+teamComp+") встала в очередь "+mode.name()+"."));

        checkForMatches(kit, mode);
    }

    private void checkForMatches(Kit kit, GameModeType mode) {
        Map<String, Queue<UUID>> queuePool = getQueuePoolForMode(mode);
        Queue<UUID> queue = queuePool.get(kit.getId());

        int requiredPlayers = mode.getTeamSize() * 2;

        while (queue != null && queue.size() >= requiredPlayers) {
            List<Player> participants = new ArrayList<>();
            for (int i = 0; i < requiredPlayers; i++) {
                getPlayerFromUUID(queue.poll()).ifPresent(participants::add);
            }

            if (participants.size() == requiredPlayers) {
                Party team1 = new Party(participants.get(0));
                for (int i = 1; i < mode.getTeamSize(); i++) {
                    team1.addMember(participants.get(i));
                }

                Party team2 = new Party(participants.get(mode.getTeamSize()));
                for (int i = mode.getTeamSize() + 1; i < requiredPlayers; i++) {
                    team2.addMember(participants.get(i));
                }

                plugin.getArenaManager().findAvailableArena(mode.getTeamSize()).ifPresentOrElse(
                        arena -> plugin.getDuelManager().startDuel(team1, team2, arena, kit),
                        () -> participants.forEach(p -> {
                            queue.add(p.getUniqueId());
                            p.sendMessage("§cНе нашлось свободной арены. Вы возвращены в очередь.");
                        })
                );
            } else {
                participants.forEach(p -> queue.add(p.getUniqueId()));
            }
        }
    }

    public boolean removeAllQueues(UUID uuid) {
        boolean r1 = soloQueuePool.values().stream().anyMatch(q -> q.remove(uuid));
        boolean r2 = duoQueuePool.values().stream().anyMatch(q -> q.remove(uuid));
        boolean r3 = trioQueuePool.values().stream().anyMatch(q -> q.remove(uuid));
        return r1 || r2 || r3;
    }


    private Map<String, Queue<UUID>> getQueuePoolForMode(GameModeType mode) {
        return switch (mode) {
            case SOLO -> soloQueuePool;
            case DUO -> duoQueuePool;
            case TRIO -> trioQueuePool;
        };
    }

    private Optional<Player> getPlayerFromUUID(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(Bukkit.getPlayer(uuid));
    }
}