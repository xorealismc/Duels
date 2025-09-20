package com.ovidius.xorealis.duels.object;

public enum GameModeType {
    SOLO(1),
    DUO(2),
    TRIO(3);

    private final int teamSize;

    GameModeType(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }
}