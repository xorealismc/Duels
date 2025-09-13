package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Arena;
import com.ovidius.xorealis.duels.object.Kit;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@RequiredArgsConstructor
public class QueueManager {
    private final XorealisDuels plugin;
    private final Map<String, Queue<UUID>> queues = new ConcurrentHashMap<>();

    public void addPlayerToQueue(Player player, Kit kit) {
        UUID playerUUID = player.getUniqueId();
        String kitId = kit.getId();

        removePlayerFromAllQueues(playerUUID);

        Queue<UUID> queue = queues.computeIfAbsent(kitId,k->new LinkedBlockingQueue<>());

        if(!queue.contains(playerUUID)) {
            queue.add(playerUUID);
            player.sendMessage("§aВы встали в очередь для дуэли 1v1 с китом: " + kit.getDisplayName());
            checkForMatches(kitId);
        }else {
            player.sendMessage("§eВы уже находитесь в этой очереди.");
        }
    }

    private void checkForMatches(String kitId) {
        Queue<UUID> queue = queues.get(kitId);

        plugin.getLogger().info("[DEBUG] Проверка очереди для кита: "+kitId+". Размер очереди: "
        +(queue!=null ? queue.size():0));

        while (queue != null && queue.size() >= 2) {
            Optional<Player> p1Opt = getPlayerFromUUID(queue.poll());
            Optional<Player> p2Opt = getPlayerFromUUID(queue.poll());

            if (p1Opt.isPresent() && p2Opt.isPresent()) {
                Player player1 = p1Opt.get();
                Player player2 = p2Opt.get();

                plugin.getLogger().info("[QUEUE] Найдены игроки: " + player1.getName() + " и " + player2.getName() + ". Поиск арены...");

                plugin.getArenaManager().findAvailableArena().ifPresentOrElse(
                        arena -> {
                            plugin.getLogger().info("[QUEUE] Арена найдена: " + arena.getId() + ". Запуск дуэли...");
                            // Убедимся, что кит существует, прежде чем начинать дуэль
                            plugin.getKitManager().getKitTemplate(kitId).ifPresent(kit -> {
                                plugin.getDuelManager().startDuel(player1, player2, arena, kit);
                            });
                        },
                        () -> {
                            plugin.getLogger().severe("[QUEUE] Свободных арен не найдено! Игроки возвращены в очередь.");
                            player1.sendMessage("§cНе нашлось свободной арены. Вы возвращены в начало очереди.");
                            player2.sendMessage("§cНе нашлось свободной арены. Вы возвращены в начало очереди.");

                            queue.add(player1.getUniqueId());
                            queue.add(player2.getUniqueId());
                        }
                );

            } else {
                plugin.getLogger().warning("[QUEUE] Один из игроков в паре вышел из сети. Поиск продолжается...");
            }
        }
    }
    public void removePlayerFromAllQueues(UUID playerUUID) {
        queues.values().forEach(q -> q.remove(playerUUID));
    }

    private Optional<Player> getPlayerFromUUID(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable(plugin.getServer().getPlayer(uuid));
    }

}
