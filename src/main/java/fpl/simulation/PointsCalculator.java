package fpl.simulation;

public class PointsCalculator {
    public static int calculate(PlayerPerformance p){
        int points = 0;

        points += p.goals*4;
        points += p.assists*2;
        points -= p.yellow;
        points -= p.red*3;

        if (p.minutes == 90) {
            points += 2;
        } else if (p.minutes > 0) {
            points += 1;
        }

        return points;
    }
}
