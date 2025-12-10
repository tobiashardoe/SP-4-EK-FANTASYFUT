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

    public static class MatchResult {
        public int homeGoals;
        public int awayGoals;
        public List<EventResult> events;

        public MatchResult(int homeGoals, int awayGoals, List<EventResult> events) {
            this.homeGoals = homeGoals;
            this.awayGoals = awayGoals;
            this.events = events;
        }
    }

    public static MatchResult simulateMatch(List<Player> homePlayers,
                                            List<Player> awayPlayers) {

        Random r = new Random();
        List<EventResult> events = new ArrayList<>();

        int homeGoals = generateGoals(r, 1.6);
        int awayGoals = generateGoals(r, 1.2);

        assignGoals(homePlayers, homeGoals, events, r);
        assignGoals(awayPlayers, awayGoals, events, r);

        List<Player> allPlayers = new ArrayList<>();
        allPlayers.addAll(homePlayers);
        allPlayers.addAll(awayPlayers);

        for (Player p : allPlayers) {

            PlayerPerformance base = generateMinutesAndCards(p, r);

            EventResult found = findResult(events, p);

            if (found != null) {
                found.perf.minutes = base.minutes;
                found.perf.yellow = base.yellow;
                found.perf.red = base.red;
            } else {
                events.add(new EventResult(p, base));
            }
        }

        return new MatchResult(homeGoals, awayGoals, events);
    }

    private static int generateGoals(Random r, double avg) {

        double L = Math.exp(-avg);
        double p = 1.0;
        int goals = 0;

        while (p > L) {
            p *= r.nextDouble();
            goals++;
        }

        return goals - 1;
    }

    private static void assignGoals(List<Player> players, int goals,
                                    List<EventResult> events, Random r) {

        for (int i = 0; i < goals; i++) {

            Player scorer = weightedGoalScorer(players, r);
            PlayerPerformance perf = new PlayerPerformance(0, 1, 0, 0, 0);

            if (r.nextDouble() < 0.65) {

                Player assister = weightedAssister(players, scorer, r);

                if (assister != null) {
                    events.add(new EventResult(assister,
                            new PlayerPerformance(0, 0, 1, 0, 0)));
                }
            }

            events.add(new EventResult(scorer, perf));
        }
    }

    private static Player weightedGoalScorer(List<Player> players, Random r) {

        Map<Player, Integer> weights = new HashMap<>();

        for (Player p : players) {

            String pos = p.getPosition();
            int w;

            if (pos.equals("FWD")) w = 8;
            else if (pos.equals("MID")) w = 4;
            else if (pos.equals("DEF")) w = 2;
            else w = 1;

            weights.put(p, w);
        }

        int total = 0;
        for (int w : weights.values()) total += w;

        int roll = r.nextInt(total);

        for (var entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) return entry.getKey();
        }

        return players.get(0);
    }

    private static Player weightedAssister(List<Player> players,
                                           Player scorer,
                                           Random r) {

        List<Player> candidates = new ArrayList<>(players);
        candidates.remove(scorer);

        if (candidates.isEmpty()) return null;

        Map<Player, Integer> weights = new HashMap<>();

        for (Player p : candidates) {

            String pos = p.getPosition();
            int w;

            if (pos.equals("MID")) w = 8;
            else if (pos.equals("FWD")) w = 4;
            else w = 1;

            weights.put(p, w);
        }

        int total = 0;
        for (int w : weights.values()) total += w;

        int roll = r.nextInt(total);

        for (var entry : weights.entrySet()) {
            roll -= entry.getValue();
            if (roll < 0) return entry.getKey();
        }

        return candidates.get(0);
    }

    private static PlayerPerformance generateMinutesAndCards(Player p,
                                                             Random r) {

        boolean plays = r.nextDouble() < 0.70;

        int minutes = 0;
        if (plays) {
            if (r.nextDouble() < 0.25) {
                minutes = 90;
            } else {
                minutes = r.nextInt(89) + 1;
            }
        }

        int yellow = 0;
        if (plays && r.nextDouble() < 0.10) {
            yellow = 1;
        }

        int red = 0;
        if (plays && r.nextDouble() < 0.02) {
            red = 1;
        }

        return new PlayerPerformance(minutes, 0, 0, yellow, red);
    }

    private static EventResult findResult(List<EventResult> list,
                                          Player p) {

        for (EventResult e : list) {
            if (e.player.getId() == p.getId()) {
                return e;
            }
        }
        return null;
    }
}
