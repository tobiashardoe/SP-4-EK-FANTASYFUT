package fpl.simulation;

public class PlayerPerformance {
    public int minutes;
    public int goals;
    public int assists;
    public int yellow;
    public int red;

    public PlayerPerformance(int minutes, int goals, int assists, int yellow, int red) {
        this.minutes = minutes;
        this.goals = goals;
        this.assists = assists;
        this.yellow = yellow;
        this.red = red;
    }
}
