package fpl.simulation;

import fpl.model.Player;
import java.util.*;

public class MatchSimulator {

    public static class EventResult {
        public Player player;
        public PlayerPerformance perf;

        public EventResult(Player player, PlayerPerformance perf) {
            this.player = player;
            this.perf = perf;
        }
    }

    public static List<EventResult> simulateMatch(List<Player> homePlayers, List<Player> awayPlayers) {

        Random r = new Random();
        List<EventResult> results = new ArrayList<>();

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(homePlayers);
        allPlayers.addAll(awayPlayers);

        for (Player p : allPlayers) {

            boolean plays = false;
            if (r.nextDouble() < 0.70) {
                plays = true;
            }

            int minutes = 0;
            if (plays) {
                if (r.nextDouble() < 0.25) {
                    minutes = 90;
                } else {
                    minutes = r.nextInt(89) + 1;
                }
            }

            int goals = 0;
            if (plays && r.nextDouble() < 0.05) {
                goals = r.nextInt(2);
            }

            int assists = 0;
            if (plays && r.nextDouble() < 0.07) {
                assists = 1;
            }

            int yellow = 0;
            if (plays && r.nextDouble() < 0.10) {
                yellow = 1;
            }

            int red = 0;
            if (plays && r.nextDouble() < 0.02) {
                red = 1;
            }

            PlayerPerformance perf =
                    new PlayerPerformance(minutes, goals, assists, yellow, red);

            results.add(new EventResult(p, perf));
        }

        return results;
    }
}
