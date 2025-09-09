package com.ovidius.xorealis.duels.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
public class Duel {
    private final Arena arena;
    private final Kit kit;
    private final Player player1;
    private final Player player2;

    @Setter private DuelState state;

    public Duel(Arena arena, Kit kit, Player player1,Player player2) {
        this.arena = arena;
        this.kit = kit;
        this.player1 = player1;
        this.player2 = player2;
        this.state=DuelState.STARTING;
    }
    public Player getOpponent(Player player) {
        if(player.equals(player1)){
            return player2;
        } else if (player.equals(player2)) {
            return player1;
        }
        return null;
    }

}
