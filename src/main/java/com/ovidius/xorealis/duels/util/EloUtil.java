package com.ovidius.xorealis.duels.util;

public class EloUtil {

    private static final int K_FACTOR = 24;
    private EloUtil() {}
    public static int calculateNewRating(int playerRating, int opponentRating, double score) {

        double expectedScore = 1.0 / (1.0 + Math.pow(10, (double) (opponentRating - playerRating) / 400.0));

        int newRating = (int) Math.round(playerRating + K_FACTOR * (score - expectedScore));

        return Math.max(0, newRating);
    }

}