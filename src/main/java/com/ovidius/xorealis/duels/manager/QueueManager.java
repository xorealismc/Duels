package com.ovidius.xorealis.duels.manager;

import com.ovidius.xorealis.duels.XorealisDuels;
import com.ovidius.xorealis.duels.object.Arena;
import com.ovidius.xorealis.duels.object.Kit;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
public class QueueManager {
    private final XorealisDuels plugin;
    private final Map<String, LinkedList<Player>> unranked1v1Queue = new HashMap<>();

    public void addPlayerToQueue(Player player, Kit kit) {
        LinkedList<Player> queue = unranked1v1Queue.computeIfAbsent(kit.getId(),k->new LinkedList<>());
        queue.add(player);

        player.sendMessage("§aВы встали в очередь для дуэли 1v1 с китом: " + kit.getDisplayName());

        checkForMathes(kit);
    }

    private void checkForMathes(Kit kit) {
        LinkedList<Player> queue = unranked1v1Queue.get(kit.getId());
        if(queue == null && queue.size()>=2) {
            Player player1 = queue.poll();
            Player player2 = queue.poll();

            if(player1==null||!player1.isOnline()||player2==null||!player2.isOnline()) {
                return;
            }
            Optional<Arena> availableArena=plugin.getArenaManager().findAvailableArena();
            if(availableArena.isPresent()) {
                com.ovidius.xorealis.duels.object.Arena arena=availableArena.get();
                plugin.getLogger().info("Match was found! "+player1.getName()+" vs "+player2.getName()+" on arena "+arena.getId());

            }else {
                player1.sendMessage("Не удалось найти свободную арену! Попробуйте позже");
                player2.sendMessage("Не удалось найти свободную арену! Попробуйте позже");
            }
        }
    }

}
